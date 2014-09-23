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
 * The Class FtpUtils.
 */
public final class FtpUtils {
	
	/**
	 * Instantiates a new fTP utils.
	 */
	private FtpUtils(){
		super();
	}

	/** The instance. */
	private static final FtpUtils FU_INSTANCE = new FtpUtils();
	
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
	 * @param localDirOrFile the local dir
	 * @param remoteDirOrFile the remote dir
	 */
	public void uploadDirectoryOrFile(final String host, final int port,
			final String userName, final String password,
			final String localDirOrFile, final String remoteDirOrFile) {
		
		final FTPClient ftpClient = new FTPClient();
		try {
			
			// Connect and login to get the session
			ftpClient.connect(host, port);
			ftpClient.login(userName, password);
			
			//Use local passive mode to pass fire-wall
			ftpClient.enterLocalPassiveMode();
			System.out.println("Connection successful!\n");
			
			final File localDirOrFileObj = new File(localDirOrFile);

			if (localDirOrFileObj.isFile()) {
				System.out.println("Uploading file: "+ localDirOrFile);
				uploadSingleFile(ftpClient, localDirOrFile, remoteDirOrFile
						+ FILE_SEPERATOR_LINUX + localDirOrFileObj.getName());
				
				System.out.println("Upload completed!");
			} else {
				uploadDirectory(ftpClient, remoteDirOrFile, localDirOrFile,EMPTY);
			}
			
			//Log out and disconnect from the server once FTP operation is completed.
			ftpClient.logout();
			ftpClient.disconnect();
			
			System.out.println("\nDisconnected from the remote host..");
			
		} catch (IOException ioexcp) {
			ioexcp.printStackTrace();
		}
	}
	
	/**
	 * Upload directory.
	 *
	 * @param ftpClient the ftp client
	 * @param remoteDir the remote dir path
	 * @param localParentDir the local parent dir
	 * @param remoteParentDir the remote parent dir
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void uploadDirectory(final FTPClient ftpClient,
			final String remoteDir, String localParentDir,
			final String remoteParentDir) throws IOException {

		localParentDir = convertToLinuxFormat(localParentDir);
		localParentDir = checkLinuxSeperator(localParentDir);
		
		System.out.println("Listing the directory tree: " + localParentDir);

		final File localDir = new File(localParentDir);
		final File[] subFiles = localDir.listFiles();
		
		if (subFiles != null && subFiles.length > 0) {
			for (final File item : subFiles) {
				
				String remoteFilePath = remoteDir + FILE_SEPERATOR_LINUX + remoteParentDir
						+ FILE_SEPERATOR_LINUX + item.getName();
				
				if (EMPTY.equals(remoteParentDir)) {
					remoteFilePath = remoteDir + FILE_SEPERATOR_LINUX + item.getName();
				}

				if (item.isFile()) {
					// Upload the file
					final String localFilePath = convertToLinuxFormat(item.getAbsolutePath());
					
					System.out.println("Uploading file: "
							+ localFilePath);
					final boolean isFileUploaded = uploadSingleFile(ftpClient,
							localFilePath, remoteFilePath);
					if (isFileUploaded) {
						System.out.println("File uploaded to: '"
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
								+ remoteFilePath+"' to remote host");
					} else {
						
						System.err.println("Could not create the directory: '"
								+ remoteFilePath+"' to remote host, directory may be existing!");
					}

					//Directory created, now upload the sub directory
					String parentDirectory = remoteParentDir + FILE_SEPERATOR_LINUX + item.getName();
					if (EMPTY.equals(remoteParentDir)) {
						parentDirectory = item.getName();
					}

					localParentDir = item.getAbsolutePath();
					uploadDirectory(ftpClient, remoteDir, localParentDir,
							parentDirectory);
				}
			}
		}
	}

	/**
	 * Upload single file.
	 *
	 * @param ftpClient the ftp client
	 * @param localFilePath the local file path
	 * @param remoteFileURI the remote file path
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private boolean uploadSingleFile(final FTPClient ftpClient,
			final String localFilePath, final String remoteFileURI)
			throws IOException {
			
		final File localFile = new File(localFilePath);
		final InputStream inputStream = new FileInputStream(localFile);
		try {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			return ftpClient.storeFile(remoteFileURI, inputStream);
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
	
	
	/**
	 * Gets the single instance of FTPUtils.
	 *
	 * @return single instance of FTPUtils
	 */
	public static FtpUtils getInstance(){
		return FU_INSTANCE;
	}
}