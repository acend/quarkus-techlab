---
title: "10.5. Build and use"
weight: 1050
sectionnumber: 10.5
description: >
  Building and using the extension.
---

In the previous completed our extension. Lets build and use it.


## Building

Since we are working locally we need to build our extension locally and add it to our local maven repository.


### Task {{% param sectionnumber %}}.1 - Build extension

Run the following command from the extension root folder:

```s
mvn clean package install
```


## Using the extension

In the `{{% param "solution_code_basedir" %}}` folder there is an `appinfo-app` application. This simple Quarkus
application contains the following dependency:

```xml
    <dependency>
      <groupId>ch.puzzle.quarkustechlab</groupId>
      <artifactId>appinfo</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

In the application.properties the following values are configured:
```properties
# Application Name
quarkus.application.name=Demo Application AppInfo

# build time (only available at build time)
quarkus.appinfo.always-include=true
quarkus.appinfo.record-build-time=true
quarkus.appinfo.built-for=quarkus-training

# runtime (changeable at runtime)
quarkus.appinfo.run-by=Puzzle ITC GmbH

```


### Task {{% param sectionnumber %}}.2 - Starting the Application

Enter the `{{% param "solution_code_basedir" %}}appinfo-app` folder and run the following command:

```s
./mvnw clean compile quarkus:dev
```

You should now be able to use the following endpoint:

Endpoint                  | Description
--------------------------|--------------------------------------------
`localhost:8080/demo`     | RestEasy endpoint provided by the application itself
`localhost:8080/appinfo`  | Undertow servlet endpoint provided by our extension
`localhost:8080/q/dev`    | Dev-UI already showing a very basic info about the appinfo extension
