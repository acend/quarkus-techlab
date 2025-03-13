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

Standardization is a general need in all fields after some technique is widely used. In the last few years the trend for scalable, event-driven systems grew massively. Everybody and everything is communicating in events. And all systems face the same question at some point - what should our events look like. If you were smart enough you asked the question rather earlier than later. If not, at some point you will face the truth that you will have to refactor a lot to achieve consistency in your events throughout your distributed system. This is where you would have wished to know CloudEvents already.

CloudEvents brings a specification to describe events in a common way. The common language increases consistency, accessibility and portability in distributed systems. Major programming languages like Java, Go, JavaScript, Ruby, Rust, Python have SDKs and APIs to implement CloudEvents in a simple way. At it's core it will bring us a blueprint or language to define a set of metadata to describe the event.

Example CloudEvent:
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


### Maven dependencies reference


#### Producer

{{< solutionref project="quarkus-cloudevents-producer" class="dependencies" >}}


#### Consumer

{{< solutionref project="quarkus-cloudevents-consumer" class="dependencies" >}}


### {{% param sectionnumber %}}.2: Implementation

Let's try to get our hands dirty and test the CloudEvent specification to fire some events.

As it just happens the Smallrye reactive messaging extension supports CloudEvents out of the box!

We create two new Quarkus projects. Create producer application with:
```s
mvn io.quarkus.platform:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=ch.puzzle \
    -DprojectArtifactId=quarkus-cloudevents-producer \
    -Dextensions="quarkus-rest,quarkus-messaging-kafka"
```

Create consumer application:
```s
mvn io.quarkus.platform:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=ch.puzzle \
    -DprojectArtifactId=quarkus-cloudevents-consumer \
    -Dextensions="quarkus-rest,quarkus-messaging-kafka"
```

Remove the test classes and add the following extensions to the `pom.xml` of the projects `quarkus-cloudevents-producer` and `quarkus-cloudevents-consumer`:

Dependencies in `pom.xml`:
```xml
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-apicurio-registry-avro</artifactId>
        <exclusions>
            <exclusion>
                <groupId>jakarta.ws.rs</groupId>
                <artifactId>jakarta.ws.rs-api</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
```

For demonstration purposes we use a Avro-schema, which is the most common approach to manage schemas with Kafka.


### {{% param sectionnumber %}}.2.1: The Producer

Create an AVRO Schema `src/main/avro/SensorMeasurement.avsc` with the following content

```json
{
  "namespace": "ch.puzzle.quarkustechlab.cloudevents",
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
{{% alert color="primary" title="Generated Java Class" %}}
According to this AVRO Schema the Java class will be generated when you run `mvn compile`. You'll find the generated class in the `target/generated-sources/avsc` directory.  

Now is a good time to have this class generated with `mvn compile`. It might be required to add the folder `target/generated-sources` as `Generated Sources Root` in your IDE.
{{% /alert %}}

This represents a simple Java POJO to hold some information about measurements we want to emit.

We create a service `KafkaProducer` which allows us to emit `SensorMeasurements` to a defined Channel `measurements` which we will connect to a Kafka Topic. This could look like the following:

```java
package ch.puzzle.quarkustechlab.cloudevents.producer.boundary;

import ch.puzzle.quarkustechlab.cloudevents.SensorMeasurement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

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

Alter the pre-generated REST resource to emit an event every time we receive a POST request to the endpoint `/measurements`:

```java
package ch.puzzle.quarkustechlab.cloudevents.producer.boundary;

import ch.puzzle.quarkustechlab.cloudevents.SensorMeasurement;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

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

Of course, we need some configuration in the `application.properties` to emit the events to our Kafka broker.

```properties
# Uncomment if you do not want to use the devservices redpanda container.
# kafka.bootstrap.servers=localhost:9092

