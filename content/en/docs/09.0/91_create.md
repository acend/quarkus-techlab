---
title: "9.1. Creating the Extension"
weight: 91
sectionnumber: 9.1
description: >
  Initialize the extension with maven.
---


## Creating the Extension

Quarkus provides the `create-extension` Maven Mojo to initialize your extension project.

### Task {{% param sectionnumber %}}.1 - Initialize Extension
Enter the {{% param "lab_code_basedir" %}} folder from your workspace and initialize the extension with the following command:
```
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create-extension -N -DgroupId=ch.puzzle.quarkustechlab -DextensionId=appinfo -DwithoutTests 
```

## Extension Structure

A Quarkus Extension consists of a maven multi-module project. The project contains the following two modules:

{{% alert color="primary" %}}
* The **runtime module** which represents the capabilities the extension developer exposes to the application’s developer (an authentication filter, an enhanced data layer API, etc). Runtime dependencies are the ones the users will add as their application dependencies (in Maven POMs or Gradle build scripts).
* The **deployment module** which is used during the augmentation phase of the build, it describes how to "deploy" a library following the Quarkus philosophy. In other words, it applies all the Quarkus optimizations to your application during the build. The deployment module is also where we prepare things for GraalVM’s native compilation.

Source: [quarkus.io](https://quarkus.io/guides/building-my-first-extension)
{{% /alert %}}


## Configuring the basic extension details

The extension