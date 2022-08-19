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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class GelfEncoder {

	private static final int MAX_FIELD_SIZE = 32_000;
	private final static JsonFactory jsonFactory;

	static {
		jsonFactory = new JsonFactory();
	}


	public static String computeJson(GelfRequest req) throws IOException {

		StringWriter w = new StringWriter();
		JsonGenerator g = jsonFactory.createGenerator(w);

		g.writeStartObject();

		g.writeStringField("version", req.getVersion().toString());
		g.writeNumberField("timestamp", req.getTimestamp());
		g.writeStringField("host", req.getHost());
		writeMessage(g, "short_message", req.getShortMessage());
		g.writeNumberField("level", req.getLevel().getNumericLevel());

		// Optional
		writeMessage(g, "full_message", req.getFullMessage());

		Map<String, Object> fieldMap = req.getAdditionalFields();
		for (String key : fieldMap.keySet()) {
			// Prepend "_" to key if not already there
			String realKey = key.startsWith("_") ? key : ("_" + key);
			Object value = fieldMap.get(key);
			if (value == null) {
				g.writeNullField(realKey);
			}
			if (value instanceof Number) {
				// Let Jackson figure out how to write Number values.
				g.writeObjectField(realKey, value);
			}
			if (value instanceof String && getSize(value.toString()) > MAX_FIELD_SIZE) {
				writeMessage(g, "_large_payload", value.toString());
			}
			else if (value instanceof String) {
				writeMessage(g, realKey, (String) value);
			}
			// Ignore everything except Number and String
		}

		g.close();
		return w.toString();
	}

	public static void writeMessage(JsonGenerator g, String key, String value) throws IOException {
		if (value == null) {
			g.writeNullField(key);
			return;
		}
		g.writeObjectField(key, value);
	}

	public static final boolean isLongField(String s) {
		try {
			return s == null ? false : s.getBytes("utf-8").length > MAX_FIELD_SIZE;
		} catch (UnsupportedEncodingException e) {
			// In case JVM does not support "utf-8", we just multiply by 4 bytes to get the
			// maximum possible length
			return (s.length() * 4) > MAX_FIELD_SIZE;
		}
	}

	public static int getSize(String s) {
		try {
			return s == null ? 0 : s.getBytes("utf-8").length;
		} catch (UnsupportedEncodingException e) {
			// In case JVM does not support "utf-8", we just multiply by 4 bytes to get the
			// maximum possible length
			return (s.length() * 4);
		}
	}
}