mp.messaging.outgoing.measurements.apicurio.registry.auto-register=true
mp.messaging.outgoing.measurements.connector=smallrye-kafka
mp.messaging.outgoing.measurements.topic=measurements
mp.messaging.outgoing.measurements.cloud-events-source=event-producer
mp.messaging.outgoing.measurements.cloud-events-type=measurement-emitted
mp.messaging.outgoing.measurements.cloud-events-subject=subject-123
```

And this is where the magic happens: Configuring the channel `measurements` with additional properties `cloud-events-XXX` will simply enrich the message envelope with the properties defined. If you don't like the config approach and would rather enrich the message programmatically, I got you covered!

```java
    [...]
    @ConfigProperty(name = "quarkus.uuid")
    String uuid;

    @ConfigProperty(name = "quarkus.application.name")
    String applicationName;

    public void emitEvent(SensorMeasurement sensorMeasurement) {
        OutgoingCloudEventMetadata<Object> metadata = OutgoingCloudEventMetadata.builder()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create(applicationName+"-"+uuid))
                .withType("measurement-emitted")
                .withSubject("subject-123")
                .build();

        logger.info("Producing Cloud Event, (spec-version: {}): id: '{}', source:  '{}', type: '{}', subject: '{}', payload-message: '{}' ",
            metadata.getSpecVersion(),
            metadata.getId(),
            metadata.getSource(),
            metadata.getType(),
            metadata.getSubject().orElse("no subject"),
            sensorMeasurement);
        
        sensorMeasurementEmitter.send(Message.of(sensorMeasurement).addMetadata(metadata));
    }
    [...]
```

And that's all you need to create events to our little system!


### {{% param sectionnumber %}}.2.2: The consumer

The consumer side of the system looks quite similar. Create an Avro schema `src/main/avro/SensorMeasurement.avsc` with the following content

```json
{
  "namespace": "ch.puzzle.quarkustechlab.cloudevents",
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

> Don't forget to run `mvn compile` to generate your SensorMeasurement class.

Instead of producing messages, we will simply define a listener on a channel connected to the same Kafka topic and print the events to the command line. Create the `EventListener`

```java
package ch.puzzle.quarkustechlab.cloudevents.consumer.boundary;

import ch.puzzle.quarkustechlab.cloudevents.SensorMeasurement;
import io.smallrye.reactive.messaging.ce.IncomingCloudEventMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class EventListener {

    private final Logger logger = LoggerFactory.getLogger(EventListener.class);

    @Incoming("measurements")
    public CompletionStage<Void> consume(Message<SensorMeasurement> message) {
        IncomingCloudEventMetadata cloudEventMetadata = message.getMetadata(
                IncomingCloudEventMetadata.class).orElseThrow(() -> new IllegalArgumentException("Expected a CloudEvent!"));
        
        logger.info("Received Cloud Events (spec-version: {}): id: '{}', source:  '{}', type: '{}', subject: '{}', payload-message: '{}' ",
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
quarkus.http.port=8081

# Uncomment if you do not want to use the devservices redpanda container.
# kafka.bootstrap.servers=localhost:9092

mp.messaging.incoming.measurements.connector=smallrye-kafka
mp.messaging.incoming.measurements.topic=measurements
```

And that's all you need to have your application up and running!


### {{% param sectionnumber %}}.2.3: Test your events

Start both of your applications in your favorite IDE or shell:

```s
./mvnw compile quarkus:dev
```

Fire some requests against your producer and test your CloudEvents getting emitted and consumed!

```s
curl -X POST localhost:8080/measurements
```

Your consumer should log the received Event
```s
Received Cloud Events (spec-version: 1.0): 
  id: '98d85610-6d8d-4943-b8ea-641c4940e148', 
  source:  'quarkus-cloudevents-producer-70430aea-5702-4f8b-a8cc-cfd0475fb1a3', 
  type: 'measurement-emitted',
  subject: 'subject-123', 
  payload-message: '{"data": 0.12169099891061863}' 
```

Take your time and start exploring your setup. If you have a closer look at the startup log of your producer you'll find the url for the schema registry which is started as a devservice. Your Avro schema is propagated to this registry and you should find your schema using the UI. Further you can have a look at the dev ui of your quarkus applications. You'll find an Apache Kafka Client Card with details about your kafka cluster like topics, nodes, consumer groups and even messages.


### {{% param sectionnumber %}}.3: Recap

With the CloudEvents standard you can increase consistency, accessibility and portability of your microservice architecture. You can save time discussing what events should look like and increase efficiency by simply implementing your events and get stuff done!
