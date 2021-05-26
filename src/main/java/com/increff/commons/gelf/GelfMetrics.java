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

package com.increff.commons.gelf;

public class GelfMetrics {

	private int numProcessed;
	private int numSuccess;
	private int numDropped;
	private int numReceived;

	public synchronized int getNumSuccess() {
		return numSuccess;
	}

	public synchronized int getNumProcessed() {
		return numProcessed;
	}

	public synchronized int getNumDropped() {
		return numDropped;
	}

	public synchronized int getNumReceived() {
		return numReceived;
	}

	// protected HELPER METHODS
	protected synchronized void addNumProcessed(int val) {
		numProcessed += val;
	}

	protected synchronized void addNumRecieved(int val) {
		numReceived += val;
	}

	protected synchronized void addNumSuccess(int val) {
		numSuccess += val;
	}

	protected synchronized void addNumDropped(int val) {
		numDropped += val;
	}

}
