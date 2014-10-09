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

/**
 * The Class TaskTimer.<br/>
 * This class will be used to get time taken in a particular task.<br/>
 * 
 * Here is a pseudo sample for timer:<br/><br/>
 *       start()<br/>
 *       //perform the task<br/>
 *       end()<br/>
 *       print(getTotalTime())<br/>
 */
public class TaskTimer {
	
	/** The start time. */
	private long startTime = 0;
	
	/** The end time. */
	private long endTime = 0;

	/**
	 * Start.
	 */
	public void start() {
		this.startTime = System.currentTimeMillis();
	}

	/**
	 * End.
	 */
	public void end() {
		this.endTime = System.currentTimeMillis();
	}

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public long getStartTime() {
		return this.startTime;
	}

	/**
	 * Gets the end time.
	 *
	 * @return the end time
	 */
	public long getEndTime() {
		return this.endTime;
	}

	/**
	 * Gets the total time.
	 *
	 * @return the total time
	 */
	public long getTotalTime() {
		return this.endTime - this.startTime;
	}
}
