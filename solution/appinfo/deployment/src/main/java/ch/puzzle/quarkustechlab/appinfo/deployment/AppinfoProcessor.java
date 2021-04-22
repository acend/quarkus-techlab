package ch.puzzle.quarkustechlab.appinfo.deployment;

import ch.puzzle.quarkustechlab.appinfo.*;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.undertow.deployment.ServletBuildItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.Instant;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

class AppinfoProcessor {

    private static Logger logger = LoggerFactory.getLogger(AppinfoProcessor.class);

    private static final String FEATURE = AppinfoNames.EXTENSION_NAME;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(STATIC_INIT)
    void syntheticBean(AppinfoConfig appinfoConfig,
                       LaunchModeBuildItem launchMode,
                       AppinfoRecorder recorder,
                       BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

        if(shouldInclude(launchMode, appinfoConfig)) {
            String buildTime = appinfoConfig.recordBuildTime ? Instant.now().toString() : null;
            String builtFor = appinfoConfig.builtFor;

            logger.info("Adding BuildInfo. RecordBuildTime={}, BuiltFor={}", appinfoConfig.recordBuildTime, builtFor);

            syntheticBeans.produce(SyntheticBeanBuildItem.configure(BuildInfo.class).scope(Singleton.class)
                    .runtimeValue(recorder.createBuildInfo(buildTime, builtFor))
                    .unremovable()
                    .done());
        }
    }

    @BuildStep
    void registerAdditionalBeans(AppinfoConfig appinfoConfig,
                                 LaunchModeBuildItem launchMode,
                                 BuildProducer<AdditionalBeanBuildItem> additionalBean) {

        if(shouldInclude(launchMode, appinfoConfig)) {
            logger.info("Adding AppinfoService");
            // Add AppInfoService as AdditionalBean - else it is not available at runtime.
            additionalBean.produce(AdditionalBeanBuildItem.builder()
                    .setUnremovable()
                    .addBeanClass(AppinfoService.class)
                    .build());
        }
    }

    @BuildStep
    void createServlet(LaunchModeBuildItem launchMode,
                       AppinfoConfig appinfoConfig,
                       BuildProducer<ServletBuildItem> additionalBean) {

        if(shouldInclude(launchMode, appinfoConfig)) {
            String basePath = appinfoConfig.basePath;
            if(basePath.startsWith("/")) {
                basePath = basePath.replaceFirst("/", "");
            }

            logger.info("Adding AppinfoServlet /{}", basePath);

            additionalBean.produce(ServletBuildItem.builder(basePath, AppinfoServlet.class.getName())
                    .addMapping("/"+basePath)
                    .build());
        }
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, AppinfoConfig appinfoConfig) {
        return launchMode.getLaunchMode().isDevOrTest() || appinfoConfig.alwaysInclude;
    }
}
