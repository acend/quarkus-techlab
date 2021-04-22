package ch.puzzle.quarkustechlab.appinfo;

import javax.enterprise.inject.spi.CDI;
import java.util.function.Supplier;

public class AppinfoServiceSupplier implements Supplier<AppinfoService>  {

    @Override
    public AppinfoService get() {
        return CDI.current().select(AppinfoService.class).get();
    }
}
