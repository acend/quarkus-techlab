---
title: "1.2 Introduction to Quarkus"
linkTitle: "1.2 Introduction to Quarkus"
weight: 120
sectionnumber: 1.2
description: >
  Introduction to Quarkus
---

## Quarkus

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

Hit CTRL+C to stop the application.


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


### Configuring Development Mode

By default, the Maven plugin picks up compiler flags to pass to javac from maven-compiler-plugin.

If you need to customize the compiler flags used in development mode, add a configuration section to the plugin block
and set the compilerArgs property just as you would when configuring maven-compiler-plugin. You can also set source,
target, and jvmArgs. For example, to pass `--enable-preview` to both the JVM and javac:

```xml
<plugin>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-maven-plugin</artifactId>
  <version>${quarkus-plugin.version}</version>

  <configuration>
    <source>${maven.compiler.source}</source>
    <target>${maven.compiler.target}</target>
    <compilerArgs>
      <arg>--enable-preview</arg>
    </compilerArgs>
    <jvmArgs>--enable-preview</jvmArgs>
  </configuration>

  ...
</plugin>
```


### Debugging

In development mode, Quarkus starts by default with debug mode enabled, listening to port 5005 without suspending
the JVM.

This behavior can be changed by giving the `debug` system property one of the following values:

* false - the JVM will start with debug mode disabled
* true - The JVM is started in debug mode and will be listening on port `5005`
* client - the JVM will start in client mode and attempt to connect to `localhost:5005`
* {port} - The JVM is started in debug mode and will be listening on {port}

An additional system property `suspend` can be used to suspend the JVM, when launched in debug mode. suspend supports
the following values:

* y or true - The debug mode JVM launch is suspended
* n or false - The debug mode JVM is started without suspending

For example you may change the Debug port with the following command
```shell script
./mvnw compile quarkus:dev -Ddebug=5000 
```

## Quarkus Dev UI
In the development mode `mvn quarkus:dev`, quarkus provides a development ui.

![Quarkus Dev UI](../dev-ui-overview.png)

Each extension is able to provide custom information shown on the dev ui. Extensions can also provide:

* Runtime information
* Custom pages with or without actions to interact the application

![Quarkus Dev UI](../dev-ui-monkeys.png)

With the provided Configuration tool, you are able to change the runtime config for example the quarkus log level
without having to restart the Quarkus application. There is also a console showing the log output.

![Quarkus Dev UI](../config-ui.png)