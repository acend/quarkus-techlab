---
title: "1.5 Quarkus with Maven"
linkTitle: "1.5 Quarkus with Maven"
weight: 150
sectionnumber: 1.5
description: >
  Quarkus with Maven
---

The preferrable software management tool for building Quarkus applications is either Maven or Gradle. In this part of the lab we are focussing building Quarkus applications with Maven.

To create a new Quarkus project with Maven simply use the following command:

```s

mvn io.quarkus:quarkus-maven-plugin:{{% param quarkusVersion%}}:create \
    -DprojectGroupId=my-groupId \
    -DprojectArtifactId=my-artifactId \
    -DprojectVersion=my-version \
    -DclassName="org.my.group.MyResource"

```

With the attributes listed below you can alter the create command to your desired customization:

Attribute | Default Value | Description
--- | --- | ---
projectGroupId | org.acme.sample | The group id of the created project
projectArtifactId | mandatory | The artifact id of the created project. Not passing it triggers the interactive mode.
projectVersion | 1.0.0-SNAPSHOT | The version of the created project
platformGroupId | io.quarkus | The group id of the target platform. Given that all the existing platforms are coming from io.quarkus this one won’t practically be used explicitly. But it’s still an option.
platformArtifactId | quarkus-universe-bom | The artifact id of the target platform BOM. It should be quarkus-bom in order to use the locally built Quarkus.
platformVersion | If it’s not specified, the latest one will be resolved. | The version of the platform you want the project to use. It can also accept a version range, in which case the latest from the specified range will be used.
className | Not created if omitted | The fully qualified name of the generated resource
path | /hello | The resource path, only relevant if className is set.
extensions | [] | The list of extensions to add to the project (comma-separated)

You will receive a generated project including the java source code files and Dockerfiles generated for you.


## {{% param sectionnumber %}}.1: Extensions

Quarkus extensions provide your project with several useful dependencies.
From inside a Quarkus project, you can obtain a list of the available extensions with:

```s

./mvnw quarkus:list-extensions

```

Adding extensions to your project is as simple as:

```s

./mvnw quarkus:add-extension -Dextensions="hibernate-validator"

```

Extensions are passed using a comma-seperated list. They can also be installed with matching patterns:

```s

./mvnw quarkus:add-extension -Dextensions="hibernate-*"

```


## {{% param sectionnumber %}}.2: Development Mode

Quarkus comes with a built-in development mode. Run your application with:

```s

./mvnw compile quarkus:dev

```

You can then update the application sources, resources and configurations. The changes are automatically reflected in your running application. This is great to do development spanning UI and database as you see changes reflected immediately.

quarkus:dev enables hot deployment with background compilation, which means that when you modify your Java files or your resource files and refresh your browser these changes will automatically take effect. This works too for resource files like the configuration property file. The act of refreshing the browser triggers a scan of the workspace, and if any changes are detected the Java files are compiled, and the application is redeployed, then your request is serviced by the redeployed application. If there are any issues with compilation or deployment an error page will let you know.

Hit CTRL+C to stop the application.


### {{% param sectionnumber %}}.2.1: Remote Development Mode

It is possible to use development mode remotely, so that you can run Quarkus in a container environment (such as OpenShift) and have changes made to your local files become immediately visible.

This allows you to develop in the same environment you will actually run your app in, and with access to the same services. Do not use this in production. This should only be used in a development environment. You should not run production application in dev mode.

To do this you must build a mutable application, using the mutable-jar format. Set the following properties in application.xml:

```text
quarkus.package.type=mutable-jar 
quarkus.live-reload.password=changeit 
quarkus.live-reload.url=http://my.cluster.host.com:8080 
```

Before you start Quarkus on the remote host set the environment variable QUARKUS_LAUNCH_DEVMODE=true. If you are on bare metal you can just set this via the export QUARKUS_LAUNCH_DEVMODE=true command, if you are running using docker start the image with -e QUARKUS_LAUNCH_DEVMODE=true. When the application starts you should now see the following line in the logs: Profile dev activated. Live Coding activated.

The remote side does not need to include Maven or any other development tools. The normal fast-jar Dockerfile that is generated with a new Quarkus application is all you need. If you are using bare metal launch the Quarkus runner jar, do not attempt to run normal devmode.

Now you need to connect your local agent to the remote host, using the `remote-dev` command:

```s
./mvnw quarkus:remote-dev -Dquarkus.live-reload.url=http://my-remote-host:8080
```

Now every time you refresh the browser you should see any changes you have made locally immediately visible in the remote app. This is done via a HTTP based long polling transport, that will synchronize your local workspace and the remote application via HTTP calls.

If you do not want to use the HTTP feature then you can simply run the `remote-dev` command without specifying the URL. In this mode the command will continuously rebuild the local application, so you can use an external tool such as odo or rsync to sync to the remote application.


### {{% param sectionnumber %}}.2.2: Configuring Development Mode

By default, the Maven plugin picks up compiler flags to pass to javac from maven-compiler-plugin.

If you need to customize the compiler flags used in development mode, add a configuration section to the plugin block and set the compilerArgs property just as you would when configuring maven-compiler-plugin. You can also set source, target, and jvmArgs. For example, to pass --enable-preview to both the JVM and javac:

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


### {{% param sectionnumber %}}.3: Debugging

In development mode, Quarkus starts by default with debug mode enabled, listening to port 5005 without suspending the JVM.

This behavior can be changed by giving the debug system property one of the following values:

* false - the JVM will start with debug mode disabled
* true - The JVM is started in debug mode and will be listening on port 5005
* client - the JVM will start in client mode and attempt to connect to localhost:5005
* {port} - The JVM is started in debug mode and will be listening on {port}

An additional system property suspend can be used to suspend the JVM, when launched in debug mode. suspend supports the following values:

* y or true - The debug mode JVM launch is suspended
* n or false - The debug mode JVM is started without suspending


### {{% param sectionnumber %}}.4: Creating native executables


Native executables make Quarkus applications ideal for containers and serverless workloads.

Make sure to have GRAALVM_HOME configured and pointing to GraalVM version 21.0.0 (Make sure to use a Java 11 version of GraalVM). Verify that your pom.xml has the proper native profile (see Maven configuration).

Create a native executable using: `./mvnw package -Pnative`. A native executable will be present in target/.

To run Integration Tests on the native executable, make sure to have the proper Maven plugin configured (see Maven configuration) and launch the `verify` goal.
