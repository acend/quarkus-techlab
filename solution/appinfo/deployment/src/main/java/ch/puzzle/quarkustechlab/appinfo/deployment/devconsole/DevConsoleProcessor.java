package ch.puzzle.quarkustechlab.appinfo.deployment.devconsole;

import ch.puzzle.quarkustechlab.appinfo.AppinfoServiceSupplier;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devconsole.spi.DevConsoleRuntimeTemplateInfoBuildItem;

public class DevConsoleProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    public DevConsoleRuntimeTemplateInfoBuildItem getAppinfoService() {
        return new DevConsoleRuntimeTemplateInfoBuildItem("data", new AppinfoServiceSupplier());
    }
}
