package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

public class Appinfo {

    String buildTime;
    String builtFor;
    String runBy;
    String createTime;
    String startupTime;
    String currentTime;
    String applicationName;
    String applicationVersion;
    String propertiesString;

    String asHumanReadableString() {
        String format = "%-15s %s%n";

        return "AppInfo\n" +
                String.format(format, "buildTime", buildTime) +
                String.format(format, "builtFor", builtFor) +

                String.format(format, "runBy", runBy) +
                String.format(format, "createTime", createTime) +
                String.format(format, "startupTime", startupTime) +

                String.format(format, "name", applicationName) +
                String.format(format, "version", applicationVersion) +

                String.format(format, "currentTime", currentTime) +

                "\n\nProperties\n" +
                propertiesString;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public String getBuiltFor() {
        return builtFor;
    }

    public void setBuiltFor(String builtFor) {
        this.builtFor = builtFor;
    }

    public String getRunBy() {
        return runBy;
    }

    public void setRunBy(String runBy) {
        this.runBy = runBy;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(String startupTime) {
        this.startupTime = startupTime;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getPropertiesString() {
        return propertiesString;
    }

    public void setPropertiesString(String propertiesString) {
        this.propertiesString = propertiesString;
    }
}