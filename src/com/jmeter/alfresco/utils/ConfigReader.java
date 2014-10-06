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
import java.util.Properties;

/**
 * The Class ConfigReader.<br/>
 * This class will read the resource file in resource bundle and prepares the key 
 * value pairs of properties and returns when fetched using keys.
 *
 * @author Abhinav Kumar Mishra
 * @since 2014
 */
public final class ConfigReader {

	/** The Constant KEYS. */
	private final static Properties KEYS = new Properties();

	static {
		final File configFile = new File(System.getenv(Constants.JMETER_HOME)
				+ File.separator + "bin" + File.separator + Constants.CONFIG);
		try (final InputStream inStream = new FileInputStream(configFile)) {
			if (inStream != null) {
				KEYS.load(inStream);
			}
		} catch (IOException ioexcp) {
			try (InputStream inStream = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream(Constants.CONFIG)) {
				if (inStream != null) {
				  KEYS.load(inStream);
				} 
			} catch (IOException ioex) {
				ioex.printStackTrace();
			}
		}
	}
    
	/**
	 * Instantiates a new property reader.
	 */
	private ConfigReader() {
		super();
	}

	/**
	 * Gets the property.
	 *
	 * @param property the property
	 * @return the property
	 */
	public static String getProperty(final String property) {
		return KEYS.getProperty(property);
	}
}
