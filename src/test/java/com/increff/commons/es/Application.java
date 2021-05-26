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

import java.io.*;
import java.time.*;
import java.util.Properties;

public class Application {



    private static int MAX_ROWS = 20;


    public static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        String propFileName = "test.properties";

        properties.load(new FileReader(propFileName));

        return properties;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Properties properties = loadProperties();

        String address = properties.getProperty("elasticsearch.url");
        String user = properties.getProperty("elasticsearch.username");
        String password = properties.getProperty("elasticsearch.password");
        int port = Integer.parseInt(properties.getProperty("elasticsearch.port"));

        ESClient client = new ESClient(address, port, user, password);

        for (int i = 2; i < MAX_ROWS; i++) {
            ESRequest req = ESRequest.builder()
                    .application("test")

                    .module("MYNTRA-PROXY")
                    .client("PUMA")
                    .host("Aman's Macbook Pro")

                    .requestName("GET")
                    .durationInMillis(5000)
                    .status(ESRequestStatus.FAILURE)
                    .timestamp(ZonedDateTime.now())
                    .build();

            client.send(req);
        }

        Thread.sleep(10000);

        ESMetrics metrics = client.getMetrics();
        System.out.println("Requests Processed: " + metrics.getNumProcessed() + "\nRequests successful: " + metrics.getNumSuccess() + "\nRequests dropped: " + metrics.getNumDropped());

        client.close();
    }
}
