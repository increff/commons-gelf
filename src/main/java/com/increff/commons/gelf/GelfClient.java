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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class GelfClient extends Thread {

	private RestTemplate t;
	private HttpHeaders headers;
	private String baseUrl;

	public GelfClient(String baseUrl) {
		this.baseUrl = baseUrl;
		this.t = new RestTemplate();
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
	}

	public void addHeader(String headerName, String headerValue) {
		headers.add(headerName, headerValue);
	}

	//if this function is runnig, it means that if large payloads req needs to be dropped, its already done
	public void send(GelfRequest req) throws IOException {
		// Create the request body as a MultiValueMap
		String json = GelfEncoder.computeJson(req);
		send(json);
	}

	protected void send(String gelfMessage) throws RestClientException {
		// Note the body object as first parameter!
		HttpEntity<?> httpEntity = new HttpEntity<String>(gelfMessage, headers);
		ResponseEntity<String> response = //
				t.exchange(baseUrl, HttpMethod.POST, httpEntity, String.class);
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			return;
		}
	}

}
