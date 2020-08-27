# Build and Deploy our Quarkus microservices

Talking about how nice Quarkus runs in the cloud environment is pretty neat, but doing is even better! So we take a step further and automate our builds and deployment with Jenkins and OpenShift. This part will consist of two sections, we will see how to implement build pipelines, which will trigger our OpenShift deployments and another which shows how to configure your OpenShift deployments.

We will see how we can build our applications like we used to as an JVM build, then we will try to build native executables with help of the [GraalVM](https://www.graalvm.org/). Native executables will optimize the entire application in compile time and will afterwards not benefit from the classical Java runtime optimizations. Always keep in mind that Java applications due to it's nature will get more performant the longer they run. With native executables we do have the exact opposite, applications will start up optimized and will run the fastest at the start. Be aware that a lot of classical Java features like reflection will not be available as you were used to when built natively. Reflections can be used, but the compiler needs to be aware of the reflection by annotating classes as `@RegisterForReflection`. You can read more about native build in the [Documentation](https://quarkus.io/guides/building-native-image).


## Builds

To build Docker images from your applications, Quarkus provides per default in each project Dockerfiles to support JVM and native builds. You don't have to worry too much about how Dockerfiles need to be written.

### JVM builds

To build a Quarkus application to be run with the JVM you can use the provided Dockerfile `Dockerfile.jvm`. 

```bash

~/data-producer ./mvnw clean package
~/data-consumer ./mvnw clean package
~/ docker build -f data-producer/src/main/docker/Dockerfile.jvm -t data-producer:latest data-producer/.
~/ docker build -f data-consumer/src/main/docker/Dockerfile.jvm -t data-consumer:latest data-consumer/.

```

The image will be produced and tagged as data-producer:latest / data-consumer:latest. You can test and run the built image locally with:

```bash

docker run --network host data-producer:latest
docker run --network host data-consumer:latest

```

When the applications are up and running you can test the API again: 

```bash

curl http://localhost:8081/data

```

You should get your desired response. 

## Jenkins build pipelines


## OpenShift deployments