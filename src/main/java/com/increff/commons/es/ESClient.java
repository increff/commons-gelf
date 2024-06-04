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

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Base64;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * This class represents an Elasticsearch client with asynchronous capabilities.
 * It is responsible for sending requests to Elasticsearch and handling the responses.
 * It also maintains metrics related to the requests and responses.
 */
@Log4j2
public class ESClient {

	/**
	 * The Elasticsearch client used to interact with the Elasticsearch server.
	 */
	private ElasticsearchAsyncClient client;

	/**
	 * The metrics object used to keep track of the number of requests processed, successful, and dropped.
	 */
	private ESMetrics metrics;

	private final static Logger LOGGER = getLogger(ESClient.class.getName());

	/**
	 * Constructor of EsClient
	 * @param baseUrl URL of server
	 * @param port Port exposed of server
	 * @param user username
	 * @param password password
	 */
	public ESClient(String baseUrl, int port, String user, String password) {

		HttpHost httpHost = new HttpHost(baseUrl, port, "http");
		RestClient httpClient = RestClient.builder(httpHost)
				.setDefaultHeaders(new Header[]{new BasicHeader("Authorization", getBasicAuth(user, password))})
				.build();
		this.metrics = new ESMetrics();
		RestClientTransport transport = new RestClientTransport(httpClient, new JacksonJsonpMapper());
		this.client = new ElasticsearchAsyncClient(transport);

//		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
//
//		RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(baseUrl, port, "http"));
//		restClientBuilder.setHttpClientConfigCallback(getHttpClientConfig(credentialsProvider)).setDefaultHeaders(compatibilityHeaders());
//
//		this.client = new RestHighLevelClient(restClientBuilder);
//		setupListener();
	}

	private String getBasicAuth(String username, String password) {
		String encodedAuth = Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes());
		return String.format("Basic %s", encodedAuth);
	}

//	private Header[] compatibilityHeaders() {
//		return new Header[]{
//				new BasicHeader(HttpHeaders.ACCEPT, "application/vnd.elasticsearch+json;compatible-with=7"),
//				new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.elasticsearch+json;compatible-with=7")
//		};
//	}

//    private RestClientBuilder.HttpClientConfigCallback getHttpClientConfig(CredentialsProvider credentialsProvider) {
//        return new RestClientBuilder.HttpClientConfigCallback() {
//            @Override
//            public HttpAsyncClientBuilder customizeHttpClient(
//                    HttpAsyncClientBuilder httpClientBuilder) {
//                return httpClientBuilder
//                        .setDefaultCredentialsProvider(credentialsProvider);
//            }
//        };
//    }

    // Listener for post call action
//	private void setupListener() {
//		actionListener = new ActionListener<IndexResponse>() {
//			@Override
//			public void onResponse(IndexResponse indexResponse) {
//				metrics.addNumProcessed(1);
//				metrics.addNumSuccess(1);
//			}
//
//			@Override
//			public void onFailure(Exception e) {
//				metrics.addNumProcessed(1);
//				metrics.addNumDropped(1);
//
//				String errorStackTrace = getErrorStackTraceString(e);
//				LOGGER.info("EsClient:RuntimeException: Unable to connect/send message to ElasticSearch\n" + errorStackTrace);
//			}
//		};
//	}

	/**
	 * Sends an asynchronous request to Elasticsearch.
	 * This method first encodes the given ESRequest into a JSON string.
	 * If an exception occurs during encoding, it is caught and logged.
	 * The JSON string is then used to create an IndexRequest, which is sent to Elasticsearch.
	 * If an exception occurs during the request, it is caught, logged, and the metrics are updated accordingly.
	 * If the request is successful, the metrics are updated to reflect the success.
	 *
	 * @param req The ESRequest to be sent to Elasticsearch.
	 */
	public void send(ESRequest req) {
		String json = null;
		try {
			json = ESEncoder.getJson(req);
		} catch (Exception e) {
			String errorStackTrace = getErrorStackTraceString(e);
			LOGGER.info("EsClient:IOException: Json Encoding Failed\n" + errorStackTrace);
		}
		StringReader jsonStringReader = new StringReader(json);
		client.index(i -> {
			IndexRequest.Builder<Object> document = i.index(req.getApplication() + "-" + LocalDate.now())
					.withJson(jsonStringReader);
			return document;
		}).whenComplete((response, exception) -> {
			if (exception != null) {
				metrics.addNumProcessed(1);
				metrics.addNumDropped(1);

				String errorStackTrace = getErrorStackTraceString(exception);
				LOGGER.info("EsClient:RuntimeException: Unable to connect/send message to ElasticSearch\n" + errorStackTrace);
			} else {
				metrics.addNumProcessed(1);
				metrics.addNumSuccess(1);
			}});
//		IndexRequest request = new IndexRequest(req.getApplication() + "-" + LocalDate.now());
//		request.source(json, XContentType.JSON);
//		client.indexAsync(request, RequestOptions.DEFAULT, actionListener);
	}

	/**
	 * Closes the Elasticsearch client.
	 * This method attempts to close the transport layer of the Elasticsearch client.
	 * If an IOException occurs during the operation, it is caught and logged.
	 */
	public void close() {
		try {
			client._transport().close();
		} catch (IOException e) {
			String errorStackTrace = getErrorStackTraceString(e);
			LOGGER.info("EsClient:IOException: Failed to close client\n" + errorStackTrace);
		}
	}

	/**
	 * Retrieves the ESMetrics object associated with this ESClient.
	 * The ESMetrics object contains metrics related to the number of requests processed, successful, and dropped.
	 *
	 * @return The ESMetrics object.
	 */
	public ESMetrics getMetrics() {
		return metrics;
	}

	private static String getErrorStackTraceString(Throwable e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

}
