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
package com.jmeter.alfresco.test;

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
import com.jmeter.alfresco.utils.JMeterConstants;
import com.jmeter.alfresco.utils.JMeterLoadTestUtils;

/**
 * The Class UploadDocumentTest.
 */
public class UploadDocumentTest extends AbstractJavaSamplerClient {
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#getDefaultParameters()
	 */
	@Override
	public Arguments getDefaultParameters() {
		final Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument(JMeterConstants.SERVER,
			    ConfigReader.getProperty(JMeterConstants.BASEPATH));
		defaultParameters.addArgument(JMeterConstants.USERNAME,ConfigReader.getProperty(JMeterConstants.U));
		defaultParameters.addArgument(JMeterConstants.PASSWORD,ConfigReader.getProperty(JMeterConstants.PW));
		
		defaultParameters.addArgument(JMeterConstants.SITE_ID,ConfigReader.getProperty(JMeterConstants.SITE_ID));
		defaultParameters.addArgument(JMeterConstants.UPLOAD_DIR,ConfigReader.getProperty(JMeterConstants.UPLOAD_DIR));

		defaultParameters.addArgument(JMeterConstants.INPUT_PATH,JMeterConstants.EMPTY);
		return defaultParameters;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.JavaSamplerClient#runTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public SampleResult runTest(final JavaSamplerContext context) {
		
		try (FileOutputStream fileInStream = new FileOutputStream("UploadDocumentTest.log")) {
			final PrintStream out = new PrintStream(fileInStream);
			System.setOut(out);
			System.setErr(out);
		}catch (FileNotFoundException fnfExcp) {
			fnfExcp.printStackTrace();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
		
		System.out.println("[UploadDocumentTest:] runTest() invoked..");

		final String serverAddress= context.getParameter(JMeterConstants.SERVER);
		
		final String uploadURI = serverAddress+ConfigReader.getProperty(JMeterConstants.UPLOAD_PATH);
		final String authURI = serverAddress+ConfigReader.getProperty(JMeterConstants.LOGIN_PATH);
		final String username = context.getParameter(JMeterConstants.USERNAME);
		final String password = context.getParameter(JMeterConstants.PASSWORD);
		final String inputUri = context.getParameter(JMeterConstants.INPUT_PATH);		
		final String siteID = context.getParameter(JMeterConstants.SITE_ID);
		final String uploadDir = context.getParameter(JMeterConstants.UPLOAD_DIR);

		String authTicket = JMeterConstants.EMPTY;
		try {
			authTicket = JMeterLoadTestUtils.getAuthTicket(authURI, username, password);
		} catch (IOException e) {
			e.printStackTrace();
		}

		final SampleResult result = new SampleResult();
		try {
			System.out.println("[UploadDocumentTest:] Starting test..");
			
			final File fileObject = new File (inputUri);
			result.sampleStart(); // start stop-watch
			final StringBuffer responseBody= new StringBuffer();

			//if uri is a directory the upload all files..
			if(fileObject.isDirectory()){
				final Set<File> setOfFiles = DirectoryTraverser.getFileUris(fileObject);
				for (Iterator<File> iterator = setOfFiles.iterator(); iterator.hasNext();) {
					final File fileObj = iterator.next();
					//call document upload
					responseBody.append(JMeterLoadTestUtils.processUpload(
							fileObj, authTicket, uploadURI, siteID,
							uploadDir));
					responseBody.append(JMeterConstants.BR);
			     }
			}else{
				responseBody.append(JMeterLoadTestUtils.processUpload(
						fileObject, authTicket, uploadURI, siteID,
						uploadDir));
			}
			result.sampleEnd();// end the stop-watch
		
			System.out.println("[UploadDocumentTest:] Ending test..");

			result.setResponseMessage(responseBody.toString());
			result.setSuccessful(true);
			result.setResponseCodeOK();
			result.setContentType(JMeterConstants.MIME_TYPE);
			
		} catch (Exception excp) {
			result.sampleEnd(); // stop stop-watch
			result.setSuccessful(false);
			result.setResponseMessage("[UploadDocumentTest:] Exception: " + excp);
			// get stack trace as a String to return as document data
			final StringWriter stringWriter = new StringWriter();
			excp.printStackTrace(new PrintWriter(stringWriter));
			result.setResponseData(stringWriter.toString(),JMeterConstants.ENCODING);
			result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
			result.setResponseCode(JMeterConstants.SERVER_ERR);
		} 
		return result;
	}
}
