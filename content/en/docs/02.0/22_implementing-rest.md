---
title: "2.2 Implementing REST Services"
linkTitle: "2.2 Implementing REST Services"
weight: 220
sectionnumber: 2.2
description: >
  This section covers implementing REST services.
---

## {{% param sectionnumber %}}.1: Implementing REST Services

In this section we learn how microservices can communicate through REST. In this example we want to build a microservice
which produces random data when it's REST interface is called. Another microservice consumes then the data and exposes
it on its own endpoint.


### {{% param sectionnumber %}}.2: Producing Data


#### Maven dependencies reference

The solution for this lab uses the following dependencies in the `pom.xml`:

{{< csvtable csv="/solution/quarkus-rest-data-producer/dependencies.csv" class="dependencies" >}}

Be aware that `quarkus.platform.version` and `quarkus-plugin.version` should be set to `{{% param "quarkusVersion" %}}` in your `pom.xml`.


#### Implementation

Create a new Quarkus application like shown before called `quarkus-rest-data-producer`. The application should expose a `DataResource` on the path `/data` which provides the user with a `SensorMeasurement` holding a random double when requested.

{{% details title="Hint" %}}

```bash
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=ch.puzzle \
    -DprojectArtifactId=quarkus-rest-data-producer \
    -DclassName="ch.puzzle.quarkustechlab.restproducer.boundary.DataResource" \
    -Dpath="/data"
```

{{% /details %}}

To write better APIs and share data over our defined resources, we need the `quarkus-rest-jackson` extension which provides us with
Jackson functionalities for our REST interfaces.
To add an extension to your existing Quarkus application simply use:

```bash
./mvnw quarkus:add-extension -Dextensions="quarkus-rest-jackson"
```

To see the available extensions you can use:

```bash
./mvnw quarkus:list-extensions
```

Alternatively you could just add the new dependency to your `pom.xml` manually.

In our lab we want to transfer sensor measurements between our microservices. Create a new class `SensorMeasurement` with a single public field called data which holds a Double. In the constructor assign the data field a random generated Double. Edit your REST resource to return a new `SensorMeasurement` whenever it's called.

It should look something like this:

```java
package ch.puzzle.quarkustechlab.restproducer.entity;

public class SensorMeasurement {

    public Double data;

    public SensorMeasurement() {
        this.data = Math.random();
    }
}

```

In the generated DataResource edit the `@GET` endpoint to return a new SensorMeasurement and change the `@Produces` type to `MediaType.APPLICATION_JSON`.

```java
package ch.puzzle.quarkustechlab.restproducer.boundary;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ch.puzzle.quarkustechlab.restproducer.entity.SensorMeasurement;

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

Start up your API and test your endpoint manually. If your `quarkus-rest-data-producer` works, let's continue consuming the data we just provided.

For more information about writing REST APIs with Quarkus see the [documentation](https://quarkus.io/guides/rest-json)


### {{% param sectionnumber %}}.3: Consuming Data


#### Maven dependencies reference

The solution for this lab uses the following dependencies in the `pom.xml`:

{{< csvtable csv="/solution/quarkus-rest-data-producer/dependencies.csv" class="dependencies" >}}

Be aware that `quarkus.platform.version` and `quarkus-plugin.version` should be set to `{{% param "quarkusVersion" %}}` in your `pom.xml`.


#### Implementation

With another microservice we would like to consume the data served by our `quarkus-rest-data-producer`. Create another quarkus application called `quarkus-rest-data-consumer` with the follwing extensions: `quarkus-rest-client-jackson`.

{{% details title="Hint" %}}

```bash
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=ch.puzzle \
    -DprojectArtifactId=quarkus-rest-data-consumer \
    -DclassName="ch.puzzle.quarkustechlab.restconsumer.boundary.DataConsumerResource" \
    -Dpath="/data" \
    -Dextensions="quarkus-rest-client-jackson"

```

{{% /details %}}

In the `quarkus-rest-data-consumer` microservice we will have another resource on the path `/data` which serves for now as a proxy to our `quarkus-rest-data-producer`. We will consume the `quarkus-rest-data-producer` microservices API with a service called `DataProducerService`. To achieve that, generate an interface called `DataProducerService` which mirrors the `quarkus-rest-data-producer` DataResource. Annotate the `DataProducerService` with the MicroProfile annotation `@RegisterRestClient` to allow Quarkus to acces the interface for CDI Injection as a REST client.

```java
package ch.puzzle.quarkustechlab.restconsumer.boundary;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/data")
@RegisterRestClient
public interface DataProducerService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    SensorMeasurement getSensorMeasurement();
}
```

Implement the same POJO `SensorMeasurement` as in the producer again for the `quarkus-rest-data-consumer` project but with an empty constructor.

{{% details title="Hint" %}}

```java
package ch.puzzle.quarkustechlab.restconsumer.entity;

public class SensorMeasurement {

    public Double data;

    public SensorMeasurement() {}
}
```

{{% /details %}}

To access the defined interface as a RestClient we need to configure it properly. To configure the rest client we can edit our `application.properties`.
We need to define at least the base url which the RestClient should use and the default injection scope for the CDI bean.

```yaml
quarkus.rest-client."ch.puzzle.quarkustechlab.restconsumer.boundary.DataProducerService".url=http://localhost:8080
quarkus.rest-client."ch.puzzle.quarkustechlab.restconsumer.boundary.DataProducerService".scope=jakarta.inject.Singleton
```

When managing multiple RestClients the configuration with the fully qualified name of the class (`ch.puzzle.quarkustechlab.restconsumer.boundary.DataProducerService`) the readability suffers pretty fast. You can extend the annotation of the RestClient (`@RegisterRestClient`) with a configKey property to shorten the configurations.

```java
@Path("/data")
@RegisterRestClient(configKey = "data-producer-api")
public interface DataProducerService {
    
[...]
```

```java
// application.properties
quarkus.rest-client.data-producer-api.url=http://localhost:8080
quarkus.rest-client.data-producer-api.scope=jakarta.inject.Singleton
```

To use the registered RestClient in our application inject it into the `DataConsumerResource` and simply call the defined interface's method. To inject a RestClient into your desired class create a field of type `DataProducerService dataProducerService` and annotate it with `@RestClient`.
You can edit our resource in the `quarkus-rest-data-consumer` to use the `DataProducerService` to create a proxy consuming the API of the `quarkus-rest-data-producer` and return it.

```java
import org.eclipse.microprofile.rest.client.inject.RestClient;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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

To run both microservices you have to alter the `application.properties` of the consumer and change its default port. Simply add `quarkus.http.port=8081` to your `application.properties` and the default port will be changed.

When you have both microservices running, try sending a request to the consumer. You will see that we receive a `SensorMeasurement`, which the `quarkus-rest-data-producer` produced. Probably you'll only see the generated object reference like this `SensorMeasurement@4c7758a8`. Do you remember what we did in the producer to get the json output?

{{% details title="Hint" %}}

To get the full json output you have to add the `quarkus-rest-jackson` extension and make sure the media type is set to json.

{{% /details %}}
