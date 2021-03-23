package ch.puzzle.consumer.boundary;

import ch.puzzle.consumer.entity.SensorMeasurement;
import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class SensorMeasurementDeserializer extends JsonbDeserializer {
    public SensorMeasurementDeserializer() {
        super(SensorMeasurement.class);
    }
}
