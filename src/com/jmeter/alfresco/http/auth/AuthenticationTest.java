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
import com.jmeter.alfresco.utils.Constants;
import com.jmeter.alfresco.utils.HttpUtils;

/**
 * The Class AuthenticationTest.
 */
public class AuthenticationTest extends AbstractJavaSamplerClient {

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
		
		try (final FileOutputStream fileInStream = new FileOutputStream(
				"AuthenticationTest.log");
				final PrintStream out = new PrintStream(fileInStream);) {
			System.setOut(out);
			System.setErr(out);
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}

		System.out.println("[AuthenticationTest:] runTest() invoked..");
	
		final String serverAddress= context.getParameter(Constants.SERVER);
		final String authURI = serverAddress+ConfigReader.getProperty(Constants.LOGIN_PATH);
		final String username = context.getParameter(Constants.USERNAME);
		final String password = context.getParameter(Constants.PASSWORD);
		
		
		final SampleResult result = new SampleResult();
		try {
			System.out.println("[AuthenticationTest:] Starting load test..");
			
			result.sampleStart(); // start stop-watch
			
			final Map<String, String> responseMap = HttpUtils.getAuthResponse(authURI, username, password);
			
			result.sampleEnd();// end the stop-watch
			
			System.out.println("[AuthenticationTest:] Ending  load test..");

			result.setResponseMessage(responseMap.get(Constants.RESP_BODY));
			result.setSuccessful(true);
			result.setResponseCode(responseMap.get(Constants.STATUS_CODE));
			result.setContentType(responseMap.get(Constants.CONTENT_TYPE));
			
		} catch (Exception excp) {
			result.sampleEnd(); // stop stop-watch
			result.setSuccessful(false);
			result.setResponseMessage("[AuthenticationTest:] Exception: " + excp);
			// get stack trace as a String to return as document data
			final StringWriter stringWriter = new StringWriter();
			excp.printStackTrace(new PrintWriter(stringWriter));
			result.setResponseData(stringWriter.toString(),Constants.ENCODING);
			result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
			result.setResponseCode(Constants.SERVER_ERR);
		} 
		return result;
	}
}
