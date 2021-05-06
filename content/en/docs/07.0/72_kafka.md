---
title: "7.2 Reactive messaging with Kafka"
linkTitle: "7.2 Reactive messaging with Kafka"
weight: 720
sectionnumber: 7.2
description: >
   Messaging with Apache Kafka in Quarkus.
---

## {{% param title %}}

We have defined our requirements for our new microservices which we want to have reactive. Apache Kafka brings a lot of handy features to build such systems at big scale.

In this chapter we want to use Apache Kafka as our message oriented middleware. Kafka has some own concepts and introduces a ton of other functionality. But for starters were going to use it as a simple message broker.


### {{% param sectionnumber %}}.1: Define Kafka Cluster

In this techlab you are going to set up your own Kafka cluster which will handle your messages. Add the following resource definition to your infrastructure project under `solution/kafka/openshift`:

```yaml

apiVersion: kafka.strimzi.io/v1beta1
kind: Kafka
metadata:
  name: quarkus-techlab
  labels:
    application: quarkus-techlab
spec:
  kafka:
    version: 2.6.0
    replicas: 1
    listeners:
      plain: {}
      tls: {}
    config:
      auto.create.topics.enable: false
      offsets.topic.replication.factor: 1
      transaction.state.log.replication.factor: 1
      transaction.state.log.min.isr: 1
      log.message.format.version: "2.6"
    resources:
      requests:
        memory: 128Mi
        cpu: "50m"
      limits:
        memory: 4Gi
        cpu: "2"
    storage:
      type: jbod
      volumes:
      - id: 0
        type: persistent-claim
        size: 2Gi
        deleteClaim: false
  zookeeper:
    replicas: 1
    resources:
      requests:
        memory: 128Mi
        cpu: "50m"
      limits:
        memory: 4Gi
        cpu: "2"
    storage:
      type: persistent-claim
      size: 2Gi
      deleteClaim: false
  entityOperator:
    topicOperator: {}
    userOperator: {}

```

For starters we need a simple Kafka Topic `manual` which we will use as communication channel to transfer data from one microservice to another.

```yaml

apiVersion: kafka.strimzi.io/v1beta1
kind: KafkaTopic
metadata:
  name: manual
  labels:
    strimzi.io/cluster: quarkus-techlab
spec:
  partitions: 1
  replicas: 1
  config:
    retention.ms: 7200000
    segment.bytes: 1073741824

```

If you apply these manifests you can see the Kafka cluster appear in your OpenShift project.

```s

oc apply -f solution/kafka/openshift

```

You will see that OpenShift will deploy a single node Kafka cluster into your namespace.


### {{% param sectionnumber %}}.2: Local Development

For local development we will use a small docker-compose file `solution/kafka/docker/docker-compose.yml`
to start a Kafka cluster:

```yaml

version: '2'

services:

  zookeeper:
    image: quay.io/strimzi/kafka:0.22.1-kafka-2.6.0
    command: [
        "sh", "-c",
        "bin/zookeeper-server-start.sh config/zookeeper.properties"
    ]
    ports:
      - "2181:2181"
    environment:
      LOG_DIR: /tmp/logs

  kafka:
    image: quay.io/strimzi/kafka:0.22.1-kafka-2.6.0
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

Create again two Quarkus projects 'quarkus-reactive-messaging-consumer' and 'quarkus-reactive-messaging-producer'.
Add the extension 'smallrye-reactive-messaging-kafka' and 'quarkus-jsonb' to your project. Create the `SensorMeasurement`
class again in both projects. For the consumer also add the 'resteasy-reactive-common' extension.

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

Let's start by creating a reactive producer which is going to do the same thing he always does: Produce random SensorMeasurements. Create a `@ApplicationScoped` class `ReactiveDataProducer`. Inside the class define a function which returns a `Multi<SensorMeasurement>`. Inside the function use the already known routine to periodically emit a new SensorMeasurement. Finally annotate the function with `@Outgoing("data-inbound")` to create a connector to your message broker.


{{% details title="Hint" %}}

```java
// ...producer.boundary.ReactiveDataProducer.java

@ApplicationScoped
public class ReactiveDataProducer {

    @Outgoing("data-inbound")
    public Multi<SensorMeasurement> produceData() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
                .onItem().transform(i -> new SensorMeasurement());
    }
}

```

{{% /details %}}

To ensure the connection from the connector to your message broker we need some configuration.

```s
#application.properties

kafka.bootstrap.servers=localhost:9092

mp.messaging.outgoing.data-inbound.connector=smallrye-kafka
mp.messaging.outgoing.data-inbound.topic=manual
mp.messaging.outgoing.data-inbound.value.serializer=io.quarkus.kafka.client.serialization.JsonbSerializer

```

We define the connector which we are going to use to communicate, the topic in which the data will be sent to and the serializer for the value.

To check if your producer is producing data correctly, you can use the kafka-container with it's console utilities! Inside the kafka container you can use the script `bin/kafka-console-consumer.sh` with the parameters `--bootstrap-server localhost:9092 --topic manual --from-beginning` to read the messages inside the 'manual' topic.

{{% details title="Hint" %}}

```s

docker exec -it docker_kafka_1 bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic manual --from-beginning

```

{{% /details %}}


### {{% param sectionnumber %}}.4: Consuming messages

On the other side of the system we want to consume the messages and stream them again to a REST API. Create a class `..consumer.boundary.ReactiveDataConsumer` and similar to the producer create a function which takes a `SensorMeasurement` as parameter and returns a `SensorMeasurement`. For simplicity reasons the function will only return the received measurement again. Annotate your created function with the connectors `@Incoming("inbound-data")` and `@Outgoing("in-memory-stream")`. Additionally we can annotate the function with `@Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)` to ensure the acknowledgement of the messages received.

{{% details title="Hint" %}}

```java
// ...producer.boundary.ReactiveDataProducer.java

@ApplicationScoped
public class ReactiveDataConsumer {

    private static final Logger log = Logger.getLogger(ReactiveDataConsumer.class.getName());

    @Incoming("data-inbound")
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
kafka.bootstrap.servers=localhost:9092

mp.messaging.incoming.data-inbound.connector=smallrye-kafka
mp.messaging.incoming.data-inbound.topic=manual
mp.messaging.incoming.data-inbound.value.deserializer=ch.puzzle.consumer.boundary.SensorMeasurementDeserializer

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
