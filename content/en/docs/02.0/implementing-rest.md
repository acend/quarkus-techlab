---
title: "2.2 Impelementing RESTs Services"
linkTitle: "2.2 Impelementing REST Services"
weight: 220
sectionnumber: 2.2
description: >
  This section covers implementing REST services.
---

## {{% param sectionnumber %}}.1: Implementing REST Services

In this section we learn how microservices can communicate through REST. In this example we want to build a microservice which produces random data when it's REST interface is called. Another microservice consumes then the data and exposes it on ot's own endpoint.


### {{% param sectionnumber %}}.2: Producing Data

Create a new Quarkus application like shown before called 'data-producer'. The application should expose a `DataResource` on the path "/data" which provides the user with a randomly generated double when requested.

```bash

mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=ch.puzzle \
    -DprojectArtifactId=data-producer \
    -DclassName="ch.puzzle.quarkustechlab.restproducer.boundary.DataResource" \
    -Dpath="/data"
```

To write better APIs and share data over our defined resources, we need the 'resteasy-jsonb' extension which provides us with
JSON-B functionalities for our REST interfaces.
To add an extension to your existing Quarkus application simply use:

```bash

./mvnw quarkus:add-extension -Dextensions="quarkus-resteasy-jsonb"

```

To see the available extensions you can use:

```bash

./mvnw quarkus:list-extensions

```

You are also able to just add the new dependency to your `pom.yml` manually.

In the generated DataResource edit the `@GET` endpoint to return a simple double and change the `@Produces` type to `MediaType.APPLICATION_JSON`.

```java

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public double produceData() {
        return Math.random() * 10;
    }

```

In our lab we want to transfer sensor measurements between our microservices. Create a new class `SensorMeasurement` with a single public field called data which holds a Double. In the constructor assign the data field a random generated Double. Edit your REST resource to return a new `SensorMeasurement` whenever it's called.

It should look something like this:

```java

public class SensorMeasurement {

    public Double data;

    public SensorMeasurement() {
        this.data = Math.random();
    }
}

```

```java

@Path("/data")
public class DataResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement produceData() {
        return new SensorMeasurement();
    }
}

```

Please update or delete the generated tests which Quarkus provides when generating a project. They will not be needed any further and only have demonstration purposes.


For more information about writing REST APIs with Quarkus see the [documentation](https://quarkus.io/guides/rest-json)


### {{% param sectionnumber %}}.3: Consuming Data

With another microservice we would like to consume the data served by our data-producer. Create another quarkus application called 'data-consumer' with the follwing extensions: "rest-client, resteasy-jsonb".

```bash

mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=ch.puzzle \
    -DprojectArtifactId=data-consumer \
    -DclassName="ch.puzzle.quarkustechlab.restconsumer.boundary.DataConsumer" \
    -Dpath="/data" \
    -Dextensions="rest-client, resteasy-jsonb"

```

In the data-consumer microservice we will have another resource on the path "/data" which serves for now as a proxy to our data-producer. We will consume the data-producer microservices API with a service called `DataProducerService`. To achieve that, generate an interface called `DataProducerService` which mirrors the data-producer's DataResource. Annotate the `DataProducerService` with the MicroProfile annotation `@RegisterRestClient` to allow Quarkus to acces the interface for CDI Injection as a REST Client.

```java

// DataProducerService

@Path("/data")
@RegisterRestClient
public interface DataProducerService {

    @GET
    @Produces("application/json")
    SensorMeasurement getSensorMeasurement();
}

```

Implement the same POJO as in the producer again for the data-consumer project.

To access the defined interface as a RestClient we need to configure it properly. To configure the rest client we can edit our `application.properties`.
We need to define at least the base url which the RestClient should use and the default injection scope for the CDI bean.

```yaml

ch.puzzle.quarkustechlab.restconsumer.boundary.DataProducerService/mp-rest/url=http://localhost:8080
ch.puzzle.quarkustechlab.restconsumer.boundary.DataProducerService/mp-rest/scope=javax.inject.Singleton

```

When managing multiple RestClients the configuration with the fully qualified name of the class (`ch.puzzle.quarkustechlab.restconsumer.boundary.DataProducerService`) the readability suffers pretty fast. You can extend the annotation of the RestClient (`@RegisterRestClient`) with a configKey property to shorten the configurations.

```java

// DataProducerService

[...]

@Path("/data")
@RegisterRestClient(configKey = "data-producer-api")
public interface DataProducerService {

[...]
```

```java

// application.properties
data-producer-api/mp-rest/url=http://localhost:8080
data-producer-api/mp-rest/scope=javax.inject.Singleton

```

To use the registered RestClient in our application inject it into the DataConsumerResource and simply call the defined interface's method. To inject a RestClient into your desired class create a field of type `DataProducerService dataProducerService` and annotate it with `@RestClient`.
You can edit our resource in the data-consumer to use the `DataProducerService` to create a proxy consuming the data-producer's API and return it.

```java

@Path("/data")
public class DataConsumerResource {

    @RestClient
    DataProducerService dataProducerService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement getData() {
        return dataProducerService.getSensorMeasurement();
    }
}

```

To run both microservices you have to alter the `application.properties` of the consumer and change it's default port. Simply add `quarkus.http.port=8081` to your `application.properties` and the default port will be changed.

When you have both microservices running, try sending a request to the consumer. You will see that we receive a SensorMeasurement, which the data-producer produced.
