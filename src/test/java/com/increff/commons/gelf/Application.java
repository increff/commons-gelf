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

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

public class Application {

	private static int MAX_ROWS = 1;

	public static void main(String[] args) throws IOException {
        Properties properties = loadProperties();
        String address = properties.getProperty("gelf.url");

		GelfClient client = new GelfClient(address);
		for (int i = 0; i < MAX_ROWS; i++) {
			GelfRequest req = new GelfRequest("new message " + i);
			req.setHost("romillaptop");
			req.setFullMessage("this is full message");
			doProxy(req);
			client.send(req);
		}
		System.out.println("Done!");
	}

	private static void doProxy(GelfRequest req) throws IOException {

		String file = "SampleTextFile_10kb.txt";
		String data = readFile(file);
		req.addAdditionalField("_http_uri", "data");
		req.addAdditionalField("_http_headers", "headers");
		req.addAdditionalField("_http_request", data);
		req.addAdditionalField("_http_response", data);
		req.addAdditionalField("_http_method", "POST");
		req.addAdditionalField("_http_status", 200);
		req.addAdditionalField("_proxy_call", "get_orders");
		req.addAdditionalField("_proxy_channel", "flipkart");
		req.addAdditionalField("_proxy_client", "1100113");
		req.addAdditionalField("_proxy_time", GelfRequest.getEpochInSeconds(System.currentTimeMillis())); // TODO
		req.addAdditionalField("_proxy_duration", 1); // seconds
		req.addAdditionalField("_proxy_status", "SUCCESS"); // keyword
		req.addAdditionalField("_proxy_error", "full stack trace here");

	}

	private static String readFile(String fileName) throws IOException {
		String path = "/com/increff/commons/gelf/" + fileName;
		return IOUtils.toString(getInputStream(path), "UTF-8");
	}

	public static InputStream getInputStream(String resource) {
		return Application.class.getResourceAsStream(resource);
	}

    public static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        String propFileName = "test.properties";

        properties.load(new FileReader(propFileName));

        return properties;
    }

}