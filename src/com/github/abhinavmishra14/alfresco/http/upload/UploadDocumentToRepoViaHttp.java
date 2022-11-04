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
package com.github.abhinavmishra14.alfresco.http.upload;

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

import com.github.abhinavmishra14.alfresco.utils.ConfigReader;
import com.github.abhinavmishra14.alfresco.utils.Constants;
import com.github.abhinavmishra14.alfresco.utils.DirectoryTraverser;
import com.github.abhinavmishra14.alfresco.utils.HttpUtils;
import com.github.abhinavmishra14.alfresco.utils.TaskTimer;

/**
 * The Class UploadDocumentToRepoViaHttp.
 * 
 * @author Abhinav Kumar Mishra
 * @since 2014
 */
public class UploadDocumentToRepoViaHttp extends AbstractJavaSamplerClient {
	
	/** The Constant LOG. */
	private static final Log LOG = LogFactory.getLog(UploadDocumentToRepoViaHttp.class);
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#getDefaultParameters()
	 */
	@Override
	public Arguments getDefaultParameters() {
		final Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument(Constants.SERVER,
			    ConfigReader.getProperty(Constants.BASEPATH));
		defaultParameters.addArgument(Constants.USERNAME, ConfigReader.getProperty(Constants.USER_PARAM));
		defaultParameters.addArgument(Constants.PASSWORD, ConfigReader.getProperty(Constants.PASSWORD_PARAM));
		defaultParameters.addArgument(Constants.DESTINATION, ConfigReader.getProperty(Constants.DESTINATION));
		defaultParameters.addArgument(Constants.UPLOAD_DIR, ConfigReader.getProperty(Constants.UPLOAD_DIR));
		defaultParameters.addArgument(Constants.INPUT_PATH, Constants.EMPTY);
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
		final String destinationNodeRef = context.getParameter(Constants.DESTINATION);
		final String uploadDir = context.getParameter(Constants.UPLOAD_DIR);
		final HttpUtils httpUtils = new HttpUtils();
		final TaskTimer taskTimer = new TaskTimer();
		String authTicket = Constants.EMPTY;
		try {
			authTicket = httpUtils.getAuthTicket(authURI, username, password);
		} catch (IOException ioex) {
			LOG.error("IOException occurred while getting the auth ticket: ", ioex);
		}

		final SampleResult result = new SampleResult();
		try {
			LOG.info("Starting load test..");
			final File fileObject = new File (inputUri);
			result.sampleStart(); // Record the start time of a sample
			final StringBuffer responseBody= new StringBuffer();			
			//starting the task timer
    		taskTimer.startTimer();
    		LOG.info("Upload timer started for ' "+inputUri+" '");
			//if uri is a directory the upload all files..
			if(fileObject.isDirectory()) {
				final Set<File> setOfUris = Collections.unmodifiableSet(
						DirectoryTraverser.getFileUris(fileObject));
				for (final Iterator<File> iterator = setOfUris.iterator(); iterator.hasNext();) {
					final File fileObj = iterator.next();
					//call document upload
					if(fileObj.isFile()) {
						responseBody.append(httpUtils.documentUploadToRepo(
								fileObj, authTicket, uploadUri, destinationNodeRef,
								uploadDir));
						responseBody.append(Constants.LINE_BR);
					}
			     }
			} else {
				responseBody.append(httpUtils.documentUploadToRepo(
						fileObject, authTicket, uploadUri, destinationNodeRef,
						uploadDir));
			}
			
			//ending the task timer
			taskTimer.endTimer();
    		LOG.info("Total time spent during upload for ' "+inputUri+" ' ::- "+taskTimer.getFormattedTotalTime());
			result.sampleEnd();// Record the end time of a sample and calculate the elapsed time
			LOG.info("Ending load test..");
			result.setResponseMessage(responseBody.toString());
			result.setSuccessful(true);
			result.setResponseCodeOK();
			result.setContentType(Constants.MIME_TYPE);
		} catch (Exception excp) {
			//ending the task timer
			taskTimer.endTimer();
    		LOG.info("Total time spent during upload for ' "+inputUri+" ' , when exception occurred::- "+taskTimer.getFormattedTotalTime());
    		result.sampleEnd(); // Record the end time of a sample and calculate the elapsed time
			result.setSuccessful(false);
			result.setResponseMessage("Exception occurred while running test: " + excp);
			// get stack trace as a String to return as document data
			final StringWriter stringWriter = new StringWriter();
			excp.printStackTrace(new PrintWriter(stringWriter));
			result.setResponseData(stringWriter.toString(),Constants.ENCODING);
			result.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT);
			result.setResponseCode(Constants.SERVER_ERR);
			LOG.error("Exception occurred while running test: ", excp);
		} 
		return result;
	}
}
