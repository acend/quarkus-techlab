---
title: "9.2 Tracing with Jaeger"
linkTitle: "9.2 Tracing with Jaeger"
weight: 920
sectionnumber: 9.2
description: >
    Tracing powered by Jaeger in Quarkus.
---

We are going to test the OpenTelemetry API live with Jaeger as our tracing service.


## Task {{% param sectionnumber %}}.1: Create the new service

Create a new Quarkus application, in this example we are going to use a simple reactive rest application:

```bash
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
  -DprojectGroupId=ch.puzzle \
  -DprojectArtifactId=opentelemetry \
  -Dextensions="quarkus-resteasy-reactive,quarkus-resteasy-reactive-jackson,quarkus-opentelemetry,quarkus-opentelemetry-exporter-otlp" \
  -DprojectVersion=1.0.0 \
  -DclassName="ch.puzzle.TracedResource"
```


## Task {{% param sectionnumber %}}.2: Start Jaeger service

To collect and visualize your traces, we are going to use a Jaeger service. Jaeger will collect your traces and display them in the Jaeger UI, running on [http://localhost:16686/](http://localhost:16686/):

```bash
docker run --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 16685:16685 jaegertracing/all-in-one:1.35
```


## Task {{% param sectionnumber %}}.3: Configure service

Alter the resource created as a default resource with the `@WithSpan` annotation:

```java
package ch.puzzle;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.opentelemetry.instrumentation.annotations.WithSpan;

@Path("/hello")
public class TracedResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @WithSpan
    public String hello() {
        return "hello";
    }
}
```

To start receiving traces from your service, we need to adapt some configuration first:

```properties
quarkus.application.name=myservice 
quarkus.opentelemetry.enabled=true 
quarkus.opentelemetry.tracer.exporter.otlp.endpoint=http://localhost:4317 

quarkus.log.console.format=%d{HH:mm:ss} %-5p traceId=%X{traceId}, parentId=%X{parentId}, spanId=%X{spanId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n  

# Alternative to the console log
quarkus.http.access-log.pattern="...traceId=%{X,traceId} spanId=%{X,spanId}" 
```

This will instruct the application to send traces from a service called `myservice`, to the endpoint `http://localhost:4317`.


## Task {{% param sectionnumber %}}.4: Test your traces

Spin up your Quarkus application:

```bash
./mvnw quarkus:dev
```

Then try to send a request to your applications `/hello` endpoint:

```bash
curl localhost:8080/hello
```

You should be greeted by the application's response. But what is happening at the back? Check the [Jaeger UI](http://localhost:16686/) if you have received any traces! You will be able to see that the request should have been sampled and collected by the Jaeger service.

![Image from Jaeger UI showing the trace](../first_trace.png)


## Task {{% param sectionnumber %}}.5: Add more spans

Okay, so far - so good. We can add more spans and visibility by adding a service which will return the `"hello"` for us, called `TracingService`. The service will have a function called `hello` which will return the string for us:

```java
package ch.puzzle;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class TracedResource {

    @Inject
    TracedService tracedService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return tracedService.hello();
    }
}
```

TracingService:
```java
package ch.puzzle;

import javax.enterprise.context.ApplicationScoped;

import io.opentelemetry.instrumentation.annotations.WithSpan;

@ApplicationScoped
public class TracedService {


    @WithSpan
    public String hello() {
        return "hello";
    }
}
```


## Task {{% param sectionnumber %}}.6: Fire again

If you call the endpoint again, you should see the second span in the [Jaeger UI](http://localhost:16686/)!

```bash
curl localhost:8080/hello
```

Verify that the span will be displayed in the [Jaeger UI](http://localhost:16686/).


## Task {{% param sectionnumber %}}.7: Adding baggage

For visibility purposes it will often be useful to have some more detailed information in the traces. To do that, we can simply add attributes to a span:

TracedResource:
```java
package ch.puzzle;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@Path("/hello")
public class TracedResource {

    @Inject
    TracedService tracedService;

    @Inject
    Tracer tracer;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @WithSpan
    public String hello() {
        Span span = Span.current();
        span.setAttribute("Additional information key", "Additional information value");
        return tracedService.hello();
    }
}
```


## Task {{% param sectionnumber %}}.8: Verify baggage in the span

Call the endpoint again and head over to the [Jaeger UI](http://localhost:16686/). You should see your attributes in the collapsed span!

If you did it correctly you can see the span like this:

![Jaeger UI with span](../baggage_trace.png)
