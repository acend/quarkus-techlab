---
title: "3.3 Hands on testing"
linkTitle: "3.3 Hands on testing"
weight: 330
sectionnumber: 3.3
description: >
  Testing your Quarkus application.
---

In this section we are going to extend our created REST microservices and create tests for the producer and consumer.


## Task {{% param sectionnumber %}}.1: Testing your Quarkus producer

You can start with your `quarkus-rest-data-producer` project. Add tests for verifying your producing interfaces. Try to use the different techniques for mocking your injected beans.

#### Maven dependencies reference

The solution for this lab uses the following dependencies in the `pom.xml`:

{{< csvtable csv="/solution/quarkus-rest-data-producer/dependencies.csv" class="dependencies" >}}

Be aware that `quarkus.platform.version` and `quarkus-plugin.version` should be set to `{{% param "quarkusVersion" %}}` in your `pom.xml`.


#### Implementation

For demonstration purposes we implement a new endpoint `/data/dummy` which simply returns a String `dummy` with the help of an injected `DummyService`.

It could look something like this:

```java
package ch.puzzle.quarkustechlab.restproducer.control;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DummyService {

    public String dummy() {
        return "dummy";
    }
}
```

```java
@Path("/data")
public class DataResource {

    @Inject
    DummyService dummyService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement data() {
        return new SensorMeasurement() ;
    }

    @GET
    @Path("/dummy")
    public String dummy() {
        return dummyService.dummy();
    }
}
```

Add the necessary dependencies to your project and you're all good to go!

Write tests verifying your two endpoints and try the different approach for mocking your `DummyService`.


## Task {{% param sectionnumber %}}.2: Testing the consumer

When you have tested your data producer microservice it's time to write tests for the consuming part. Be careful on the consuming side, when mocking the `@RestClient` in the API you have to alter the CDI injection scope. As mentioned in the chapter before, the scope `Singleton` will not be available for mocking in your tests. Alter the scope of the injected rest-client. There are two possible approaches to do this: You can annotate the client with `@ApplicationScoped` to alter the scope or you can modify your `application.properties` to only modify the scope of the bean in the `test` profile.

```s
quarkus.http.port=8081
quarkus.rest-client.data-producer-api.url=http://localhost:8080
quarkus.rest-client.data-producer-api.scope=jakarta.inject.Singleton
%test.quarkus.rest-client.data-producer-api.scope=jakarta.enterprise.context.ApplicationScoped
```

This will modify the scope of your bean only during the test context.

Either way write tests for verifying your `/data` endpoint and mock your rest client.
