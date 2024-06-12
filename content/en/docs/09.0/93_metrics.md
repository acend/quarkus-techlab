---
title: "9.3 Metrics with micrometer"
linkTitle: "9.3 Metrics with micrometer"
weight: 930
sectionnumber: 9.3
description: >
    Metrics with micrometer in Quarkus.
---

## {{% param sectionnumber %}}.1: Metrics

Knowing about the state of your applications in a microservice architecture is crucial. We will learn in this chapter how to use the metrics extension to get insights about our applications.

You can start by copying the rest application `quarkus-rest-data-producer` and `quarkus-rest-data-consumer` or you can just use these project and enhance them with metrics.


### Maven dependencies reference


#### Producer

{{< solutionref project="quarkus-metrics-data-producer" class="dependencies" >}}


#### Consumer

{{< solutionref project="quarkus-metrics-data-consumer" class="dependencies" >}}


### Implementation

Add the `quarkus-micrometer-registry-prometheus` extension to both of your projects. This will add the micrometer as well as the according prometheus extension to your project.

{{% details title="Hint" %}}

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>
```

{{% /details %}}

Per default the applications will expose all the metrics collected on the endpoint `/q/metrics`.

The most common metric types used are:

* **Counter**: The counter will simply increment and exposed at the current state
* **Gauge**: The gauge will expose a numerical value at the current state
* **Timer**: A time will give insight about a record of time spent in a defined state

Let's test these impelementations in our producing application.


### {{% param sectionnumber %}}.1.1: Timer

We start by implementing a timer which tracks the time of our default `/data` endpoint to process a request. We can simply annotate the function we want the timer to record with `@Timed` and the metric will be collected and exposed. Metrics will allow us to give them custom names, describe them and tag them for better readability. The annotation can be provided with the fields `value, description, extraTags` for this purpose.

Add the annotation to your endpoint and test your endpoint to see the result afterwards exposed to the `/q/metrics` endpoint.

{{% details title="Hint" %}}

```java
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(value = "GET_data", description = "Timer for the endpoint /data", extraTags = {"producer", "GET"})
    public SensorMeasurement getMeasurement() {
        return new SensorMeasurement() ;
    }
```

{{% /details %}}


### {{% param sectionnumber %}}.1.2: Counter

Create another endpoint to test the counter metric. Let's simply create an endpoint which returns some String and increments its counter metric by one. To create a counter you can use the annotation `@Counted` which works similar to the `@Timed` annotation. One interesting feature is the field `recordFailuresOnly(): boolean` which allows us to track only failed calls of the annotated function.

Add the endpoint and annotation to your application. Call your API and verify the value of the counter in the `/q/metrics` endpoint.

{{% details title="Hint" %}}

```java
    @GET
    @Counted
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public String incrementCounter() {
        return "+1";
    }
```

{{% /details %}}


### {{% param sectionnumber %}}.1.3: Gauge

The gauge, representing a numerical value at a given time, cannot be created by simply annotating an endpoint. We have to create a field `MeterRegistry registry` and register a gauge with a state and value function. Create the fields `MeterRegistry registry` and `Long gauge`. Write a method which returns the value of the `gauge: Long` field and register the gauge in the registry in the constructor of the DataResource class. Finally create an endpoint which increments the gauges value and test your implementation!

{{% details title="Hint" %}}

```java
@Path("/data")
public class DataResource {

    private Long gauge = 0L;
    private final MeterRegistry registry;

    public DataResource(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("example_gauge", this, DataResource::getGauge);
    }

    // [...]

    @GET
    @Path("/gauge")
    @Produces(MediaType.APPLICATION_JSON)
    public Long incrementGauge() {
        return ++gauge;
    }

