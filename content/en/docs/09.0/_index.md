---
title: "9. Building Quarkus Extensions"
weight: 9
sectionnumber: 9
description: >
  Writing your own Quarkus Extension.
---


## Lab Content

In this section we will create our own extension in the `{{% param "lab_code_basedir" %}}` folder from your workspace.

The simple extension provides a very basic servlet to expose some application information. The required code for exposing 
this information will be wrapped in a quarkus extension. The extension will also provide an integration in the Quarkus Dev UI.

This extension will be used in another application as dependency to show its functionality. This application is fully 
provided in the `{{% param "solution_code_basedir" %}}`

## Quarkus Application Bootstrap

The bootstrapping of a Quarkus application takes place in three distinct bootstrap phases:

{{% alert color="primary" %}}
* **Augmentation.** During the build time, the Quarkus extensions will load and scan your application’s bytecode 
(including the dependencies) and configuration. At this stage, the extension can read configuration files, scan classes 
for specific annotations, etc. Once all the metadata has been collected, the extensions can pre-process the libraries 
bootstrap actions like your ORM, DI or REST controllers configurations. The result of the bootstrap is directly recorded 
into bytecode and will be part of your final application package.
* **Static Init.** During the run time, Quarkus will execute first a static init method which contains some extensions 
actions/configurations. When you will do your native packaging, this static method will be pre-processed during the 
build time and the objects it has generated will be serialized into the final native executable, so the initialization 
code will not be executed in the native mode (imagine you execute a Fibonacci function during this phase, the result of 
the computation will be directly recorded in the native executable). When running the application in JVM mode, this 
static init phase is executed at the start of the application.
* **Runtime Init.** Well nothing fancy here, we do classic run time code execution. So, the more code you run during 
the two phases above, the faster your application will start.

Source: [quarkus.io](https://quarkus.io/guides/building-my-first-extension)
{{% /alert %}}