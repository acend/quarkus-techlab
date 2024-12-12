package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = AppinfoNames.CONFIG_PREFIX)
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface AppinfoRunTimeConfig {

    /**
     * Simple runBy information string
     */
    String runBy();
}
