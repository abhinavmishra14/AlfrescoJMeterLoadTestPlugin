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
package com.jmeter.alfresco.http.upload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.jmeter.alfresco.utils.ConfigReader;
import com.jmeter.alfresco.utils.Constants;
import com.jmeter.alfresco.utils.DirectoryTraverser;
import com.jmeter.alfresco.utils.HttpUtils;

/**
 * The Class UploadDocumentTestHttp.
 * 
 * @author Abhinav Kumar Mishra
 * @since 2014
 */
public class UploadDocumentTestHttp extends AbstractJavaSamplerClient {
	
	/** The Constant logger. */
	private static final Log LOG = LogFactory.getLog(UploadDocumentTestHttp.class);
	
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
		
		defaultParameters.addArgument(Constants.SITE_ID,ConfigReader.getProperty(Constants.SITE_ID));
		defaultParameters.addArgument(Constants.UPLOAD_DIR,ConfigReader.getProperty(Constants.UPLOAD_DIR));

		defaultParameters.addArgument(Constants.INPUT_PATH,Constants.EMPTY);
		return defaultParameters;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.JavaSamplerClient#runTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public SampleResult runTest(final JavaSamplerContext context) {
	
		LOG.info("runTest() invoked..");

		final String serverAddress= context.getParameter(Constants.SERVER);
		final String uploadUri = serverAddress+ConfigReader.getProperty(Constants.UPLOAD_PATH);
		final String authURI = serverAddress+ConfigReader.getProperty(Constants.LOGIN_PATH);
		final String username = context.getParameter(Constants.USERNAME);
		final String password = context.getParameter(Constants.PASSWORD);
		final String inputUri = context.getParameter(Constants.INPUT_PATH);		
		final String siteID = context.getParameter(Constants.SITE_ID);
		final String uploadDir = context.getParameter(Constants.UPLOAD_DIR);
		
		final HttpUtils httpUtils = new HttpUtils();

		String authTicket = Constants.EMPTY;
		try {
			authTicket = httpUtils.getAuthTicket(authURI, username, password);
		} catch (IOException ioex) {
			LOG.error("IOException occured while getting the auth ticket: ", ioex);
		}

		final SampleResult result = new SampleResult();
		try {
			LOG.info("Starting load test..");
			
			final File fileObject = new File (inputUri);
			result.sampleStart(); // Record the start time of a sample
			final StringBuffer responseBody= new StringBuffer();

			//if uri is a directory the upload all files..
			if(fileObject.isDirectory()){
				final Set<File> setOfUris = Collections.unmodifiableSet(
						DirectoryTraverser.getFileUris(fileObject));
				for (final Iterator<File> iterator = setOfUris.iterator(); iterator.hasNext();) {
					final File fileObj = iterator.next();
					//call document upload
					if(fileObj.isFile()){
						responseBody.append(httpUtils.documentUpload(
								fileObj, authTicket, uploadUri, siteID,
								uploadDir));
						responseBody.append(Constants.BR);
					}
			     }
			}else{
				responseBody.append(httpUtils.documentUpload(
						fileObject, authTicket, uploadUri, siteID,
						uploadDir));
			}
			result.sampleEnd();// Record the end time of a sample and calculate the elapsed time
		
			LOG.info("Ending load test..");

			result.setResponseMessage(responseBody.toString());
			result.setSuccessful(true);
			result.setResponseCodeOK();
			result.setContentType(Constants.MIME_TYPE);
			
		} catch (Exception excp) {
			result.sampleEnd(); // Record the end time of a sample and calculate the elapsed time
			result.setSuccessful(false);
			result.setResponseMessage("Exception occured while running test: " + excp);
			// get stack trace as a String to return as document data
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
