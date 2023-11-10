package ch.puzzle.quarkustechlab.extensions.appinfo.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

public final class StaticMetadataBuildItem extends SimpleBuildItem {

    private final String quarkusVersion;
    private final String builtFor;
    private final boolean recordBuildTime;
    private final boolean alwaysInclude;
    private final String basePath;

    public String getQuarkusVersion() {
        return quarkusVersion;
    }

    public String getBuiltFor() {
        return builtFor;
    }

    public boolean isRecordBuildTime() {
        return recordBuildTime;
    }

    public boolean isAlwaysInclude() {
        return alwaysInclude;
    }

    public String getBasePath() {
        return basePath;
    }

    public StaticMetadataBuildItem(String quarkusVersion, String builtFor, boolean alwaysInclude, String basePath, boolean recordBuildTime) {
        this.quarkusVersion = quarkusVersion;
        this.builtFor = builtFor;
        this.alwaysInclude = alwaysInclude;
        this.basePath = basePath;
        this.recordBuildTime = recordBuildTime;
    }
}