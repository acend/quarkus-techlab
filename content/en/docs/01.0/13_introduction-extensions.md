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
Extensions run on top of the fundament the Quarkus core components provide.

{{% alert color="primary" %}}

![Quarkus Extensions](../extensions.png)

Image source: Red Hat
{{% /alert %}}

Some important parts of the core module are:

* Configuration (MicroProfile Configuration)
* Logging (centralized log management)
* ArC (Build time CDI dependency injection)


### Why Extensions

In the Quarkus world extensions are used for:

* Providing developers a platform to extend the Quarkus core functionality.
* Configure, boot and integrate third-party frameworks or technologies into your application (e.g. MicroProfile Reactive Messaging).
* Adapting and optimizing libraries or frameworks to the Quarkus world. They often contain the right information needed
for GraalVM to compile your application natively.


### Structure of Extensions

An extension has a simple structure.

* parent: maven parent module
* deployment: all code used at build time
* runtime: all code used at runtime

More details you'll find in the extensions chapter.


### Example

Let us consider an example. Assume we have an external library with several `MessageConverter` classes. What you want to
achieve is that the classes get added to the dependency injection context. As they are in an external dependency you
cannot annotate them.

For this to achieve we could create a simple extension:

```shell script
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create-extension -N -DgroupId=ch.puzzle.quarkustechlab -DextensionId=message-converter-as-beans -DwithoutTests
```

In this simple extensions we only need the deployment module.

Quarkus applications assume that they live in a closed world. Dynamic loading of jars at runtime is therefore not
possible. This is some kind of limitation but also opens the possibility to index all classes and look them up. This
is done using Jandex[^1]. By default not all classes are included in the Jandex. We can add external dependencies with
adding a build step in our Processor-Class in the deployment module.

```java
@BuildStep
IndexDependencyBuildItem indexExternalDependency() {
    return new IndexDependencyBuildItem("my.dependency.group.id", "my-dependency-artifact-id");
}
```

What we have to do next is a lookup in the Jandex to find the classes which have to be added as CDI beans. For this we
create another build step.

```java
@BuildStep
void messageConvertersAsBean(CombinedIndexBuildItem index, BuildProducer<AdditionalBeanBuildItem> additionalBeans) { 
    List<String> converters = index.getIndex().getKnownClasses().stream() 
            .filter(ci -> !Modifier.isAbstract(ci.flags())) 
            .map(ci -> ci.name().toString()) 
            .filter(c -> c.startsWith("my.dependency.package.")) 
            .filter(c -> c.endsWith("MessageConverter")) 
            .collect(Collectors.toList());

    additionalBeans.produce(new AdditionalBeanBuildItem.Builder() 
            .addBeanClasses(converters)
            .setUnremovable() 
            .setDefaultScope(DotNames.APPLICATION_SCOPED) 
            .build());
}
```

This achieves that all non-abstract classes from `my.dependency.package` ending with `MessageConverter` will be available
as a `@ApplicationScoped` CDI bean.

That is a very simple example how an extension could help to adapt a dependency to the Quarkus world.

[^1]: [Jandex is a space efficient Java annotation indexer and offline reflection library](https://github.com/wildfly/jandex)
