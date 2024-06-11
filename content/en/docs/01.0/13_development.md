---
title: "1.3 Development"
linkTitle: "1.3 Development"
weight: 130
sectionnumber: 1.3
description: >
  Quarkus from the development perspective
---

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
start the image with `-e QUARKUS_LAUNCH_DEVMODE=true`.

On command line, use the following command:
```s
JAVA_ENABLE_DEBUG=true QUARKUS_LAUNCH_DEVMODE=true java -jar target/quarkus-app/quarkus-run.jar -Dquarkus.package.type=mutable-jar
```

When the application starts you should now see the following
line in the logs: Profile dev activated. Live Coding activated.

The remote side does not need to include Maven or any other development tools. The normal fast-jar Dockerfile that
is generated with a new Quarkus application is all you need. If you are using bare metal launch the Quarkus runner
jar, do not attempt to run normal devmode.

Now you need to connect your local agent to the remote host, using the `remote-dev` command:

```s
./mvnw quarkus:remote-dev \
  -Ddebug=false \
  -Dquarkus.package.type=mutable-jar \
  -Dquarkus.live-reload.url=http://my-remote-host:8080 \
  -Dquarkus.live-reload.password=changeit
```

Now every time you refresh the browser you should see any changes you have made locally immediately visible in the
remote app. This is done via a HTTP based long polling transport, that will synchronize your local workspace and the
remote application via HTTP calls.

You can use the following docker file to build a remote debug enabled container:

```Dockerfile
# Dockerfile for remote-dev-mode
FROM adoptopenjdk/openjdk14-openj9:x86_64-alpine-jre-14_36.1_openj9-0.19.0
RUN apk add curl

ENV QUARKUS_LAUNCH_DEVMODE=true \
    JAVA_ENABLE_DEBUG=true

COPY target/quarkus-app/lib/ /deployments/lib/
COPY target/quarkus-app/*.jar /deployments/
COPY target/quarkus-app/app/ /deployments/app/
COPY target/quarkus-app/quarkus/ /deployments/quarkus/

CMD ["java", "-jar", \
  "-Dquarkus.http.host=0.0.0.0", \
  "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", \
  "-Dquarkus.package.type=mutable-jar", \
  "-Dquarkus.live-reload.password=changeit", \
  "/deployments/quarkus-run.jar"]
```


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

{{% alert color="primary" title="Supported database types" %}}

* Postgresql (container)
* MySQL (container)
* MariaDB (container)
* H2 (in-process)
* Apache Derby (in-process)
* Oracle (container)
* DB2 (container) (requires license acceptance)
* MSSQL (container) (requires license acceptance)

