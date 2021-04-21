---
title: "8.1 Tracing powered by Jaeger"
linkTitle: "8.1 Tracing powered by Jaeger"
weight: 810
sectionnumber: 8.1
description: >
    Tracing powered by Jaeger in Quarkus.
---

## {{% param sectionnumber %}}.1: OpenTracing with Jaeger

Let us enhance our application and add distributed tracing to the microservices. We will start by duplicating the 'rest' project. If you are not confident with your solution, simply copy the solution in the '/solution' folder provided.

We start by adding the 'quarkus-smallrye-opentracing' extension to the consumer and the producer project.

{{% details title="Hint" %}}

```xml

<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-opentracing</artifactId>
</dependency>

```

{{% /details %}}

Create a `docker-compose.yaml` file and add the following service to it:

```yaml

  jaeger:
    image: quay.io/jaegertracing/all-in-one:1.22.0
    hostname: jaeger
    container_name: jaeger
    ports:
      - 5775:5775/udp
      - 6831:6831/udp
      - 6832:6832/udp
      - 5778:5778
      - 14268:14268
      - 16686:16686
      
```

Configure your microservices to report the traces to your Jaeger instance. Add the following properties to your `application.properties`:

```s

quarkus.jaeger.endpoint=http://localhost:14268/api/traces
quarkus.jaeger.service-name=quarkus-tracing-producer
quarkus.jaeger.sampler-type=const
quarkus.jaeger.sampler-param=1
quarkus.log.console.format=%d{HH:mm:ss} %-5p traceId=%X{traceId}, spanId=%X{spanId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n

```

Start up your jaeger server and your two microservices. You can visit the Jaeger UI at [localhost:16686](http://localhost:16686). Test your API endpoint of the consumer and check the UI for traces.

Let's create a service class for providing the `SensorMeasurements` in the producer. Write a new `@ApplicationScoped` class `..control.DataService` and add a function which returns with a 50/50 chance a new SensorMeasurement or throws an Exception. Change the REST endpoint to call and return the DataService's function. Test your API again and check the traces in the Jaeger UI. You will see that the trace leading to an exception will be marked and could be further investigated.

{{% details title="Hint" %}}

DataService:
```java

@ApplicationScoped
@Traced
public class DataService {

    public SensorMeasurement createSensorMeasurementOrFail() throws Exception {
        if (Math.random() > 0.6)
            throw new Exception("Random failure");
        return new SensorMeasurement();
    }
}

```

DataResource:
```java

@Path("/data")
public class DataResource {

    @Inject
    DataService dataService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement hello() throws Exception {
        return dataService.createSensorMeasurementOrFail() ;
    }
}

```

{{% /details %}}

We can extend our observability by using the OpenTracings baggage. We can add key-value pairs of strings to our traces to add further information available in the traces. Let's alter the `DataService` to create a `SensorMeasurement` adding it to the baggage with the key "measurement" and then returning it.

Inject a `Tracer tracer` into your `DataService` class and use the `tracer.activeSpan().setBaggageItem("key", "value")` funcitonality to add the created `SensorMeasurement` to the baggage of the active span. Test your API again and check the results reflected in the Jaeger UI.

{{% details title="Hint" %}}

```java

@ApplicationScoped
@Traced
public class DataService {

    @Inject
    Tracer tracer;

    public SensorMeasurement createSensorMeasurementOrFail() throws Exception {
        if (Math.random() > 0.6)
            throw new Exception("Random failure");
        SensorMeasurement measurement = new SensorMeasurement();
        tracer.activeSpan().setBaggageItem("measurement", JsonbBuilder.create().toJson(measurement));
        return measurement;
    }
}

```

{{% /details %}}
