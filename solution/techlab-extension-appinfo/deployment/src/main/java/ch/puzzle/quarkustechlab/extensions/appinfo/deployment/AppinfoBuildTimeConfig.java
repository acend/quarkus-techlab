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