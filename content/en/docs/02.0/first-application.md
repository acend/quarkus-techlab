---
title: "2.1 Your first Quarkus application"
linkTitle: "2.1 Your first Quarkus application"
weight: 210
sectionnumber: 2.1
description: >
  This setion covers initializing a Quarkus application.
---

## Task {{% param sectionnumber %}}.1: Create your application

To create your first Quarkus application you have several possibilities:
  * Create your application with the [Quickstart UI](https://code.quarkus.io/)
  * Create your application with maven

To create your application with maven you can execute the following maven
command:

```bash 

mvn io.quarkus:quarkus-maven-plugin:1.7.0.Final:create \
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

You should get the 'hello' response in your console. Try altering the response
given in the `GreetingResource.java` and hit the API again, Quarkus should perform
a live reload and print the altered response without manually restarting your
application.

Other RESTeasy functionalities work like they always do. For further information 
on basic REST interaction with Quarkus see [Documentation](https://quarkus.io/guides/rest-json).
