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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A GELF message according to the <a href="http://graylog2.org/gelf#specs">GELF
 * specification</a> <code>
 {
 "version": "1.1",
 "host": "example.org",
 "short_message": "A short message that helps you identify what is going on",
 "full_message": "Backtrace here\n\nmore stuff",
 "timestamp": 1385053862.3072,
 "level": 1,
 "_user_id": 9001,
 "_some_info": "foo",
 "_some_env_var": "bar"
}
 </code>
 */

public class GelfRequest {

	private GelfVersion version;
	private String host;
	private String shortMessage;
	private String fullMessage;
	private long timestamp;
	private GelfLevel level;
	private Map<String, Object> additionalFields;
	private boolean hasLongField;

	public GelfRequest(String shortMessage) {
		this(shortMessage, "localhost");
	}

	public GelfRequest(String shortMessage, String host) {
		this(shortMessage, host, GelfVersion.V1_1);
	}

	public GelfRequest(String shortMessage, String host, GelfVersion version) {
		this.shortMessage = shortMessage;
		this.host = host;
		this.version = version;
		additionalFields = new HashMap<>();
		timestamp = getEpochInSeconds(System.currentTimeMillis());
		level = GelfLevel.ALERT;
		hasLongField = false;

		markLongField(shortMessage);

	}

	public GelfVersion getVersion() {
		return version;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	public void setFullMessage(String fullMessage) {
		markLongField(fullMessage);
		this.fullMessage = fullMessage;
	}

	public double getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public GelfLevel getLevel() {
		return level;
	}

	public void setLevel(GelfLevel level) {
		this.level = level;
	}

	public Map<String, Object> getAdditionalFields() {
		return Collections.unmodifiableMap(additionalFields);
	}

	public void addAdditionalField(String key, String value) {
		markLongField(value);
		additionalFields.put(key, value);
	}

	public void addAdditionalField(String key, Number value) {
		additionalFields.put(key, value);
	}

	@Override
	public String toString() {
		return String.format("GelfRequest{version=\"%s\" timestamp=\"%d\" short_message=\"%s\", level=\"%s\"}", version,
				timestamp, shortMessage, level);
	}

	public static long getEpochInSeconds(long milliseconds) {
		return milliseconds / 1000;
	}

	public boolean hasLongField() {
		return hasLongField;
	}

	private void markLongField(String value) {
		boolean isLongField = GelfEncoder.isLongField(value);
		hasLongField = hasLongField || isLongField;

	}

}