Source: [quarkus.io](https://quarkus.io/guides/datasource)
{{% /alert %}}

For most types of datasources the zero configuration spin up requires docker to be available on the local environment.
Under the hood Quarkus is using TestContainers for this task.


### Postgres Example

As an example lets have a look at an application requiring a postgresql database. you may find
the code in the `{{% param solution_code_basedir %}}dev-services` folder. The example additionally uses Flyway to
provision some data and uses the `hibernate-orm-panache` implementation which we will not cover any further. However,
this does not change how the devservices work.

The dev-services `pom.xml` contains the following dependencies:

{{< csvtable csv="/solution/dev-services/dependencies.csv" class="dependencies" >}}

In our application properties we have configured the datasource type:
```properties
# Datasource
quarkus.datasource.db-kind=postgresql

# DevServices
quarkus.datasource.devservices.port=5432
quarkus.datasource.devservices.image-name=postgres:13.4

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

We further have an entity Employee ([view source](https://github.com/puzzle/quarkus-techlab/blob/master/solution/dev-services/src/main/java/ch/puzzle/quarkustechlab/entity/Employee.java)) and some database initialize scripts ([view db folder](https://github.com/puzzle/quarkus-techlab/tree/master/solution/dev-services/src/main/resources/db)) to add some data.

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
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
INFO  [org.fly.cor.int.lic.VersionPrinter] (Quarkus Main Thread) Flyway Community Edition 7.7.3 by Redgate
INFO  [org.fly.cor.int.dat.bas.DatabaseType] (Quarkus Main Thread) Database: jdbc:postgresql://localhost:5432/default (PostgreSQL 13.2)
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
  * If you are using Flyway you may for example clean and migrate your database using the Dev UI

Example of an ChaosMonkey extension which allows to inject random errors in your rest endpoints.

![Quarkus Dev UI](../dev-ui-monkeys.png)

With the provided Configuration tool, you are able to change the runtime config for example the quarkus log level
without having to restart the Quarkus application. There is also a console showing the log output.

![Quarkus Dev UI](../config-ui.png)


## Quarkus CLI

With the release of version 2.0 of Quarkus the new quarkus-cli was presented. The CLI will improve interaction with quarkus projects.

Currently the Quarkus CLI is available as a jar installable using jbang.

On Linux, macOS, and Windows (using WSL or bash compatible shell like cygwin or mingw)

```s
curl -Ls https://sh.jbang.dev | bash -s - app install --fresh --force quarkus@quarkusio
```

On Windows using Powershell:

```s
iex "& { $(iwr https://ps.jbang.dev) } app install --fresh --force quarkus@quarkusio"
```

If jbang has already been installed, you can install it directly:

```s
# This can also be used to update to the latest version
jbang app install --fresh --force quarkus@quarkusio

# Use the latest (or locally built) snapshot (with qss as an alias)
jbang app install --force --name qss ~/.m2/repository/io/quarkus/quarkus-cli/999-SNAPSHOT/quarkus-cli-999-SNAPSHOT-runner.jar
```

Once installed quarkus will be in your PATH and if you run quarkus --version it will print the installed version:

```s
quarkus --version
Client Version {quarkus-version}
```

Check out `quarkus --help` to get a help information with all the available commands.


### Creating a new project

To create a new Quarkus project simply run the `create` command of the CLI:

```s
quarkus create app test-name

-----------

applying codestarts...
📚  java
🔨  maven
📦  quarkus
📝  config-properties
🔧  dockerfiles
🔧  maven-wrapper
🚀  resteasy-codestart

-----------
[SUCCESS] ✅  quarkus project has been successfully generated in:
--> <pwd>/test-name
-----------
Navigate into this directory and get started: quarkus dev
```

To check out the options for project creation see `quarkus create app --help`.


### Working with extensions

The Quarkus CLI will give you quality of life features when working with extensions.
You can list your installed extensions in a Quarkus project by invoking

```s
quarkus ext ls

Looking for the newly published extensions in registry.quarkus.io
Current Quarkus extensions installed: 

quarkus-resteasy                                  

To get more information, append `--full` to your command line.
```

When looking for new extensions to install you can use the `--installable / -i` option. This will simply list all extensions available. You can also filter your query by using the `--search / -s <key>` for the keyword `<key>`.

```s
quarkus ext ls -is openshift

Current Quarkus extensions installable: 

quarkus-openshift                                 

To get more information, append `--full` to your command line.

To list only extensions from specific category, append `--category "categoryId"` to your command line.

Add an extension to your project by adding the dependency to your pom.xml or use `quarkus extension add "artifactId"`
```

When you have found your desired extension you can add the extension with

```s
quarkus ext add smallrye-health

[SUCCESS] ✅  Extension io.quarkus:quarkus-smallrye-health has been installed
```

Or to remove extensions use

```s
quarkus ext rm smallrye-health

[SUCCESS] ✅  Extension io.quarkus:quarkus-smallrye-health has been uninstalled
```


### Building and running the project

Building your project with the Quarkus CLI is as simple as:

```s
quarkus build
```

To start up your application in dev mode you can use:

```s
quarkus dev
```

{{% details title="Hint" %}}

They say cool kids use:

```s
alias q=quarkus
```

{{% /details %}}
