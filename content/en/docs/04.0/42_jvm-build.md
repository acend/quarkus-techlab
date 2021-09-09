---
title: "4.2 JVM Build"
linkTitle: "4.2 JVM Build"
weight: 420
sectionnumber: 4.2
description: >
   Test JVM builds for Quarkus.
---


{{% alert color="primary" %}}
Be aware, a generated Quarkus project contains a `.dockerignore` file. This file limits the scope of the files sent
to the docker deamon for building a docker container (similar concept as the `.gitignore` files). If you encounter
errors like the following you may tweak your `.dockerignore` file or for simplicity delete it.

```
COPY failed: stat /var/lib/docker/tmp/docker-builder885613220/pom.xml: no such file or directory
```
{{% /alert %}}


## {{% param sectionnumber %}}.1 Creating Docker Container

To build a Quarkus application to be run with the JVM you can use the provided Dockerfile `Dockerfile.jvm`.

```s

~/data-producer ./mvnw clean package
~/data-consumer ./mvnw clean package
~/ docker build -f data-producer/src/main/docker/Dockerfile.jvm -t data-producer:latest data-producer/.
~/ docker build -f data-consumer/src/main/docker/Dockerfile.jvm -t data-consumer:latest data-consumer/.

```

The image will be produced and tagged as data-producer:latest / data-consumer:latest. You can test and run the built image locally with:

```s

docker run --network host data-producer:latest
docker run --network host data-consumer:latest

```

When the applications are up and running you can test the API again:

```s

curl http://localhost:8081/data

```

You should get your desired response.


## {{% param sectionnumber %}}.2: Multistage Dockerfiles

For a generic approach to build applications from scratch inside a container we suggest using a multistage Dockerfile. As the name states, the multistage file can be composed from multiple stages. In this example, we create a stage called 'build' and an unnamed stage. The build stage uses maven to package our application, the next stage references the build stage and copies the artifact into the resulting docker image.

```Dockerfile

# Dockerfile.multistage

## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/centos-quarkus-maven:21.2.0-java11 AS build
COPY pom.xml /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml -B de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies
COPY src /usr/src/app/src
USER root
RUN chown -R quarkus /usr/src/app
USER quarkus
RUN mvn -f /usr/src/app/pom.xml clean package

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/ubi-minimal:8.4 

ARG JAVA_PACKAGE=java-11-openjdk-headless
ARG RUN_JAVA_VERSION=1.3.8
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'
# Install java and the run-java script
# Also set up permissions for user `1001`
RUN microdnf install curl ca-certificates ${JAVA_PACKAGE} \
    && microdnf update \
    && microdnf clean all \
    && mkdir /deployments \
    && chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments \
    && curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o /deployments/run-java.sh \
    && chown 1001 /deployments/run-java.sh \
    && chmod 540 /deployments/run-java.sh \
    && echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/lib/security/java.security

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --from=build --chown=1001 /usr/src/app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=1001 /usr/src/app/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=1001 /usr/src/app/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=1001 /usr/src/app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 1001

ENTRYPOINT [ "/deployments/run-java.sh" ]

```
