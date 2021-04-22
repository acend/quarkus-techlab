package ch.puzzle.quarkustechlab.restconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Path("/data")
public class DataConsumerResource {

    private final Logger logger = Logger.getLogger(DataConsumerResource.class.getName());
    private final MeterRegistry registry;

    @RestClient
    DataProducerService dataProducerService;

    public DataConsumerResource(MeterRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    public SensorMeasurement getData() {
        logger.info("Collecting data from producer");
        Supplier<SensorMeasurement> supplier = () -> dataProducerService.getSensorMeasurement();
        SensorMeasurement sensorMeasurement = registry.timer("REST_call_data").wrap(supplier).get();
        logger.info("Returning data");
        return sensorMeasurement;
    }
}