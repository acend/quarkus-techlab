package ch.puzzle.appinfo.extension.deployment;

import ch.puzzle.quarkus.training.extension.appinfo.*;

import com.sun.org.apache.bcel.internal.classfile.Synthetic;
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

class AppInfoProcessor {

    private static Logger logger = LoggerFactory.getLogger(AppInfoProcessor.class);

    private static final String FEATURE = AppInfoNames.EXTENSION_NAME;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(STATIC_INIT)
    void syntheticBean(AppInfoConfig appInfoConfig,
                                         LaunchModeBuildItem launchMode,
                                         AppInfoRecorder recorder,
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
    void registerAdditionalBeans(AppInfoConfig appInfoConfig,
                                 LaunchModeBuildItem launchMode,
                                 BuildProducer<AdditionalBeanBuildItem> additionalBean) {

        if(shouldInclude(launchMode, appInfoConfig)) {
            logger.info("Adding AppInfoService");
            // Add AppInfoService as AdditionalBean - else it is not available at runtime.
            additionalBean.produce(AdditionalBeanBuildItem.builder()
                    .setUnremovable()
                    .addBeanClass(AppInfoService.class)
                    .build());
        }
    }

    @BuildStep
    void createServlet(LaunchModeBuildItem launchMode,
                                   AppInfoConfig appInfoConfig,
                                   BuildProducer<ServletBuildItem> additionalBean) {

        if(shouldInclude(launchMode, appInfoConfig)) {
            String basePath = appInfoConfig.basePath;
            if(appInfoConfig.basePath.startsWith("/")) {
                basePath = appInfoConfig.basePath.replaceFirst("/", "");
            }

            logger.info("Adding AppInfoServlet /"+basePath);

            additionalBean.produce(ServletBuildItem.builder(basePath, AppInfoServlet.class.getName())
                    .addMapping("/"+basePath)
                    .build());
        }
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, AppInfoConfig appInfoConfig) {
        return launchMode.getLaunchMode().isDevOrTest() || appInfoConfig.alwaysInclude;
    }
}
