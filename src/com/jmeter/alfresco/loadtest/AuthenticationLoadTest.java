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
package com.jmeter.alfresco.loadtest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.jmeter.alfresco.utils.ConfigReader;
import com.jmeter.alfresco.utils.JMeterLoadTestConstants;
import com.jmeter.alfresco.utils.JMeterLoadTestUtils;

/**
 * The Class AuthenticationLoadTest.
 */
public class AuthenticationLoadTest extends AbstractJavaSamplerClient {

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#getDefaultParameters()
	 */
	@Override
	public Arguments getDefaultParameters() {
		final Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument(JMeterLoadTestConstants.SERVER,
				ConfigReader.getProperty(JMeterLoadTestConstants.BASEPATH));
		defaultParameters.addArgument(JMeterLoadTestConstants.USERNAME,ConfigReader.getProperty(JMeterLoadTestConstants.U));
		defaultParameters.addArgument(JMeterLoadTestConstants.PASSWORD,ConfigReader.getProperty(JMeterLoadTestConstants.PW));
		return defaultParameters;
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.JavaSamplerClient#runTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public SampleResult runTest(final JavaSamplerContext context) {
		
		try (FileOutputStream fileInStream = new FileOutputStream("AuthenticationLoadTest.log")) {
			final PrintStream out = new PrintStream(fileInStream);
			System.setOut(out);
			System.setErr(out);
		}catch (FileNotFoundException fnfExcp) {
			fnfExcp.printStackTrace();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
		
		System.out.println("[AuthenticationLoadTest:] runTest() invoked..");
	
		final String serverAddress= context.getParameter(JMeterLoadTestConstants.SERVER);
		final String authURI = serverAddress+ConfigReader.getProperty(JMeterLoadTestConstants.LOGIN_PATH);
		final String username = context.getParameter(JMeterLoadTestConstants.USERNAME);
		final String password = context.getParameter(JMeterLoadTestConstants.PASSWORD);
		
		
		final SampleResult result = new SampleResult();
		try {
			System.out.println("[AuthenticationLoadTest:] Starting load test..");
			
			result.sampleStart(); // start stop-watch
			
			final Map<String, String> responseMap = JMeterLoadTestUtils.getAuthResponse(authURI, username, password);
			
			result.sampleEnd();// end the stop-watch
			
			System.out.println("[AuthenticationLoadTest:] Ending  load test..");

			result.setResponseMessage(responseMap.get(JMeterLoadTestConstants.RESP_BODY));
			result.setSuccessful(true);
			result.setResponseCode(responseMap.get(JMeterLoadTestConstants.STATUS_CODE));
			result.setContentType(responseMap.get(JMeterLoadTestConstants.CONTENT_TYPE));
			
		} catch (Exception excp) {
			result.sampleEnd(); // stop stop-watch
			result.setSuccessful(false);
			result.setResponseMessage("[AuthenticationLoadTest:] Exception: " + excp);
			// get stack trace as a String to return as document data
			final StringWriter stringWriter = new StringWriter();
			excp.printStackTrace(new PrintWriter(stringWriter));
			result.setResponseData(stringWriter.toString(),JMeterLoadTestConstants.ENCODING);
			result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
			result.setResponseCode(JMeterLoadTestConstants.SERVER_ERR);
		} 
		return result;
	}
}
