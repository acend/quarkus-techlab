# Reactive Messaging with Kafka in Quarkus

## The Reactive Manifesto

"Today's demands are simply not met by yesterday’s software architectures." - The Reactive Manifesto

Old fashioned applications - often built as monoliths - struggle to meet the requirements of todays workload dimensions. Applications should become scalable, robust and easy to maintain. Similar to the [twelve-factor-app](https://12factor.net/) manifesto there is a Reactive Manifesto. It describes requirements to tackle problems for modern enterprise solutions.

[The Reactive Manifesto](https://www.reactivemanifesto.org/) defines that reactive systems are:
    * Responsive
    * Resilient
    * Elastic
    * Message Driven

We have already learned basics of messaging in a microservice architecture. Time to take a next step and make our messaging reactive. The approach we have seen in the chapter before is completely legitimate and there is nothing wrong with this approach.

## Reactive Messaging

In the last chapter we learned about basic messaging concepts and how two microservices can communicate with a message broker. In reactive messaging we connect channels directly to components. Instead of having a Thread running manually, we can annotate functions to bind them to events sent in a specific channel or data stream. This makes our code more readable and act in a reactive manner. Let's look at an example: 

```java 

/* [...] */

    @Incoming("data-inbound-reactive")
    @Outgoing("data-outbound-reactive")
    public String streamProcess(String value) {
        return value.toUpperCase();
    }

/* [...] */

```

If you read this example it's pretty clear what is happening. We are connecting with a Connector to a Channel (Queue or Topic) we call "data-inbound-reactive" and define it as the inbound connector for this method. On the other side we connect the outcome of this method to the "data-outbound-reactive" stream. Whenever the "data-inobund-reactive" stream sends an message we perform a transformation to uppercase and return the value into the "data-outbound-reactive" channel. Simple as that!

### Connectors

Connector can:

    * retrieve messages from a remote broker (inbound)
    * send messages to a remove broker (outbound)

A connector can, of course, implement both directions.

Inbound connectors are responsible for:

    * Getting messages from the remote broker,
    * Creating a Reactive Messaging Message associated with the retrieved message.
    * Potentially associating technical metadata with the message. This includes unmarshalling the payload.
    * Associating a acknowledgement callback to acknowledge the incoming message when the Reactive Messaging message is acknowledged.
	
Important: 
Reactive matters! The first step should follow the reactive streams principle: uses non-blocking technology, respects downstream requests.

Outbound connectors are responsible for:

    * Receiving Reactive Messaging Message and transform it into a structure understand by the remote broker. This includes marshalling the payload.
    * If the Message contains outbound metadata (metadata set during the processing to influence the outbound structure and routing), taking them into account.
    * Sending the message to the remote broker.
    * Acknowledging the Reactive Messaging Message when the broker has accepted / acknowledged the message.

### Event Driven Architecture

With this reactive messaging approach we can build our applications on an event driven approach. Some interaction or trigger emits an event to a certain channel. Subscribers of this channel consume the message and react based on the event received. This loosens the coupling in our application and lowers the cohesion between logically seperated components. 

In an event driven approach everything that happens in our application gets triggered by an event. Events in the event-driven software model describe what happens within a software system. If we imagine our application as a set of logically ordered processes every task in these processes gets triggered by such an event and might emit another new event. This concept is very intuitive to apply because it is a very natural way of thinking about how everyday things and tasks work.

Some typical patterns in event-driven architecture:

### Event notification

In this approach, microservices emit events through channels to trigger behaviour or notify other components about the change of a state in the application. Notification events do not carry too much data and are very light weight. This results in a very effective and ressource friendly communication between the microservices. 

### Event-carried state transfer

Instead of only notifying about events this approach sends a payload as a message to another component containing every information needed to perform actions triggered by this event. This model comes very close to the typical RESTful approach and can be implemented very similar. Depending on the amount of data in the payload the network traffic might suffer under the amount of data transferred.

### Event-sourcing

The goal of event-sourcing is to represent every change in a system's state as an emitted event in chronological order. The event stream becomes the principle source of truth about the applications state. Changes in state, as sequences of events, are persisted in the event stream and can be 'replayed'.

## Kafka

We have defined our requirements for our new microservices which we want to have reactive. Apache Kafka brings a lot of handy features to build such systems at big scale. 

In this chapter we want to use Apache Kafka as our message oriented middleware. Kafka has some own concepts and introduces a ton of other functionality. But for starters were going to use it as a simple message broker.

### Define Kafka Cluster

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

### Producing messages

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

### Consuming messages

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