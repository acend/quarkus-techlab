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


### Task {{% param sectionnumber %}}.1 - Defining build time configuration

We will define our build-time configuration to contain the following properties:

* **builtFor:** Simple string
* **recordBuildTime:** Boolean whether the collection of information at build time should run or not
* **alwaysInclude:** Boolean if the extension should always be included or only for prod and test profile.
* **basePath:** Basepath of the extension endpoint.

Use the template below for creating the `AppinfoBuildTimeConfig.java` in your **deployment** module:

Things to complete

* Annotate the interface with the `@ConfigMapping` and `@ConfigRoot` annotation
  * Set correct prefix the `@ConfigMapping` (hint: refer to our class `AppinfoNames.java`)
  * Set correct and phase for the `@ConfigRoot` (hint: have a look at the `ConfigPhase` class)
* Define config items from the list above

```java
// TODO: annotate class
public interface AppinfoBuildTimeConfig {

    /**
     * Simple builtFor information string
     */
    // TODO: define builtFor as config item

    /**
     * Include build time collection feature in build
     * Default should be true
     */
    // TODO: define recordBuildTime as config item with reasonable default

    /**
     * Always include this. By default this will only be included in dev and test.
     * Setting this to true will also include this in Prod
     * Default should be false
     */
    // TODO: define alwaysInclude as config item with reasonable default

    /**
     * Specify basePath for extension endpoint
     * The default should be appinfo (hint: refer to the AppinfoNames.java class)
     */
    // TODO: define basePath
}
```

{{% details title="Task hint" %}}
The build-time configuration looks like this:

```java
package ch.puzzle.quarkustechlab.extensions.appinfo.deployment;

import ch.puzzle.quarkustechlab.extensions.appinfo.runtime.AppinfoNames;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = AppinfoNames.CONFIG_PREFIX)
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface AppinfoBuildTimeConfig {

  /**
   * Simple builtFor information string
   */
  String builtFor();

  /**
   * Include build time collection feature in build
   */
  @WithDefault("true")
  boolean recordBuildTime();

  /**
   * Always include this. By default this will only be included in dev and test.
   * Setting this to true will also include this in Prod
   */
  @WithDefault("false")
  boolean alwaysInclude();

  /**
   * Specify basePath for extension endpoint
   */
  @WithDefault(AppinfoNames.EXTENSION_NAME)
  String basePath();
}
```
{{% /details %}}


### Task {{% param sectionnumber %}}.2 - Defining run time configuration

Our extension uses the runtime configuration `quarkus.appinfo.run-by`. Therefore, we also define this configuration
in our runtime module. 

Use the template below for creating the `AppinfoRunTimeConfig.java` config interface in your **runtime** module. Do not
forget to annotate the interface with correct values for `@ConfigMapping` and `@ConfigRoot`.

```java
// TODO: annotate class
public interface AppinfoRunTimeConfig {

   /**
     * Simple runBy information string
     */
    // TODO: define runBy as config item
}
```


{{% details title="Task hint" %}}
The run-time configuration looks like this:

```java
package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = AppinfoNames.CONFIG_PREFIX)
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface AppinfoRunTimeConfig {

  /**
   * Simple runBy information string
   */
  String runBy();
}

```
{{% /details %}}
