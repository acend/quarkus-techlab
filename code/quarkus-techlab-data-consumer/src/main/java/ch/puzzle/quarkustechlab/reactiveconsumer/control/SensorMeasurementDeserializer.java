package ch.puzzle.quarkustechlab.reactiveconsumer.control;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class SensorMeasurementDeserializer extends JsonbDeserializer<SensorMeasurement> {

    public SensorMeasurementDeserializer() {
        super(SensorMeasurement.class);
    }
}
