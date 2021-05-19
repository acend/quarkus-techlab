---
title: "2.1 Your first Quarkus application"
linkTitle: "2.1 Your first Quarkus application"
weight: 210
sectionnumber: 2.1
description: >
  This setion covers initializing a Quarkus application.
---

## Create your first application

To create your first Quarkus application you have several possibilities:

* Create your application with maven
* Create your application with the [Quickstart UI code.quarkus.io](https://code.quarkus.io/)
* Use IntelliJ or eclipse plugins which will assist creating projects (these are usually also based on code.quarkus.io)

### Task {{% param sectionnumber %}}.1: Create your application with maven

To create your application with maven you can execute the following maven
command:

```bash
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=ch.puzzle \
    -DprojectArtifactId=getting-started \
    -DclassName="ch.puzzle.quarkustechlab.GreetingResource" \
    -Dpath="/hello"
```

Which creates a generated getting-started application bootstrapped for you. The
application holds at the moment a rest resource called `GreetingResource.java`
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

You should get the 'hello' response in your console. Other RESTeasy functionalities work like they always do.
For further information on basic REST interaction with Quarkus see [Documentation](https://quarkus.io/guides/rest-json).

### Task {{% param sectionnumber %}}.2: Exploring the dev mode

Now leave the dev mode running and experiment with the dev-ui. After each change, hit the API again and see how Quarkus
performs the live reload.

* Alter the response given in the `GreetingResource`
* Add a logger and a log statement to your `GreetingResource`
* Try changing the log level in the `application.properties` configuration file (quarkus.log.level=DEBUG)
* Point your browser to the Development UI at [http://localhost:8080/q/dev](http://localhost:8080/q/dev). Explore the
provided information. 
* Try to change the log level using the configuration editor in the development ui.
* Check the invocation trees from the ArC panel. What happens if you invoke your /hello endpoint?
* When was the application started? (tip: check the fired events)
* Check the details about your runtime environment (environment variables and system properties) in the configuration
editor.

{{% details title="GreetingResource Hint" %}}
```java
@Path("/hello")
public class GreetingResource {

    private static final Logger logger = LoggerFactory.getLogger(GreetingResource.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        logger.debug("Info log");
        return "Hello RESTEasy";
    }
}
```
{{% /details %}}