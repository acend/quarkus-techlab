# AppInfo project

Simple Project to test the AppInfo Extension

You first have to build the appinfo extension locally

```
cd appinfo
mvn clean package install
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/appinfo-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

### Build Native Docker Container
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker
docker build -f src/main/docker/Dockerfile.native -t appinfo-native-container:latest .
docker run -p 8080:8080 -e QUARKUS_APPINFO_RUN_BY=docker-test -it appinfo-native-container:latest
```

## Inspect generated code
Run the following commands
```shell script
./mvnw package
cd target/quarkus-app/quarkus
jar -xvf generated-bytecode.jar
```

Now open the class files in your IDE.
