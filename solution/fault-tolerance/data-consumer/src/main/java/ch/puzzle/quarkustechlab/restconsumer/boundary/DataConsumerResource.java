package ch.puzzle.quarkustechlab.restconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import ch.puzzle.quarkustechlab.restconsumer.health.HealthService;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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

    @GET
    @Path("/slow")
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement getSlowMeasurement() {
        SensorMeasurement slowMeasurement = dataProducerService.getSlowMeasurement();
        healthService.registerMessageFetch();
        return slowMeasurement;
    }

    @GET
    @Path("/fallback")
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement getFallbackMeasurement() {
        SensorMeasurement slowMeasurement = dataProducerService.getFallbackMeasurement();
        healthService.registerMessageFetch();
        return slowMeasurement;
    }
}