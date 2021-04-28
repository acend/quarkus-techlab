---
title: "1.5 Quarkus with Maven"
linkTitle: "1.5 Quarkus with Maven"
weight: 150
sectionnumber: 1.5
description: >
  Quarkus with Maven
---

The preferrable software management tool for building Quarkus applications is either Maven or Gradle. In this part of the lab we are focussing building Quarkus applications with Maven.

To create a new Quarkus project with Maven simply use the following command:

```s
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=my-groupId \
    -DprojectArtifactId=my-artifactId \
    -DprojectVersion=my-version \
    -DclassName="org.my.group.MyResource"
```

With the attributes listed below you can alter the create command to your desired customization:

Attribute | Default Value | Description
--- | --- | ---
projectGroupId | org.acme.sample | The group id of the created project
projectArtifactId | mandatory | The artifact id of the created project. Not passing it triggers the interactive mode.
projectVersion | 1.0.0-SNAPSHOT | The version of the created project
platformGroupId | io.quarkus | The group id of the target platform. Given that all the existing platforms are coming from io.quarkus this one won’t practically be used explicitly. But it’s still an option.
platformArtifactId | quarkus-universe-bom | The artifact id of the target platform BOM. It should be quarkus-bom in order to use the locally built Quarkus.
platformVersion | If it’s not specified, the latest one will be resolved. | The version of the platform you want the project to use. It can also accept a version range, in which case the latest from the specified range will be used.
className | Not created if omitted | The fully qualified name of the generated resource
path | /hello | The resource path, only relevant if className is set.
extensions | [] | The list of extensions to add to the project (comma-separated)

You will receive a generated project including the java source code files and Dockerfiles generated for you.


## {{% param sectionnumber %}}.1: Extensions

Quarkus extensions provide your project with several useful dependencies.
From inside a Quarkus project, you can obtain a list of the available extensions with:

```s
./mvnw quarkus:list-extensions
```

Adding extensions to your project is as simple as:

```s
./mvnw quarkus:add-extension -Dextensions="hibernate-validator"
```

Extensions are passed using a comma-seperated list. They can also be installed with matching patterns:

```s
./mvnw quarkus:add-extension -Dextensions="hibernate-*"
```

### {{% param sectionnumber %}}.4: Creating native executables


Native executables make Quarkus applications ideal for containers and serverless workloads.

Make sure to have GRAALVM_HOME configured and pointing to GraalVM version 21.0.0 (Make sure to use a Java 11 version of GraalVM). Verify that your pom.xml has the proper native profile (see Maven configuration).

Create a native executable using: `./mvnw package -Pnative`. A native executable will be present in target/.

To run Integration Tests on the native executable, make sure to have the proper Maven plugin configured (see Maven configuration) and launch the `verify` goal.
