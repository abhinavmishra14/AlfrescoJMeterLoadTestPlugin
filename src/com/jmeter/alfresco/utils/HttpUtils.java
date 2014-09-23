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
package com.jmeter.alfresco.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.http.ParseException;

/**
 * The Class HttpUtils.
 */
public final class HttpUtils {

	/**
	 * Gets the login response.
	 *
	 * @param authURI the path
	 * @param username the username
	 * @param password the password
	 * @return the login response
	 * @throws ParseException the parse exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Map<String, String> getAuthResponse(final String authURI,
			final String username, final String password)
			throws ParseException, IOException {
		
		System.out.println("[JMeterLoadTestUtils:] Authenticating request..");
		final Map<String, String> responseMap = new HashMap<String, String>();
		GetMethod getRequest = null;
		try {
			final HttpClient httpclient = new HttpClient();
			getRequest = new GetMethod(getAuthURL(authURI, username, password));
			int statusCode = httpclient.executeMethod(getRequest);
			System.out.println("[JMeterLoadTestUtils:] Auth Response Status: "+ statusCode
					+"|"+ getRequest.getStatusText());
	
			responseMap.put(Constants.RESP_BODY, getRequest.getResponseBodyAsString());
			responseMap.put(Constants.CONTENT_TYPE, getRequest.getResponseHeader(Constants.CONTENT_TYPE_HDR).getValue());
			responseMap.put(Constants.STATUS_CODE, String.valueOf(statusCode));
			
		} finally {
			if(getRequest!=null){
				getRequest.releaseConnection();
			}
		}
		return responseMap;
	}
	
		
	/**
	 * Gets the auth ticket.
	 *
	 * @param authURI the auth uri
	 * @param username the username
	 * @param password the password
	 * @return the auth ticket
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getAuthTicket(final String authURI,
			final String username, final String password) throws IOException {
		final Map<String, String> responseMap = getAuthResponse(authURI, username, password);
		final String ticketFrmResponse = responseMap.get(Constants.RESP_BODY);
		int startindex = ticketFrmResponse.indexOf("TICKET");
		int endindex = ticketFrmResponse.indexOf("</");
		return ticketFrmResponse.substring(startindex, endindex);
	}
	

	/**
	 * Document upload.
	 *
	 * @param docFileObj the doc file obj
	 * @param authTicket the auth ticket
	 * @param uploadURI the upload uri
	 * @param siteID the site id
	 * @param uploadDir the upload dir
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String documentUpload(final File docFileObj,
			final String authTicket, final String uploadURI,
			final String siteID, final String uploadDir) throws IOException {

		String uploadResponse = Constants.EMPTY;
		PostMethod postRequest = null;
		try {
			final String uploadURL = getFileUploadURL(uploadURI,authTicket);
			
			System.out.println("[JMeterLoadTestUtils:] documentUpload() | Upload URL: " + uploadURL);
			
			final HttpClient httpClient = new HttpClient();
			postRequest = new PostMethod(uploadURL);
		    final String mimeType = getMimeType(docFileObj);
			final String docName = docFileObj.getName();
			System.out.println("[JMeterLoadTestUtils:] documentUpload() | Uploading document: "+docName+" , content-type: "+mimeType);

			final Part[] parts = {
					new FilePart("filedata", docName, docFileObj, mimeType,null),
					new StringPart("filename", docName),
					new StringPart("overwrite", "true"),
					new StringPart("siteid",siteID),
					new StringPart("containerid",ConfigReader.getProperty(Constants.CONTAINER_ID)),
					new StringPart("uploaddirectory",uploadDir) 
			      };
			
			postRequest.setRequestEntity(new MultipartRequestEntity(parts, postRequest.getParams()));
			
			final int statusCode = httpClient.executeMethod(postRequest);	
			
			uploadResponse = postRequest.getResponseBodyAsString();
			System.out.println("[JMeterLoadTestUtils:] documentUpload() | Upload status: "+statusCode+"  \nUpload response: "+uploadResponse);
		
		} finally{
			if(postRequest!=null){
				//releaseConnection http connection
				postRequest.releaseConnection();
			}
		}
		return uploadResponse;
	}

	/**
	 * Gets the auth url.
	 *
	 * @param path the path
	 * @param username the username
	 * @param password the password
	 * @return the url
	 */
	private static String getAuthURL(final String path, final String username,
			final String password) {
		final StringBuffer urlStrb = new StringBuffer(path);
		urlStrb.append(Constants.QUES);
		urlStrb.append(Constants.U);
		urlStrb.append(Constants.EQL);
		urlStrb.append(username);
		urlStrb.append(Constants.AMPERSND);
		urlStrb.append(Constants.PW);
		urlStrb.append(Constants.EQL);
		urlStrb.append(password);
		return urlStrb.toString();
	}
	
	
	/**
	 * Url file upload.
	 *
	 * @param path the path
	 * @param ticket the ticket
	 * @return the string
	 */
	private static String getFileUploadURL(final String path, final String ticket) {
		final StringBuffer urlStrb = new StringBuffer(path);
		urlStrb.append(Constants.QUES);
		urlStrb.append(Constants.TICKET_QRY);
		urlStrb.append(Constants.EQL);
		urlStrb.append(ticket);
		return urlStrb.toString();
	}
	
	/**
	 * Gets the mime type.
	 *
	 * @param fileObj the file obj
	 * @return the mime type
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String getMimeType(final File fileObj) throws IOException {
		final Path source = Paths.get(fileObj.getPath());
		return Files.probeContentType(source);
	}
}
