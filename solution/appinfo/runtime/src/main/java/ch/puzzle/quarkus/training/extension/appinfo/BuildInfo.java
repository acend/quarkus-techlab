package ch.puzzle.quarkus.training.extension.appinfo;

public class BuildInfo {

    String time;
    String builtFor;

    public BuildInfo() {
    }

    public BuildInfo(String buildTime, String builtFor) {
        this.time = buildTime;
        this.builtFor = builtFor;
    }

    public String getBuiltFor() {
        return builtFor;
    }

    public void setBuiltFor(String builtFor) {
        this.builtFor = builtFor;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
