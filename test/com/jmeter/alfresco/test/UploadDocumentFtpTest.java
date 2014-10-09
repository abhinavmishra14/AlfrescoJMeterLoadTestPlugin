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

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

import com.jmeter.alfresco.utils.FtpUtils;
import com.jmeter.alfresco.utils.TaskTimer;

/**
 * The Class UploadDocumentHttpTest.
 * 
 * @author Abhinav Kumar Mishra
 * @since 2014
 */
public class UploadDocumentFtpTest extends TestCase{

	/**
	 * Test document upload.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testDocumentUpload() throws IOException {
		final String host = "127.0.0.1";
		final int port = 21;
		final String userName = "admin";
		final String password = "admin";
		final String remoteDir = "Alfresco/Sites/testpoc/documentLibrary/testUpload";
		final String localDir = "D:\\Data\\trunk";
		
		final FtpUtils ftpUtils = new FtpUtils();
		final TaskTimer taskTimer = new TaskTimer();
		//starting the task timer
		taskTimer.startTimer();
		System.out.println("Timer started for upload: "+taskTimer.getStartTime()+" ms.");
		
		final String uploadResp= ftpUtils.uploadDirectoryOrFile(host, port, userName, password,
				localDir, remoteDir);
		
		//ending the task timer
		taskTimer.endTimer();
		System.out.println("Total time spent during upload: "+taskTimer.getTotalTime()+" ms.");
		assertEquals("Upload completed successfully!", uploadResp);
	}
}
