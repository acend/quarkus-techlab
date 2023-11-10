package ch.puzzle.quarkustechlab.messaging.consumer.boundary;

import ch.puzzle.quarkustechlab.messaging.consumer.entity.SensorMeasurement;
import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class SensorMeasurementDeserializer extends JsonbDeserializer<SensorMeasurement> {

    public SensorMeasurementDeserializer() {
        super(SensorMeasurement.class);
    }
}