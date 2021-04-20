package ch.puzzle.appinfo.extension.deployment.devconsole;

import ch.puzzle.quarkus.training.extension.appinfo.AppInfoServiceSupplier;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devconsole.spi.DevConsoleRuntimeTemplateInfoBuildItem;

public class DevConsoleProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    public DevConsoleRuntimeTemplateInfoBuildItem collectBeanInfo() {
        return new DevConsoleRuntimeTemplateInfoBuildItem("data", new AppInfoServiceSupplier());
    }
}
