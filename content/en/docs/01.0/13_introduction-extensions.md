---
title: "1.3 Introduction to Extensions"
linkTitle: "1.3 Introduction to Extensions"
weight: 130
sectionnumber: 1.3
description: >
  Introduction to Quarkus Extensions
---

## Quarkus Extensions

Quarkus highly uses the concept of extensions. The Quarkus framework is composed of core parts and a set of extensions.
In the Quarkus world extensions are meant to integrate third-party frameworks (e.g. MicroProfile Reactive Messaging). 
They run on top of the Quarkus application.

Extensions are also used to adapt or optimize libraries or frameworks to the Quarkus world. The usually do the optimizations
needed that third-party code fits the quarkus world (e.g. build optimizations). 

{{% alert color="primary" %}}

![Quarkus Extensions](../extensions.png)

Image source: Red Hat
{{% /alert %}}

