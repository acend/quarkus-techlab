package ch.puzzle.quarkustechlab.reactiverest.consumer.entity;

import java.time.Instant;

public class SensorMeasurement {
    public Long id;
    public Double data;
    public Instant time;

    public SensorMeasurement() {
        this.id = id;
        this.data = Math.random();
        this.time = Instant.now();
    }
}
