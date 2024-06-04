/*
 * Copyright (c) 2021. Increff
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.increff.commons.es;
/**
 * This class is responsible for maintaining metrics related to the processing of ESRequests.
 * It keeps track of the number of requests processed, successful, dropped, and received.
 * All methods are synchronized to ensure thread safety.
 */
public class ESMetrics {

	/**
	 * The number of requests processed.
	 */
	private int numProcessed;

	/**
	 * The number of successful requests.
	 */
	private int numSuccess;

	/**
	 * The number of dropped requests.
	 */
	private int numDropped;

	/**
	 * The number of received requests.
	 */
	private int numReceived;

	/**
	 * Retrieves the number of successful requests.
	 * @return The number of successful requests.
	 */
	public synchronized int getNumSuccess() {
		return numSuccess;
	}

	/**
	 * Retrieves the number of processed requests.
	 * @return The number of processed requests.
	 */
	public synchronized int getNumProcessed() {
		return numProcessed;
	}

	/**
	 * Retrieves the number of dropped requests.
	 * @return The number of dropped requests.
	 */
	public synchronized int getNumDropped() {
		return numDropped;
	}

	/**
	 * Retrieves the number of received requests.
	 * @return The number of received requests.
	 */
	public synchronized int getNumReceived() {
		return numReceived;
	}

	/**
	 * Increases the number of processed requests by the given value.
	 * @param val The value to add to the number of processed requests.
	 */
	protected synchronized void addNumProcessed(int val) {
		numProcessed += val;
	}

	/**
	 * Increases the number of received requests by the given value.
	 * @param val The value to add to the number of received requests.
	 */
	protected synchronized void addNumRecieved(int val) {
		numReceived += val;
	}

	/**
	 * Increases the number of successful requests by the given value.
	 * @param val The value to add to the number of successful requests.
	 */
	protected synchronized void addNumSuccess(int val) {
		numSuccess += val;
	}

	/**
	 * Increases the number of dropped requests by the given value.
	 * @param val The value to add to the number of dropped requests.
	 */
	protected synchronized void addNumDropped(int val) {
		numDropped += val;
	}

}
