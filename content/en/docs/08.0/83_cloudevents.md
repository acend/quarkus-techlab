---
title: "8.3 Cloud Events"
linkTitle: "8.3 Cloud Events"
weight: 830
sectionnumber: 8.3
description: >
   Engaging the cloud with standardized events
---

## {{% param title %}}

[Github](https://github.com/cloudevents)

[CNCF](https://www.cncf.io/projects/cloudevents/) Incubating project since 2018.

![Quarkus](img/quarkus.png)
![CloudEvents](img/cloudevents.png)

Standardization is a general need in all fields after some technique is widely used. In the last few years the trend for scalable, event-driven systems grew massively. Everybody and everything is communicating in events. And all systems face the same question at some point - what should our events look like. If you were smart enough you asked the question rather earlier than later. If not, at some point you will face the truth that you will have to refactor a lot to achieve consistency in your events throughout your distributed system. This is where you would have wished to know CloudEvents already.

CloudEvents brings a specification to describe events in a common way. The common language increases consistency, accessibility and portability in distributed systems. Major programming languages like Java, Go, JavaScript, Ruby, Rust, Python have SDKs and APIs to implement CloudEvents in a simple way. At it's core it will bring us a blueprint or language to define a set of metadata to describe the event.

> Example CloudEvent

```json
{
    "specversion" : "1.0",
    "type" : "com.github.pull_request.opened",
    "source" : "https://github.com/cloudevents/spec/pull",
    "subject" : "123",
    "id" : "A234-1234-1234",
    "time" : "2018-04-05T17:31:00Z",
    "comexampleextension1" : "value",
    "comexampleothervalue" : 5,
    "datacontenttype" : "text/xml",
    "data" : "<much wow=\"xml\"/>"
}
```


### {{% param sectionnumber %}}.1: Specification

It all comes down to a handful attributes:

**Required attributes:**

attribute | type | description
---|---|---
id | String | Identifies the event (e.g. UUID)
source | URI-reference | Identifies the context in which an event happened
specversion | String | Version of the CloudEvents specification which the event uses
type | String | Describes the type of event related to the originating occurrence

**Optional attributes:**

attribute | type | description
---|---|---
datacontenttype | String [RFC 2046](https://datatracker.ietf.org/doc/html/rfc2046) | Content type of data value
subject | String | This describes the subject of the event in the context of the event producer (identified by source). In publish-subscribe scenarios, a subscriber will typically subscribe to events emitted by a source
time | Timestamp [RFC 3339](https://datatracker.ietf.org/doc/html/rfc3339) | Timestamp of when the occurrence happened

In addition to the specification of CloudEvents itself, there are extensions giving extra flexibility for other meta fields to enrich your event. For example OpenTracing header fields can be added by the [Distributed Tracing](https://github.com/cloudevents/spec/blob/main/cloudevents/extensions/distributed-tracing.md) extension.


### {{% param sectionnumber %}}.2: Implementation

Let's try to get our hands dirty and test the CloudEvent specification to fire some events.

As it just happens the Smallrye reactive messaging extension supports CloudEvents out of the box!

We create two new Quarkus projects:

> Creation of Quarkus projects

```shell

mvn io.quarkus.platform:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=data-producer \
    -Dextensions="resteasy-reactive"

mvn io.quarkus.platform:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=data-consumer \
    -Dextensions="resteasy-reactive"

```

Remove the test classes and add the following extensions to your projects' `pom.xml`:

> Dependencies in `pom.xml`

```xml

    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-confluent-registry-avro</artifactId>
    </dependency>
    <dependency>
      <groupId>io.confluent</groupId>
      <artifactId>kafka-avro-serializer</artifactId>
      <version>6.1.1</version>
      <exclusions>
        <exclusion>
          <groupId>jakarta.ws.rs</groupId>
          <artifactId>jakarta.ws.rs-api</artifactId>
        </exclusion>
      </exclusions>
    </dependency>

...

  <repositories>
    <repository>
      <id>confluent</id>
      <url>https://packages.confluent.io/maven/</url>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
    </repository>
  </repositories>

```

For demonstration purposes we use a Avro-schema, which is be the most common approach to manage schemas with Kafka.


### {{% param sectionnumber %}}.2.1: The Producer

Create a Avro schema `src/main/avro/SensorMeasurement.avsc` with the following content:

```json

{
  "namespace": "org.acme",
  "type": "record",
  "name": "SensorMeasurement",
  "fields": [
    {
      "name": "data",
      "type": "double"
    }
  ]
}

```

This represents a simple Java POJO to hold some information about measurements we want to emit.

We create a service `org.acme.KafkaProducer` which allows us to emit `SensorMeasurements` to a defined Channel `measurements` which we will connect to a Kafka Topic. This could look like the following:

```java

package org.acme;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class KafkaProducer {

    @Channel("measurements")
    @Inject
    Emitter<SensorMeasurement> sensorMeasurementEmitter;

    public void emitEvent(SensorMeasurement sensorMeasurement) {
        sensorMeasurementEmitter.send(Message.of(sensorMeasurement));
    }
}

```

Then we alter the pre-generated REST resource to emit an event every time we receive a POST request to the endpoint `/measurements`:

```java

package org.acme;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Random;

@Path("/measurements")
public class MeasurementsResource {

    private final KafkaProducer kafkaProducer;

    public MeasurementsResource(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @POST
    public Response emitMeasurement() {
        SensorMeasurement measurement = SensorMeasurement.newBuilder().setData(new Random().nextDouble()).build();
        kafkaProducer.emitEvent(measurement);
        return Response.ok().build();
    }
}

```

Of course we need some configuration in the `application.properties` to emit the events to our Kafka broker:

```properties

mp.messaging.outgoing.measurements.connector=smallrye-kafka
mp.messaging.outgoing.measurements.value.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
mp.messaging.outgoing.measurements.topic=measurements
mp.messaging.outgoing.measurements.cloud-events-source=event-producer
mp.messaging.outgoing.measurements.cloud-events-type=measurement-emitted
mp.messaging.outgoing.measurements.cloud-events-subject=subject-123

```

And this is where the magic happens: Configuring the channel "measurements" with additional properties `cloud-events-XXX` will simply enrich the message envelope with the properties defined. If you don't like the config approach and would rather enrich the message programmatically, I got you covered!

```java

    public void emitEvent(SensorMeasurement sensorMeasurement) {
        OutgoingCloudEventMetadata<Object> metadata = OutgoingCloudEventMetadata.builder()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create("event-producer"))
                .withType("measurement-emitted")
                .withSubject("subject-123")
                .build();
        sensorMeasurementEmitter.send(Message.of(sensorMeasurement).addMetadata(metadata));
    }

```

And that's all you need to create events to our little system!


### {{% param sectionnumber %}}.2.2: The consumer

The data-consumer's side of the system looks quite similar. Create a Avro schema `src/main/avro/SensorMeasurement.avsc` with the following content:

```json

{
  "namespace": "org.acme",
  "type": "record",
  "name": "SensorMeasurement",
  "fields": [
    {
      "name": "data",
      "type": "double"
    }
  ]
}

```

Instead of producing messages, we will simply define a listener on a channel connected to the same Kafka topic and print the events to the command line!

Create the EventListener `org.acme.EventListener`:

```java

package org.acme;

import io.smallrye.reactive.messaging.ce.IncomingCloudEventMetadata;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class EventListener {

    private final Logger logger = Logger.getLogger(EventListener.class);

    @Incoming("measurements")
    public CompletionStage<Void> consume(Message<SensorMeasurement> message) {
        IncomingCloudEventMetadata cloudEventMetadata = message.getMetadata(IncomingCloudEventMetadata.class).orElseThrow(() -> new IllegalArgumentException("Expected a CloudEvent!"));
        logger.infof("Received Cloud Events (spec-version: %s): id: '%s', source:  '%s', type: '%s', subject: '%s', payload-message: '%s' ",
                cloudEventMetadata.getSpecVersion(),
                cloudEventMetadata.getId(),
                cloudEventMetadata.getSource(),
                cloudEventMetadata.getType(),
                cloudEventMetadata.getSubject().orElse("no subject"),
                message.getPayload());
        return message.ack();
    }
}

```

Add the following properties to the `application.properties` file:

```properties

mp.messaging.incoming.measurements.connector=smallrye-kafka
mp.messaging.incoming.measurements.topic=measurements

quarkus.http.port=8081

```

And that's all you need to have your application up and running! If you have Docker installed, starting the application will also start a mock Kafka broker and connect your applications automatically. If you don't have Docker installed you will need to configure the connection to a Kafka broker yourself by adding the following property to the applications:

```properties

kafka.bootstrap.servers=your-kafka-broker:9092

```


### {{% param sectionnumber %}}.2.3: Test your events

Start both of your applications in your favorite IDE or shell:

```shell

./mvnw compile quarkus:dev

```

Fire some requests against your producer and test your CloudEvents getting emitted and consumed!

```shell

$ curl -X POST localhost:8080/measurements

2022-07-21 11:01:56,654 INFO  [org.acm.EventListener] (vert.x-eventloop-thread-10) Received Cloud Events (spec-version: 1.0): id: '98d85610-6d8d-4943-b8ea-641c4940e148', source:  'event-producer', type: 'measurement-emitted', subject: 'subject-123', payload-message: '{"data": 0.12169099891061863}' 


```


### {{% param sectionnumber %}}.3: Recap

With the CloudEvents standard you can increase consistency, accessibility and portability of your microservice architecture. You can save time discussing what events should look like and increase efficiency by simply implementing your events and get stuff done!
