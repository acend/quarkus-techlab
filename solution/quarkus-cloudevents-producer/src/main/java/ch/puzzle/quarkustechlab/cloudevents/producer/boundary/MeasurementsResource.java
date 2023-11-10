package ch.puzzle.quarkustechlab.cloudevents.producer.boundary;

import ch.puzzle.quarkustechlab.cloudevents.SensorMeasurement;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.Random;

@Path("/measurements")
public class MeasurementsResource {

    private final KafkaProducer kafkaProducer;

    public MeasurementsResource(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @POST
    public Response emitMeasurement() {
        SensorMeasurement measurement = SensorMeasurement.newBuilder().setData(new Random().nextDouble()).build();
        kafkaProducer.emitEvent(measurement);
        return Response.ok().build();
    }
}