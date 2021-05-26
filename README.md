# commons-gelf
This library is useful for logging HTTP requests and responses using the GELF protocol to Graylog and to ELK 

## Overview
This library is useful for logging HTTP requests and responses using the GELF protocol to Graylog and to ELK for monitoring.
It is extensively used in SaaS based applications, especially in Assure integrations as it provides a structured way to log JSON format messages.

## Usage
### pom.xml

```xml
<dependency>
    <groupId>com.increff.commons</groupId>
    <artifactId>commons-gelf</artifactId>
    <version>{commons-gelf.version}</version>
</dependency>
```
### Code for Graylog

```java
@Autowired
private GelfManager gm;
//...
        function doSomething(){
        GelfRequet req=new GelfRequest();
        req.setFullMessage("this is a full message");
        req.addAdditionalField(someKey,someValue);
//...
        gm.add(req)
        }
```
Code for ELK
```java
@Autowired
private ESClient esClient;
//...
        function doSomething(){
        ESRequest eq=new ESRequest();
        req.setApplication("Assure Proxy");
        req.setModule("FLIPKARTV3");
//...
        esClient.send(eq)
        }
```
### Note
You will need to first configure `GelfManager` and `ESClient` with credentials and end-points, so that they send data to the right servers.

## Key Classes
### GelfRequest
A GELF message is a JSON string with the following fields:

- version: GELF spec version
- host: the name of the host, source or application that sent this message
- shortMessage: A short descriptive message
- fullMessage: A long message that can i.e. contain a backtrace; optional.
- timestamp: Seconds since UNIX epoch with optional decimal places for milliseconds. Will be set to the current timestamp (now) by the server if absent.
- level: Can be one of EMERGENCY, ALERT, CRITICAL, ERROR, WARNING, NOTICE, INFO or DEBUG
- \_[additionalFields]: Every field you send and prefix with an underscore (_) will be treated as an additional field. Allowed characters in field names are any word character (letter, number, underscore), dashes and dots.

### Example Payload
```json
{
  "version": "1.1",
  "host": "example.org",
  "short_message": "A short message that helps you identify what is going on",
  "full_message": "Backtrace here\n\nmore stuff",
  "timestamp": 1385053862.3072,
  "level": 1
}
```
### GelfClient
A Gelf Client is responsible for converting a GelfRequest object to JSON format using a GelfEncoder and sending the logs to the GrayLog server using a POST request.

#### Gelf Manager
This manager is what is used in applications as it internally uses a `GelfClient` and implements the Runnable class. It consists of a queue with a maximum size of 1000. In addition to the (consumer or primary) queue that Graylog provides, this queue acts as a producer or secondary queue. Messages are first populated in this secondary queue and then sent to the Graylog server. In case the secondary queue provided hits maximum capacity then messages start getting dropped. There are two options offered here,

1. Either the messages can be discarded altogether
2. They can be written to the disk but this risks bloating up your disk space.

Apart from this the manager also provides certain metrics such as the number of messages received, the number processed, how many of these have been successful (sent to the server with a success status code) and how many have been dropped. If there is an error in sending logs from the manager's queue to Graylog, 10 retries are provided interspersed with wait times of 1 second. The secondary queue is queried every second to see if there are any messages to be sent to the server.

#### ESClient
This is the ElasticSearch client. It is used to send ESRequest data to the ElasticSearch endpoint.

#### ESRequest
This contains payload for ElasticSearch. The payload is designed in a manner to monitor API requests / responses only (and not their full payload)

## License
Copyright (c) Increff

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied. See the License for the specific language governing permissions and limitations under
the License.
