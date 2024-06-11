package ch.puzzle.quarkustechlab.restconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.control.HealthService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Path("/data")
public class DataConsumerResource {

    private static final Logger logger = LoggerFactory.getLogger(DataConsumerResource.class);

    private final MeterRegistry registry;

    @RestClient
    DataProducerService dataProducerService;

    @Inject
    HealthService healthService;

    public DataConsumerResource(MeterRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement getData() {
        logger.info("Collecting data from producer");
        Supplier<SensorMeasurement> supplier = () -> dataProducerService.getSensorMeasurement();
        SensorMeasurement sensorMeasurement = registry.timer("REST_call_data").wrap(supplier).get();
        logger.info("Returning data");
        healthService.registerMessageFetch();
        return sensorMeasurement;
    }
}
