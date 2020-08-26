# REST Services in Quarkus

Most modern APIs are based on REST principles. In this chapter we learn how to implement REST Services and Clients in Quarkus to produce and consume REST APIs. 

## Create your first Quarkus application

To create your first Quarkus application you have several possibilities:
  * Create your application with the [Quickstart UI](https://code.quarkus.io/)
  * Create your application with maven

To create your application with maven you can execute the following maven
command:

```bash 

mvn io.quarkus:quarkus-maven-plugin:1.7.0.Final:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=getting-started \
    -DclassName="org.acme.quickstart.GreetingResource" \
    -Dpath="/hello"

``` 

Which creates a generated getting-started application bootstrapped for you. The
application holds at the moment an rest resource called `GreetingResource.java`
which exposes a REST resource for you. 
To test the application you can start the application in dev-mode by executing 

```bash 

./mvnw compile quarkus:dev

``` 

The command starts the application in dev-mode which means you do have active
live reloading on each API call. Try hitting the API and test the
`GreetingResource.java`: 

```bash

curl http://localhost:8080/hello

```

You should get the 'hello' response in your console. Try altering the response
given in the `GreetingResource.java` and hit the API again, Quarkus should perform
a live reload and print the altered response without manually restarting your
application.

Other RESTeasy functionalities work like they always do. For further information 
on basic REST interaction with Quarkus see [Documentation](https://quarkus.io/guides/rest-json).

## Producing Data

Create a new Quarkus application like shown before called 'data-producer'. The application should expose a `DataResource` on the path "/data" which provides the user with a randomly generated double when requested. 

```bash

mvn io.quarkus:quarkus-maven-plugin:1.7.0.Final:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=data-producer \
    -DclassName="org.acme.quickstart.DataResource" \
    -Dpath="/data"
```

To write better APIs and share data over our defined resources, we need the 'resteasy-jsonb' extension which provides us with 
JSON-B functionalities for our REST interfaces. 
To add a extension to your existing Quarkus application simply use:

```bash

./mvnw quarkus:add-extension -Dextensions="quarkus-resteasy-jsonb"

```

To see the available extensions you can use: 

```bash

./mvnw quarkus:list-extensions

```

In the generated DataResource edit the `@GET` endpoint to return a simple double and change to produced type to `MediaType.APPLICATION_JSON`.

```java

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public double produceData() {
        return Math.random() * 10;
    }

```

In our lab we want to transfer sensor measurements between our microservices. Create a new class SensorMeasurement with a single public field called data which holds a Double. In the constructor assign the data field a random generated Double. Edit your REST resource to return a new `SensorMeasurement` whenever it's called.

It should look something like this:

```java

package org.acme.quickstart.entity;

public class SensorMeasurement {

    public Double data;

    public SensorMeasurement() {
        this.data = Math.random();
    }
}

```

```java

package org.acme.quickstart;

import org.acme.quickstart.entity.SensorMeasurement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/data")
public class DataResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement produceData() {
        return new SensorMeasurement();
    }
}

```


For more information about writing REST APIs with Quarkus see the [documentation](https://quarkus.io/guides/rest-json)

## Consuming Data

With another microservice we would like to consume the data served by our data-producer. Create another quarkus application called 'data-consumer' with the follwing extensions: "rest-client, resteasy-jsonb".

```bash

mvn io.quarkus:quarkus-maven-plugin:1.7.0.Final:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=data-consumer \
    -DclassName="org.acme.rest.client.DataConsumer" \
    -Dpath="/data" \
    -Dextensions="rest-client, resteasy-jsonb"

```

