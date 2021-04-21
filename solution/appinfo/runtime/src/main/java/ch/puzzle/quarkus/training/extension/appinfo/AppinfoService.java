package ch.puzzle.quarkus.training.extension.appinfo;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.Map;

@Singleton
public class AppinfoService {

    private static final Logger logger = LoggerFactory.getLogger(AppinfoService.class);

    private final Config config;
    private final String createTime;
    private String startupTime;

    public AppinfoService() {
        this.createTime = Instant.now().toString();
        this.config = ConfigProvider.getConfig();
    }

    void onStart(@Observes StartupEvent ev) {
        logger.info("AppInfoService Startup: "+Instant.now());
        this.startupTime = Instant.now().toString();
    }

    public Appinfo getAppInfo() {
        Appinfo ai = new Appinfo();

        ai.setBuildTime(this.getBuildTimeInfo().getTime());
        ai.setBuiltFor(this.getBuildTimeInfo().getBuiltFor());

        ai.setRunBy(getConfig("run-by", String.class));
        ai.setStartupTime(this.startupTime);
        ai.setCreateTime(this.createTime);
        ai.setCurrentTime(Instant.now().toString());
        ai.setApplicationName(config.getValue("quarkus.application.name", String.class));
        ai.setApplicationVersion(config.getValue("quarkus.application.version", String.class));
        ai.setPropertiesString(collectProperties());

        return ai;
    }

    private BuildInfo getBuildTimeInfo() {
        return CDI.current().select(BuildInfo.class).get();
    }

    private <T> T getConfig(String propertyName, Class<T> propertyType) {
        return config.getValue(AppinfoNames.CONFIG_PREFIX+"."+propertyName, propertyType);
    }

    private String collectProperties() {
        StringBuilder sb = new StringBuilder();
        for (ConfigSource configSource : config.getConfigSources()) {
            sb.append(String.format("%n%s %s%n", "ConfigSource:", configSource.getName()));
            for (Map.Entry<String, String> property : configSource.getProperties().entrySet()) {
                sb.append(String.format("   %-40s %s%n", property.getKey(), property.getValue()));
            }
        }

        return sb.toString();
    }
}
