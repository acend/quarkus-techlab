---
title: "9.3. Collect BuildInfo"
weight: 930
sectionnumber: 9.3
description: >
  Collection the information for our BuildInfo object
---

In the previous section you accessed the `BuildInfo` object. However, at this time this information is not yet collected.

In this section we will:

* Define the build-time configuration
* Collect the information `buildTime`
* Read and store the `builtFor` configuration value
* Create our BuildInfo object
* Conditionally disable the collection of the values above

{{% alert title="Code Location" color="warning" %}}
If not stated otherwise, all classes in this section belong to `deployment/src/main/java/ch/puzzle/quarkustechlab/appinfo/deployment/`.
There should already be the generated `AppinfoProcessor` class in this folder.
{{% /alert %}}


## Configuration

Quarkus uses different configuration phases. The most important are:

Phase                         | Description
------------------------------|--------------------------------------------
`BUILD_TIME`                  | Only available at build time
`BUILD_AND_RUN_TIME_FIXED`    | Read at build time and exposed but not changeable at run time.
`RUN_TIME`                    | Available at run time

For more details have a look at [Configuration Root Phases](https://quarkus.io/guides/writing-extensions#configuration-root-phases)


### Task {{% param sectionnumber %}}.1 - Defining configuration

We will define our built-time configuration to contain the following properties:

* **builtFor:** Simple string
* **recordBuildTime:** Boolean whether the collection of information at build time should run or not
* **alwaysInclude:** Boolean if the extension should always be included or only for prod and test profile.

Use the template below for creating the `AppinfoConfig.java` and complete the TODOs:

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


## Bootstrapping a Quarkus application

In the lab introduction we have seen the three phases of bootstrap.

For detailed information have a look at [Three Phases of Bootstrap and Quarkus Philosophy](https://quarkus.io/guides/writing-extensions#bootstrap-three-phases)


### Augmentation

The work in this phase is done with build steps handled by the Build Step Processors (`AppinfoProcessor`).

A build step can produce and consume build items and may therefore depend on each other. Think about a build step which
is consuming a build item produced by an earlier stage. For example build steps are able to access the Jandex annotation
information and search for specific annotations in code and act accordingly.

The output of these build steps is recorded bytecode `@Record(STATIC_INIT)` or `@Record(RUNTIME_INIT)`.


### Static Init

Bytecode recorded with `@Record(STATIC_INIT)` will be executed from a static init method on the main class. In a native
build the code produced by these steps is directly serialized into the native executable. In the JVM mode the code from
static init is run before runtime init but there is not much difference. There are some limitations with this approach.
For example you may not be able to listen on ports or start threads in this phase.

{{% alert color="primary" %}} In servers like WildFly, deployment related classes such as XML parsers hang around for the life of the application, using up valuable memory. Quarkus aims to eliminate this, so that the only classes loaded at runtime are actually used at runtime.

As an example, the only reason that a Quarkus application should load an XML parser is if the user is using XML in their application. Any XML parsing of configuration should be done in the Augmentation phase. - [quarkus.io](https://quarkus.io/guides/writing-extensions#bootstrap-three-phases) {{% /alert %}}


### Runtime Init

Bytecode recorded with `@Record(RUNTIME_INIT)` is run from the applications main method. In a native executable this
code will run at boot. This is for example a good phase for code which needs to open ports.

Generally you should move as much code as possible to the STATIC_INIT phase as this speeds up the boot time in your
native image.


### Task {{% param sectionnumber %}}.2 - Collection Build Information

In this task we create a build step which reads the build configuration and collects the build time. This information
will be recorded as BuildInfo which is then available at runtime.

For registering the creation of the built information we create a recorder which records the invocation and returns a
BeanInfo object. At the deployment type the invocations are made on the recorder object. These contain the runtime logic
but the invocations do not get executed. They are recorded and they generate bytecode that performs the same actions of
invocations at runtime. You may understand this as deferred execution (recorded at deployment time but invoked at runtime).

The Recorder class belongs to our runtime module. Create the class `AppinfoRecorder` in the runtime module. Use the
template below for your class.

The recorder:

* Records a call with `time` and the `builtFor` strings.
* Returns a `RuntimeValue<BuildInfo>`
* do not forget to annotate the recorder

```java
// TODO: annotate class as a recorder
public class AppinfoRecorder {

    public RuntimeValue<BuildInfo> createBuildInfo(String time, String builtFor) {
        // TODO: return correct runtime value with the time and builtFor set.
    }
}
```


{{% details title="Task hint" %}}
The recorder looks like this:

```java
@Recorder
public class AppinfoRecorder {

    public RuntimeValue<BuildInfo> createBuildInfo(String time, String builtFor) {
        return new RuntimeValue<>(new BuildInfo(time, builtFor));
    }
}
```
{{% /details %}}

Now we open the `AppinfoProcessor` class and add a build step which invokes the recorder to actually record our build
values. Remember that we want to conditionally include or exclude the recording of the build information.

The build step does the following:

* Produces a `SyntheticBeanBuildItem` using a `BuildProducer`
* Consumes (hint: these are the method arguments)
  * `AppinfoConfig` for accessing the build time config properties
  * The `LaunchModeBuildItem` which contains the mode quarkus is started.
  * The `AppinfoRecorder` to record the invocation
  * A `BuildProducer<SyntheticBeanBuildItem>` object
* Is a `STATIC_INIT` recording
* Do not forget to annotate the `BuildStep`

Use this template as starting point:

```java
// TODO: annotations for build step and record
void syntheticBean(/* TODO: method arguments */) {

    // depending on launchMode and alwaysInclude property: should the functionality be ignored?
    if(shouldInclude(launchMode, appinfoConfig)) {
        // collect build time?
        String buildTime = appinfoConfig.recordBuildTime ? Instant.now().toString() : null;
        String builtFor = appinfoConfig.builtFor;

        logger.info("Adding BuildInfo. RecordBuildTime="+appinfoConfig.recordBuildTime+", BuiltFor="+builtFor);

        syntheticBeans.produce(SyntheticBeanBuildItem.configure(/* TODO: destination class */).scope(Singleton.class)
                .runtimeValue(/* TODO: call the recorder */)
                .unremovable()
                .done());
    }
}

/**
 * Conditionally include functionality based on configuration property
 */ 
private static boolean shouldInclude(LaunchModeBuildItem launchMode, AppinfoConfig appinfoConfig) {
    // TODO: return true if launchMode is dev or test or the appinfoConfig.alwaysInclude is true
}
```

{{% details title="Task hint" %}}
Your code may look like this:

```java
@BuildStep
@Record(STATIC_INIT)
void syntheticBean(AppinfoConfig appInfoConfig,
                   LaunchModeBuildItem launchMode,
                   AppinfoRecorder recorder,
                   BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

    if(shouldInclude(launchMode, appInfoConfig)) {
        String buildTime = appInfoConfig.recordBuildTime ? Instant.now().toString() : null;
        String builtFor = appInfoConfig.builtFor;

        logger.info("Adding BuildInfo. RecordBuildTime="+appInfoConfig.recordBuildTime+", BuiltFor="+builtFor);

        syntheticBeans.produce(SyntheticBeanBuildItem.configure(BuildInfo.class).scope(Singleton.class)
                .runtimeValue(recorder.createBuildInfo(buildTime, builtFor))
                .unremovable()
                .done());
    }
}

private static boolean shouldInclude(LaunchModeBuildItem launchMode, AppinfoConfig appInfoConfig) {
    return launchMode.getLaunchMode().isDevOrTest() || appInfoConfig.alwaysInclude;
}
```
{{% /details %}}
