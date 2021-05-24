---
title: "1.2 Introduction to Quarkus"
linkTitle: "1.2 Introduction to Quarkus"
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


## Development Mode

Quarkus comes with a built-in development mode. Run your application with:

```s
./mvnw compile quarkus:dev
```

You can then update the application sources, resources and configurations. The changes are automatically reflected in
your running application. This is great to do development spanning UI and database as you see changes reflected
immediately.

quarkus:dev enables hot deployment with background compilation, which means that when you modify your Java files or
your resource files and refresh your browser these changes will automatically take effect. This works too for resource
files like the configuration property file. The act of refreshing the browser triggers a scan of the workspace, and if
any changes are detected the Java files are compiled, and the application is redeployed, then your request is serviced
by the redeployed application. If there are any issues with compilation or deployment an error page will let you know.

Hit `CTRL+C` to stop the application.


### Remote Development Mode

It is possible to use development mode remotely, so that you can run Quarkus in a container environment
(such as OpenShift) and have changes made to your local files become immediately visible.

This allows you to develop in the same environment you will actually run your app in, and with access to the same
services. Do not use this in production. This should only be used in a development environment. You should not run
production application in dev mode.

To do this you must build a mutable application, using the mutable-jar format. Set the following properties in
application.properties:

```text
quarkus.package.type=mutable-jar
quarkus.live-reload.password=changeit
quarkus.live-reload.url=http://my.cluster.host.com:8080
```

Before you start Quarkus on the remote host set the environment variable `QUARKUS_LAUNCH_DEVMODE=true`. If you are on
bare metal you can just set this via the export `QUARKUS_LAUNCH_DEVMODE=true` command, if you are running using docker
start the image with `-e QUARKUS_LAUNCH_DEVMODE=true`. When the application starts you should now see the following
line in the logs: Profile dev activated. Live Coding activated.

The remote side does not need to include Maven or any other development tools. The normal fast-jar Dockerfile that
is generated with a new Quarkus application is all you need. If you are using bare metal launch the Quarkus runner
jar, do not attempt to run normal devmode.

Now you need to connect your local agent to the remote host, using the `remote-dev` command:

```s
./mvnw quarkus:remote-dev -Dquarkus.live-reload.url=http://my-remote-host:8080
```

Now every time you refresh the browser you should see any changes you have made locally immediately visible in the
remote app. This is done via a HTTP based long polling transport, that will synchronize your local workspace and the
remote application via HTTP calls.

If you do not want to use the HTTP feature then you can simply run the `remote-dev` command without specifying the
URL. In this mode the command will continuously rebuild the local application, so you can use an external tool such
as odo or rsync to sync to the remote application.


### Debugging

In dev mode by default the debug port 5005 is enabled.

This behavior can be changed by giving the `debug` system property one of the following values:

* false - the JVM will start with debug mode disabled
* true - The JVM is started in debug mode and will be listening on port `5005`
* {port} - The JVM is started in debug mode and will be listening on {port}

For example you may change the Debug port with the following command
```s
./mvnw compile quarkus:dev -Ddebug=5000 
```


## Quarkus DevServices

If your are running tests or in development mode, quarkus provides a feature called DevSevices. DevServices are a way to
enhance developer joy by providing required datasources with zero configuration. This is supported for the most common
databases.

For most types of datasources the zero configuration spin up requires docker to be available on the local environment.
Under the hood quarkus is using testcontainers for this task.


### Postgres Example

As an example lets have a look at an application requiring a postgresql database. you may find
the code in the `{{% param solution_code_basedir %}}dev-services` folder. The example additionally uses Flyway to
provision some data and uses the `hibernate-orm-panache` implementation which we will not cover any further. However,
this does not change how the devservices work.

Our pom.xml looks like this:
```xml
  <dependencies>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-jdbc-postgresql</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-flyway</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-hibernate-orm-panache</artifactId>
    </dependency>
    <!-- ... -->
  </dependencies>
```

In our application properties we have configured the datasource type:
```properties
# Datasource
quarkus.datasource.db-kind=postgresql

# DevServices
quarkus.datasource.devservices.port=5432
quarkus.datasource.devservices.image-name=postgres:13.2

# Flyway
quarkus.flyway.baseline-description=Initial version
quarkus.flyway.migrate-at-start=true
%dev.quarkus.flyway.locations=db/migration,db/dev
```

The absolute required minimal configuration would be:
```properties
# Datasource
quarkus.datasource.db-kind=postgresql
```

With specifying `quarkus.datasource.devservices.port` we control and fix the port the spinned up datasource will use. If
we do not specify it quarkus will us a random port. With `quarkus.datasource.devservices.image-name` we can control the
database image quarkus will use.

We further have an entity Employee (view source) and some database initialize scripts (view db folder) to add some data.

If we start the application we will see that Quarkus connects to our docker daemon an uses testcontainers to spin up the
database.

```text
INFO  [org.tes.doc.DockerClientProviderStrategy] (build-25) Found Docker environment with local Unix socket (unix:///var/run/docker.sock)
INFO  [org.tes.DockerClientFactory] (build-25) Docker host IP address is localhost
INFO  [org.tes.DockerClientFactory] (build-25) Connected to docker: 
  Server Version: 19.03.13
  API Version: 1.40
  Operating System: Ubuntu 20.04.2 LTS
  Total Memory: 23709 MB
INFO  [org.tes.uti.ImageNameSubstitutor] (build-25) Image name substitution will be performed by: DefaultImageNameSubstitutor (composite of 'ConfigurationFileImageNameSubstitutor' and 'PrefixingImageNameSubstitutor')
INFO  [org.tes.DockerClientFactory] (build-25) Ryuk started - will monitor and terminate Testcontainers containers on JVM exit
INFO  [org.tes.DockerClientFactory] (build-25) Checking the system...
INFO  [org.tes.DockerClientFactory] (build-25) ✔︎ Docker server version should be at least 1.6.0
INFO  [org.tes.DockerClientFactory] (build-25) ✔︎ Docker environment should have more than 2GB free disk space
INFO  [🐳 .2]] (build-25) Creating container for image: postgres:13.2
INFO  [🐳 .2]] (build-25) Starting container with ID: 8ced042125623cb84d25e679f748927c729deb45b88da1d4f6ae130e391ad7c3
INFO  [🐳 .2]] (build-25) Container postgres:13.2 is starting: 8ced042125623cb84d25e679f748927c729deb45b88da1d4f6ae130e391ad7c3
INFO  [🐳 .2]] (build-25) Container postgres:13.2 started in PT1.575032S
```

We may also see the docker container using the docker command line tools. The database will us the port `5432` as
specified in the `application.properties`. If you want to connect to the database using your favourite tool use the
following properties:

* Connection String: `jdbc:postgresql://localhost:5432/default`
* Username: `quarkus`
* Password: `quarkus`


## Quarkus Dev UI

In the development mode Quarkus provides a development ui available at `/q/dev`.

![Quarkus Dev UI](../dev-ui-overview.png)

Each extension is able to provide custom information shown on the dev ui. Extensions can also provide:

* Runtime information
* Custom pages with or without actions to interact the application

![Quarkus Dev UI](../dev-ui-monkeys.png)

With the provided Configuration tool, you are able to change the runtime config for example the quarkus log level
without having to restart the Quarkus application. There is also a console showing the log output.

![Quarkus Dev UI](../config-ui.png)

[^1]: [Apicurio Registry](https://www.apicur.io/registry/)
[^2]: [Apache Avro](https://avro.apache.org/)
