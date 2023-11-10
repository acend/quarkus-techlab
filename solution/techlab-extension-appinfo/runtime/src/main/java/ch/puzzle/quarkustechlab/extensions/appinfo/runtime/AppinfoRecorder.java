package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class AppinfoRecorder {

    public RuntimeValue<BuildInfo> createBuildInfo(String time, String builtFor) {
        return new RuntimeValue<>(new BuildInfo(time, builtFor));
    }
}