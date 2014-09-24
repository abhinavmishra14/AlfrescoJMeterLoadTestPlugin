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
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import com.jmeter.alfresco.utils.Constants;
import com.jmeter.alfresco.utils.DirectoryTraverser;
import com.jmeter.alfresco.utils.HttpUtils;

/**
 * The Class UploadDocumentHttpTest.
 * 
 * @author Abhinav Kumar Mishra
 * @since 2014
 */
public class UploadDocumentHttpTest extends TestCase{

	/**
	 * Test document upload.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testDocumentUpload() throws IOException {
		final String uploadURI = "http://127.0.0.1:8080/alfresco/service/api/upload";
		final String authURI =  "http://127.0.0.1:8080/alfresco/service/api/login";
		final String username = "admin";
		final String password = "admin";
		final String inputUri = "C:/Users/Abhi/Desktop/data"; // files to be uploaded from this directory
		final String siteID = "testpoc"; //id of the site for e.g if site name is TestPoc the id will be testpoc
		final String uploadDir = "testUpload"; //directory created under document library
		
		final HttpUtils httpUtils = new HttpUtils();
		String authTicket = Constants.EMPTY;
		try {
			authTicket = httpUtils.getAuthTicket(authURI, username, password);
		} catch (IOException e) {
			e.printStackTrace();
		}

		final StringBuffer responseBody= new StringBuffer();

		final File fileObject = new File (inputUri);
		//if uri is a directory the upload all files..
		if(fileObject.isDirectory()){
			final Set<File> setOfFiles = DirectoryTraverser.getFileUris(fileObject);
			for (Iterator<File> iterator = setOfFiles.iterator(); iterator.hasNext();) {
				final File fileObj = iterator.next();
				//call document upload
				responseBody.append(httpUtils.documentUpload(
						fileObj, authTicket, uploadURI, siteID,
						uploadDir));
				responseBody.append(Constants.BR);
		     }
		}else{
			responseBody.append(httpUtils.documentUpload(
					fileObject, authTicket, uploadURI, siteID,
					uploadDir));
		}

		System.out.println("Response>>>: "+responseBody);
		assertEquals(true, responseBody.toString().contains("File uploaded successfully"));
	}
}
