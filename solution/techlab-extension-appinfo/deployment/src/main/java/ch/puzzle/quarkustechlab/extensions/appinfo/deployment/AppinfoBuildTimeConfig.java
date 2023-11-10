package ch.puzzle.quarkustechlab.extensions.appinfo.deployment;

import ch.puzzle.quarkustechlab.extensions.appinfo.runtime.AppinfoNames;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = AppinfoNames.EXTENSION_NAME, phase = ConfigPhase.BUILD_TIME)
public class AppinfoBuildTimeConfig {

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

    /**
     * Specify basePath for extension endpoint
     */
    @ConfigItem(defaultValue = AppinfoNames.EXTENSION_NAME)
    String basePath;
}