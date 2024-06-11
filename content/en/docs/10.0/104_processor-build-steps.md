---
title: "10.4. Build Steps"
weight: 1040
sectionnumber: 10.4
description: >
  Complete our build processor with build steps.
---

In the previous section we created our extension configuration. Earlier we have written our runtime code which accesses
the `BuildInfo` object. However, at this time this information has not yet been collected.

In this section we will complete our `TechlabExtensionAppinfoProcessor`:

* Collect the information `buildTime`
* Read and store the `builtFor` configuration value
* Create our BuildInfo object
* Conditionally disable the collection of the values above
* Write build steps for our undertow web servlet
* Instruct Quarkus ArC to include an additional bean

{{% alert title="Code Location" color="warning" %}}
If not stated otherwise, all classes in this section belong to `deployment/src/main/java/ch/puzzle/quarkustechlab/extensions/appinfo/deployment/`.
There should already be the generated `TechlabExtensionAppinfoProcessor` class in this folder.
{{% /alert %}}


## Bootstrapping a Quarkus application

In the lab introduction we have seen the three phases of bootstrap.

For detailed information have a look at [Three Phases of Bootstrap and Quarkus Philosophy](https://quarkus.io/guides/writing-extensions#bootstrap-three-phases)


### Augmentation

The work in this phase is done with build steps handled by the Build Step Processors (`TechlabExtensionAppinfoProcessor`).

A build step can produce and consume build items and may therefore depend on each other. Think about a build step which
is consuming a build item produced by an earlier stage. For example build steps are able to access the Jandex annotation
information and search for specific annotations in code and act accordingly.  


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

Generally you should move as much code as possible to the `STATIC_INIT` phase as this speeds up the boot time in your
native image.


### Task {{% param sectionnumber %}}.3 - Collection Build Information

In this task we create a build step which reads the build configuration and collects the build time. This information
will be recorded as BuildInfo which is then available at runtime.

For registering the creation of the built information we create a recorder which records the invocation and returns a
BeanInfo object. At the deployment type the invocations are made on the recorder object. These contain the runtime logic
but the invocations do not get executed. They are recorded and they generate bytecode that performs the same actions of
invocations at runtime. You may understand this as deferred execution (recorded at deployment time but invoked at runtime).

The Recorder class belongs to our runtime module. Create the class `AppinfoRecorder` in the **runtime** module. Use the
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
package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class AppinfoRecorder {

    public RuntimeValue<BuildInfo> createBuildInfo(String time, String builtFor) {
        return new RuntimeValue<>(new BuildInfo(time, builtFor));
    }
}
```
{{% /details %}}

Now we open the `TechlabExtensionAppinfoProcessor` class and add a build step which invokes the recorder to actually record our build
values. Remember that we want to conditionally include or exclude the recording of the build information.

The build step does the following:

* Produces a `SyntheticBeanBuildItem` using a `BuildProducer`
* Consumes (hint: these are the method arguments)
  * `AppinfoConfig` for accessing the build time config properties
  * The `LaunchModeBuildItem` which contains the mode quarkus is started.
  * The `AppinfoRecorder` to record the invocation
  * A `BuildProducer<SyntheticBeanBuildItem>` object
* Is a `STATIC_INIT` recording

For a complete list of all Build items have a look at [All BuildItems](https://quarkus.io/guides/all-builditems)

Use this template as starting point:

```java
private static final Logger logger = LoggerFactory.getLogger(TechlabExtensionAppinfoProcessor.class);

// TODO: annotations for build step and record
void syntheticBean(/* TODO: method arguments */) {

    // depending on launchMode and alwaysInclude property: should the functionality be ignored?
    if(shouldInclude(launchMode, appinfoConfig)) {
        // collect build time?
        String buildTime = appinfoConfig.recordBuildTime ? Instant.now().toString() : null;
        String builtFor = appinfoConfig.builtFor;

        logger.info("Adding BuildInfo. RecordBuildTime={}, BuiltFor={}", appinfoConfig.recordBuildTime, builtFor);

        syntheticBeans.produce(SyntheticBeanBuildItem.configure(/* TODO: destination class */).scope(Singleton.class)
                .runtimeValue(/* TODO: call the recorder */)
                .unremovable()
                .done());
    }
}

/**
 * Conditionally include functionality based on configuration property
 */ 
private static boolean shouldInclude(LaunchModeBuildItem launchMode, AppinfoBuildTimeConfig appinfoConfig) {
    // TODO: return true if launchMode is dev or test or the appinfoConfig.alwaysInclude is true
}
```

{{% details title="Task hint" %}}
Your code may look like this:

```java
package ch.puzzle.quarkustechlab.extensions.appinfo.deployment;

import ch.puzzle.quarkustechlab.extensions.appinfo.runtime.AppinfoRecorder;
import ch.puzzle.quarkustechlab.extensions.appinfo.runtime.BuildInfo;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

class TechlabExtensionAppinfoProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TechlabExtensionAppinfoProcessor.class);

    private static final String FEATURE = "techlab-extension-appinfo";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(STATIC_INIT)
    void syntheticBean(AppinfoBuildTimeConfig appinfoConfig,
                       LaunchModeBuildItem launchMode,
                       AppinfoRecorder recorder,
                       BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

        if(shouldInclude(launchMode, appinfoConfig)) {
            String buildTime = appinfoConfig.recordBuildTime ? Instant.now().toString() : null;
            String builtFor = appinfoConfig.builtFor;

            logger.info("Adding BuildInfo. RecordBuildTime={}, BuiltFor={}", appinfoConfig.recordBuildTime, builtFor);

            syntheticBeans.produce(SyntheticBeanBuildItem.configure(BuildInfo.class).scope(Singleton.class)
                    .runtimeValue(recorder.createBuildInfo(buildTime, builtFor))
                    .unremovable()
                    .done());
        }
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, AppinfoBuildTimeConfig appinfoConfig) {
        return launchMode.getLaunchMode().isDevOrTest() || appinfoConfig.alwaysInclude;
    }
}
```
{{% /details %}}


## Undertow Servlet

Our extension provides an undertow servlet as an endpoint. We have already written the servlet code `AppinfoServlet`  
in a previous section. Quarkus provides a `ServletBuildItem` which will be used to add our undertow servlet.


### Task {{% param sectionnumber %}}.1 - Undertow BuildStep

Open the `TechlabExtensionAppinfoProcessor` and create a new BuildStep which produces a `ServletBuildItem`. As the collection of the build
information this step should also use the `shouldInclude` method to conditionally exclude it.

Some details what the implementation should do

* Produce a `ServletBuildItem` (hint: `ServletBuildItem.builder(...)`)
* BasePath should be configurable (hint: you have to add a new build-config `basePath`)
* Should use the `shouldInclude` method to conditionally exclude it

{{% details title="Task hint" %}}
The additional build configuration in `AppinfoBuildTimeConfig` looks like this:
```java
/**
 * Specify basePath for extension endpoint
 */
