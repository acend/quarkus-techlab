---
title: "2.1 Your first Quarkus application"
linkTitle: "2.1 Your first Quarkus application"
weight: 210
sectionnumber: 2.1
description: >
  This section covers creating a Quarkus application and the first steps.
---

## Create your first application

To create your first Quarkus application you have several possibilities:

* Create your application with maven
* Create your application with the Quickstart UI [code.quarkus.io](https://code.quarkus.io/)
* Use IntelliJ or eclipse plugins which will assist creating projects (these are usually also based on code.quarkus.io)


### Maven dependencies reference

{{< solutionref project="quarkus-getting-started" class="dependencies" >}}


### Task {{% param sectionnumber %}}.1: Create your application with maven

To create your application with maven you can execute the following maven
command:

```bash
mvn io.quarkus:quarkus-maven-plugin:{{% param "quarkusVersion" %}}:create \
    -DprojectGroupId=ch.puzzle \
    -DprojectArtifactId=quarkus-getting-started \
    -DclassName="ch.puzzle.quarkustechlab.GreetingResource" \
    -Dpath="hello"
```

Which creates a generated getting-started application bootstrapped for you. The
application holds at the moment a rest resource called `GreetingResource.java`
which exposes a REST resource for you.

{{% alert color="primary" %}}
Be aware that if you are creating your application using GitBash the shell actually transforms the Path `/hello` to something like `/C:/Program Files/Git/hello` (due to MinGWs Posix Path Conversion).

you can fix this if you specify the path as following:
`-Dpath="hello"` (remove slash)

Issue: <https://github.com/quarkusio/quarkus/issues/2149>
{{% /alert %}}


To test the application you can start the application in dev-mode by executing

```bash
./mvnw compile quarkus:dev
```

The command starts the application in dev-mode which means you do have active
live reloading on each API call. Try hitting the API and test the
`GreetingResource.java`:

```bash
curl http://localhost:8080/hello
```

You should get the `Hello from RESTEasy Reactive` response in your console. Other RESTEasy functionalities work like they always do.
For further information on basic REST interaction with Quarkus see [Documentation](https://quarkus.io/guides/rest-json).


### Task {{% param sectionnumber %}}.2: Exploring the dev mode

Now leave the dev mode running and experiment with the dev-ui. After each change, hit the API again and see how Quarkus
performs the live reload.

* Alter the response given in the `GreetingResource`
* Add a logger and a log statement to your `GreetingResource`
* Try changing the log level in the `application.properties` configuration file (`quarkus.log.level=DEBUG`)
* Point your browser to the Development UI at [http://localhost:8080/q/dev-ui](http://localhost:8080/q/dev). Explore the
provided information.
* Try to change the log level using the configuration editor in the development ui.
* Check the invocation trees from the ArC panel. You first have to enable this feature via `ARC-Panel`-> `Config-Editor` -> `quarkus.arc.dev-mode.monitoring-enabled` and rebuild the app. What happens if you invoke your `/hello` endpoint?
* When was the application started? (tip: check the fired events)
* Check the details about your runtime environment (environment variables and system properties) in the configuration
editor.
* Try overriding your configuration (e.g. log level) from command line (environment variable or system property)
* Try starting with a different profile activated


{{% details title="GreetingResource Hint" %}}
```java
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/hello")
public class GreetingResource {

    private static final Logger logger = LoggerFactory.getLogger(GreetingResource.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        logger.debug("Info log");
        return "Hello from RESTEasy Reactive";
    }
}
```
{{% /details %}}


## Exploring the quarkus byte code

In the introduction section we had a brief overview of how quarkus jars may look like. It's not expected that you have
to do things like this at a regular basis and to get all details is pretty hard. However, it may give you a feeling
where things came from and how they are wired up.


### Task {{% param sectionnumber %}}.2: Exploring the quarkus application deployment

To inspect the generated items proceed like this:

Generate a full package
```s
./mvnw clean package
```

Enter the target directory and extract the `generated-bytecode.jar`.
```s
cd target/quarkus-app/quarkus
jar -xvf generated-bytecode.jar
```

Now open the file `io/quarkus/runner/ApplicationImpl.class` in your IDE. You may find a static block which looks
something like this:

```java
// $FF: synthetic class
public class ApplicationImpl extends Application {

    /* removed for simplicity */

    static {
        DisabledInitialContextManager.register();
        System.setProperty("java.util.concurrent.ForkJoinPool.common.threadFactory", "io.quarkus.bootstrap.forkjoin.QuarkusForkJoinWorkerThreadFactory");
        System.setProperty("io.netty.allocator.maxOrder", "3");
        System.setProperty("logging.initial-configurator.min-level", "500");
        System.setProperty("io.netty.machineId", "80:90:28:d0:a0:e1:64:c8");
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        System.setProperty("io.quarkus.security.http.test-if-basic-auth-implicitly-required", "true");
        LaunchMode.set(LaunchMode.NORMAL);
        StepTiming.configureEnabled();
        ExecutionModeManager.staticInit();
        Timing.staticInitStarted(false);
        Config.ensureInitialized();
        LOG = Logger.getLogger("io.quarkus.application");
        StartupContext var0 = new StartupContext();
        STARTUP_CONTEXT = var0;

        try {
            StepTiming.configureStart();
            ((StartupTask)(new NativeImageConfigBuildStep.build282698227())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new VertxCoreProcessor.ioThreadDetector1463825589())).deploy(var0);
            StepTiming.printStepTime(var0);
            /* ... */
            ((StartupTask)(new ResteasyReactiveProcessor.serverSerializers1997124575())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new ResteasyReactiveProcessor.setupEndpoints615463616())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new ResteasyReactiveProcessor.setupDeployment713137389())).deploy(var0);
            StepTiming.printStepTime(var0);
            ((StartupTask)(new ResteasyReactiveProcessor.addDefaultAuthFailureHandler1048820038())).deploy(var0);
            StepTiming.printStepTime(var0);
        } catch (Throwable var2) {
            ApplicationStateNotification.notifyStartupFailed(var2);
            var0.close();
            throw (Throwable)(new RuntimeException("Failed to start quarkus", var2));
        }
    }
    /* ... */
}
```

Our application contains REST endpoints and uses different Serializers. You may find a line containing `((StartupTask)(new ResteasyReactiveProcessor.serverSerializers1997124575())).deploy(var0);` in the static block.
Where does this come from? Open the file `ResteasyReactiveProcessor$serverSerializers...` from the `io/quarkus/deployment/steps` folder. You may find the deploy code for the resteasy de-/serializers.
```java
// $FF: synthetic class
public class ResteasyReactiveProcessor$serverSerializers1997124575 implements StartupTask {

    /* removed for simplicity */

    public void deploy(StartupContext var1) {
        var1.setCurrentBuildStepName("ResteasyReactiveProcessor.serverSerializers");
        Object[] var2 = this.$quarkus$createArray();
        this.deploy_0(var1, var2);
    }

    public void deploy_0(StartupContext var1, Object[] var2) {
        ResteasyReactiveRecorder var4 = new ResteasyReactiveRecorder();
        ServerSerialisers var3 = var4.createServerSerialisers();
        // ...
    }
}
```

If you are wondering where the code from this deploy block comes from: Check the resteasy reactive quarkus extension code on Github [ResteasyReactiveProcessor](https://github.com/quarkusio/quarkus/blob/b7135d81d36fa9f713ca8aed4b482e08b0ac7f51/extensions/resteasy-reactive/quarkus-resteasy-reactive/deployment/src/main/java/io/quarkus/resteasy/reactive/server/deployment/ResteasyReactiveProcessor.java#L1025C69-L1025C69).

