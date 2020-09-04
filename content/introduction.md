# Introduction

"Quarkus is a Kubernetes Native Java stack tailored for GraalVM & OpenJDK
HotSpot, crafted from the best of breed Java libraries and standards. Also
focused on developer experience, making things just work with little to no
configuration and allowing to do live coding." - quarkus.io

In short, Quarkus brings a framework built upon JakartaEE standards to build
microservices in the Java environment. Per default Quarkus comes with full CDI
integration, RESTeasy-JAX-RS, dev mode and many more features.

Quarkus provides a list of extensions and frameworks which can be included into
your Quarkus project. Extensions (Hibernate ORM, Liquibase, Flyway, SmallRye
Reactive Messageing, and many others) are minified and customized to work with
the minimal resource consuming framework. 

Due to the optimization of extensions and the framework itself, Quarkus can be
used to create very resource friendly and efficient microservices. For example
a normal REST API created in Quarkus takes around 12MB Memory RSS when built
and compiled with the GraalVM as a native Image, Compiled and run by the
JVM the application takes about 73MB Memory RSS which is still pretty slim
compared to a standard Java stack which takes about 136MB Memory RSS.

Also the startup times benefit massively from the minified dependencies and
framework. A REST API starts when built as a native image in about 0.016
seconds. When run in a normal JVM the application starts up in about 0.943
seconds. A traditional Java stack uses about 9.5 seconds to start up. 

Due to the low memory consumption and fast startup times, Quarkus applications
are very well suited for the usage in a cloud native environment. It makes the
application fast and dynamically scalable.

Quarkus is open source and developed under the Apache License version 2.0. The
entire source code is hosted on [Github](https://github.com/quarkusio/quarkus)
and has an active community. 