    Long getGauge() {
        return gauge;
    }

}
```

{{% /details %}}


### {{% param sectionnumber %}}.1.4: Use the registry for custom metrics

Using only the annotation based metrics will often not be enough to gain deep insights into your application. Let's imagine we have a crucial piece code in a function we want to time separately. We alter the code of the producers GET `/data` endpoint to wait for a random amount of time.

```java
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(value = "GET_data", description = "Timer for the endpoint /data", extraTags = {"producer", "GET"})
    public SensorMeasurement getMeasurement() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextLong(0, 5_000));
        return new SensorMeasurement();
    }
```

Everytime the consumer now consumes data from the producer's API it will be delayed. Let's alter the consumers endpoint so we can separate the REST call and measure the time.

```java
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public SensorMeasurement getData() {
        logger.info("Collecting data from producer");
        SensorMeasurement sensorMeasurement = dataProducerService.getSensorMeasurement();
        logger.info("Returning data");
        return sensorMeasurement;
    }
```

As you can see we already have a timed function which exposes it's metric, but we want to know detailed how much time only the `dataProducerService.getSensorMeasurement()` call uses. To do that, you can wrap the call inside a `Supplier<SensorMeasurement>` and register it's time metrics with `registry.timer("name").wrap(supplier).get()` so the usage of the supplier defined will be measured separately.

{{% details title="Hint" %}}

```java
package ch.puzzle.quarkustechlab.restconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.control.HealthService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Path("/data")
public class DataConsumerResource {

    private final Logger logger = LoggerFactory.getLogger(DataConsumerResource.class);
    private final MeterRegistry registry;

    @RestClient
    DataProducerService dataProducerService;

    public DataConsumerResource(MeterRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public SensorMeasurement getData() {
        logger.info("Collecting data from producer");
        Supplier<SensorMeasurement> supplier = () -> dataProducerService.getSensorMeasurement();
        SensorMeasurement sensorMeasurement = registry.timer("REST_call_data").wrap(supplier).get();
        logger.info("Returning data");
        return sensorMeasurement;
    }
}
```

{{% /details %}}


### {{% param sectionnumber %}}.2: Collecting data

In the next section we are going a step further and use our exposed metrics to collect and monitor them. Start by setting up a docker-compose file which builds your two microservices. We can use the docker-composes `build` functionality to build our docker images. Create the two services in your docker-compose file and configure the build for your microservices.

{{% details title="Hint" %}}

```yml
version: '2'

services:
  consumer:
    build:
      context: ../data-consumer
      dockerfile: src/main/docker/Dockerfile.jvm
    hostname: consumer
    container_name: consumer
    environment:
      - QUARKUS_HTTP_PORT=8080
      - QUARKUS_REST_CLIENT_DATA_PRODUCER_API_URL=http://producer:8080
    ports:
      - 8081:8080

  producer:
    build:
      context: ../data-producer
      dockerfile: src/main/docker/Dockerfile.jvm
    hostname: producer
    container_name: producer
    ports:
      - 8080:8080
```

{{% /details %}}

Add two more services prometheus and grafana to your docker compose environment

* **Prometheus** will help us scrape the applications for metrics
* **Grafana** will help us to display and monitor these metrics collected


#### {{% param sectionnumber %}}.2.1: Prometheus

For prometheus take the container image `{{% param prometheusImage %}}`

* Expose the port `9090`
* Mount your prometheus rules configuration file `prometheus.yaml` at `/etc/prometheus/prometheus.yml`.

Your prometheus block in the `docker-compose.yaml` should look like this.

```yml
  prometheus:
    image: {{% param prometheusImage %}}
    hostname: prometheus
    container_name: prometheus
    ports:
      - 9090:9090
    volumes:
      - ./config/prometheus/prometheus.yaml:/etc/prometheus/prometheus.yml
```

The `prometheus.yaml` rules file should look like this:

```yml
# prometheus.yml

# my global config
global:
  scrape_interval:     15s
  evaluation_interval: 15s

# Alertmanager configuration
alerting:
  alertmanagers:
  - static_configs:
    - targets:
       - alertmanager:9093

rule_files:
- alert.rules

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'producer'
    metrics_path: /q/metrics
    static_configs:
      - targets: [ 'producer:8080' ]

