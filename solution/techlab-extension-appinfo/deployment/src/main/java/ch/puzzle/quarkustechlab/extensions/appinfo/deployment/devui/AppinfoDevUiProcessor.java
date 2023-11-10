package ch.puzzle.quarkustechlab.extensions.appinfo.deployment.devui;

import ch.puzzle.quarkustechlab.extensions.appinfo.deployment.StaticMetadataBuildItem;
import ch.puzzle.quarkustechlab.extensions.appinfo.runtime.ConfigSourceJsonRPCService;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;

public class AppinfoDevUiProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    CardPageBuildItem createAppInfoCard(HttpRootPathBuildItem rootPath, StaticMetadataBuildItem smd) {

        CardPageBuildItem cardPageBuildItem = new CardPageBuildItem();

        // Show our StaticMetadataBuildItem
        cardPageBuildItem.addPage(Page.rawDataPageBuilder("Custom BuildConfig Dump")
                .icon("font-awesome-solid:sitemap")
                .buildTimeDataKey("smd"));

        // Show Configuration Page
        cardPageBuildItem.addPage(Page.webComponentPageBuilder()
                .title("Configuration Sources")
                .componentLink("appinfo-config-sources.js")
                .icon("font-awesome-solid:gears"));

        // Link to the Appinfo Endpoint
        cardPageBuildItem.addPage(Page.externalPageBuilder("Appinfo Endpoint")
                .url(rootPath.resolvePath("appinfo"))
                .doNotEmbed()
                .icon("font-awesome-solid:link"));

        // Link to Extension Guide
        cardPageBuildItem.addPage(Page.externalPageBuilder("Write Your Own Extension Guide")
                .url("https://quarkus.io/guides/writing-extensions", "https://quarkus.io/guides/writing-extensions")
                .doNotEmbed()
                .icon("font-awesome-solid:book"));

        // Add the StaticMetadataBuildItem to our Dev UI Card
        cardPageBuildItem.addBuildTimeData("smd", smd);

        return cardPageBuildItem;
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    JsonRPCProvidersBuildItem createJsonRPCServiceForConfigSources() {
        return new JsonRPCProvidersBuildItem(ConfigSourceJsonRPCService.class);
    }
}