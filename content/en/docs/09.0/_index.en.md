---
title: "9. Building a Quarkus Extension"
weight: 9
sectionnumber: 9
description: >
  Writing your Onw Quarkus Extension.
---


## {{% param sectionnumber %}}.1: Creating the Extension

Quarkus provides create-extension Maven Mojo to initialize your extension project.

Initialize the AppInfo Extension:
```
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create-extension -N \
    -DgroupId=ch.puzzle \ 
    -DextensionId=appinfo-extension \  
    -DwithoutTests 
```


