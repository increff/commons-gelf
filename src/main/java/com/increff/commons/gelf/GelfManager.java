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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.LinkedBlockingDeque;

import lombok.extern.log4j.Log4j;
import org.springframework.web.client.HttpStatusCodeException;

/*
 * This class is designed to create a fast and fault tolerant Gelf client.
 * 
 * All new messages are queued, and a background thread tries to push to GrayLog.
 * This way calling methods are not slowed down
 * 
 * If there is no space in queue, then oldest message is dropped(queue.pollFirst())
 * Then new message is put in queue. 
 * 
 * If a message delivery fails because of 402, 403, 404, 502 etc, then message is added to top of queue, 
 * so that it is retried quickly. However, if queue is full, then that too will be dropped
 * 
 *  If, there was no message, or status was 404 or 502 (given by load balancers) then the thread
 *  sleeps for RETRY_SLEEP_TIME milliseconds to avoid unnecessary loops
			
 * All GelfManager methods are synchronized to ensure thread safety.
 * 

 */
/** this is under development, do not use this class **/
@Log4j
public class GelfManager implements Runnable {

	private static int RETRY_MAX_COUNT = 10;
	private static int RETRY_WAIT_TIME = 60_000; // 60 seconds
	private static int MAX_QUEUE_SIZE = 1000;
	private static int EMPTY_WAIT_TIME = 1_000; // 1 second

	private GelfMetrics m;
	private GelfClient c;
	private LinkedBlockingDeque<GelfRequest> q;

	private double avgGraylog;
	private boolean running;
	private int retryCount;
	private IGelfLogProvider logProvider;

	public GelfManager(String baseUrl) {
		this.q = new LinkedBlockingDeque<>(MAX_QUEUE_SIZE);
		this.m = new GelfMetrics();
		this.c = new GelfClient(baseUrl);
		this.avgGraylog = -1;
	}

	// FOR STARTING AND STOPPING
	public void setLogProvider(IGelfLogProvider logProvider) {
		this.logProvider = logProvider;
	}

	public synchronized void start() {
		if (!running) {
			Thread t = new Thread(this);
			t.start();
			running = true;
		}
	}

	public synchronized void stop() {
		running = false;
		GelfRequest msg = null;
		while (!q.isEmpty()) { // log all pending messages
			msg = getFirst();
			dropRequest(msg);
		}
	}

	public synchronized boolean isRunning() {
		return running;
	}

	// METRICES
	public synchronized int getQueueSize() {
		return q.size();
	}

	public synchronized GelfMetrics getMetrics() {
		return m;
	}

	// FOR MANAGING MESSAGES
	public synchronized void add(GelfRequest req) {
		// we want to keep the latest requet, so remove first message if queue is full
		if (q.remainingCapacity() < 10) {
			GelfRequest dropReq = getFirst();
			dropRequest(dropReq);
		}
		if (req.hasLongField()) {
			dropRequest(req);
			return;
		}
		q.offer(req);
		m.addNumRecieved(1);
	}

	public synchronized void addLargeReq(GelfRequest req) {
		if (q.remainingCapacity() < 10) {
			GelfRequest dropReq = getFirst();
			log.info("Graylog Dropping Request: queue capacity: " + q.remainingCapacity());
			dropRequest(dropReq);
		}
		q.offer(req);
		m.addNumRecieved(1);
	}

	private synchronized GelfRequest getFirst() {
		// Retrieves and removes the first element of this deque, or returns null if
		// this deque is empty.
		GelfRequest r = q.pollFirst();
		if (r != null) {
			m.addNumProcessed(+1);
		}
		return r;
	}

	private synchronized void retry(GelfRequest req) {
		// Inserts the specified element at the front of this deque if it is possible to
		// do so immediately without violating capacity restrictions,returning true upon
		// success and false if no space is currently available.
		boolean result = q.offerFirst(req);
		if (!result) {
			dropRequest(req);
		} else {
			m.addNumProcessed(-1);
		}
	}

	private void dropRequest(GelfRequest req) {
		if (req == null) {
			return;
		}
		m.addNumDropped(1);
		if (logProvider == null) {
			return;
		}
		try {
			String json = GelfEncoder.computeJson(req);
			logProvider.log(json);
		} catch (Exception e) {
			// return, cannot do much here really
		}
	}

	public void run() {
		// Note: We do not reset retryCount because if some message has failed even
		// after trying for RETRY_MAX_COUNT, then it is likely that the next message
		// will also fail. This can make the queue too large. Thus after
		// RETRY_MAX_COUNT, it is best to keep on trying to send messages
		// and set it to 0 only when a message has been successfully delivered

		GelfRequest req = null;
		int errStatus = 0, waitTimeMs = 0;
		while (isRunning()) {
			waitTimeMs = 0;
			errStatus = 0;
			try {
				req = getFirst();
				if (req != null) {
					ZonedDateTime t1 = ZonedDateTime.now();
					c.send(req);
					ZonedDateTime t2 = ZonedDateTime.now();
					log.info("Graylog send to graylog duration: " + Duration.between(t1, t2).toMillis());
					errStatus = 200;
					int time = (int) Duration.between(t1, t2).toMillis();
					if(avgGraylog == -1)
						avgGraylog = time;
					else{
						avgGraylog += time;
						avgGraylog = avgGraylog/2.0;
					}
					log.info("Graylog Average: " + avgGraylog);
					retryCount = 0;
					m.addNumSuccess(1);
				}
			} catch (HttpStatusCodeException e) {
				errStatus = e.getRawStatusCode();
				retryCount++;
			} catch (Exception e) {
				errStatus = 9999; // Some uknown issue has happened
				retryCount++;
			}

			if (errStatus == 200) {
				// request sent successfully, do nothing!
				waitTimeMs = 0;
			} else if (errStatus == 0) {
				waitTimeMs = EMPTY_WAIT_TIME; // no request, sleep for 1 seconds
			} else if (retryCount < RETRY_MAX_COUNT) {
				// error in sending, requeue & sleep for RETRY_SLEEP_TIME seconds
				// So total maximum we will wait for RETRY_MAX_COUNT*RETRY_SLEEP_TIME seconds
				// This is 10 minutes for now
				retry(req);
				waitTimeMs = RETRY_WAIT_TIME;
			} else {
				dropRequest(req);
			}

			try {
				// Read on Thread.sleep(0) also
				// https://stackoverflow.com/questions/3257708/thread-sleep0-what-is-the-normal-behavior
				Thread.sleep(waitTimeMs);
			} catch (InterruptedException e) {
				stop();
			}

		}
	}

}
