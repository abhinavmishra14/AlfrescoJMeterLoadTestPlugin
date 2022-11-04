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
package com.github.abhinavmishra14.alfresco.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * The Class FtpUtils.<br/>
 * This class is a utility class, will be used to upload a file or directory to
 * remote host via FTP.
 * 
 * @author Abhinav Kumar Mishra
 * @since 2014
 */
public final class FtpUtils {
	
	/** The Constant logger. */
	private static final Log LOG = LogFactory.getLog(FtpUtils.class);
	
	/** The Constant EMPTY. */
	private static final String EMPTY = "";
	
	/** The Constant FILE_SEPERATOR_LINUX. */
	private static final String FILE_SEPERATOR_LINUX = "/";
	
	/** The Constant FILE_SEPERATOR_WIN. */
	private static final String FILE_SEPERATOR_WIN = "\\";

	/**
	 * Upload directory or file.
	 *
	 * @param host the host
	 * @param port the port
	 * @param userName the user name
	 * @param password the password
	 * @param fromLocalDirOrFile the local dir
	 * @param toRemoteDirOrFile the remote dir
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String uploadDirectoryOrFile(final String host, final int port,
			final String userName, final String password,
			final String fromLocalDirOrFile, final String toRemoteDirOrFile)
			throws IOException {
		
		final FTPClient ftpClient = new FTPClient();
		String responseMessage = Constants.EMPTY;
		try {
			// Connect and login to get the session
			ftpClient.connect(host, port);
			final int replyCode = ftpClient.getReplyCode();
			
            if (FTPReply.isPositiveCompletion(replyCode)) {
            	final boolean loginSuccess = ftpClient.login(userName, password);
            	if(loginSuccess) {
            		if(LOG.isDebugEnabled()) {
        			  LOG.debug("Connected to remote host!");
            		}
        			//Use local passive mode to pass fire-wall
        			//In this mode a data connection is made by opening a port on the server for the client to connect 
        			//and this is not blocked by fire-wall.
        			ftpClient.enterLocalPassiveMode();
        			final File localDirOrFileObj = new File(fromLocalDirOrFile);
        			if (localDirOrFileObj.isFile()) {
        				if(LOG.isDebugEnabled()) {
            				LOG.debug("Uploading file: "+ fromLocalDirOrFile);
                  		}
        				uploadFile(ftpClient, fromLocalDirOrFile, toRemoteDirOrFile
        						+ FILE_SEPERATOR_LINUX + localDirOrFileObj.getName());				
        			} else {
        				uploadDirectory(ftpClient, toRemoteDirOrFile, fromLocalDirOrFile, EMPTY);
        			}
        			responseMessage = "Upload completed successfully!";
            	} else {
            		responseMessage = "Could not login to the remote host!";
            	}
    			//Log out and disconnect from the server once FTP operation is completed.
    			if (ftpClient.isConnected()) {
    				try {
    					ftpClient.logout();
    				} catch (IOException ignored) {
    					LOG.error("Ignoring the exception while logging out from remote host: ", ignored);
    				}
    				try {
    					ftpClient.disconnect();
    					if(LOG.isDebugEnabled()) {
    						LOG.debug("Disconnected from remote host!");
                  		}
    				} catch (IOException ignored) {
    					LOG.error("Ignoring the exception while disconnecting from remote host: ", ignored);
    				}
    			}
            } else {
            	responseMessage = "Host connection failed!";
            }		
			if(LOG.isDebugEnabled()) {
				LOG.debug("ResponseMessage:=> "+responseMessage);
      		}
		} catch (IOException ioexcp) {
			LOG.error("IOException occured in uploadDirectoryOrFile(..): ", ioexcp);
		    throw ioexcp;
		}
		return responseMessage;
	}
	
	/**
	 * Upload directory.
	 *
	 * @param ftpClient the ftp client
	 * @param toRemoteDir the to remote dir
	 * @param fromLocalParentDir the from local parent dir
	 * @param remoteParentDir the remote parent dir
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void uploadDirectory(final FTPClient ftpClient,
			final String toRemoteDir, String fromLocalParentDir,
			final String remoteParentDir) throws IOException {
		fromLocalParentDir = convertToLinuxFormat(fromLocalParentDir);
		fromLocalParentDir = checkLinuxSeperator(fromLocalParentDir);
		if(LOG.isDebugEnabled()) {
			LOG.debug("Listing the directory tree: " + fromLocalParentDir);
  		}
		final File localDir = new File(fromLocalParentDir);
		final File [] listedFiles = localDir.listFiles();
		List<File> subFiles = null;
		if (listedFiles != null) {
			subFiles = Arrays.asList(listedFiles);
		}
		if (subFiles != null && !subFiles.isEmpty()) {
			for (final File item : subFiles) {
				String remoteFilePath = toRemoteDir + FILE_SEPERATOR_LINUX + remoteParentDir
						+ FILE_SEPERATOR_LINUX + item.getName();
				if (EMPTY.equals(remoteParentDir)) {
					remoteFilePath = toRemoteDir + FILE_SEPERATOR_LINUX + item.getName();
				}
				if (item.isFile()) {
					// Upload the file
					final String localFilePath = convertToLinuxFormat(item.getAbsolutePath());
					if(LOG.isDebugEnabled()) {
						LOG.debug("Thread-"+Thread.currentThread().getName()+", uploading file: "+ localFilePath);
			  		}
					final boolean isFileUploaded = uploadFile(ftpClient,localFilePath, remoteFilePath);
					if (isFileUploaded) {
						if(LOG.isDebugEnabled()) {
							LOG.debug("File uploaded: "+ remoteFilePath);
				  		}
					} else {
						LOG.warn("Could not upload the file: "+ localFilePath+" on remote host, file may be existing!");
					}
				} else {
					//Recursively traverse the directory and create the directory.
					//Create directory on the server
					if(LOG.isDebugEnabled()) {
						LOG.debug("Thread-"+Thread.currentThread().getName()+", creating remote dir: "+ remoteFilePath);
			  		}
					final boolean isDirCreated = ftpClient.makeDirectory(remoteFilePath);
					if (isDirCreated) {
						if(LOG.isDebugEnabled()) {
							LOG.debug("Created the directory: "+ remoteFilePath+" on remote host");
				  		}
					} else {
						LOG.warn("Could not create the directory: "
								+ remoteFilePath+" on remote host, directory may be existing!");
					}
					//Directory created, now upload the sub directory
					String parentDirectory = remoteParentDir + FILE_SEPERATOR_LINUX + item.getName();
					if (EMPTY.equals(remoteParentDir)) {
						parentDirectory = item.getName();
					}
					fromLocalParentDir = item.getAbsolutePath();
					//Call to uploadDirectory to upload the sub-directories
					uploadDirectory(ftpClient, toRemoteDir, fromLocalParentDir,parentDirectory);
				}
			}
		}
	}

	/**
	 * Upload file.
	 *
	 * @param ftpClient the ftp client
	 * @param frmLocalFilePath the frm local file path
	 * @param toRemoteFilePath the to remote file path
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private boolean uploadFile(final FTPClient ftpClient,
			final String frmLocalFilePath, final String toRemoteFilePath)
			throws IOException {
			
		final File localFile = new File(frmLocalFilePath);
		final InputStream inputStream = new FileInputStream(localFile);
		try {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			final boolean isFileUploaded = ftpClient.storeFile(toRemoteFilePath, inputStream);
			final int replyCode = ftpClient.getReplyCode();
			//If reply code is 550 then,Requested action not taken. File unavailable (e.g., file not found, no access).
			//If reply code is 150 then,File status okay (e.g., File found)
			//If reply code is 226 then,Closing data connection. 
			//If reply code is 426 then,Connection closed; transfer aborted. 
			LOG.debug("Reply code from remote host after storeFile(..) call: "+replyCode);
			return isFileUploaded;
		} finally {
			inputStream.close();
		}
	}
	
	/**
	 * Check the linux seperator.
	 *
	 * @param aStr the a str
	 * @return the string
	 */
	private String checkLinuxSeperator(String aStr) {
		if (!aStr.endsWith(FILE_SEPERATOR_LINUX)) {
			aStr = aStr + FILE_SEPERATOR_LINUX;
		}
		return aStr;
	}
	
	/**
	 * Convert to linux format.
	 *
	 * @param inputPath the input path
	 * @return the string
	 */
	private String convertToLinuxFormat(final String inputPath) {
		return inputPath.replace(FILE_SEPERATOR_WIN, FILE_SEPERATOR_LINUX);
	}
}