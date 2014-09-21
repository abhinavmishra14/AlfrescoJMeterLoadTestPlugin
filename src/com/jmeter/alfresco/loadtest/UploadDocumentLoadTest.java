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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.jmeter.alfresco.utils.ConfigReader;
import com.jmeter.alfresco.utils.DirectoryTraverser;
import com.jmeter.alfresco.utils.JMeterLoadTestConstants;
import com.jmeter.alfresco.utils.JMeterLoadTestUtils;

/**
 * The Class UploadDocumentLoadTest.
 */
public class UploadDocumentLoadTest extends AbstractJavaSamplerClient {
	
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
		
		defaultParameters.addArgument(JMeterLoadTestConstants.SITE_ID,ConfigReader.getProperty(JMeterLoadTestConstants.SITE_ID));
		defaultParameters.addArgument(JMeterLoadTestConstants.UPLOAD_DIR,ConfigReader.getProperty(JMeterLoadTestConstants.UPLOAD_DIR));

		defaultParameters.addArgument(JMeterLoadTestConstants.INPUT_PATH,JMeterLoadTestConstants.EMPTY);
		return defaultParameters;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.JavaSamplerClient#runTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public SampleResult runTest(final JavaSamplerContext context) {
		
		try (FileOutputStream fileInStream = new FileOutputStream("UploadDocumentLoadTest.log")) {
			final PrintStream out = new PrintStream(fileInStream);
			System.setOut(out);
			System.setErr(out);
		}catch (FileNotFoundException fnfExcp) {
			fnfExcp.printStackTrace();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
		
		System.out.println("[UploadDocumentLoadTest:] runTest() invoked..");

		final String serverAddress= context.getParameter(JMeterLoadTestConstants.SERVER);
		
		final String uploadURI = serverAddress+ConfigReader.getProperty(JMeterLoadTestConstants.UPLOAD_PATH);
		final String authURI = serverAddress+ConfigReader.getProperty(JMeterLoadTestConstants.LOGIN_PATH);
		final String username = context.getParameter(JMeterLoadTestConstants.USERNAME);
		final String password = context.getParameter(JMeterLoadTestConstants.PASSWORD);
		final String inputUri = context.getParameter(JMeterLoadTestConstants.INPUT_PATH);		
		final String siteID = context.getParameter(JMeterLoadTestConstants.SITE_ID);
		final String uploadDir = context.getParameter(JMeterLoadTestConstants.UPLOAD_DIR);

		String authTicket = JMeterLoadTestConstants.EMPTY;
		try {
			authTicket = JMeterLoadTestUtils.getAuthTicket(authURI, username, password);
		} catch (IOException e) {
			e.printStackTrace();
		}

		final SampleResult result = new SampleResult();
		try {
			System.out.println("[UploadDocumentLoadTest:] Starting load test..");
			
			final File fileObject = new File (inputUri);
			result.sampleStart(); // start stop-watch
			final StringBuffer responseBody= new StringBuffer();

			//if uri is a directory the upload all files..
			if(fileObject.isDirectory()){
				final Set<File> setOfFiles = DirectoryTraverser.getFileUris(fileObject);
				for (Iterator<File> iterator = setOfFiles.iterator(); iterator.hasNext();) {
					final File fileObj = iterator.next();
					//call document upload
					responseBody.append(JMeterLoadTestUtils.documentUpload(
							fileObj, authTicket, uploadURI, siteID,
							uploadDir));
					responseBody.append(JMeterLoadTestConstants.BR);
			     }
			}else{
				responseBody.append(JMeterLoadTestUtils.documentUpload(
						fileObject, authTicket, uploadURI, siteID,
						uploadDir));
			}
			result.sampleEnd();// end the stop-watch
		
			System.out.println("[UploadDocumentLoadTest:] Ending load test..");

			result.setResponseMessage(responseBody.toString());
			result.setSuccessful(true);
			result.setResponseCodeOK();
			result.setContentType(JMeterLoadTestConstants.MIME_TYPE);
			
		} catch (Exception excp) {
			result.sampleEnd(); // stop stop-watch
			result.setSuccessful(false);
			result.setResponseMessage("[UploadDocumentLoadTest:] Exception: " + excp);
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
