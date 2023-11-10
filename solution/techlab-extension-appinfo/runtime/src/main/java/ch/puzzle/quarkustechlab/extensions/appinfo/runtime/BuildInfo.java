package ch.puzzle.quarkustechlab.extensions.appinfo.runtime;

public class BuildInfo {

    String time;
    String builtFor;

    public BuildInfo() {
    }

    public BuildInfo(String buildTime, String builtFor) {
        this.time = buildTime;
        this.builtFor = builtFor;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBuiltFor() {
        return builtFor;
    }

    public void setBuiltFor(String builtFor) {
        this.builtFor = builtFor;
    }
}
