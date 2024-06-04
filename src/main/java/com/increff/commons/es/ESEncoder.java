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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZoneId;


/**
 * This class is responsible for encoding ESRequest objects into JSON format.
 * It uses the Jackson library's JsonFactory to create a JsonGenerator, which is used to write the
 * fields of the ESRequest to a JSON string.
 */
public class ESEncoder {

    /**
     * A static JsonFactory instance used to create JsonGenerator instances.
     */
    private final static JsonFactory jsonFactory;

    static {
        jsonFactory = new JsonFactory();
    }

    /**
     * Converts the given ESRequest into a JSON string.
     * This method writes the fields of the ESRequest to a JSON string using a JsonGenerator.
     * If an IOException occurs during the operation, it is thrown to the caller.
     *
     * @param req The ESRequest to be converted into a JSON string.
     * @return The JSON string representation of the ESRequest.
     * @throws IOException If an error occurs during the operation.
     */
    public static String getJson(ESRequest req) throws IOException {
        StringWriter w = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(w);

        g.writeStartObject();

        g.writeStringField("application", req.getApplication());
        g.writeStringField("host", req.getHost());
        g.writeStringField("module", req.getModule());
        g.writeStringField("url", req.getUrl());
        g.writeStringField("client", req.getClient());

        g.writeStringField("timestamp", req.getTimestamp().withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime().toString());
        g.writeStringField("request_name", req.getRequestName());
        g.writeNumberField("duration_millis", req.getDurationInMillis());
        g.writeStringField("status", req.getStatus().name());
        g.writeStringField("requestBody", req.getRequestBody());
        g.writeStringField("responseBody", req.getResponseBody());
        g.writeStringField("http_headers", req.getHttpHeaders());
        g.writeStringField("http_status", req.getHttpStatus());
        g.writeStringField("end_timestamp", req.getEndTimestamp().withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime().toString());
        g.writeStringField("http_method", req.getHttpMethod());
        g.writeStringField("response_headers", req.getResponseHeaders());
        g.writeStringField("transactionId", req.getTransactionId());
        g.writeStringField("remarks", req.getRemarks());
        g.close();
        return w.toString();
    }
}
