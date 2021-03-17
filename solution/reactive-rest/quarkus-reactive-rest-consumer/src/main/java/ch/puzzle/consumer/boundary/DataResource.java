package ch.puzzle.consumer.boundary;

import ch.puzzle.consumer.entity.SensorMeasurement;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/data")
public class DataResource {

    @Inject
    @RestClient
    DataService dataService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<SensorMeasurement> findAll() {
        return dataService.findById(1L);
    }
}