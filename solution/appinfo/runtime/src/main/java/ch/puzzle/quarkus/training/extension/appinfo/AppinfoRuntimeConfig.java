package ch.puzzle.quarkus.training.extension.appinfo;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = AppinfoNames.EXTENSION_NAME, phase = ConfigPhase.RUN_TIME)
public class AppinfoRuntimeConfig {

    /**
     * Simple build information string
     */
    @ConfigItem
    String runBy;
}
