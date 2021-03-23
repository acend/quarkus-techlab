package ch.puzzle.producer.entity;

import java.time.Instant;

public class SensorMeasurement {

    public Double data;
    public Instant time;

    public SensorMeasurement() {
        this.data = Math.random();
        this.time = Instant.now();
    }

    public SensorMeasurement(Double data, Instant time) {
        this.data = data;
        this.time = time;
    }
}
