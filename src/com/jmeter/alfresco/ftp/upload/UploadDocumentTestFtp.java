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
package com.jmeter.alfresco.ftp.upload;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.jmeter.alfresco.utils.ConfigReader;
import com.jmeter.alfresco.utils.Constants;
import com.jmeter.alfresco.utils.FtpUtils;
import com.jmeter.alfresco.utils.TaskTimer;

/**
 * The Class UploadDocumentTestFtp.
 * 
 * @author Abhinav Kumar Mishra
 * @since 2014
 */
public class UploadDocumentTestFtp extends AbstractJavaSamplerClient {
	
	/** The Constant logger. */
	private static final Log LOG = LogFactory.getLog(UploadDocumentTestFtp.class);
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#getDefaultParameters()
	 */
	@Override
	public Arguments getDefaultParameters() {
		final Arguments defaultParameters = new Arguments();
		defaultParameters.addArgument(Constants.FTP_HOST,
			    ConfigReader.getProperty(Constants.FTP_HOST));
		
		defaultParameters.addArgument(Constants.FTP_PORT,
			    ConfigReader.getProperty(Constants.FTP_PORT));
		
		defaultParameters.addArgument(Constants.USERNAME,ConfigReader.getProperty(Constants.U));
		defaultParameters.addArgument(Constants.PASSWORD,ConfigReader.getProperty(Constants.PW));
		defaultParameters.addArgument(Constants.LOCAL_FILE_OR_DIR,Constants.EMPTY);
		defaultParameters.addArgument(Constants.REMOTE_FILE_OR_DIR,Constants.EMPTY);
		return defaultParameters;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.JavaSamplerClient#runTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public SampleResult runTest(final JavaSamplerContext context) {
		
		LOG.info("runTest() invoked..");

		final String host = context.getParameter(Constants.FTP_HOST);
		final int port = Integer.valueOf(context.getParameter(Constants.FTP_PORT));
		final String userName = context.getParameter(Constants.USERNAME);
		final String password = context.getParameter(Constants.PASSWORD);
		final String localDirOrFile = context.getParameter(Constants.LOCAL_FILE_OR_DIR);
		final String remoteDirOrFile = context.getParameter(Constants.REMOTE_FILE_OR_DIR);
		
		final SampleResult result = new SampleResult();
		final TaskTimer taskTimer = new TaskTimer();
		try {
			
			LOG.info("Starting load test..");
			result.sampleStart(); // Record the start time of a sample
			final FtpUtils fileUtils = new FtpUtils();
			result.latencyEnd(); //Set the time to the first response 
			
			//starting the task timer
    		taskTimer.startTimer();
    		LOG.info("Timer started for upload: "+taskTimer.getStartTime()+" ms.");
    		
			final String responseMessage = fileUtils.uploadDirectoryOrFile(host, port, userName, password,
					localDirOrFile, remoteDirOrFile);
			
			//ending the task timer
			taskTimer.endTimer();
    		LOG.info("Total time spent during upload: "+taskTimer.getTotalTime()+" ms.");
    		
			result.sampleEnd();// Record the end time of a sample and calculate the elapsed time
			LOG.info("Ending load test..");
			result.setResponseMessage("OK,"+responseMessage+" See the log for details.");
			result.setSuccessful(true);
			result.setResponseCodeOK();
			result.setContentType(Constants.EMPTY);
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
