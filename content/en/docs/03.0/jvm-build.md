---
title: "3.2 JVM Build"
linkTitle: "3.2 JVM Build"
weight: 31
sectionnumber: 3.2
description: >
   Test JVM builds for Quarkus.
---

## {{% param sectionnumber %}}.1

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
