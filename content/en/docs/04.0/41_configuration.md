---
title: "4.1 Configuration"
linkTitle: "4.1 Configuration"
weight: 410
sectionnumber: 4.1
onlyWhen: openshift
description: >
   Configurate our microservices to be cloud ready.
---

## {{% param sectionnumber %}}.1

For the next chapter we need to prepare our applications to run in a cloud environment. One important aspect of this will be adding health checks to our applications. Implementing or adding health checks to your Quarkus application is - as expected - easy. Simply add the extension 'smallrye-health' to your applications with the following command:

```bash

 ./mvnw quarkus:add-extension -Dextensions="quarkus-smallrye-health"

```

When you restart your applications they both will expose automatically the '/health' endpoint which indicates that the application is up and running.

Additionally we need to configure the connection from our data-consumer to the data-producer. As for now the data-consumer simply points to the url configured in the `application.properties` which gets injected to the defined RestClient.

Extend your application.properties of the data-consumer to:

```java

quarkus.http.port=8080
%dev.quarkus.http.port=8081

application.data-producer.url=data-producer
%dev.application.data-producer.url=localhost
application.data-producer.port=8080
%dev.application.data-producer.port=8080

data-producer-api/mp-rest/url=http://${application.data-producer.url}:${application.data-producer.port}
data-producer-api/mp-rest/scope=javax.inject.Singleton

```

The prefix `%dev.` in front of a configuration property defines a quarkus profile. Whenever the defined profile is active the value will be overwritten.
