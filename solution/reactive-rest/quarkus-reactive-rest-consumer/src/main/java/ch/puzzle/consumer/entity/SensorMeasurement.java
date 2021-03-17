package ch.puzzle.consumer.entity;

import java.time.Instant;

public class SensorMeasurement {

    public Long id;
    public Double data;
    public Instant time;

    public SensorMeasurement() {
    }

    public SensorMeasurement(Long id, Double data, Instant time) {
        this.id = id;
        this.data = data;
        this.time = time;
    }
}

