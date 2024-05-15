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

import lombok.extern.log4j.Log4j;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Log4j
public class ESClient {
	private RestHighLevelClient client;

	private ActionListener<IndexResponse> actionListener;

	private ESMetrics metrics;

	private final static Logger LOGGER = Logger.getLogger(ESClient.class);

	public ESClient(String baseUrl, int port, String user, String password) {

		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(user, password));

		this.metrics = new ESMetrics();

		RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(baseUrl, port, "http"));
		restClientBuilder.setHttpClientConfigCallback(getHttpClientConfig(credentialsProvider)).setDefaultHeaders(compatibilityHeaders());

		this.client = new RestHighLevelClient(restClientBuilder);


		setupListener();
	}

	private Header[] compatibilityHeaders() {
		return new Header[]{
				new BasicHeader(HttpHeaders.ACCEPT, "application/vnd.elasticsearch+json;compatible-with=7"),
				new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.elasticsearch+json;compatible-with=7")
		};
	}

    private RestClientBuilder.HttpClientConfigCallback getHttpClientConfig(CredentialsProvider credentialsProvider) {
        return new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(
                    HttpAsyncClientBuilder httpClientBuilder) {
                return httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider);
            }
        };
    }

    // Listener for post call action
	private void setupListener() {
		actionListener = new ActionListener<IndexResponse>() {
			@Override
			public void onResponse(IndexResponse indexResponse) {
				metrics.addNumProcessed(1);
				metrics.addNumSuccess(1);
			}

			@Override
			public void onFailure(Exception e) {
				metrics.addNumProcessed(1);
				metrics.addNumDropped(1);

				String errorStackTrace = getErrorStackTraceString(e);
				LOGGER.info("EsClient:RuntimeException: Unable to connect/send message to ElasticSearch\n" + errorStackTrace);
			}
		};
	}

	// This message sends an async request
	public void send(ESRequest req) {
		String json = null;
		try {
			json = ESEncoder.getJson(req);
		} catch (Exception e) {
			String errorStackTrace = getErrorStackTraceString(e);
			LOGGER.info("EsClient:IOException: Json Encoding Failed\n" + errorStackTrace);
		}

		IndexRequest request = new IndexRequest(req.getApplication() + "-" + LocalDate.now());
		request.source(json, XContentType.JSON);

		client.indexAsync(request, RequestOptions.DEFAULT, actionListener);
	}

	public void close() {
		try {
			client.close();
		} catch (IOException e) {
			String errorStackTrace = getErrorStackTraceString(e);
			LOGGER.info("EsClient:IOException: Failed to close client\n" + errorStackTrace);
		}
	}

	public ESMetrics getMetrics() {
		return metrics;
	}

	public List<SearchHit> search(SearchSourceBuilder sourceBuilder, String indexPattern) throws IOException {

		try {

			List<SearchHit> list= new ArrayList<>();
			sourceBuilder.sort(SortBuilders.fieldSort("timestamp").order(SortOrder.DESC));

			SearchRequest searchRequest = new SearchRequest();
			if(!StringUtils.isEmpty(indexPattern)) {
				searchRequest.indices(indexPattern);
			}
			searchRequest.source(sourceBuilder);

			SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);

			SearchHits hits = searchResponse.getHits();
			for (SearchHit hit : hits) {
				list.add(hit);
			}
			return list;
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return new ArrayList<>();

	}

	private static String getErrorStackTraceString(Exception e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}

}
