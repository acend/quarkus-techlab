package ch.puzzle.quarkustechlab.restconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.control.HealthService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;

@Singleton
@Path("/data")
public class DataConsumerResource {

    @RestClient
    DataProducerService dataProducerService;

    @Inject
    HealthService healthService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement getData() {
        SensorMeasurement measurement = dataProducerService.getSensorMeasurement();
        healthService.registerMessageFetch();
        return measurement;
    }
}
