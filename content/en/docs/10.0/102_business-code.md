---
title: "10.2. Main functionality"
weight: 1020
sectionnumber: 10.2
description: >
  Write the main extension functionality code.
---

Since we have created the extension we can now start to write the functionality that our extension will provide to an
application.


## Extension Functionality

Our extension should provide an endpoint returning some application details. The application is supposed to expose the
following information:

Information      | Description
-----------------|--------------------------------------------
`Build time`     | Collected at build time
`Create time`    | Collected at specific instance creation
`Startup time`   | Collected using Quarkus life cycle event `StartupEvent`
`Current time`   | That one is easy.
`Built for`      | Extension configuration value `quarkus.appinfo.built-for`
`Run by`         | Extension configuration value `quarkus.appinfo.run-by`
`Name`           | Quarkus application name `quarkus.application.name`
`Version`        | Quarkus application version `quarkus.application.version`
`Properties`     | Collected using `ConfigProvider`


### Task {{% param sectionnumber %}}.1 - Add dependencies

For our endpoint we will use the `quarkus-undertow` extension. This extension provides a straight forward way to create
a simple endpoint based on the undertow web server[^1].

* Add the `quarkus-undertow` extension to your runtime `pom.xml`.
* Add the `quarkus-undertow-deployment` extension to your deployment `pom.xml`.

{{% details title="Hint Runtime Module" %}}
Your dependency block in the runtime `pom.xml` should look like this:

```xml
  <dependencies>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-arc</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-undertow</artifactId>
    </dependency>
 </dependencies>
```
{{% /details %}}


{{% details title="Hint Deployment Module" %}}
Your dependency block in the deplyoment `pom.xml` should look like this:

```xml
  <dependencies>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-arc-deployment</artifactId>
    </dependency>
    <dependency>
      <groupId>ch.puzzle</groupId>
      <artifactId>techlab-extension-appinfo</artifactId>
      <version>${project.version}</version>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-undertow-deployment</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-junit5-internal</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
```
{{% /details %}}


### Task {{% param sectionnumber %}}.2 - Creating Java POJOs

{{% alert title="Source Folder" color="warning" %}}
The creation of the extension did not create any source folder in the runtime module. Make sure you create the
following directories yourself `runtime/src/main/java/ch/puzzle/quarkustechlab/extensions/appinfo/runtime`.

In the further tasks we usually only reference class names like `Appinfo.java`. If not stated otherwise, all classes
belong to the path created above.
{{% /alert %}}

For the extension we create some POJOs for holding our application information.

Class      | Description
-----------------|--------------------------------------------
`Appinfo.java`        | Holding the application info
`BuildInfo.java`      | Holding the information collected at build-time

Create the `Appinfo.java` and `BuildInfo.java` class according to the diagram below. The method `asHumanReadableString`
in `Appinfo.java` is supposed to export the information as a simple string. Usually we would create an API for exporting
the information on the servlet. However, for simplicity of the lab it is sufficient to just return a raw string (you can
 also use a generated `toString` implementation).

![Java Classes](../java-pojo.png)

Before you start to implement the classes `Appinfo.java` and `BuildInfo.java`  create the following `AppinfoNames.java` class:

```java
package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

public class AppinfoNames {
  public static final String EXTENSION_NAME = "appinfo";
  public static final String CONFIG_PREFIX = "quarkus."+ EXTENSION_NAME;
}
```

Now try to implement the `Appinfo.java` and `BuildInfo.java` classes based on the diagram above.

{{% details title="Hint" %}}
Your `Appinfo.java` class should look something like this:

```java
package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

public class Appinfo {

    String buildTime;
    String builtFor;
    String runBy;
    String createTime;
    String startupTime;
    String currentTime;
    String applicationName;
    String applicationVersion;
    String propertiesString;

    String asHumanReadableString() {
        String format = "%-15s %s%n";

        return "Appinfo\n" +
                String.format(format, "buildTime", buildTime) +
                String.format(format, "builtFor", builtFor) +

                String.format(format, "runBy", runBy) +
                String.format(format, "createTime", createTime) +
                String.format(format, "startupTime", startupTime) +

                String.format(format, "name", applicationName) +
                String.format(format, "version", applicationVersion) +

                String.format(format, "currentTime", currentTime) +

                "\n\nProperties\n" +
                propertiesString;
    }

    // Getter and setter are omitted for readability. be sure to create them.
}
```

Your `BuildInfo.java` class should look something like this:
```java
package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

public class BuildInfo {

    String time;
    String builtFor;

    public BuildInfo() {
    }

    public BuildInfo(String buildTime, String builtFor) {
        this.time = buildTime;
        this.builtFor = builtFor;
    }

    // Getter and setter are omitted for readability. be sure to create them.
}
```
{{% /details %}}


### Task {{% param sectionnumber %}}.3 - Creating the Appinfo service

We created the POJOs holding our information. Now create the Service `AppinfoService.java` which collects this
information. You can use the following boilerplate code:

