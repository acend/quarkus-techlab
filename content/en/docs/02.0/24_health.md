---
title: "2.4 Health checks"
linkTitle: "2.4 Health checks"
weight: 240
sectionnumber: 2.4
description: >
  Using and writing health checks.
---

In this section we will add health checks to our microservices.


## Adding Health checks

We will be using the `smallrye-health` quarkus extension which relies on the MicroProfile Health specification. 

If you are manually importing the health extension use the following dependency:

```
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
``` 

This extension will expose the following REST endpoints:

* `/q/health/live`: is the application up and running?
* `/q/health/ready`: is the application ready to serve requests?
* `/q/health`: accumulation of all checks

In a cloud environment these endpoints will be used to probe the health of your application. these checks will therefore
affect how your application is handled by the cloud platform.

**Liveness**: Determines if you application is alive and healthy. If liveness checks fail, it is common that the platform
will restart the pod of the application.

**Readiness**: Determines if your application is ready to server requests. If the readiness check fails, the load-balancer
(service) in front of the application will stop sending traffic to the application allowing it to recover.

A simple health response may look like this
```json
{
    "status": "UP",
    "checks": [
        {
            "name": "Last message check",
            "status": "UP",
            "data": {
                "lastMessageTime": 1621409879174,
                "ageInMs": 2854
            }
        }
    ]
}
```


### Task {{% param sectionnumber %}}.1: Adding the dependency

Add the `smallrye-health` extension to your data-producer and data-consumer service.

{{% details title="Hint" %}}
```s
./mvnw -pl data-producer quarkus:add-extension -Dextensions="smallrye-health"
./mvnw -pl data-consumer quarkus:add-extension -Dextensions="smallrye-health"
```
{{% /details %}}


### Task {{% param sectionnumber %}}.2: Test health checks

Start the application and query the health-endpoints either by using curl or your webbrowser.

{{% details title="Hint" %}}
```s
./mvnw -pl data-producer quarkus:dev

curl localhost:8080/q/health 

{
    "status": "UP",
    "checks": [
    ]
}
```
{{% /details %}}


## Custom health check

As a simple example we will write a custom health check observing the last SensorMeasurement fetched from the
data-producer service. It should switch to failed state if the last fetched Measurement has some defined age.

A health check is usually evaluated based on its http response code. The main target for health checks is providing a
technical interface used by an underlying platform. Beside the http response code the health check can also provide some
information in the response body. However, it is not evaluated to determine if the check succeeded or failed.


### Task {{% param sectionnumber %}}.3: Write a custom liveness check

Create a `HealthService` which records the last message received from the data-producer.

{{% details title="HealthService Hint" %}}
```java
@ApplicationScoped
public class HealthService {

    Instant lastMessageTime;

    public void registerMessageFetch() {
        this.lastMessageTime = Instant.now();
    }

    public Instant getLastMessageTime() {
        return lastMessageTime;
    }
}
```
{{% /details %}}

Inject the `HealthService` to the `DataConsumerResource` and register the invocation to the data-producer.

{{% details title="DataConsumerResource Hint" %}}
```java
@Path("/data")
public class DataConsumerResource {

    /* stripped for simplicity */

    @Inject
    HealthService healthService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement getData() {
        SensorMeasurement measurement = dataProducerService.getSensorMeasurement();
        healthService.registerMessageFetch();
        return measurement;
    }
}
```
{{% /details %}}

Write a `RecentMessageHealthCheck` which implements `HealthCheck`.

* Fail the health check if the last fetched `SensorMeasurement` from the data-producer is older than 60 seconds.
* Add the time the last message was fetched to the health check response
* Add the age of the last fetched SensorMeasurement to the health check response.

You may start your `RecentMessageHealthCheck` with the following Template:

```java
@Liveness
@ApplicationScoped
public class RecentMessageHealthCheck implements HealthCheck {

    // TODO: inject HealthService

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Last message check");
        
        // TODO: health check implementation

        return responseBuilder.build();
    }
}
```

{{% details title="RecentMessageHealthCheck Hint" %}}
```java
@Liveness
@ApplicationScoped
public class RecentMessageHealthCheck implements HealthCheck {

    @Inject
    HealthService healthService;

    @Override
    public HealthCheckResponse call() {
        Instant lastMessageTime = healthService.getLastMessageTime();

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Last message check")
                .status(lastMessageTime == null || ((lastMessageTime.toEpochMilli() + 60000) >= Instant.now().toEpochMilli()));

        if(lastMessageTime != null) {
            responseBuilder.withData("lastMessageTime", lastMessageTime.toEpochMilli())
                    .withData("ageInMs", (Instant.now().toEpochMilli() - lastMessageTime.toEpochMilli()));
        }

        return responseBuilder.build();
    }
}
```
{{% /details %}}


## Health UI

The smallrye-health extension ships with a simple health ui. Point your browser to <http://localhost:8081/q/health-ui/>
and explore the health ui.

![Health UI](../health-ui.png)
