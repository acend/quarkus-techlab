---
title: "8.2 Reactive messaging with Kafka"
linkTitle: "8.2 Reactive messaging with Kafka"
weight: 820
sectionnumber: 8.2
description: >
   Messaging with Apache Kafka in Quarkus.
---

## {{% param title %}}

We have defined our requirements for our new microservices which we want to have reactive. Apache Kafka brings a lot of handy features to build such systems at big scale.

In this chapter we want to use Apache Kafka as our message oriented middleware. Kafka has some own concepts and introduces a ton of other functionality. But for starters were going to use it as a simple message broker.


### {{% param sectionnumber %}}.1: Apache Kafka

Apache Kafka is an event streaming platform used to collect, process, store, and integrate data at scale. It has numerous use cases including distributed streaming, stream processing, data integration, and pub/sub messaging. If you'd like to go more in depth, continue reading the article [here](https://developer.confluent.io/what-is-apache-kafka/).


### {{% param sectionnumber %}}.2: Local Development

For local development we do have the choice to either run our Kafka services via Quarkus Devservices or with docker-compose.
If you want to use the Quarkus Devservices simply remove in the further configurations of the applications the property `kafka.bootstrap.servers=localhost:9092` from your `application.properties`. This will setup a [Redpanda](https://vectorized.io/redpanda) container for your environment.

{{% details title="Without Devservices" %}}
If you choose to test your local services with a Kafka broker you can use a small docker-compose file `solution/kafka/docker/docker-compose.yml`
to start a Kafka cluster:

```yaml

version: '3'

services:

  zookeeper:
    image: docker.io/strimzi/kafka:0.30.0-kafka-3.1.0
    command: [
        "sh", "-c",
        "bin/zookeeper-server-start.sh config/zookeeper.properties"
    ]
    ports:
      - "2181:2181"
    environment:
      LOG_DIR: /tmp/logs

  kafka:
    image: docker.io/strimzi/kafka:0.30.0-kafka-3.1.0
    command: [
        "sh", "-c",
        "bin/kafka-server-start.sh config/server.properties --override listeners=$${KAFKA_LISTENERS} --override advertised.listeners=$${KAFKA_ADVERTISED_LISTENERS} --override zookeeper.connect=$${KAFKA_ZOOKEEPER_CONNECT}"
    ]
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      LOG_DIR: "/tmp/logs"
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181

```

Start your cluster with:

```s

docker-compose -f solution/kafka/docker/docker-compose.yml up -d

```
{{% /details %}}

Create again two Quarkus projects 'quarkus-reactive-messaging-consumer' and 'quarkus-reactive-messaging-producer'.

```s

# Create producer application

mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
      -DprojectGroupId=ch.puzzle \
      -DprojectArtifactId=quarkus-reactive-messaging-producer \
      -Dextensions="smallrye-reactive-messaging-kafka,quarkus-jackson,quarkus-jsonb" \
      -DprojectVersion=1.0.0

# Create consumer application

mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
      -DprojectGroupId=ch.puzzle \
      -DprojectArtifactId=quarkus-reactive-messaging-consumer \
      -Dextensions="smallrye-reactive-messaging-kafka,quarkus-resteasy-reactive-jackson,quarkus-resteasy-reactive" \
      -DprojectVersion=1.0.0

```

Next, create the SensorMeasurement in both project.

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
// ...producer.boundary.ReactiveDataProducer.java

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

To ensure the connection from the connector to your message broker we need some configuration.

```s
#application.properties

# If you'd like to configure your own kafka cluster
# kafka.bootstrap.servers=localhost:9092

mp.messaging.outgoing.data.connector=smallrye-kafka
mp.messaging.outgoing.data.topic=data
mp.messaging.outgoing.data.value.serializer=io.quarkus.kafka.client.serialization.JsonbSerializer

```

We define the connector which we are going to use to communicate, the topic in which the data will be sent to and the serializer for the value.

To check if your producer is producing data correctly, you can use the kafka-container with it's console utilities! Inside the kafka container you can use the script `bin/kafka-console-consumer.sh` with the parameters `--bootstrap-server localhost:9092 --topic data --from-beginning` to read the messages inside the 'data' topic.

{{% details title="Hint" %}}

```s

docker exec -it docker_kafka_1 bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic data --from-beginning

```

{{% /details %}}


### {{% param sectionnumber %}}.4: Consuming messages

On the other side of the system we want to consume the messages and stream them again to a REST API. Create a class `..consumer.boundary.ReactiveDataConsumer` and similar to the producer create a function which takes a `SensorMeasurement` as parameter and returns a `SensorMeasurement`. For simplicity reasons the function will only return the received measurement again. Annotate your created function with the connectors `@Incoming("data")` and `@Outgoing("in-memory-stream")`. Additionally we can annotate the function with `@Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)` to ensure the acknowledgement of the messages received.

{{% details title="Hint" %}}

```java
// ...producer.boundary.ReactiveDataProducer.java

@ApplicationScoped
public class ReactiveDataConsumer {

    private static final Logger log = Logger.getLogger(ReactiveDataConsumer.class.getName());

    @Incoming("data")
    @Outgoing("in-memory-stream")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public SensorMeasurement consume(SensorMeasurement sensorMeasurement) {
        return sensorMeasurement;
    }
}

```

{{% /details %}}

To receive and deserialize our messages we need to impelement a SensorMeasurementDeserializer which extends the JsonbDeserializer:

```java
// ..producer.boundary.SensorMeasurementDeserializer


public class SensorMeasurementDeserializer extends JsonbDeserializer<SensorMeasurement> {

    public SensorMeasurementDeserializer() {
        super(SensorMeasurement.class);
    }
}

```

After creating the deserializer we need to setup the connectors for the consumer to connect to our Kafka cluster:

```s
quarkus.http.port=8081

# Remove property for Devservices
kafka.bootstrap.servers=localhost:9092

mp.messaging.incoming.data.connector=smallrye-kafka
mp.messaging.incoming.data.topic=data
mp.messaging.incoming.data.value.deserializer=ch.puzzle.consumer.boundary.SensorMeasurementDeserializer

```

As you might have noticed, we defined a `@Outgoing("in-memory-stream")` which does not have any connectors defined in the `application.properties`. This is an in-memory stream and we are going to use it to produce the data in our REST API.

Create or update your `..consumer.boundary.DataResource` to expose an endpoint `/data`. Create a `@GET` annotated endpoint to emit a `Multi<SensorMeasurement>` as a stream of data. To read the data from the in-memory stream create an injected field `@Channel(in-memory-stream") Multi<SensorMeasurement> channel` and simply return this channel in your API. Set up the annotations for your reactive rest-easy endpoint to convert the data to JSON.

{{% details title="Hint" %}}

```java

@Path("/data")
public class DataResource {

    @Inject
    @Channel("in-memory-stream")
    Multi<SensorMeasurement> channel;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> stream() {
        return channel;
    }
}

```

{{% /details %}}

Start up your kafka cluster with the created docker-compose file and your two microservices and test your API.

The result should look similar to this:

```s
$ curl -N localhost:8081/data

data:{"data":0.838331984637051,"time":"2021-03-23T10:52:11.563830Z"}

data:{"data":0.21252592222197708,"time":"2021-03-23T10:52:13.563800Z"}

data:{"data":0.2170284442342123,"time":"2021-03-23T10:52:15.563695Z"}

```

If you followed the tutorial step by step, you should receive now empty data points. Alter the constructor in the producer to produce random events without restarting the services and see the data chaning without restarting your services manually!
