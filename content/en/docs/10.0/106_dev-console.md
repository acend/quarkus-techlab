---
title: "10.6. Development UI"
weight: 1060
sectionnumber: 10.6
description: >
  Providing extension information for the Dev UI.
---

Our current extension only provides a basic set of information for the dev ui. It shows the information from the
extension descriptor `quarkus-extension.yaml`

![Basic Dev UI Information](../extension-devui-raw.png)


## Development UI Integration

We will now provide more information for the Quarkus Dev UI. For this we need to create a `DevConsoleProcessor` and the
templates for the Dev UI.


### Task {{% param sectionnumber %}}.1 - Creating the processor for the dev console

For the integration in the Dev UI we create a new `DevConsoleProcessor` in our **deployment** module.

* Create a new package `ch.puzzle.quarkustechlab.appinfo.deployment.devconsole`
* Create the `DevConsoleProcessor` in this package

The `DevConsoleProcessor` needs to provide an object which can be used from within the templates of our Dev UI.

Use the following snippet:

```java
public class DevConsoleProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    public DevConsoleRuntimeTemplateInfoBuildItem getAppinfoService() {
        return new DevConsoleRuntimeTemplateInfoBuildItem("data", new AppinfoServiceSupplier());
    }
}
```

As you see the DevConsoleProcessor creates a template item named `data` which refers a Supplier. This supplier needs to
provide the `AppinfoService`. Create the following `AppinfoServiceSupplier` in your **runtime** module.

```java
public class AppinfoServiceSupplier implements Supplier</* TODO: Class */>  {

    @Override
    public /* TODO: Class */ get() {
        // programmatically access CDI and return the desired class
    }
}
```

{{% details title="Task hint" %}}
Your code should look like this:

```java
public class AppinfoServiceSupplier implements Supplier<AppinfoService>  {

    @Override
    public AppinfoService get() {
        return CDI.current().select(AppinfoService.class).get();
    }
}
```
{{% /details %}}


### Task {{% param sectionnumber %}}.2 - Creating the Dev UI Template

Now we need to create our rendered template. Templates are located in your **deployment** module at
`src/main/resources/dev-templates`.

* Create the `src/main/resources/dev-templates` folder
* Create the file `embedded.html` in this folder

This file will be interpreted by the Qute template engine. Using the [Qute reference guide](https://quarkus.io/guides/qute-reference)
try to create a template that looks like this (The endpoint information should link to the actual extension endpoint):

![Appinfo Extension Dev UI](../extension-devui-integration.png)

You can start with the following snippet:
```html
<span class="badge badge-light">
    <i class="fa fa-cogs fa-fw"></i> BUILD_TIME_TEXT <span class="badge badge-light">{info:OBJECT_PATH}</span>
</span>
<br />
<span class="badge badge-light">
    <i class="fa fa-clock fa-fw"></i> START_TIME_TEXT <span class="badge badge-light">{info:OBJECT_PATH}</span>
</span>
```

For more information about the engine itself have a look at the [Qute Templating Engine](https://quarkus.io/guides/qute).

{{% details title="Starting hints" %}}

* `BUILD_TIME_TEXT` is just the human readable Information like "Built Time"
* `OBJECT_PATH` is the navigation to your information. As we used `data` in our `DevConsoleRuntimeTemplateInfoBuildItem`
which refers to the `AppinfoService`. Your may navigate to the information with `data.appinfo.XY`
* Quarkus properties can be accessed directly from a template using `{config:property('PROPERTY')}`
{{% /details %}}

{{% details title="Full solution hint" %}}
```html
<span class="badge badge-light">
    <i class="fa fa-cogs fa-fw"></i> Build Time <span class="badge badge-light">{info:data.appinfo.buildTime}</span>
</span>
<br />
<span class="badge badge-light">
    <i class="fa fa-clock fa-fw"></i> Start Time <span class="badge badge-light">{info:data.appinfo.startupTime}</span>
</span>
<br />
<span class="badge badge-light">
    <i class="fa fa-compass fa-fw"></i> Built for <span class="badge badge-light">{info:data.appinfo.builtFor} </span>
</span>
<span class="badge badge-light">
    <i class="fa fa-child fa-fw"></i> Run by <span class="badge badge-light">{info:data.appinfo.runBy} </span>
</span>
<br />
<br />
<a href="/{config:property('quarkus.appinfo.base-path')}" class="badge badge-light">
    <i class="fa fa-map-signs fa-fw"></i> Endpoint <span class="badge badge-light">{config:property('quarkus.appinfo.base-path')}</span>
</a>
```
{{% /details %}}


### Task {{% param sectionnumber %}}.3 - Rebuild extension

Since you changed the extension code you have to rebuild the extension. Head over to the previous section
to find the instructions. You also have to restart the `appinfo-app` service to pickup the new dependency.

Now navigate to [localhost:8080/q/dev](http://localhost:8080/q/dev) to see the output.
