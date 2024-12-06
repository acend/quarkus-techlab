---
title: "10.1. Creating the Extension"
weight: 1010
sectionnumber: 10.1
description: >
  Initialize the extension with maven.
---


## Creating the Extension

Quarkus provides the `create-extension` Maven Mojo to initialize your extension project.


### Task {{% param sectionnumber %}}.1 - Initialize Extension

Initialize the extension with the following command:
```s
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create-extension -N \
  -DgroupId=ch.puzzle \
  -DpackageName=ch.puzzle.quarkustechlab.extensions.appinfo \
  -DextensionId=techlab-extension-appinfo \
  -DextensionName="Appinfo Extension" \
  -DextensionDescription="Simple quarkus extension with some application details" \
  -DwithoutTests 
```

{{% alert color="warning" title="ConfigRoot" %}}
This extension lab uses a legacy annotation `@ConfigRoot` for the configuration class. Make sure your `maven-compiler-plugin` in your `techlab-extension-appinfo/pom.xml` looks like this:

```xml
<plugin>    
    <artifactId>maven-compiler-plugin</artifactId>    
    <version>${compiler-plugin.version}</version>    
    <configuration>    
        <parameters>true</parameters>    
         <compilerArgs>    
            <arg>-AlegacyConfigRoot=true</arg>    
        </compilerArgs>    
    </configuration>    
</plugin>
```
{{% /alert %}}

{{% alert color="info" title="Naming" %}}
We will use the prefix `techlab-` for our extension. Extensions starting with `quarkus-` are usually meant to be official extensions built and supported by the quarkus core team. Therefore, you should not prefix your own extensions with `quarkus-`.
{{% /alert %}}


## Extension Structure

A Quarkus Extension consists of a maven multi-module project. The project contains the following two modules:

{{% alert color="primary" %}}

* The **runtime module** which represents the capabilities the extension developer exposes to the application’s developer (an authentication filter, an enhanced data layer API, etc). Runtime dependencies are the ones the users will add as their application dependencies (in Maven POMs or Gradle build scripts).
* The **deployment module** which is used during the augmentation phase of the build, it describes how to "deploy" a library following the Quarkus philosophy. In other words, it applies all the Quarkus optimizations to your application during the build. The deployment module is also where we prepare things for GraalVM’s native compilation.

Source: [quarkus.io](https://quarkus.io/guides/building-my-first-extension)
{{% /alert %}}


## Configuring the basic extension details

The maven Mojo generated the two modules `runtime` and `deployment` modules and the parent pom. A quarkus extension
provides some meta information about the extension in an extension description file located at
`runtime/src/main/resources/META-INF/quarkus-extension.yaml`


### Task {{% param sectionnumber %}}.2 - Describe your Extension

Open the `quarkus-extension.yaml` and complete the missing information

```yaml
name: Appinfo    
#description: Appinfo ...    
metadata:    
#  keywords:    
#    - appinfo    
#  guide: ...    
#  categories:    
#    - "miscellaneous"    
#  status: "preview"     
```

You may wonder what these fields are used for:

* keywords: Used by [code.quarkus.io](https://code.quarkus.io) for searching
* categories: Used by [code.quarkus.io](https://code.quarkus.io) to categorize the extensions
  * Used: `web`, `data`, `messaging`, `core`, `reactive`, `cloud`, `observability`, `security`, `serialization`, `miscellaneous`, `compatibility`, `alt-languages`, `integration`
* name: describes your extension. Shown in Quarkus Dev-UI.
* description: describes your extension. Shown in Quarkus Dev-UI.
* guide: Link to an extension guide. Shown in Quarkus Dev-UI.
* status: Describes the extension maturity. This is also shown on [code.quarkus.io](https://code.quarkus.io) and on the Quarkus Dev-UI.
  * Used: `stable`, `preview`, `experimental`
  
Keywords and categories are not really used for custom extension. However, we recommend to set them with reasonable values as well.

{{% details title="Sample description hint" %}}
```yaml
name: Appinfo
description: Simple Appinfo Extension
metadata:
  keywords:
    - appinfo
  guide: https://www.puzzle.ch
  categories:
    - "miscellaneous"
  status: "experimental"
```
{{% /details %}}

