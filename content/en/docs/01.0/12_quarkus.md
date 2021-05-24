---
title: "1.2 Quarkus"
linkTitle: "1.2 Quarkus"
weight: 120
sectionnumber: 1.2
description: >
  Introduction to Quarkus
---


## Quarkus

{{% alert color="primary" %}}
Quarkus is a Kubernetes Native Java stack tailored for GraalVM & OpenJDK
HotSpot, crafted from the best of breed Java libraries and standards. Also
focused on developer experience, making things just work with little to no
configuration and allowing to do live coding. - quarkus.io
{{% /alert %}}

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


## How Quarkus works

When creating applications we usually have to phases before we can use our application.

* **Build-Time phase:** We compile our code in a package which will be used to run the application (usually a jar, war, ..).
usually happens at the CI/CD pipeline or locally at the developers environment.
* **Run-Time phase:** The application executes the code packed in the build phase. Phase happens in the actual run-time environment.

From an application perspective the application in the run-time phase has two states:

* **Bootstrapping:** The application is currently starting and does some task before it is fully started and ready to serve
requests.
* **Ready:** Application is up and running and ready to serve requests.

First, lets see what a traditional application does while bootstrapping:

* Load configuration files from disk
* Parse configuration files (XML, yaml, json, ...)
* Scan the classpath for annotations, load classes, enable/disable features
* CDI, Java reflection, dynamic preparation, indexing, caching, ...

For this to achieve the application loads a lot of classes and do a lot of actions which require time. This is the main
reason for a slow startup. Loaded classes will use memory even if they do not belong to the application and will not
be used again.

In summary:

* Too much classes beeing loaded
* Too much metadata processing for annotations, cdi, reflection, proxies, ...

This is where Quarkus does things differently. It moves everything possible to the build-time phase instead of run-time
phase.

* A lot of the configuration can be parsed, interpreted and turned into bytecode at build-time.
* Annotations can be decomposed into method calls at build-time
* CDI beans can be built at build-time
* Reflection and proxies behavior can be analyzed and turned into bytecode as well


### Record and replay

How Quarkus solves the movement from run-time to built-time is with the concept of recording and replay.

If you run your quarkus build with
```s
./mvnw -X clean package -Dquarkus.log.level=debug
```

You may see the augmentation process which Quarkus is using to record the bytecode output of these steps.

```
[DEBUG] [io.quarkus.deployment.QuarkusAugmentor] Beginning Quarkus augmentation
...
[DEBUG] [io.quarkus.builder] Starting step ...
[DEBUG] [io.quarkus.builder] Finished step ...
...
[DEBUG] [io.quarkus.builder] Starting step ...
[DEBUG] [io.quarkus.builder] Finished step ...
```

Doing so Quarkus generates the bytecode used instead the time and memory consuming steps a traditional application does
at bootstrapping.

You can inspect the generated code like this:
```s
cd target/quarkus-app/quarkus/
jar -xvf generated-bytecode.jar
```

* The recorded bytecode is under `io/quarkus/deployment/steps/`
* The main class and the invocation code is `io/quarkus/runner/ApplicationImpl.class`

In the class `io/quarkus/runner/ApplicationImpl.class` you can see the invocations of the recorded bytecode.
```java
// $FF: synthetic class
public class ApplicationImpl extends Application {
    static Logger LOG;
    public static StartupContext STARTUP_CONTEXT;

    public ApplicationImpl() {
    }

    /* Phase STATIC_INIT */
    static {
        System.setProperty("io.netty.allocator.maxOrder", "1");
        System.setProperty("io.netty.machineId", "e9:71:7b:30:45:7b:fa:40");
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        ProfileManager.setLaunchMode(LaunchMode.NORMAL);
        StepTiming.configureEnabled();
        Timing.staticInitStarted();
        Config.ensureInitialized();
        LOG = Logger.getLogger("io.quarkus.application");
        StartupContext var0 = new StartupContext();
        STARTUP_CONTEXT = var0;

        try {
            StepTiming.configureStart();
            ((StartupTask)(new syntheticBean1188624218())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new ioThreadDetector-1463825589())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new setupLoggingStaticInit-1235809433())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new blockingOP558072755())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new build163995889())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new servletContextBean-1962634234())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new initStatic1190120725())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new generateResources-1025303321())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new setupResteasyInjection2143006352())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new build-649634386())).deploy(var0);
            StepTiming.printStepTime(var0);
        } catch (Throwable var2) {
            ApplicationStateNotification.notifyStartupFailed(var2);
            var0.close();
            throw (Throwable)(new RuntimeException("Failed to start quarkus", var2));
        }
    }

    /* Phase RUNTIME_INIT */
    protected final void doStart(String[] var1) {
        /* ... */
    }
}
```

In summary:

* Quarkus tries to do all heavy work in the build-time phase and replay the recorded bytecode at run-time phase
* Using this it reduces the bootstrapping time needed to startup
* Only classes needed will get loaded
* Building is done using the maven quarkus plugin
* Adaption of a framework or technology to this build-time approach is usually done within extensions.


## Configuration Phases

Since a lot of tasks in Quarkus are run at build time, this also affects the configuration. In Quarkus there are
multiple configuration phases.

Phase name       | Read & avail. at build time | Avail. at run time | Read during static init | Re-read during startup (native executable) | Notes
-----------------|-----------------------------|--------------------|-------------------------|--------------------------------------------|------
`BUILD_TIME`     | Yes | No | No | No | Appropriate for things which affect build.
`BUILD_AND_RUN_TIME_FIXED`   | Yes | Yes | No | No | Appropriate for things which affect build and must be visible for run time code. Not read from config at run time.
`BOOTSTRAP`      | No | Yes | No | Yes | Used when runtime configuration needs to be obtained from an external system (like `Consul`), but details of that system need to be configurable (for example Consul's URL). The high level way this works is by using the standard Quarkus config sources (such as properties files, system properties, etc.) and producing `ConfigSourceProvider` objects which are subsequently taken into account by Quarkus when creating the final runtime `Config` object.
`RUN_TIME`       | No | Yes | Yes | Yes | Not available at build, read at start in all modes.

Source and more details [Configuration Root Phases](https://quarkus.io/guides/writing-extensions#configuration-root-phases)

We will use configuration contexts in the &laquo;Quarkus Extension Lab&raquo;.


### Example

Let us have a look at an example. You want to use the Apicurio Registry[^1] as your Schema Registry for your Kafka Avro[^2]
Schemas and as well for your API designs. Apicurio provides different storage implementations and you like to have
stored the data in a oracle database. There is also an official docker-image `apicurio/apicurio-registry-sql` available.

However, is this image usable for our case? Unfortunately not. The image is built for postgres. The needed configuration
for your runtime environment is easily overridable using environment variables like
`QUARKUS_DATASOURCE_USERNAME`.

These are typical properties which must be overridable at runtime. However, properties like JDBC driver are fixed at build-time
even if the database driver would be included we would not be able to override the `QUARKUS_DATASOURCE_JDBC_DRIVER` property.

![Configuration Properties](../config-properties.png)

We can also see this in the [All Configuration Overview](https://quarkus.io/guides/all-config). Properties with the lock
symbol are fixed at build time. Changing these strictly requires rebuilding the application.

[^1]: [Apicurio Registry](https://www.apicur.io/registry/)
[^2]: [Apache Avro](https://avro.apache.org/)
