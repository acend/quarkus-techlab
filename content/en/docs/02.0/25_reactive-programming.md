---
title: "2.5 Reactive Programming"
linkTitle: "2.5 Reactive Programming"
weight: 250
sectionnumber: 2.5
description: >
  Introduction to reactive programming with Quarkus.
---

Quarkus is reactive. As we learned in the previous chapters it uses Vert.x as it's reactive engine under the hood. In this section we are going to take a look how we create reactive non-blocking flows in our system.

Be aware that bringing reactive flows to your system brings further complexity to the software. Reactive flows need to be consistent through all layers to benefit from their advantages. Traditional database connectors will break the non-blocking nature of the reactive approach.

In this chapter we will implement a simple rest application to show an example of a pure reactive approach through all components in the system. We will rely on the Mutiny project to expose Uni and Multi datatypes on the API.

If you are not familiar with reactive programming and are interested to go further down the rabbit hole, consider reading some additional literature:

* [Reactive Manifesto](https://www.reactivemanifesto.org/en)
* [Mutiny! Documentation](https://smallrye.io/smallrye-mutiny/{{% param "mutinyVersion" %}}/tutorials/hello-mutiny/)


## {{% param sectionnumber %}}.1: Producing Data

We will start similar to the previous example with a producer which exposes data on his REST endpoint.


### Maven dependencies reference

{{< solutionref project="quarkus-reactive-rest-producer" class="dependencies" >}}


### Task {{% param sectionnumber %}}.1.1: Create producer project

Start by creating a project `quarkus-reactive-rest-producer`. Add the extensions `quarkus-rest-jackson` to the project.

{{% details title="Hint" %}}

```shell
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
  -DprojectGroupId=ch.puzzle \
  -DprojectArtifactId=quarkus-reactive-rest-producer \
  -Dextensions="quarkus-rest-jackson" \
  -DprojectVersion=1.0.0 \
  -DclassName="ch.puzzle.quarkustechlab.reactiverest.producer.boundary.DataResource"
```

{{% /details %}}


### Task {{% param sectionnumber %}}.1.1: First reactive endpoint

The producer application should expose `SensorMeasurement` to the consumer. Let's create a simple class with the following content:

```java
package ch.puzzle.quarkustechlab.reactiverest.producer.entity;

import java.time.Instant;

public class SensorMeasurement {

    public Long id;
    public Double data;
    public Instant time;

    public SensorMeasurement() {
        this.data = Math.random();
        this.time = Instant.now();
    }
}
```

Next up we will write our first reactive REST endpoint. Alter the generated `DataResource` class to serve the path `/data` and implement a simple endpoint to return a `Uni<SensorMeasurement>` whenever a GET request is incoming.

Creating a `Uni` holding a `SensorMeasurement` is as simple as `Uni.createFrom().item(new SensorMeasurement())`.

{{% details title="Hint" %}}
```java
@Path("/data")
public class DataResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<SensorMeasurement> getMeasurement() {
        return Uni.createFrom().item(new SensorMeasurement());
    }
}
```
{{% /details %}}

When you hit your API with a GET call to the defined `/data` endpoint, you will get a JSON response with a `SensorMeasurement`.

```shell
GET http://localhost:8080/data

HTTP/1.1 200 OK
content-length: 64
Content-Type: application/json

{
  "data": 0.8526516453994077,
  "time": "2021-03-18T08:20:13.667398Z"
}

Response code: 200 (OK); Time: 115ms; Content length: 64 bytes
```


### Task {{% param sectionnumber %}}.1.1: Even more reactive

To use the full potential of the reactive world we will need to have our database access reactive as well. Let's start by initializing a database. We don't need to worry about creating the database instance ourselves, the quarkus dev services will start our desired database in a container all by itself.

Add the extension `quarkus-reactive-pg-client` to your project.

```shell
./mvnw quarkus:add-extension -Dextensions="quarkus-reactive-pg-client"
```

This will allow you to use a reactive way to connect and query your database. Configure the extension in your `application.properties` with the following content:

```shell
myapp.schema.create=true
```

For starters we are going to create a class which observes the start-up event and initializes the database whenever the application starts. Create a class `...producer.control.DBInit`. The class should have a private field `io.vertx.mutiny.pgclient.PgPool` injected and have a method `void onStart(@Observes StartupEvent ev)` which initializes the database with the following schema:

```sql
CREATE TABLE sensormeasurements (
  id SERIAL PRIMARY KEY, 
  data DOUBLE PRECISION, 
  time TIMESTAMP WITH TIME ZONE DEFAULT NOW()::timestamp
)
```

To query the database you can use your `PgPool client` like this:

```java
client.query("YOUR QUERY").execute().await().indefinitely();
```

See if you can create your schema. To execute multiple queries you can use `.flatMap(...)`:

```java
client.query("QUERY 1").execute()
                .flatMap(r -> client.query("QUERY 2").execute())
                .flatMap(r -> client.query("QUERY 3").execute())
                .flatMap(r -> client.query("...").execute())
                .await().indefinitely();
```

Test if you can drop your table if it exists, re-create the table and fill it with some dummy data.

{{% details title="Hint" %}}
```java
package ch.puzzle.quarkustechlab.reactiverest.producer.boundary;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DBInit {

    private final PgPool client;
    private final boolean schemaCreate;
    private static final Logger log = LoggerFactory.getLogger(DBInit.class.getName());

    public DBInit(PgPool client, @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") boolean schemaCreate) {
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            log.info("Initializing Database");
            initDb();
        }
    }

    private void initDb() {
        client.query("DROP TABLE IF EXISTS sensormeasurements").execute()
                .flatMap(r -> client.query("CREATE TABLE sensormeasurements (id SERIAL PRIMARY KEY, data DOUBLE PRECISION, time TIMESTAMP WITH TIME ZONE DEFAULT NOW()::timestamp)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.1)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.2)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.3)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.4)").execute())
                .await().indefinitely();
    }
}
```
{{% /details %}}

Next up we will use an [Active Record](https://www.martinfowler.com/eaaCatalog/activeRecord.html) pattern approach to enable persisting our SensorMeasurements to the database. Let's start with the most obvious job - to fetch all SensorMeasurements in the database and expose them as a `Multi<SensorMeasurement` in our API.

Create a function `public static Multi<SensorMeasurement> findAll(PgPool client)` in your `SensorMeasurement` class. You can query and transform the result to a `Multi<...>` like this:

```java
client.query("SELECT id, data, time from sensormeasurements").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(SensorMeasurement::new)

```

The default constructor created can not handle the call for now. Let's update the constructor so we can create a `SensorMeasurement` from a `io.vertx.mutiny.sqlclient.Row`:

```java
public SensorMeasurement(Row row) {
    this.id = row.getLong("id");
    this.data = row.getDouble("data");
    this.time = Instant.from(row.getOffsetDateTime("time"));
}
```

In our `DataResource` we have to inject a `io.vertx.mutiny.pgclient.PgPool`. Alter your default `@GET` annotated function to return all `SensorMeasurement` objects in the database as a `Multi<...>`.

{{% details title="Solution" %}}
**DataResource.java:**
```java
package ch.puzzle.quarkustechlab.reactiverest.producer.boundary;

import ch.puzzle.quarkustechlab.reactiverest.producer.entity.SensorMeasurement;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/data")
public class DataResource {

    @Inject
    PgPool client;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> findAll() {
        return SensorMeasurement.findAll(client);
    }
}
```

**DBInit.java:**
```java
package ch.puzzle.quarkustechlab.reactiverest.producer.control;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.pgclient.PgPool;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class DBInit {

    private final PgPool client;
    private final boolean schemaCreate;
    private static final Logger log = LoggerFactory.getLogger(DBInit.class.getName());

    public DBInit(PgPool client, @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") boolean schemaCreate) {
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent ev) {
        if (schemaCreate) {
            log.info("Initializing Database");
            initDb();
        }
    }

    private void initDb() {
        client.query("DROP TABLE IF EXISTS sensormeasurements").execute()
                .flatMap(r -> client.query("CREATE TABLE sensormeasurements (id SERIAL PRIMARY KEY, data DOUBLE PRECISION, time TIMESTAMP WITH TIME ZONE DEFAULT NOW()::timestamp)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.1)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.2)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.3)").execute())
                .flatMap(r -> client.query("INSERT INTO sensormeasurements (data) VALUES (0.4)").execute())
                .await().indefinitely();
    }
}
```

**SensorMeasurement.java:**
```java
package ch.puzzle.quarkustechlab.reactiverest.producer.entity;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;

import java.time.Instant;

public class SensorMeasurement {

    public Long id;
    public Double data;
    public Instant time;

    public SensorMeasurement() {
        this.data = Math.random();
        this.time = Instant.now();
    }


    public SensorMeasurement(Row row) {
        this.id = row.getLong("id");
        this.data = row.getDouble("data");
        this.time = Instant.from(row.getOffsetDateTime("time"));
    }

    public static Multi<SensorMeasurement> findAll(PgPool client) {
        return client.query("SELECT id, data, time from sensormeasurements").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(SensorMeasurement::new);
    }
}
```

{{% /details %}}

Test your API again and check if you receive your created data in the response.


### Task {{% param sectionnumber %}}.1.2: Find and persist in a reactive way

Let's create two more API endpoints:

* GET `/data/{id}`: Find a single `SensorMeasurement` by id
* POST `/data`: Persist a data entry to the DB

To save a entity we will use the `client.preparedQuery` instead. You can use the prepared query like this:

```java
client.preparedQuery("INSERT INTO sensormeasurements (data, time) VALUES ($1, $2) RETURNING (id, data, time)")
                .execute(Tuple.of(data, time.atOffset(ZoneOffset.UTC)))

```

Try to implement the `save` and `findById` on your own!

{{% details title="Solution" %}}
**SensorMeasurement.java:**
```java
public static Uni<SensorMeasurement> findById(PgPool client, Long id) {
    return client.preparedQuery("SELECT id, data, time from sensormeasurements where id = $1").execute(Tuple.of(id))
            .onItem().transform(RowSet::iterator)
            .onItem().transform(iterator -> iterator.hasNext() ? new SensorMeasurement(iterator.next()) : null);
}

public Uni<SensorMeasurement> save(PgPool client) {
    return client.preparedQuery("INSERT INTO sensormeasurements (data, time) VALUES ($1, $2) RETURNING (id, data, time)")
            .execute(Tuple.of(data, time.atOffset(ZoneOffset.UTC)))
            .onItem().transform(RowSet::iterator)
            .onItem().transform(iterator -> iterator.hasNext() ? this : null);
}
```

**DataResource.java:**
```java
@GET
@Path("/{id}")
@Produces(MediaType.APPLICATION_JSON)
public Uni<Response> findById(@PathParam(value = "id") Long id) {
    return SensorMeasurement.findById(client, id)
            .onItem().transform(sensorMeasurement -> sensorMeasurement != null ? Response.ok(sensorMeasurement) : Response.status(Response.Status.NOT_FOUND))
            .onItem().transform(Response.ResponseBuilder::build);
}

@POST
@Produces(MediaType.APPLICATION_JSON)
public Uni<SensorMeasurement> create(SensorMeasurement sensorMeasurement) {
    return sensorMeasurement.save(client);
}
```
{{% /details %}}

Test your API again to ensure all your implemented REST endpoints work.

Get data
```bash
curl localhost:8080/data/1
```

Result
```
{"id":1,"data":0.1,"time":"2022-08-29T14:17:32.069072Z"}
```

Create a new Measurement
```
curl -X POST localhost:8080/data -H "Content-Type: application/json" \
   -d '{"data":0.1, "time":"2022-01-01T00:00:00.000000Z"}'
```

Result
```
{"id":null,"data":0.1,"time":"2022-01-01T00:00:00Z"}%
```


### Task {{% param sectionnumber %}}.2: The consumer side

We have learned how to implement a reactive REST API to serve data in a complete reactive non-blocking way. Now it's time to take a look at the opposite, how do we reactively consume the API.


#### Maven dependencies reference

{{< solutionref project="quarkus-reactive-rest-consumer" class="dependencies" >}}


#### Implementation

Create another Quarkus project `quarkus-reactive-rest-consumer` with the following extensions: `quarkus-rest-jackson`, `quarkus-rest-client-jackson`:

```bash
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=ch.puzzle \
    -DprojectArtifactId=quarkus-reactive-rest-consumer \
    -Dextensions="quarkus-rest-jackson,quarkus-rest-client-jackson" \
    -DprojectVersion=1.0.0 \
    -DclassName="ch.puzzle.quarkustechlab.reactiverest.consumer.boundary.DataResource"
```

Change the default port of the consumer application in the `application.properties`, so we can have both up and running:

```properties
quarkus.http.port=8081
```

We will duplicate the `SensorMeasurement` class from the producer but without the Active Record pattern functions.

```java
package ch.puzzle.quarkustechlab.reactiverest.consumer.entity;

import java.time.Instant;

public class SensorMeasurement {
    public Long id;
    public Double data;
    public Instant time;

    public SensorMeasurement() {
        this.id = id;
        this.data = Math.random();
        this.time = Instant.now();
    }
}
```

To consume the producer's REST API we create a `@RestClient` named `data-service`. Create a new interface `DataService` and annotate it with:

```java
package ch.puzzle.quarkustechlab.reactiverest.consumer.boundary;

import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/data")
@RegisterRestClient(configKey = "data-service")
public interface DataService {
  [...]
}
```

Define the producers API method headers and you have your service ready to go.

{{% details title="Solution" %}}

**DataService.java:**
```java
package ch.puzzle.quarkustechlab.reactiverest.consumer.boundary;

import ch.puzzle.quarkustechlab.reactiverest.consumer.entity.SensorMeasurement;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/data")
@RegisterRestClient(configKey = "data-service")
public interface DataService {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<SensorMeasurement> findById(@PathParam("id") Long id);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<SensorMeasurement> create(SensorMeasurement sensorMeasurement);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<SensorMeasurement>> findAll();
}
```

{{% /details %}}

We configure the rest client in our application properties.

```properties
quarkus.http.port=8081

quarkus.rest-client.data-service.url=http://localhost:8080
quarkus.rest-client.data-service.scope=jakarta.inject.Singleton
```

Let's create another REST API resource to tunnel the requests and consume the produced events. Duplicate the definition of the producer's `DataResource` into a new class in the consumer. Inject the defined `DataService` as a `@RestClient` into the created resource and use it to tunnel the requests to the producer's API.

{{% details title="Solution" %}}

**DataResource.java:**
```java
package ch.puzzle.quarkustechlab.reactiverest.consumer.boundary;

import ch.puzzle.quarkustechlab.reactiverest.consumer.entity.SensorMeasurement;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;


@Path("/data")
public class DataResource {

    @Inject
    @RestClient
    DataService dataService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> findAll() {
        return dataService.findAll()
                .onItem().transform(list -> list != null ? Response.ok(list) : Response.serverError())
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> findById(@PathParam(value = "id") Long id) {
        return dataService.findById(id)
                .onItem().transform(item -> id != null ? Response.ok(item) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<SensorMeasurement> create(SensorMeasurement sensorMeasurement) {
        return dataService.create(sensorMeasurement);
    }
}
```

{{% /details %}}


### Task {{% param sectionnumber %}}.3: Streaming data

We learned how to implement a reactive REST API. Now let's stream some data as `SERVER_SENT_EVENTS`. We will create two endpoints in our producer's API:

* `/latest` - to stream the latest persisted data
* `/average` - to stream a current average of our data

Start by creating the endpoint for receiving the latest `SensorMeasurement`. Create a `@GET` endpoint with the path `/latest` which produces a `MediaType.SERVER_SENT_EVENTS`. Extend your `SensorMeasurement` class with a function which provides you a `Uni<SensorMeasurement>` and select the measurement with the latest `Instant time`. To emit the latest measurement periodically we will use the `Multi.createFrom().ticks().every(Duration.ofSeconds(2))...` feature. The `...ticks().every(Duration.ofSeconds(2))` emits an event every two seconds. Use this to transform it into the latest measurement. To ensure your `SERVER_SENT_EVENTS` will get converted to a JSON you can use the annotation `@RestSseElementType(MediaType.APPLICATION_JSON)`.

{{% details title="Hint" %}}

**...producer.boundary.DataResource:**
```java
    @GET
    @Path("/latest")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> latest() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
                .onItem().transform(i -> SensorMeasurement.getLatest(client).await().indefinitely());
    }
```

**...producer.entity.SensorMeasurement:**
```java
    public static Uni<SensorMeasurement> getLatest(PgPool client) {
        return client.query("SELECT id, data, time from sensormeasurements where time = (SELECT max(time) from sensormeasurements) limit 1").execute()
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? new SensorMeasurement(iterator.next()) : null);
    }
```
{{% /details %}}

Test your API with `curl -N localhost:8080/data/latest`.

Can you implement the similar API endpoint for calculating the average?

{{% details title="Hint" %}}
**...producer.boundary.DataResource:**
```java
    @GET
    @Path("/average")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> average() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
                .onItem().transform(i -> SensorMeasurement.getAverage(client).await().indefinitely());
    }
```

**...producer.entity.SensorMeasurement:**
```java
    public static Uni<SensorMeasurement> getAverage(PgPool client) {
        return client.query("SELECT 0 as id, avg(data) as data, NOW() as time from sensormeasurements").execute()
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? new SensorMeasurement(iterator.next()) : null);
    }
```
{{% /details %}}
