---
title: "2.4 Reactive Programming"
linkTitle: "2.4 Reactive Programming"
weight: 240
sectionnumber: 2.4
description: >
  Introduction to reactive programming with Quarkus.
---

Quarkus is reactive. As we learned in the previous chapters it uses Vert.x as it's reactive engine under the hood. In this section we are going to take a look how we create reactive non-blocking flows in our system.

Be aware that bringing reactive flows to your system brings further complexity to the software. Reactive flows need to be consistent through all layers to benefit from their advantages. Traditional database connectors will break the non-blocking nature of the reactive approach.

In this chapter we will implement a simple rest application to show an example of a pure reactive approach through all components in the system. We will rely on the Mutiny project to expose Uni and Multi datatypes on the API.


## {{% param sectionnumber %}}.1: Producing Data

We will start similar to the previous example with a producer which exposes data on his REST endpoint.


### Task {{% param sectionnumber %}}.1.1: Create producer project

Start by creating a project 'quarkus-reactive-rest-producer'. Add the extensions 'quarkus-resteasy-reactive', 'quarkus-resteasy-reactive-jsonb', 'quarkus-reactive-pg-client' to the project.

{{% details title="Hint" %}}

```s
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create -DprojectGroupId=ch.puzzle -DprojectArtifactId=quarkus-reactive-rest-producer -Dextensions="quarkus-resteasy-reactive,quarkus-resteasy-reactive-jsonb" -DprojectVersion=1.0.0 -DclassName="ch.puzzle.producer.boundary.DataResource"
```

{{% /details %}}


### Task {{% param sectionnumber %}}.1.1: First reactive endpoint

The producer application should expose `SensorMeasurement` to the consumer. Let's create a simple class `ch.puzzle.producer.entity.SensorMeasurement` with the follwoing content:

```java

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

Next up we will write our first reactive REST endpoint. Alter the generated `DataResource` class to serve the path '/data' and implement a simple endpoint to return a `Uni<SensorMeasurement>` whenever a GET request is incoming.

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

```s

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

To use the full potential of the reactive world we will need to have our database access reactive as well. Let's start by initializing a database.

Create a `docker-compose.yaml` with a PostgreSQL database exposing the port 5432 to your system.

{{% details title="Hint" %}}
```yaml

version: '3'

services:
  quarkus-db:
    image: docker.io/postgres:11.11
    hostname: quarkus-db
    container_name: quarkus-db
    volumes:
      - quarkus-db:/var/lib/postgresql/data/
    networks:
      - quarkus
    environment:
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=1234
    ports:
      - 5432:5432

networks:
  quarkus:
    driver: bridge
    ipam:
      driver: default

volumes:
  quarkus-db:

```
{{% /details %}}

Add the extension 'quarkus-reactive-pg-client' to your project. This will allow you to use a reactive way to connect and query your database. Configure the extension in your `application.properties` with the following content:

```s

quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=admin
quarkus.datasource.password=1234
quarkus.datasource.reactive.url=postgresql://localhost:5432/admin
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

@ApplicationScoped
public class DBInit {

    private final PgPool client;
    private final boolean schemaCreate;
    private static final Logger log = Logger.getLogger(DBInit.class.getName());

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
                .onItem().transformToMulti(set -> Multi.createFrom().items(() -> StreamSupport.stream(set.spliterator(), false)))
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

In our `DataResource` we have to inject a `io.vertx.mutiny.pgclient.PgPool`. Alter your default `@GET` annotated function to return all `SensorMeasurement` objects in the database as a `Multi`.

{{% details title="Hint" %}}
```java

@Path("/data")
public class DataResource {

    private final PgPool client;

    public DataResource(PgPool client) {
        this.client = client;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> findAll() {
        return SensorMeasurement.findAll(client);
    }

}

```
{{% /details %}}

Test your API again and check if you receive your created data in the response.

Let's create the other API endpoints to find a single `SensorMeasurement` by id and a POST endpoint to save a `SensorMeasurement`.

To save a entity we will use the `client.preparedQuery` instead. You can use the prepared query like this:

```java

client.preparedQuery("INSERT INTO sensormeasurements (data, time) VALUES ($1, $2) RETURNING (id, data, time)")
                .execute(Tuple.of(data, time.atOffset(ZoneOffset.UTC)))

```

Try to implement the `save` and `findById` on your own!

{{% details title="SensorMeasurement" %}}
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
{{% /details %}}

{{% details title="DataResource" %}}
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


### Task {{% param sectionnumber %}}.2: The consumer side

We have learned how to implement a reactive REST API to serve data in a complete reactive way. Now it's time to take a look at the opposite, how do we reactively consume the API.

Create another Quarkus project with the following extensions: "quarkus-resteasy-reactive, quarkus-jsonb, quarkus-resteasy-reactive-jsonb, quarkus-rest-client-mutiny, quarkus-rest-client-jsonb".

{{% details title="Hint" %}}

```s
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create -DprojectGroupId=ch.puzzle -DprojectArtifactId=quarkus-reactive-rest-producer -Dextensions="quarkus-resteasy-reactive,quarkus-jsonb,quarkus-resteasy-reactive-jsonb,quarkus-rest-client-mutiny,quarkus-rest-client-jsonb" -DprojectVersion=1.0.0 -DclassName="ch.puzzle.producer.boundary.DataResource"
```

{{% /details %}}

We will duplicate the `SensorMeasurement` class without the Active Record pattern functions.

```java

public class SensorMeasurement {

    public Long id;
    public Double data;
    public Instant time;

    public SensorMeasurement() {
    }

    public SensorMeasurement(Long id, Double data, Instant time) {
        this.id = id;
        this.data = data;
        this.time = time;
    }
}

```
