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

/**
 * The Class UploadDocumentHttpTest.
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
		
		final FtpUtils fileUtils = new FtpUtils();
		final String uploadResp= fileUtils.uploadDirectoryOrFile(host, port, userName, password,
				localDir, remoteDir);
		assertEquals("Upload completed, See the log file for more details!", uploadResp);
	}
}