```java
// TODO: annotate class as singleton
public class AppinfoService {

    private static final Logger logger = LoggerFactory.getLogger(AppinfoService.class);

    private final Config config;
    private final String createTime;
    private String startupTime;

    public AppinfoService() {
        // TODO: 1. Register createTime
        // TODO: 2. Store access to Configuration in config
    }
    
    void onStart(@Observes StartupEvent ev) {
        logger.info("AppinfoService Startup: "+Instant.now());
        // TODO: Register startupTime
    }
    
    private BuildInfo getBuildTimeInfo() {
        // TODO: Programmatically access the CDI context to get the BuildInfo object
    }

    public Appinfo getAppinfo() {
        Appinfo ai = new Appinfo();
        // TODO: 1. Fill build information like buildTime, builtFor
        // TODO: 2. Fill runtime information like runBy, startupTime, createTime, currentTime, applicationName, applicationVersion
        // TODO: 3. Fill properties information
        return ai;
    }

    private <T> T getConfig(String propertyName, Class<T> propertyType) {
        return config.getValue(AppinfoNames.CONFIG_PREFIX+"."+propertyName, propertyType);
    }

    private String collectProperties() {
        StringBuilder sb = new StringBuilder();
        for (ConfigSource configSource : config.getConfigSources()) {
            sb.append(String.format("%n%s %s%n", "ConfigSource:", configSource.getName()));
            for (Map.Entry<String, String> property : configSource.getProperties().entrySet()) {
                sb.append(String.format("   %-40s %s%n", property.getKey(), property.getValue()));
            }
        }

        return sb.toString();
    }
}
```

{{% details title="Task hint" %}}

Accessing the complete configuration in a programmatic approach can be done with `ConfigProvider.getConfig()`.

Accessing objects in a programmatic CDI fashion is done with the following:
```java
CDI.current().select(BuildInfo.class).get();
```

You may also see code like the following which does the same as the CDI fashion but is more Quarkus specific:
```java
Arc.container().instance(BuildInfo.class).get();
```

Completing the TODOs in the `AppinfoService.java` the class should look like below.

```java
package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

import jakarta.inject.Singleton;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.CDI;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

@Singleton
public class AppinfoService {

    private static final Logger logger = LoggerFactory.getLogger(AppinfoService.class);

    private final Config config;
    private final String createTime;
    private String startupTime;

    public AppinfoService() {
        this.createTime = Instant.now().toString();
        this.config = ConfigProvider.getConfig();
    }

    void onStart(@Observes StartupEvent ev) {
        logger.info("AppinfoService Startup: "+Instant.now());
        this.startupTime = Instant.now().toString();
    }

    private BuildInfo getBuildTimeInfo() {
        return CDI.current().select(BuildInfo.class).get();
    }

    public Appinfo getAppinfo() {
        Appinfo ai = new Appinfo();

        ai.setBuildTime(this.getBuildTimeInfo().getTime());
        ai.setBuiltFor(this.getBuildTimeInfo().getBuiltFor());

        ai.setRunBy(getConfig("run-by", String.class));
        ai.setStartupTime(this.startupTime);
        ai.setCreateTime(this.createTime);
        ai.setCurrentTime(Instant.now().toString());
        ai.setApplicationName(config.getValue("quarkus.application.name", String.class));
        ai.setApplicationVersion(config.getValue("quarkus.application.version", String.class));
        ai.setPropertiesString(collectProperties());

        return ai;
    }

    private <T> T getConfig(String propertyName, Class<T> propertyType) {
        return config.getValue(AppinfoNames.CONFIG_PREFIX+"."+propertyName, propertyType);
    }

    private String collectProperties() {
        StringBuilder sb = new StringBuilder();
        for (ConfigSource configSource : config.getConfigSources()) {
            sb.append(String.format("%n%s %s%n", "ConfigSource:", configSource.getName()));
            for (Map.Entry<String, String> property : configSource.getProperties().entrySet()) {
                sb.append(String.format("   %-40s %s%n", property.getKey(), property.getValue()));
            }
        }

        return sb.toString();
    }
}
```
{{% /details %}}


### Task {{% param sectionnumber %}}.4 - Adding the undertow servlet

As we have our main functionality complete we can now create the undertow servlet class `AppinfoServlet.java`.

From the previous task you should know how to access the CDI context. Use the following template:

```java
// TODO: Annotate class as a web servlet
public class AppinfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // TODO: Access the AppinfoService and pass the human readable string to the response.        
    }

    AppinfoService getAppinfoService() {
        // TODO: Programmatically access the CDI context to get the AppinfoService object
    }
}
```

{{% details title="Task hint" %}}
Your class should look like this:
```java
package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet
public class AppinfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write(getAppinfoService().getAppinfo().asHumanReadableString());
    }

    AppinfoService getAppinfoService() {
        return CDI.current().select(AppinfoService.class).get();
    }
}
```
{{% /details %}}


[^1]: Undertow web server: https://undertow.io/
