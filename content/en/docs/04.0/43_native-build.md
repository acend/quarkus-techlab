---
title: "4.3 Native Build"
linkTitle: "4.3 Native Build"
weight: 430
sectionnumber: 4.3
description: >
   Test Native builds for Quarkus.
---

{{% alert color="primary" title="Docker build context and native build resources" %}}
Be aware, a generated Quarkus project contains a `.dockerignore` file. This file limits the scope of the files sent
to the docker daemon for building a docker container (similar concept as the `.gitignore` files). If you encounter
errors like the following you may tweak your `.dockerignore` file or for simplicity delete it.

```
COPY failed: stat /var/lib/docker/tmp/docker-builder885613220/pom.xml: no such file or directory
```

Quarkus native builds are taking a lot of memory resources. Docker installations on windows and mac os are known to set
limits for your docker environment. Remember to also check these limits.

You can specify the following property to your `mvn package` build to limit the resources:
```
-Dquarkus.native.native-image-xmx=2G
```
{{% /alert %}}


## {{% param sectionnumber %}}.1 Creating Docker Container

As the name says, native builds will run the Quarkus application as a native executable. The executable will get optimized and prepared in the ahead-of-time compilation process. As you would expect the compilation and build of a native executable takes a ridiculous amount of memory and time. Native executables are built by the GraalVM or the upstream community project Mandrel. If you want to read further about native executables in Quarkus head over to the official [Documentation](https://quarkus.io/guides/building-native-image).

For now, we simply want to build native images to be fast as lightning!

There are multiple ways to build native images. One possibility is to install the GraalVM and use Maven locally with `./mvnw package -Pnative` then use the Dockerfile.native to create your image.
The lazy way is simpler. We create a multistage Dockerfile to do both steps in our docker build process.

```Dockerfile
# Dockerfile.multistage

## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/centos-quarkus-maven:21.0.0-java11 AS build
COPY pom.xml /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml -B de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies
COPY src /usr/src/app/src
USER root
RUN chown -R quarkus /usr/src/app
USER quarkus
RUN mvn -f /usr/src/app/pom.xml -Pnative clean package

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/ubi-minimal
WORKDIR /work/
COPY --from=build /usr/src/app/target/*-runner /work/application

# set up permissions for user `1001`
RUN chmod 775 /work /work/application \
  && chown -R 1001 /work \
  && chmod -R "g+rwX" /work \
  && chown -R 1001:root /work

EXPOSE 8080
USER 1001

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]

```

Now you can create your own native executable with:

```s

~/data-producer ./mvnw clean package
~/data-consumer ./mvnw clean package
~/ docker build -f data-producer/src/main/docker/Dockerfile.multistage -t data-producer:native data-producer/.
~/ docker build -f data-consumer/src/main/docker/Dockerfile.multistage -t data-consumer:native data-consumer/.

```

Now start the built native images. You will realize that the startup time is almost instantaneous.

```s
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/
2020-08-31 15:02:53,244 INFO  [io.quarkus] (main) data-consumer 1.0-SNAPSHOT native (powered by Quarkus {{% param "quarkusVersion" %}}) started in 0.031s. Listening on: http://0.0.0.0:8080
2020-08-31 15:02:53,244 INFO  [io.quarkus] (main) Profile prod activated.
2020-08-31 15:02:53,244 INFO  [io.quarkus] (main) Installed features: [cdi, rest-client, resteasy, resteasy-jsonb, smallrye-health]

```