  - job_name: 'consumer'
    metrics_path: /q/metrics
    static_configs:
      - targets: [ 'consumer:8080' ]
```

This will configure the prometheus instance to scrape the services defined in the jobs periodically.


#### {{% param sectionnumber %}}.2.2: Grafana

Grafana will allow us to build dashboards displaying and evaluating the data collected by our prometheus instance.

For grafana use the container image `{{% param grafanaImage %}}`

* Expose the port `3000`
* To access the admin interface you can set your admin password via the environment variable: `GF_SECURITY_ADMIN_PASSWORD`.
* Mount your datasource configuration file `datasource.yaml` at `/etc/grafana/provisioning/datasources/datasource.yaml`.

{{% details title="Hint Grafana Docker-Compose" %}}

```yaml
  grafana:
    image: {{% param grafanaImage %}}
    hostname: grafana
    container_name: grafana
    ports:
      - 3000:3000
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - ./config/grafana/datasource.yaml:/etc/grafana/provisioning/datasources/datasource.yaml
```

{{% /details %}}

Your datasource configuration `datasource.yaml` file should look like this:

```yml
apiVersion: 1

deleteDatasources:
  - name: prometheus
    orgId: 1

datasources:
  - name: prometheus
    type: prometheus
    access: proxy
    orgId: 1
    url: http://prometheus:9090
    isDefault: true
    jsonData:
      graphiteVersion: "1.1"
      tlsAuth: false
      tlsAuthWithCACert: false
    version: 1
    editable: true
```

This will allow grafana to access the metrics and data collected by prometheus.

{{% details title="Complete Docker-Compose Solution" %}}

```yaml
version: '2'

services:
  consumer:
    build:
      context: ../quarkus-metrics-data-consumer
      dockerfile: src/main/docker/Dockerfile.jvm
    hostname: consumer
    container_name: consumer
    environment:
      - QUARKUS_HTTP_PORT=8080
      - QUARKUS_REST_CLIENT_DATA_PRODUCER_API_URL=http://producer:8080
    ports:
      - 8081:8080

  producer:
    build:
      context: ../quarkus-metrics-data-producer
      dockerfile: src/main/docker/Dockerfile.jvm
    hostname: producer
    container_name: producer
    ports:
      - 8080:8080

  grafana:
    image: {{% param grafanaImage %}}
    hostname: grafana
    container_name: grafana
    ports:
      - 3000:3000
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - ./config/grafana/datasource.yaml:/etc/grafana/provisioning/datasources/datasource.yaml

  prometheus:
    image: {{% param prometheusImage %}}
    hostname: prometheus
    container_name: prometheus
    ports:
      - 9090:9090
    volumes:
      - ./config/prometheus/prometheus.yaml:/etc/prometheus/prometheus.yml
```

{{% /details %}}

Start up your docker environment. Test your API and check the collected data in either prometheus directly or build a little dashboard displaying the metrics exposed!


#### {{% param sectionnumber %}}.2.2: Grafana dashboard loading

You can also configure grafana to load dashboard definitions from a file. Create a custom config `custom.yaml` to instruct grafana to load dashboards from a folder.

```yaml
apiVersion: 1

providers:
  - name: 'default'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    updateIntervalSeconds: 10
    options:
      path: /opt/dashboards
```

This allows you to add dashboards exported as json into a folder which are loaded by grafana. A tiny sample dashboard is available in the `/solution` folder.

You have to mount the `custom.yaml` config and a dashboard folder in your `docker-compose.yaml` grafana configuration. Your grafana block will look like this.

```yaml
  grafana:
    image: {{% param grafanaImage %}}
    hostname: grafana
    container_name: grafana
    ports:
      - 3000:3000
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - ./config/grafana/dashboards/:/opt/dashboards
      - ./config/grafana/custom.yaml:/etc/grafana/provisioning/dashboards/custom.yaml
      - ./config/grafana/datasource.yaml:/etc/grafana/provisioning/datasources/datasource.yaml
```
