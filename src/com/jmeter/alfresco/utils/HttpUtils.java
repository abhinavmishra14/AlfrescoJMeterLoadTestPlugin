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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ParseException;

/**
 * The Class HttpUtils.
 * 
 * @author Abhinav Kumar Mishra
 * @since 2014
 */
public final class HttpUtils {
	
	/** The Constant logger. */
	private static final Log LOG = LogFactory.getLog(HttpUtils.class);
	
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
	public Map<String, String> getAuthResponse(final String authURI,
			final String username, final String password)
			throws ParseException, IOException {
		
		LOG.debug("Authenticating request..");
		final Map<String, String> responseMap = new ConcurrentHashMap<String, String>();
		GetMethod getRequest = null;
		try {
			final HttpClient httpclient = new HttpClient();
			getRequest = new GetMethod(getAuthURL(authURI, username, password));
			final int statusCode = httpclient.executeMethod(getRequest);
			LOG.debug("Auth Response Status: "+ statusCode+"|"+ getRequest.getStatusText());
	
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
	public String getAuthTicket(final String authURI,
			final String username, final String password) throws IOException {
		final Map<String, String> responseMap = getAuthResponse(authURI, username, password);
		final String ticketFrmResponse = responseMap.get(Constants.RESP_BODY);
		final int startindex = ticketFrmResponse.indexOf("TICKET");
		final int endindex = ticketFrmResponse.indexOf("</");
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
	public String documentUpload(final File docFileObj,
			final String authTicket, final String uploadURI,
			final String siteID, final String uploadDir) throws IOException {

		String uploadResponse = Constants.EMPTY;
		PostMethod postRequest = null;
		try {
			final String uploadURL = getFileUploadURL(uploadURI,authTicket);
			LOG.info("documentUpload() | Upload URL: " + uploadURL);
			
			final HttpClient httpClient = new HttpClient();
			postRequest = new PostMethod(uploadURL);
		    final String mimeType = getMimeType(docFileObj);
			final String docName = docFileObj.getName();
			LOG.debug("documentUpload() | Uploading document: "+docName+" , content-type: "+mimeType);

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
			LOG.info("documentUpload() | Upload status: "+statusCode);
			LOG.debug("documentUpload() | Upload response: "+uploadResponse);
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
	private String getAuthURL(final String path, final String username,
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
	private String getFileUploadURL(final String path, final String ticket) {
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
	public String getMimeType(final File fileObj) throws IOException {
		final Path source = Paths.get(fileObj.getPath());
		return Files.probeContentType(source);
	}
}
