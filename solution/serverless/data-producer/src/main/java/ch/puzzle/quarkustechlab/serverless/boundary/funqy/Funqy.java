package ch.puzzle.quarkustechlab.serverless.boundary.funqy;

import ch.puzzle.quarkustechlab.serverless.control.DataService;
import ch.puzzle.quarkustechlab.serverless.entity.SensorMeasurement;
import io.quarkus.funqy.Funq;

import javax.inject.Inject;
import java.util.Map;
import java.util.Random;

public class Funqy {

    @Inject
    DataService dataService;

    private static final String CHARM_QUARK_SYMBOL = "c";

    @Funq
    public String dto(Dto dto) {
        return dto.name;
    }

    public static class Dto {
        String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Funq
    public String charm(Answer answer) {
        return CHARM_QUARK_SYMBOL.equalsIgnoreCase(answer.value) ? "You Quark!" : "👻 Wrong answer";
    }

    public static class Answer {
        public String value;

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @Funq
    public SensorMeasurement data() {
        return dataService.getMeasurement();
    }
}
