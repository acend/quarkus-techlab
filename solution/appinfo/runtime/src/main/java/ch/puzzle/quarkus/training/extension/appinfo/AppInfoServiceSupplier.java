package ch.puzzle.quarkus.training.extension.appinfo;

import javax.enterprise.inject.spi.CDI;
import java.util.function.Supplier;

public class AppInfoServiceSupplier implements Supplier<AppInfoService>  {

    @Override
    public AppInfoService get() {
        return CDI.current().select(AppInfoService.class).get();
    }
}
