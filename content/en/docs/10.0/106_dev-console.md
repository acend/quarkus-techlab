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

In this section we will expose static information and dynamic information about our config sources. After finishing the
section our Dev UI Card should look like this:

![Appinfo Extension Dev UI Card](../devui-card.png)

Further we create a dedicated configuration page to display various config sources and their values present in our application.

![Appinfo Extension Dev UI](../devui.png)

### Task {{% param sectionnumber %}}.1 - Static Information

Quarkus knows various ways to easily display information in the [Dev Ui Guide](https://quarkus.io/guides/dev-ui). 

We will create a simple Java POJO which extends the `SimpleBuildItem` in our deployment module to expose the static information. 

```java
package ch.puzzle.quarkustechlab.extensions.appinfo.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

public final class StaticMetadataBuildItem extends SimpleBuildItem {

    private final String quarkusVersion;
    private final String builtFor;
    private final boolean recordBuildTime;
    private final boolean alwaysInclude;
    private final String basePath;

    // Getters not shown here. Make sure you generate them.
    
    public StaticMetadataBuildItem(String quarkusVersion, String builtFor, boolean alwaysInclude, String basePath, boolean recordBuildTime) {
        this.quarkusVersion = quarkusVersion;
        this.builtFor = builtFor;
        this.alwaysInclude = alwaysInclude;
        this.basePath = basePath;
        this.recordBuildTime = recordBuildTime;
    }
}
```

This POJO needs to be filled by a `BuildStep` which is run at build time. Add the following `BuildStep` in your `TechlabExtensionAppinfoProcessor`
```java
    @BuildStep
    StaticMetadataBuildItem createStaticMetadata(AppinfoBuildTimeConfig appInfoBuildTimeConfig) {
            return new StaticMetadataBuildItem(Version.getVersion(),
            appInfoBuildTimeConfig.builtFor,
            appInfoBuildTimeConfig.alwaysInclude,
            appInfoBuildTimeConfig.basePath,
            appInfoBuildTimeConfig.recordBuildTime);
    }
```

To show information of this class we need to customize our Dev Ui Card. Create a package `devui` in your deployment module and create a class `AppinfoDevUiProcessor`.

```java
package ch.puzzle.quarkustechlab.extensions.appinfo.deployment.devui;

import ch.puzzle.quarkustechlab.extensions.appinfo.deployment.StaticMetadataBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;

public class AppinfoDevUiProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    CardPageBuildItem createAppInfoCard(HttpRootPathBuildItem rootPath, StaticMetadataBuildItem smd) {

        CardPageBuildItem cardPageBuildItem = new CardPageBuildItem();

        cardPageBuildItem.addPage(Page.rawDataPageBuilder("Custom BuildConfig Dump")
                .icon("font-awesome-solid:sitemap")
                .buildTimeDataKey("smd"));

        cardPageBuildItem.addPage(Page.externalPageBuilder("Appinfo Endpoint")
                .url(rootPath.resolvePath("appinfo"))
                .doNotEmbed()
                .icon("font-awesome-solid:link"));

        cardPageBuildItem.addPage(Page.externalPageBuilder("Write Your Own Extension Guide")
                .url("https://quarkus.io/guides/writing-extensions", "https://quarkus.io/guides/writing-extensions")
                .doNotEmbed()
                .icon("font-awesome-solid:book"));

        cardPageBuildItem.addBuildTimeData("smd", smd);


        return cardPageBuildItem;
    }
}
```

The `createAppInfoCard` class does the following:
* Registering our POJO as build time data
* Create a page in the dev ui showing the content of our POJO using a `Page.RawDataPageBuilder`
* Show a link to our Appinfo Servlet 
* Show an external Link to the quarkus guide for writing extensions

To test our dev ui integration you need to rebuild your extension
```s
mvn clean package install
```

Then make sure you restart the `quarkus-appinfo-application` application which includes your extension.

### Task {{% param sectionnumber %}}.2 - Dynamic Information

To show runtime data we use a JsonRPC which allows to simply fetch or stream data. 
We need two parts for this - the java part and then the usage in the web component.

Our java part will reside in the runtime module as a `ConfigSourceJsonRPCService`. This Service will query our configuration and return the details as a json response to the ui.

```java
package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

import io.smallrye.common.annotation.NonBlocking;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

@ApplicationScoped
public class ConfigSourceJsonRPCService {

    @Inject
    Config config;

    @NonBlocking
    public JsonArray getAll() {
        var array = new JsonArray();
        config.getConfigSources().forEach(cs -> {
            array.add(getJsonRepresentationForConfigSource(cs));
        });

        return array;
    }

    private JsonObject getJsonRepresentationForConfigSource(ConfigSource c) {
        var properties = new JsonArray();
        c.getProperties().forEach((prop, value) -> {
            properties.add(new JsonObject().put("key", prop).put("value", value));
        });

        return new JsonObject()
                .put("name", c.getName())
                .put("size", c.getProperties().size())
                .put("ordinal", c.getOrdinal())
                .put("properties", properties);
    }
}
```

Our JsonRPC Service needs to be registered as a BuildStep in our `AppinfoDevUiProcessor`.

```java
    @BuildStep(onlyIf = IsDevelopment.class)
    JsonRPCProvidersBuildItem createJsonRPCServiceForConfigSources() {
        return new JsonRPCProvidersBuildItem(ConfigSourceJsonRPCService.class);
    }
```

Now let's create the web component. Create a web component template `appinfo-config-sources.js` in your deployment module `src/main/resources/dev-ui`.

```javascript
import { QwcHotReloadElement, html, css} from 'qwc-hot-reload-element';
import { JsonRpc } from 'jsonrpc';

import '@vaadin/details';
import '@vaadin/horizontal-layout';
import 'echarts-gauge-grade';
import 'qui-badge';
import 'qwc-no-data';

import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';

/**
 * This component shows the Rest Easy Reactive Endpoint scores
 */
export class AppinfoConfigSources extends QwcHotReloadElement {
    jsonRpc = new JsonRpc(this);

    // Component style
    static styles = css`
        .heading{
            width: 100em;
            padding: 15px;
            background: var(--lumo-contrast-5pct);
            border-bottom: 1px solid var(--lumo-contrast-10pct);
        }
        .heading-details {
            display: flex;
            gap:10px;
            padding-top: 10px;
            font-size: 0.8em;
        }
        .property {
            width: 100em;
            text-align: left;
            padding-left: 20px;
            padding-right: 20px;
            color: var(--lumo-contrast-70pct);
        }
        .props {
            color: var(--lumo-primary-text-color);
        }
    `;

    // Component properties
    static properties = {
        _configsources: {state: true}
    };

    constructor() {
        super();
        this._configsources = null;
    }

    // Components callbacks

    /**
     * Called when displayed
     */
    connectedCallback() {
        super.connectedCallback();
        this.hotReload();
    }

    /**
     * Called when it needs to render the components
     * @returns {*}
     */
    render() {
        if(this._configsources) {
            return html`${this._configsources.map(cs=>{
                return html`${this._renderConfigSource(cs)}`;
            })}`;
        } else {
            return html`<span>Loading Config Sources...</span>`;
        }
    }

    // View / Templates
    _renderConfigSource(cs) {
        // let level = this._getLevel(cs.size);
        
        return html`
            <vaadin-details theme="reverse">
                <div class="heading" slot="summary">
                    <qui-badge level='success'><span>${cs.name}</span></qui-badge>
                    <div class="heading-details">
                        <code class="props">${cs.size} Properties</code> <code>Ordinal ${cs.ordinal}</code>
                    </div>
                </div>

                <div class="properties">
                    ${this._renderProperties(cs.properties)}
                </div>
            </vaadin-details>`;
    }

    _renderProperties(props){
        const propsTemplates = [];

        props.forEach(property => {
            propsTemplates.push(html`<div class="property">${property.key} = <qui-badge level='contrast'>${property.value}</qui-badge></div>`);
        })

        return html`${propsTemplates}`;
    }

    hotReload(){
        this._configsources = null;
        this.jsonRpc.getAll().then(response => {
            this._configsources = response.result;
        });
    }
}
customElements.define('appinfo-config-sources', AppinfoConfigSources);
```

### Task {{% param sectionnumber %}}.3 - Rebuild extension

Since you changed the extension code you have to rebuild the extension. Head over to the previous section
to find the instructions. You also have to restart the `appinfo-app` service to pickup the new dependency.

Now navigate to [localhost:8080/q/dev](http://localhost:8080/q/dev) to see the output.
