---
title: "11.1 Quarkus serverless with Funqy"
linkTitle: "11.1 Quarkus serverless with Funqy"
weight: 1110
onlyWhenNot: mobi
sectionnumber: 11.1
description: >
    Quarkus serverless with Funqy
---

## {{% param sectionnumber %}}.1: Funqy

With the serverless community growing Quarkus created the Funqy API. The API provides functionality to deploy small (HTTP-)services to most of the common serverless environments. The same application can be deployed or served on a diverse set of FaaS environments like AWS Lambda, Azure Functions, Google Cloud Functions, Knative, OpenShift Serverless.

The main idea behind Funqy is pretty simple. Create Java methods, define optional parameters and return values, annotate your function with the `@Funq` annotation and you're all set for your serverless journey! Funqy classes in Quarkus are normal Quarkus components which can be injected with the normal functionalities.


## {{% param sectionnumber %}}.2: Our first serverless journey

Let's dive straight into it and start by creating a normal Quarkus application with the `quarkus-funqy-http` extension.

```s
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=ch.puzzle \
    -DprojectArtifactId=quarkus-serverless-data-producer \
    -DclassName="ch.puzzle.quarkustechlab.serverless.boundary.Funqy" \
    -Dextensions="funqy-http"
```

This will set up a small example application already providing a serverless function. Head over to the code and check the example:

```java
public class Funqy {

    private static final String CHARM_QUARK_SYMBOL = "c";

    @Funq
    public String charm(Answer answer) {
        return CHARM_QUARK_SYMBOL.equalsIgnoreCase(answer.value) ? "You Quark!" : "👻 Wrong answer";
    }

    public static class Answer {
        public String value;
    }
}
```

Add getter and setter functions for the `Answer` class. Somehow the Quarkus plugin does not generate them automatically for this test setup.

This class will provide us with an endpoint serving `/charm` which will listen to any traffic incoming. You can startup your Funqy application like you are used to with `./mvnw compile quarkus:dev` and try it out for yourself.


## {{% param sectionnumber %}}.3: Create serverless producer

To stay true to our API we implement the same producer API now with the Funqy API. Create a class `SensorMeasurement` in the `entity` package. Funqy components are normal Quarkus components so we can use the normal API for example CDI to inject beans into our services. Create a `DataService` class and inject it into your `Funqy` class. Let the `DataService` create a new `SensorMeasurement` and create and endpoint `/data` to expose the data for consumers.

Test your API again, try to mix now the REST consumer from the previous example with your serverless Funqy service together! It works like a charm!

{{% details title="Hint" %}}

```java
public class Funqy {

    @Inject
    DataService dataService;

    @Funq
    public SensorMeasurement data() {
        return dataService.getMeasurement();
    }
}
```

```java
@ApplicationScoped
public class DataService {

    public SensorMeasurement getMeasurement() {
        return new SensorMeasurement();
    }
}
```

```java
public class SensorMeasurement {

    public Double data;

    public SensorMeasurement() {
        this.data = Math.random();
    }
}
```

{{% /details %}}

