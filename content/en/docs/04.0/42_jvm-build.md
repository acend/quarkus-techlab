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

```s
=> ERROR [build 2/8] COPY --chown=quarkus:quarkus mvnw /code/mvnw
[...]
failed to solve: rpc error: code = Unknown desc = failed to compute cache key: failed to calculate checksum of ref 
6jmcf7surio3u6alym2zn08cm::ewthv5cbdp8e5pdt3562y9mzg: "/pom.xml": not found
```
{{% /alert %}}


## {{% param sectionnumber %}}.1 Creating Docker Container

To build a Quarkus application to be run with the JVM you can use the provided Dockerfile `Dockerfile.jvm`.

```s
~/data-producer$ ./mvnw clean package
~/data-consumer$ ./mvnw clean package

~$ docker build -f data-producer/src/main/docker/Dockerfile.jvm -t data-producer:latest data-producer/.
~$ docker build -f data-consumer/src/main/docker/Dockerfile.jvm -t data-consumer:latest data-consumer/.
```

{{% alert color="primary" title="Docker Version" %}}
Depending on your docker version you have to specify the output format. If you get a warning that there is no output specified for docker-container driver just add `-o type=docker` to your command line.
{{% /alert %}}

The image will be produced and tagged as `data-producer:latest` / `data-consumer:latest`. You can test and run the built image locally with:

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

For a generic approach to build applications from scratch inside a container we suggest using a multistage Dockerfile. As the name states, the multistage file can be composed from multiple stages. In this example, we create a stage called `build` and an unnamed stage. The `build` stage uses maven to package our application, the next stage references the build stage and copies the artifact into the resulting docker image.

```Dockerfile
## Stage 1 : build with maven builder image with native capabilities
FROM {{% param "ubiQuarkusNativeImage" %}} AS build
COPY --chown=quarkus:quarkus mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/
USER quarkus
WORKDIR /code
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.6.0:go-offline
COPY src /code/src
RUN ./mvnw package

## Stage 2 : create the docker final image
FROM {{% param "openJdkImage" %}}

ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 --from=build /code/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=build /code/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=build /code/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=build /code/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
```

If you are in a restricted environment, where possible dependencies and packages cannot be downloaded, try to use this Dockerfile instead:

```Dockerfile
FROM {{% param "openJdkImage" %}}

USER root

RUN chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments \
    && echo "#!/bin/sh" > /deployments/run-java.sh \
    && echo "java -jar /deployments/quarkus-run.jar" >> /deployments/run-java.sh \
    && chown 1001 /deployments/run-java.sh \
    && chmod 540 /deployments/run-java.sh \
    && echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/lib/security/java.security

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=1001 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=1001 target/quarkus-app/*.jar /deployments/
COPY --chown=1001 target/quarkus-app/app/ /deployments/app/
COPY --chown=1001 target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 1001

ENTRYPOINT [ "/deployments/run-java.sh" ]
```
