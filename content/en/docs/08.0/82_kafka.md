---
title: "8.2 Reactive messaging with Kafka"
linkTitle: "8.2 Reactive messaging with Kafka"
weight: 820
sectionnumber: 8.2
description: >
   Messaging with Apache Kafka in Quarkus.
---

## {{% param title %}}

We have defined our requirements for our new microservices which we want to have reactive. Apache Kafka brings a lot of handy features to build such systems at large scale.

In this chapter we want to use Apache Kafka as our message oriented middleware. Kafka has some own concepts and introduces a ton of other functionality. But for starters were going to use it as a simple message broker.


### {{% param sectionnumber %}}.1: Apache Kafka

Apache Kafka is an event streaming platform used to collect, process, store, and integrate data at scale. It has numerous use cases including distributed streaming, stream processing, data integration, and pub/sub messaging. If you'd like to go more in depth, continue reading the article [here](https://developer.confluent.io/what-is-apache-kafka/).


### {{% param sectionnumber %}}.2: Local Development


#### Maven dependencies reference


##### Producer

{{< solutionref project="quarkus-reactive-messaging-producer" class="dependencies" >}}


##### Consumer

{{< solutionref project="quarkus-reactive-messaging-consumer" class="dependencies" >}}


#### Implementation

For local development we do have the choice to either run our Kafka services via Quarkus dev-services or with docker-compose.
If you want to use the Quarkus Dev Services simply remove the line `kafka.bootstrap.servers=localhost:9092` in your `applications.properties` file. This will set up a [Redpanda](https://www.redpanda.com/) container for your development environment.

We do recommend to use the dev-services.

[//]: # ({{% alert color="primary" title="Redpanda Docker Image" %}})
[//]: # (Dev Services defaults to Redpanda images from `vectorized/redpanda`. If you do have problems downloading the Redpanda kafka image you can switch to a different provider like Strimzi.)
[//]: # ()
[//]: # (Add the following line to your `applications.properties` to switch to strimzi test containers.)
[//]: # (```yaml)
[//]: # (quarkus.kafka.devservices.provider=strimzi)
[//]: # (```)
[//]: # ()
[//]: # (You can also configure a specific docker image with the following property:)
[//]: # (```shell)
[//]: # (quarkus.kafka.devservices.image-name=docker.redpanda.com/redpandadata/redpanda:v24.3.1)
[//]: # (```)
[//]: # ({{% /alert %}})

{{% alert color="primary" title="Error: Address already in use" %}}
The devservices will try to start your Kafka broker on a random port. For some reason the devservices will sometimes always try to take the same port which is already taken. 

In that case you simply can add the following property to your system to override the port to any given free port in your `application.properties`:

````text
# application.properties
quarkus.kafka.devservices.port=8888
```
{{% /alert %}}

{{% details title="Run without dev-services" %}}
If you choose to test your local services with a Kafka broker you can use a small docker-compose file `solution/kafka-stack/docker/docker-compose.yml`
to start a Kafka cluster:

```yaml
version: '2'

services:
  kafka:
    image: {{% param "strimziVersion" %}}
    command: [
      "sh", "-c",
      "./bin/kafka-storage.sh format -t $$(./bin/kafka-storage.sh random-uuid) -c ./config/kraft/server.properties && ./bin/kafka-server-start.sh ./config/kraft/server.properties"
    ]
    ports:
    - "9092:9092"    
    environment:
      LOG_DIR: "/tmp/logs"
```

Start your cluster with:

```s
~$ docker-compose -f solution/kafka-stack/docker/docker-compose.yml up -d
```
{{% /details %}}

Create two Quarkus projects `quarkus-reactive-messaging-consumer` and `quarkus-reactive-messaging-producer`.

Create producer application
```s
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
      -DprojectGroupId=ch.puzzle \
      -DprojectArtifactId=quarkus-reactive-messaging-producer \
      -Dextensions="quarkus-messaging-kafka,quarkus-jackson,quarkus-jsonb" \
      -DprojectVersion=1.0.0
```

Create consumer application
```s
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
      -DprojectGroupId=ch.puzzle \
      -DprojectArtifactId=quarkus-reactive-messaging-consumer \
      -Dextensions="quarkus-messaging-kafka,quarkus-rest-jackson,quarkus-rest-jsonb" \
      -DclassName="ch.puzzle.quarkustechlab.messaging.consumer.boundary.DataResource" \
      -DprojectVersion=1.0.0
```

{{% alert color="warning" title="Quarkus DEV-UI in producer" %}}
As we do not include resteasy in your producer the producer does not have a ui at all and is not able to serve the dev-ui. If you want to use the quarkus dev-ui in your producer you have to add the following dependency to your `pom.xml`:
```
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-vertx-http</artifactId>
    </dependency>
```
{{% /alert %}}


Next, create the SensorMeasurement in both project in the package `ch.puzzle.quarkustechlab.messaging.[producer|consumer].entity`.

```java
public class SensorMeasurement {

    public Double data;
    public Instant time;

    public SensorMeasurement() {
    }

    public SensorMeasurement(Double data, Instant time) {
        this.data = data;
        this.time = time;
    }
}
```


### {{% param sectionnumber %}}.3: Producing messages

Let's start by creating a reactive producer which is going to do the same thing he always does: Produce random SensorMeasurements. Create a `@ApplicationScoped` class `ReactiveDataProducer`. Inside the class define a function which returns a `Multi<SensorMeasurement>`. Inside the function use the already known routine to periodically emit a new SensorMeasurement. Finally annotate the function with `@Outgoing("data")` to create a connector to your message broker.


{{% details title="Hint" %}}

```java
package ch.puzzle.quarkustechlab.messaging.producer.boundary;

import ch.puzzle.quarkustechlab.messaging.producer.entity.SensorMeasurement;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@ApplicationScoped
public class ReactiveDataProducer {

    @Outgoing("data")
    public Multi<SensorMeasurement> produceData() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
                .onItem().transform(i -> new SensorMeasurement(new Random().nextDouble(), Instant.now()));
    }
}
```

{{% /details %}}

To ensure the connection from the connector to your message broker we need some configuration in our `application.properties`.

```s
# Uncomment if you do not want to use the devservices redpanda container.
# kafka.bootstrap.servers=localhost:9092

mp.messaging.outgoing.data.connector=smallrye-kafka
mp.messaging.outgoing.data.topic=data
mp.messaging.outgoing.data.value.serializer=io.quarkus.kafka.client.serialization.JsonbSerializer
```

We define the connector which we are going to use to communicate, the topic in which the data will be sent to and the serializer for the value.

If you are using the dev-services for your kafka cluster you can use the dev-ui to check the messages (remember to add the `quarkus-vertx-http` dependency in your producer).
Head over to the dev-ui and select "Topics" from the Apache Kafka Client card. When clicking on the topic-name "data", you'll see the produced messages. You'll find something like this:

```
129     0   13/02/2025 15:38:45     {"data":0.3588087861484209,"time":"2025-03-13T14:38:45.956236432Z"}
128     0   13/02/2025 15:38:43     {"data":0.7354823691119821,"time":"2025-03-13T14:38:43.956618972Z"}
127     0   13/02/2025 15:38:41     {"data":0.8793867438783177,"time":"2025-03-13T14:38:41.956167120Z"}
...
```

Whenever you are using the docker-compose kafka stack you have to use the basic tooling available in the docker container. You will need to find the name of your kafka container using `docker ps`.  Then consume the data with:
```s
docker exec -it docker_kafka_1 bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic data --from-beginning
```


### {{% param sectionnumber %}}.4: Consuming messages

On the other side of the system we want to consume the messages and stream them again to a REST API. Create a class `ch.puzzle.quarkustechlab.messaging.consumer.boundary.ReactiveDataConsumer` and similar to the producer create a function which takes a `SensorMeasurement` as parameter and returns a `SensorMeasurement`. For simplicity reasons the function will only return the received measurement again. Annotate your created function with the connectors `@Incoming("data")` and `@Outgoing("in-memory-stream")`. Additionally we can annotate the function with `@Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)` to ensure the acknowledgement of the messages received.

{{% details title="Hint" %}}

```java
package ch.puzzle.quarkustechlab.messaging.consumer.boundary;

import ch.puzzle.quarkustechlab.messaging.consumer.entity.SensorMeasurement;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ReactiveDataConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReactiveDataConsumer.class.getName());

    @Incoming("data")
    @Outgoing("in-memory-stream")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public SensorMeasurement consume(SensorMeasurement sensorMeasurement) {
        return sensorMeasurement;
    }
}
```

{{% /details %}}

To receive and deserialize our messages we need to implement a `SensorMeasurementDeserializer` which extends the `JsonbDeserializer`:

```java
package ch.puzzle.quarkustechlab.messaging.consumer.boundary;

import ch.puzzle.quarkustechlab.messaging.consumer.entity.SensorMeasurement;
import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class SensorMeasurementDeserializer extends JsonbDeserializer<SensorMeasurement> {

    public SensorMeasurementDeserializer() {
        super(SensorMeasurement.class);
    }
}
```

After creating the deserializer we need to set up the connectors for the consumer to connect to our Kafka cluster:

```s
quarkus.http.port=8081

# Uncomment if you do not want to use the devservices redpanda container.
# kafka.bootstrap.servers=localhost:9092

mp.messaging.incoming.data.connector=smallrye-kafka
mp.messaging.incoming.data.topic=data
mp.messaging.incoming.data.value.deserializer=ch.puzzle.quarkustechlab.messaging.consumer.boundary.SensorMeasurementDeserializer
```

As you might have noticed, we defined a `@Outgoing("in-memory-stream")` which does not have any connectors defined in the `application.properties`. This is an in-memory stream and we are going to use it to produce the data in our REST API.

Create or update your consumers `DataResource` class to expose an endpoint `/data`. Create a `@GET` annotated endpoint to emit a `Multi<SensorMeasurement>` as a stream of data. To read the data from the in-memory stream create an injected field `@Channel(in-memory-stream") Multi<SensorMeasurement> channel` and simply return this channel in your API. Set up the annotations for your reactive rest-easy endpoint to convert the data to JSON.

{{% details title="Hint" %}}

```java
package ch.puzzle.quarkustechlab.messaging.consumer.boundary;

import ch.puzzle.quarkustechlab.messaging.consumer.entity.SensorMeasurement;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("/data")
public class DataResource {

    @Inject
    @Channel("in-memory-stream")
    Multi<SensorMeasurement> channel;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> stream() {
        return channel;
    }
}
```

{{% /details %}}

Start your two microservices and test your API with:

```s
curl -N localhost:8081/data
```

The result should look similar to this:
```
data:{"data":0.838331984637051,"time":"2021-03-23T10:52:11.563830Z"}

data:{"data":0.21252592222197708,"time":"2021-03-23T10:52:13.563800Z"}

data:{"data":0.2170284442342123,"time":"2021-03-23T10:52:15.563695Z"}
```

If you followed the tutorial step by step, you should receive data points from your producer.
