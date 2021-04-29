package ch.puzzle.quarkustechlab.serverless.control;

import ch.puzzle.quarkustechlab.serverless.entity.SensorMeasurement;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DataService {

    public SensorMeasurement getMeasurement() {
        return new SensorMeasurement();
    }
}
