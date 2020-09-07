---
title: "7.1 Reactive messaging with Kafka"
linkTitle: "7.1 Reactive messaging with Kafka"
weight: 31
sectionnumber: 7.1
description: >
   Messaging with Apache Kafka in Quarkus.
---

## {{% param title %}}

We have defined our requirements for our new microservices which we want to have reactive. Apache Kafka brings a lot of handy features to build such systems at big scale. 

In this chapter we want to use Apache Kafka as our message oriented middleware. Kafka has some own concepts and introduces a ton of other functionality. But for starters were going to use it as a simple message broker.

### {{% param sectionnumber %}}.1: Define Kafka Cluster

In this techlab you are going to set up your own Kafka cluster which will handle your messages. Add the following resource definition to your infrastructure project under `quarkus-techlab-infrastructure/src/main/openshift/kafka`: 

```yaml

apiVersion: kafka.strimzi.io/v1beta1
kind: Kafka
metadata:
  name: quarkus-techlab-user
spec:
  kafka:
    version: 2.5.0
    replicas: 1
    listeners:
      plain: {}
      tls: {}
    config:
      auto.create.topics.enable: false
      offsets.topic.replication.factor: 1
      transaction.state.log.replication.factor: 1
      transaction.state.log.min.isr: 1
      log.message.format.version: "2.5"
    storage:
      type: jbod
      volumes:
      - id: 0
        type: persistent-claim
        size: 10Gi
        deleteClaim: false
  zookeeper:
    replicas: 1
    storage:
      type: persistent-claim
      size: 10Gi
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
    strimzi.io/cluster: quarkus-techlab-user
spec:
  partitions: 1
  replicas: 1
  config:
    retention.ms: 7200000
    segment.bytes: 1073741824

```

If you apply these manifests you can see the Kafka cluster appear in your OpenShift project.

```s

oc apply -f quarkus-techlab-infrastructure/src/main/openshift/kafka

```

You will see that OpenShift will deploy a single node Kafka cluster into your namespace. 

For local development we will create a small docker-compose file to start a Kafka cluster:

quarkus-techlab-infrastructure/src/main/docker/kafka/docker-compose.yml
```yaml

version: '2'

services:

  zookeeper:
    image: strimzi/kafka:0.11.3-kafka-2.1.0
    command: [
      "sh", "-c",
      "bin/zookeeper-server-start.sh config/zookeeper.properties"
    ]
    ports:
      - "2181:2181"
    environment:
      LOG_DIR: /tmp/logs

  kafka:
    image: strimzi/kafka:0.11.3-kafka-2.1.0
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

docker-compose -f quarkus-techlab-infrastructure/src/main/docker/kafka/docker-compose.yml up -d

```

### {{% param sectionnumber %}}.2: Producing messages

In order to use reactive messaging with kafka in our microservices we will add another extension to them: 

```s

./quarkus-techlab-data-producer/mvnw quarkus:add-extension -Dextensions="smallrye-reactive-messaging-kafka"
./quarkus-techlab-data-consumer/mvnw quarkus:add-extension -Dextensions="smallrye-reactive-messaging-kafka"

```

Let's start by creating a reactive producer which is going to do the same thing he always does: Produce random SensorMeasurements.

ch.puzzle.quarkustechlab.reactiveproducer.boundary.ReactiveDataProducer.java
```java

@ApplicationScoped
public class ReactiveDataProducer {

    @Outgoing("data-inbound-reactive")
    public Flowable<SensorMeasurement> generateStream() {
        return Flowable.interval(5, TimeUnit.SECONDS)
                .map(tick -> new SensorMeasurement());
    }
}

```

As you can see we create a Flowable of SensorMeasurement which you can imagine as a stream of data sent to the channel "data-inbound-reactive". After setting up the data producer we need to connect the Connectors to our Kafka cluster.

application.properties
```yaml

[...]

# Configure the SmallRye Kafka connector
kafka.bootstrap.servers=quarkus-techlab-kafka-bootstrap:9092
%dev.kafka.bootstrap.servers=localhost:9092

# Configure the Kafka sink
mp.messaging.outgoing.data.connector=smallrye-kafka
mp.messaging.outgoing.data.topic=manual
mp.messaging.outgoing.data.value.serializer=io.quarkus.kafka.client.serialization.JsonbSerializer

```

### {{% param sectionnumber %}}.3: Consuming messages

On the other side we want to consume the data we just produced in the Kafka `manual` Topic. 
Let's create a ReactiveDataConsumer class: 

ch.puzzle.quarkustechlab.reactiveconsumer.boundary.ReactiveDataConsumer.java
```java

@ApplicationScoped
public class ReactiveDataConsumer {

    private final Logger logger = Logger.getLogger(ReactiveDataConsumer.class.getName());

    @Incoming("data")
    public void consumeStream(SensorMeasurement sensorMeasurement) {
        logger.info("Received reactive message: " + JsonbBuilder.create().toJson(sensorMeasurement));
    }
}

```

To receive and deserialize our messages we need to impelement a SensorMeasurementDeserializer which inherits from the JsonbDeserializer: 

ch.puzzle.quarkustechlab.reactiveconsumer.control.SensorMeasurementDeserializer
```java

public class SensorMeasurementDeserializer extends JsonbDeserializer<SensorMeasurement> {

    public SensorMeasurementDeserializer() {
        super(SensorMeasurement.class);
    }
}

``` 

After creating the deserializer we need to setup the connectors for the consumer to connect to our Kafka cluster:

```yaml

[...]

# Configure the SmallRye Kafka connector
kafka.bootstrap.servers=quarkus-techlab-kafka-bootstrap:9092
%dev.kafka.bootstrap.servers=localhost:9092

# Configure the Kafka sink
mp.messaging.incoming.data.connector=smallrye-kafka
mp.messaging.incoming.data.topic=manual
mp.messaging.incoming.data.value.deserializer=io.quarkus.kafka.client.serialization.JsonbDeserializer

```

When you are finished, test your applications first locally. If they work and deliver the desired output, commit your changes, push them and release them with your pipelines!