@ConfigItem(defaultValue = AppinfoNames.EXTENSION_NAME)
String basePath;
```

The code in the `TechlabExtensionAppinfoProcessor` should look like this:

```java
@BuildStep
void createServlet(LaunchModeBuildItem launchMode,
                   AppinfoBuildTimeConfig appinfoConfig,
                   BuildProducer<ServletBuildItem> additionalBean) {

    if(shouldInclude(launchMode, appinfoConfig)) {
        String basePath = appinfoConfig.basePath;
        if(basePath.startsWith("/")) {
            basePath = basePath.replaceFirst("/", "");
        }

        logger.info("Adding AppinfoServlet /{}", basePath);

        additionalBean.produce(ServletBuildItem.builder(basePath, AppinfoServlet.class.getName())
                .addMapping("/"+basePath)
                .build());
    }
}
```
{{% /details %}}


### Task {{% param sectionnumber %}}.2 - Additional Beans

Beside the `AppinfoServlet` our extension also depends on the `AppinfoService`. We have to instruct the build processor
to analyze and register this additional class. This ensures that the `AppinfoService` will be available for the Quarkus
container.

The `AdditionalBeanBuildItem` is a build item from Quarkus ArC (the Quarkus Dependency Injection). For a complete list of
build items have a look at [All BuildItems](https://quarkus.io/guides/all-builditems).

Use this build step below to add the additional bean:

```java
@BuildStep
void registerAdditionalBeans(AppinfoBuildTimeConfig appinfoConfig,
                             LaunchModeBuildItem launchMode,
                             BuildProducer<AdditionalBeanBuildItem> additionalBean) {

    if(shouldInclude(launchMode, appinfoConfig)) {
        logger.info("Adding AppinfoService");
        // Add AppinfoService as AdditionalBean - else it is not available at runtime.
        additionalBean.produce(AdditionalBeanBuildItem.builder()
                .setUnremovable()
                .addBeanClass(AppinfoService.class)
                .build());
    }
}
```
