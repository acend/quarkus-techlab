---
title: "10. Serverless"
weight: 10
sectionnumber: 10
description: >
  Testing the serverless abilities of Quarkus.
---


## Lab Content

In this lab we are going to test the abilities to write serverless applications with Quarkus.


## {{% param sectionnumber %}}.1: Introduction

The trend shifting architectural designs more into the event-driven world was pretty fast adopted by the cloud native community. Kubernetes presented a new way of providing applications with their Knative project. Knative allows us to provide event-driven and serverless components built upon the already known and intuitive Kubernetes API.

At the core of Knative there are two components that can be used to provided the wanted functionalities:

* **Knative Serving**: Servings manage stateless services on Kubernetes
* **Knative Eventing**: Eventings manage the route between on-cluster or off-cluster components by exposing routing as configuration

These two components are delivered as custom resource definitions (CRD). For further and detailed readings check out the official [Knative Documentation](https://knative.dev/docs).

The main goal is to provide event-driven applications which do not need to idle for the time not used. As you can imagine, fast startup times are very crucial for the success of serverless applications. This is where the strenghts of the Quarkus framework come into play. We can develop small lightweight applications starting up almost instantanious.

In the following sections we are going to test these functionalities provided by the Knative / OpenShift Serverless project combined with Quarkus!
