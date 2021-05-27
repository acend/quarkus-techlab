---
title: "10.3. Configuration"
weight: 1030
sectionnumber: 10.3
description: >
  Define extension configuration
---

In this section we will create our extension configuration specification.


## Configuration

Quarkus uses different configuration phases. The most important are:

Phase                         | Description
------------------------------|--------------------------------------------
`BUILD_TIME`                  | Only available at build time
`BUILD_AND_RUN_TIME_FIXED`    | Read at build time and exposed but not changeable at run time.
`RUN_TIME`                    | Available at run time

For more details have a look at [Configuration Root Phases](https://quarkus.io/guides/writing-extensions#configuration-root-phases)

An extension named `appinfo` will have the configuration prefix `quarkus.appinfo`.


### Task {{% param sectionnumber %}}.1 - Defining built time configuration

We will define our build-time configuration to contain the following properties:

* **builtFor:** Simple string
* **recordBuildTime:** Boolean whether the collection of information at build time should run or not
* **alwaysInclude:** Boolean if the extension should always be included or only for prod and test profile.

Use the template below for creating the `AppinfoConfig.java` in your **deployment** module:

Things to complete

* Annotate the class with the `@ConfigRoot` annotation
  * Set the correct value for name (hint: extension name)
  * Set correct and phase (hint: have a look at the `ConfigPhase` class)
* Define config items from the list above

```java
// TODO: annotate class
public class AppinfoConfig {

    /**
     * Simple builtFor information string
     */
    // TODO: define builtFor as config item

    /**
     * Include build time collection feature in build
     */
    // TODO: define recordBuildTime as config item with reasonable default

    /**
     * Always include this. By default this will only be included in dev and test.
     * Setting this to true will also include this in Prod
     */
    // TODO: define alwaysInclude as config item with reasonable default
}
```

{{% details title="Task hint" %}}
The built-time configuration looks like this:

```java
@ConfigRoot(name = AppinfoNames.EXTENSION_NAME, phase = ConfigPhase.BUILD_TIME)
public class AppinfoConfig {

    /**
     * Simple builtFor information string
     */
    @ConfigItem
    String builtFor;

    /**
     * Include build time collection feature in build
     */
    @ConfigItem(defaultValue = "true")
    boolean recordBuildTime;

    /**
     * Always include this. By default this will only be included in dev and test.
     * Setting this to true will also include this in Prod
     */
    @ConfigItem(defaultValue = "false")
    boolean alwaysInclude;
}
```
{{% /details %}}


### Task {{% param sectionnumber %}}.2 - Defining run time configuration

Our extension uses the runtime configuration `quarkus.appinfo.run-by`. Therefore, we also define this configuration
in our runtime module. If we do not specify this config as part of the extension Quarkus will run but complain about
unrecognized configuration keys.

Use the template below for creating the `AppinfoRuntimeConfig.java` in your **runtime** module.

```java
// TODO: annotate class
public class AppinfoRuntimeConfig {

   /**
     * Simple runBy information string
     */
    // TODO: define runBy as config item
}
```


{{% details title="Task hint" %}}
The run-time configuration looks like this:

```java
@ConfigRoot(name = AppinfoNames.EXTENSION_NAME, phase = ConfigPhase.RUN_TIME)
public class AppinfoRuntimeConfig {

    /**
     * Simple runBy information string
     */
    @ConfigItem
    String runBy;
}
```
{{% /details %}}
