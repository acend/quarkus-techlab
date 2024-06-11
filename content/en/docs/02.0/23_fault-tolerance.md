---
title: "2.3 REST Fault Tolerance"
linkTitle: "2.3 REST Fault Tolerance"
weight: 230
sectionnumber: 2.3
description: >
  This sections shows how to apply fault tolerance to our microservices.
---


## {{% param sectionnumber %}}.1: Fault Tolerance

As our application grows in complexity and in horizontal distribution, microservices will get their own lifecycles. We need our application to be more resilient when microservices are temporarily unavailable. In a complex structure where one HTTP request from an entrypoint triggers multiple other REST calls in our distributed system a single error at the end of the call-trace will result in chaotic behaviour if we don't anticipate this early enough.

The microprofile `fault-tolerance` (Quarkus extension: `quarkus-smallrye-fault-tolerance`) comes in very handy to implement simple but effective design patterns to be prepared for said events. Add this extension to both of your microservices!

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-smallrye-fault-tolerance</artifactId>
</dependency>
```

**Hint:** Maybe it's a good time to tag your repositories for the consumer and producer at this point. We are going to intentionally break some code and test fault tolerance which we will revert after each example.


### {{% param sectionnumber %}}.1.1: Resilience through Retries

A common approach to gain resilience in our system is to add a retry mechanism. With the annotation `@Retry` we can enable an automated retries whenever the called method throws a `RuntimeException`.

Let's take a look at an example:

In the `quarkus-rest-data-producer` project let's change our REST endpoint, which serves data to the consumer, so it will fail randomly.

```java
package ch.puzzle.quarkustechlab.restproducer.boundary;

import java.util.Random;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/data")
public class DataResource {

    private static final Logger logger = LoggerFactory.getLogger(DataResource.class);

    Random random = new Random();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement getSensorMeasurement() {
        logger.info("getSensorMeasurement called!");
        if (random.nextBoolean()) {
            logger.error("Failed!");
            throw new RuntimeException();
        }
        return new SensorMeasurement();
    }
}
```

As you can see we introduced a random boolean which will make the endpoint to fail half the time. We added a log for you to see that the retries will work and the endpoint will be called multiple times!

If we start up both microservices and try to consume data multiple times, we can see that the rest-consumer has trouble to consume data when the producer throws an exception. The default behaviour is just to return the Exception when the endpoint is called. This behaviour is nothing we would like to have in our production ready environment.

In our `quarkus-rest-data-consumer` project we will add the retry mechanism. Add the extension `quarkus-smallrye-fault-tolerance` if you don't already have and edit our `DataProducerService` interface:

```java
package ch.puzzle.quarkustechlab.restconsumer.boundary;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@Path("/data")
@RegisterRestClient(configKey = "data-producer-api")
public interface DataProducerService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 10)
    SensorMeasurement getSensorMeasurment();
    
}
```

You can see that we added the `@Retry` annotation and configured it to have a maximum of retries before it fails. Let's try to consume data again. If you send a request to your `quarkus-rest-data-consumer` you can see now from the amount of logs produced by the `quarkus-rest-data-producer` that after a failure the endpoint is instantaniously called again.


### {{% param sectionnumber %}}.1.2: Timeouts

The `@Timeout` annotation can be used to mark functions which will have a finite amount of time before a `TimeoutException` will be thrown.

We update our `quarkus-rest-data-producer` to take a random amount of time to answer with the desired response.

```java
package ch.puzzle.quarkustechlab.restproducer.boundary;

import java.util.Random;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/data")
public class DataResource {

    private static final Logger logger = LoggerFactory.getLogger(DataResource.class);

    Random random = new Random();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement getSensorMeasurement() throws InterruptedException {
        logger.info("getSensorMeasurement called!");
        Thread.sleep(random.nextInt(1000));
        return new SensorMeasurement();
    }
}

```

Then update the `DataProducerService` of our `quarkus-rest-data-consumer` to time out after `500ms`:

```java
package ch.puzzle.quarkustechlab.restconsumer.boundary;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@Path("/data")
@RegisterRestClient(configKey = "data-producer-api")
public interface DataProducerService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout(500)
    SensorMeasurement getSensorMeasurment();
    
}
```

If you send a request to the consumer, you will see that about half of the time we will run into a `TimeoutException`. The method took longer than 500 ms to finish, so the `@Timeout(500)` interrupted the invocation.


### {{% param sectionnumber %}}.1.3: Fallbacks

When we insert timeouts or a maximum amount of retries for a certain part of our code, we want to handle these exceptional states. We can use the `@Fallback` annotation to define a fallback for the failing method. We can annotate a function with `@Fallback(fallbackMethod = "getDefaultMeasurement")` so when the annotated method fails, the defined fallback method `getDefaultMeasurement()` will be invoked instead.

Let's update the example from before to use a fallback if it takes longer than the defined 500 ms to respond:

```java
package ch.puzzle.quarkustechlab.restconsumer.boundary;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;

@Path("/data")
@RegisterRestClient(configKey = "data-producer-api")
public interface DataProducerService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout(500)
    @Fallback(fallbackMethod = "getDefaultMeasurement")
    SensorMeasurement getSensorMeasurement();
    
    default SensorMeasurement getDefaultMeasurement() {
        return new SensorMeasurement();
    }
}
```

We have seen that we can increase resilience in our microservices without touching the business logic at all.
You can try to make your application more fault-tolerant and commit your changes whenever you're ready to move on!
