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

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * ELK Request component
 */

@Builder
@Getter
@Setter
public class ESRequest {
	// This is always a high level app name eg. proxy, assure, iris, irisx.
	private String application;

	// Host or origin of the request
	private String host;
	//submodule being monitored under an app.
	// eg. for proxy applications this can be: myntra, amazon, flipkart etc.
	// eg. for for assure applications this can be: cims, wms, oms
	private String module;
	// underlined client being served by the request.
	private String client;
	// Name or the purpose of the request
	private String requestName;
	private String url;
	private ZonedDateTime timestamp; //
	private ZonedDateTime endTimestamp;
	private int durationInMillis; // RTT
	private ESRequestStatus status;
	private String requestBody;
	private String responseBody;
	private String httpHeaders;
	private String httpMethod;
	private String httpStatus;
}
