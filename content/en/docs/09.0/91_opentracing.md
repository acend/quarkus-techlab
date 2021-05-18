---
title: "9.1 OpenTracing"
linkTitle: "9.1 OpenTracing"
weight: 910
sectionnumber: 9.1
description: >
    Introduction OpenTracing.
---


## {{% param sectionnumber %}}.1: Distributed Systems

Applications consisting of several microservices reduce the cohesion and complexity of single parts of applications massively. By enforcing a single responsibility principle on the application's architectural top layer the bottom-up vision brings a very clear image of what each part of a system does. On the other side the top-down vision of an application gets more complex. Handling errors across multiple microservices will become very frustrating and time consuming. This is very tracing comes in handy.


## {{% param sectionnumber %}}.2: Distributed Tracing with Opentracing

The [Opentracing](https://opentracing.io) project defines an API for distributed tracing in modern applications. It defines a certain terminology or semantic specification to avoid language-specific concepts.


### {{% param sectionnumber %}}.2.1: The OpenTracing Data Model

*Traces* in Opentracing are defined implicitly by their *Spans*. In particular, a *Trace* can be thought of as a directed acyclic graph (DAG) of *Spans*, where the edges between *Spans* are called *References*.

For example, the following is an example *Trace* made up of 8 *Spans*:

```text

Causal relationships between Spans in a single Trace


        [Span A]  ←←←(the root span)
            |
     +------+------+
     |             |
 [Span B]      [Span C] ←←←(Span C is a `ChildOf` Span A)
     |             |
 [Span D]      +---+-------+
               |           |
           [Span E]    [Span F] >>> [Span G] >>> [Span H]
                                       ↑
                                       ↑
                                       ↑
                         (Span G `FollowsFrom` Span F)

```

Sometimes it's easier to visualize *Traces* with a time axis as in the diagram below:

```text

Temporal relationships between Spans in a single Trace


––|–––––––|–––––––|–––––––|–––––––|–––––––|–––––––|–––––––|–> time

 [Span A···················································]
   [Span B··············································]
      [Span D··········································]
    [Span C········································]
         [Span E·······]        [Span F··] [Span G··] [Span H··]

```

Each *Span* encapsulates the following state:

* An operation name
* A start timestamp
* A finish timestamp
* A set of zero or more key:value *Span Tags*. The keys must be strings. The values may be strings, bools, or numeric types.
* A set of zero or more *Span Logs*, each of which is itself a key:value map paired with a timestamp. The keys must be strings, though the values may be of any type. Not all OpenTracing implementations must support every value type.
* A *SpanContext* (see below)
* References to zero or more causally-related *Spans* (via the *SpanContext* of those related *Spans*)

Each *SpanContext* encapsulates the following state:

* Any OpenTracing-implementation-dependent state (for example, trace and span ids) needed to refer to a distinct *Span* across a process boundary
* *Baggage Items*, which are just key:value pairs that cross process boundaries


### {{% param sectionnumber %}}.2.2: The OpenTracing API

There are three critical and inter-related types in the OpenTracing specification: `Tracer`, `Span`, and `SpanContext`. Below, we go through the behaviors of each type; roughly speaking, each behavior becomes a "method" in a typical programming language, though it may actually be a set of related sibling methods due to type overloading and so on.

* The `Tracer` interface creates `Spans` and understands how to `Inject` (serialize) and `Extract` (deserialize) them across process boundaries. Formally, it has the following capabilities.
* The `Span` allows us to retreive the `Span`'s `SpanContext`, Overwrite operations names, Finish the `Span`, Set `Span` Tags and log structured data.
* The `SpanContext` is more of a "concept" than a useful piece of functionality at the generic OpenTracing layer. That said, it is of critical importance to OpenTracing implementations and does present a thin API of its own. Most OpenTracing users only interact with `SpanContext` via references when starting new `Span`s, or when injecting/extracting a trace to/from some transport protocol.
