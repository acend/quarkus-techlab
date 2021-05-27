---
title: "8.1 Introduction"
linkTitle: "8.1 Introduction"
weight: 810
sectionnumber: 8.1
description: >
   Introduction to reactive messaging.
---


## {{% param sectionnumber %}}.1: The Reactive Manifesto

"Today's demands are simply not met by yesterday’s software architectures." - The Reactive Manifesto

Old fashioned applications - often built as monoliths - struggle to meet the requirements of todays workload dimensions. Applications should become scalable, robust and easy to maintain. Similar to the [twelve-factor-app](https://12factor.net/) manifesto there is a Reactive Manifesto. It describes requirements to tackle problems for modern enterprise solutions.

[The Reactive Manifesto](https://www.reactivemanifesto.org/) defines that reactive systems are:

* Responsive
* Resilient
* Elastic
* Message Driven

We have already learned basics of messaging in a microservice architecture. Time to take a next step and make our messaging reactive. The approach we have seen in the chapter before is completely legitimate and there is nothing wrong with this approach.


## {{% param sectionnumber %}}.2: Reactive Messaging

In the last chapter we learned about basic messaging concepts and how two microservices can communicate with a message broker. In reactive messaging we connect channels directly to components. Instead of having a Thread running manually, we can annotate functions to bind them to events sent in a specific channel or data stream. This makes our code more readable and act in a reactive manner. Let's look at an example:

```java
@Incoming("data-inbound-reactive")
@Outgoing("data-outbound-reactive")
public String streamProcess(String value) {
    return value.toUpperCase();
}
```


If you read this example it's pretty clear what is happening. We are connecting with a Connector to a Channel (Queue or Topic) we call "data-inbound-reactive" and define it as the inbound connector for this method. On the other side we connect the outcome of this method to the "data-outbound-reactive" stream. Whenever the "data-inobund-reactive" stream sends an message we perform a transformation to uppercase and return the value into the "data-outbound-reactive" channel. Simple as that!


### {{% param sectionnumber %}}.2.1: Connectors

Connector can:

* retrieve messages from a remote broker (inbound)
* send messages to a remove broker (outbound)

A connector can, of course, implement both directions.

Inbound connectors are responsible for:

* Getting messages from the remote broker,
* Creating a Reactive Messaging Message associated with the retrieved message.
* Potentially associating technical metadata with the message. This includes unmarshalling the payload.
* Associating a acknowledgement callback to acknowledge the incoming message when the Reactive Messaging message is acknowledged.

Important:
Reactive matters! The first step should follow the reactive streams principle: uses non-blocking technology, respects downstream requests.

Outbound connectors are responsible for:

* Receiving Reactive Messaging Message and transform it into a structure understand by the remote broker. This includes marshalling the payload.
* If the Message contains outbound metadata (metadata set during the processing to influence the outbound structure and routing), taking them into account.
* Sending the message to the remote broker.
* Acknowledging the Reactive Messaging Message when the broker has accepted / acknowledged the message.


### {{% param sectionnumber %}}.2.2: Connectors Event Driven Architecture

With this reactive messaging approach we can build our applications on an event driven approach. Some interaction or trigger emits an event to a certain channel. Subscribers of this channel consume the message and react based on the event received. This loosens the coupling in our application and lowers the cohesion between logically seperated components.

In an event driven approach everything that happens in our application gets triggered by an event. Events in the event-driven software model describe what happens within a software system. If we imagine our application as a set of logically ordered processes every task in these processes gets triggered by such an event and might emit another new event. This concept is very intuitive to apply because it is a very natural way of thinking about how everyday things and tasks work.

Some typical patterns in event-driven architecture:


### {{% param sectionnumber %}}.2.3: Connectors Event notification

In this approach, microservices emit events through channels to trigger behaviour or notify other components about the change of a state in the application. Notification events do not carry too much data and are very light weight. This results in a very effective and ressource friendly communication between the microservices.


### {{% param sectionnumber %}}.2.4: Connectors Event-carried state transfer

Instead of only notifying about events this approach sends a payload as a message to another component containing every information needed to perform actions triggered by this event. This model comes very close to the typical RESTful approach and can be implemented very similar. Depending on the amount of data in the payload the network traffic might suffer under the amount of data transferred.


### {{% param sectionnumber %}}.2.4: Connectors Event-sourcing

The goal of event-sourcing is to represent every change in a system's state as an emitted event in chronological order. The event stream becomes the principle source of truth about the applications state. Changes in state, as sequences of events, are persisted in the event stream and can be 'replayed'.
