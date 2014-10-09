/*
 * Created By: Abhinav Kumar Mishra
 * Copyright &copy; 2014. Abhinav Kumar Mishra. 
 * All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jmeter.alfresco.http.auth;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.jmeter.alfresco.utils.ConfigReader;
import com.jmeter.alfresco.utils.Constants;
import com.jmeter.alfresco.utils.HttpUtils;
import com.jmeter.alfresco.utils.TaskTimer;

/**
 * The Class AuthenticationTest.
 * 
 * @author Abhinav Kumar Mishra
 * @since 2014
 */
public class AuthenticationTest extends AbstractJavaSamplerClient {

	/** The Constant logger. */
	private static final Log LOG = LogFactory.getLog(AuthenticationTest.class);
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#getDefaultParameters()
	 */
	@Override
	public Arguments getDefaultParameters() {
		final Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument(Constants.SERVER,
				ConfigReader.getProperty(Constants.BASEPATH));
		defaultParameters.addArgument(Constants.USERNAME,ConfigReader.getProperty(Constants.U));
		defaultParameters.addArgument(Constants.PASSWORD,ConfigReader.getProperty(Constants.PW));
		return defaultParameters;
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.JavaSamplerClient#runTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public SampleResult runTest(final JavaSamplerContext context) {

		LOG.info("runTest() invoked..");
	
		final String serverAddress= context.getParameter(Constants.SERVER);
		final String authURI = serverAddress+ConfigReader.getProperty(Constants.LOGIN_PATH);
		final String username = context.getParameter(Constants.USERNAME);
		final String password = context.getParameter(Constants.PASSWORD);
		
		
		final SampleResult result = new SampleResult();
		final TaskTimer taskTimer = new TaskTimer();

		try {
			LOG.info("Starting load test..");
			result.sampleStart(); // Record the start time of a sample
			final HttpUtils httpUtils = new HttpUtils();
			
			//starting the task timer
    		taskTimer.startTimer();
    		LOG.info("Timer started for upload: "+taskTimer.getStartTime()+" ms.");
			
    		final Map<String, String> responseMap = httpUtils.getAuthResponse(authURI, username, password);
			
    		//ending the task timer
			taskTimer.endTimer();
    		LOG.info("Total time spent during upload: "+taskTimer.getTotalTime()+" ms.");
    		
			result.sampleEnd();// Record the end time of a sample and calculate the elapsed time
			LOG.info("Ending load test..");
			result.setResponseMessage(responseMap.get(Constants.RESP_BODY));
			result.setSuccessful(true);
			result.setResponseCode(responseMap.get(Constants.STATUS_CODE));
			result.setContentType(responseMap.get(Constants.CONTENT_TYPE));
		} catch (Exception excp) {
			//ending the task timer
			taskTimer.endTimer();
    		LOG.info("Total time spent during upload when exception occured: "+taskTimer.getTotalTime()+" ms.");
    		
			result.sampleEnd(); // Record the end time of a sample and calculate the elapsed time
			LOG.info("Ending load test..");
			result.setSuccessful(false);
			result.setResponseMessage("Exception occured while running test: " + excp);
			// Get stack trace as a String to return as document data
			final StringWriter stringWriter = new StringWriter();
			excp.printStackTrace(new PrintWriter(stringWriter));
			result.setResponseData(stringWriter.toString(),Constants.ENCODING);
			result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
			result.setResponseCode(Constants.SERVER_ERR);
			LOG.error("Exception occured while running test: ", excp);
		} 
		return result;
	}
}
