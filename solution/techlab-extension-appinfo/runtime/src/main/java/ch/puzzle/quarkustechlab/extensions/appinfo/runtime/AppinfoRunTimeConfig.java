package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = AppinfoNames.EXTENSION_NAME, phase = ConfigPhase.RUN_TIME)
public class AppinfoRunTimeConfig {

    /**
     * Simple runBy information string
     */
    @ConfigItem
    String runBy;
}
