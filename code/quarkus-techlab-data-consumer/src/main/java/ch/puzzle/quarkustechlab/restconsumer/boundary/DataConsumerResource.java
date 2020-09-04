package ch.puzzle.quarkustechlab.restconsumer.boundary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/data")
public class DataConsumerResource {

    @RestClient
    DataProducerService dataProducerService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement getData() {
        return dataProducerService.getSensorMeasurement();
    }
}