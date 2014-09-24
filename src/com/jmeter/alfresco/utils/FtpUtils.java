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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 * The Class FtpUtils.<br/>
 * This class is a utility class, will be used to upload a file or directory to
 * remote host via FTP.
 */
public final class FtpUtils {

	/** The Constant EMPTY. */
	private static final String EMPTY = "";
	
	/** The Constant BACK_SLASH. */
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
	 */
	public String uploadDirectoryOrFile(final String host, final int port,
			final String userName, final String password,
			final String fromLocalDirOrFile, final String toRemoteDirOrFile) {
		
		final FTPClient ftpClient = new FTPClient();
		String responseMessage = Constants.EMPTY;
		try {
			// Connect and login to get the session
			ftpClient.connect(host, port);
			ftpClient.login(userName, password);
			//Use local passive mode to pass fire-wall
			ftpClient.enterLocalPassiveMode();
			System.out.println("Successfully connected to remote host!\n");
			final File localDirOrFileObj = new File(fromLocalDirOrFile);
			if (localDirOrFileObj.isFile()) {
				System.out.println("Uploading file: "+ fromLocalDirOrFile);
				
				uploadFile(ftpClient, fromLocalDirOrFile, toRemoteDirOrFile
						+ FILE_SEPERATOR_LINUX + localDirOrFileObj.getName());				
			} else {
				uploadDirectory(ftpClient, toRemoteDirOrFile, fromLocalDirOrFile,EMPTY);
			}

			//Log out and disconnect from the server once FTP operation is completed.
			ftpClient.logout();
			ftpClient.disconnect();
			responseMessage = "Upload completed successfully!";
			System.out.println(responseMessage);
			
			System.out.println("\nSuccessfully disconnected to remote host!");
		} catch (IOException ioexcp) {
			responseMessage = ioexcp.getMessage();
			ioexcp.printStackTrace();
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
		
		System.out.println("Listing the directory tree: " + fromLocalParentDir);

		final File localDir = new File(fromLocalParentDir);
		final File[] subFiles = localDir.listFiles();
		
		if (subFiles != null && subFiles.length > 0) {
			for (final File item : subFiles) {
				
				String remoteFilePath = toRemoteDir + FILE_SEPERATOR_LINUX + remoteParentDir
						+ FILE_SEPERATOR_LINUX + item.getName();
				if (EMPTY.equals(remoteParentDir)) {
					remoteFilePath = toRemoteDir + FILE_SEPERATOR_LINUX + item.getName();
				}

				if (item.isFile()) {
					// Upload the file
					final String localFilePath = convertToLinuxFormat(item.getAbsolutePath());
					System.out.println("Uploading file: "+ localFilePath);
					final boolean isFileUploaded = uploadFile(ftpClient,
							localFilePath, remoteFilePath);
					if (isFileUploaded) {
						System.out.println("File uploaded: '"
								+ remoteFilePath+"'");
					} else {
						System.err.println("Could not upload the file: '"
								+ localFilePath+"'");
					}
				} else {
					//Recursively traverse the directory and create the directory.
					// Create directory on the server
					final boolean isDirCreated = ftpClient.makeDirectory(remoteFilePath);
					if (isDirCreated) {
						System.out.println("Created the directory: '"
								+ remoteFilePath+"' on remote host");
					} else {
						System.err.println("Could not create the directory: '"
								+ remoteFilePath+"' on remote host, directory may be existing!");
					}

					//Directory created, now upload the sub directory
					String parentDirectory = remoteParentDir + FILE_SEPERATOR_LINUX + item.getName();
					if (EMPTY.equals(remoteParentDir)) {
						parentDirectory = item.getName();
					}

					fromLocalParentDir = item.getAbsolutePath();
					//Call to uploadDirectory to upload the sub-directories
					uploadDirectory(ftpClient, toRemoteDir, fromLocalParentDir,
							parentDirectory);
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
			return ftpClient.storeFile(toRemoteFilePath, inputStream);
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