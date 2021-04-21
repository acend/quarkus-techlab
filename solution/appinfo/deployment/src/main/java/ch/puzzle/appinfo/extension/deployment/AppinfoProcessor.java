package ch.puzzle.appinfo.extension.deployment;

import ch.puzzle.quarkus.training.extension.appinfo.*;

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
    void syntheticBean(AppinfoConfig appInfoConfig,
                       LaunchModeBuildItem launchMode,
                       AppinfoRecorder recorder,
                       BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

        if(shouldInclude(launchMode, appInfoConfig)) {
            String buildTime = appInfoConfig.recordBuildTime ? Instant.now().toString() : null;
            String builtFor = appInfoConfig.builtFor;

            logger.info("Adding BuildInfo. RecordBuildTime="+appInfoConfig.recordBuildTime+", BuiltFor="+builtFor);

            syntheticBeans.produce(SyntheticBeanBuildItem.configure(BuildInfo.class).scope(Singleton.class)
                    .runtimeValue(recorder.createBuildInfo(buildTime, builtFor))
                    .unremovable()
                    .done());
        }
    }

    @BuildStep
    void registerAdditionalBeans(AppinfoConfig appInfoConfig,
                                 LaunchModeBuildItem launchMode,
                                 BuildProducer<AdditionalBeanBuildItem> additionalBean) {

        if(shouldInclude(launchMode, appInfoConfig)) {
            logger.info("Adding AppInfoService");
            // Add AppInfoService as AdditionalBean - else it is not available at runtime.
            additionalBean.produce(AdditionalBeanBuildItem.builder()
                    .setUnremovable()
                    .addBeanClass(AppinfoService.class)
                    .build());
        }
    }

    @BuildStep
    void createServlet(LaunchModeBuildItem launchMode,
                                   AppinfoConfig appInfoConfig,
                                   BuildProducer<ServletBuildItem> additionalBean) {

        if(shouldInclude(launchMode, appInfoConfig)) {
            String basePath = appInfoConfig.basePath;
            if(appInfoConfig.basePath.startsWith("/")) {
                basePath = appInfoConfig.basePath.replaceFirst("/", "");
            }

            logger.info("Adding AppInfoServlet /"+basePath);

            additionalBean.produce(ServletBuildItem.builder(basePath, AppinfoServlet.class.getName())
                    .addMapping("/"+basePath)
                    .build());
        }
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, AppinfoConfig appInfoConfig) {
        return launchMode.getLaunchMode().isDevOrTest() || appInfoConfig.alwaysInclude;
    }
}
