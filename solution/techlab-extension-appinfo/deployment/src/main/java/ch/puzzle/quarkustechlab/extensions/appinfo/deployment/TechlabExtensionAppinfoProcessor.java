package ch.puzzle.quarkustechlab.extensions.appinfo.deployment;

import ch.puzzle.quarkustechlab.extensions.appinfo.runtime.AppinfoRecorder;
import ch.puzzle.quarkustechlab.extensions.appinfo.runtime.AppinfoService;
import ch.puzzle.quarkustechlab.extensions.appinfo.runtime.AppinfoServlet;
import ch.puzzle.quarkustechlab.extensions.appinfo.runtime.BuildInfo;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.builder.Version;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.undertow.deployment.ServletBuildItem;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

class TechlabExtensionAppinfoProcessor {

    private static final String FEATURE = "techlab-extension-appinfo";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    private static final Logger logger = LoggerFactory.getLogger(TechlabExtensionAppinfoProcessor.class);

    @BuildStep
    @Record(STATIC_INIT)
    void syntheticBean(AppinfoBuildTimeConfig appinfoConfig,
                       LaunchModeBuildItem launchMode,
                       AppinfoRecorder recorder,
                       BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

        if(shouldInclude(launchMode, appinfoConfig)) {
            String buildTime = appinfoConfig.recordBuildTime() ? Instant.now().toString() : null;
            String builtFor = appinfoConfig.builtFor();

            logger.info("Adding BuildInfo. RecordBuildTime={}, BuiltFor={}", appinfoConfig.recordBuildTime(), builtFor);

            syntheticBeans.produce(SyntheticBeanBuildItem.configure(BuildInfo.class).scope(Singleton.class)
                    .runtimeValue(recorder.createBuildInfo(buildTime, builtFor))
                    .unremovable()
                    .done());
        }
    }

    @BuildStep
    void createServlet(LaunchModeBuildItem launchMode,
                       AppinfoBuildTimeConfig appinfoConfig,
                       BuildProducer<ServletBuildItem> additionalBean) {

        if(shouldInclude(launchMode, appinfoConfig)) {
            String basePath = appinfoConfig.basePath();
            if(basePath.startsWith("/")) {
                basePath = basePath.replaceFirst("/", "");
            }

            logger.info("Adding AppinfoServlet /{}", basePath);

            additionalBean.produce(ServletBuildItem.builder(basePath, AppinfoServlet.class.getName())
                    .addMapping("/"+basePath)
                    .build());
        }
    }

    @BuildStep
    void registerAdditionalBeans(AppinfoBuildTimeConfig appinfoConfig,
                                 LaunchModeBuildItem launchMode,
                                 BuildProducer<AdditionalBeanBuildItem> additionalBean) {

        if(shouldInclude(launchMode, appinfoConfig)) {
            logger.info("Adding AppinfoService");
            // Add AppinfoService as AdditionalBean - else it is not available at runtime.
            additionalBean.produce(AdditionalBeanBuildItem.builder()
                    .setUnremovable()
                    .addBeanClass(AppinfoService.class)
                    .build());
        }
    }

    @BuildStep
    StaticMetadataBuildItem createStaticMetadata(AppinfoBuildTimeConfig appInfoBuildTimeConfig) {
        return new StaticMetadataBuildItem(Version.getVersion(),
                appInfoBuildTimeConfig.builtFor(),
                appInfoBuildTimeConfig.alwaysInclude(),
                appInfoBuildTimeConfig.basePath(),
                appInfoBuildTimeConfig.recordBuildTime());
    }

    private static boolean shouldInclude(LaunchModeBuildItem launchMode, AppinfoBuildTimeConfig appinfoConfig) {
        return launchMode.getLaunchMode().isDevOrTest() || appinfoConfig.alwaysInclude();
    }
}
