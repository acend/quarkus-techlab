package ch.puzzle.quarkustechlab.reactiverest.consumer.boundary;

import ch.puzzle.quarkustechlab.reactiverest.consumer.entity.SensorMeasurement;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/data")
@RegisterRestClient(configKey = "data-service")
public interface DataService {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<SensorMeasurement> findById(@PathParam("id") Long id);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<SensorMeasurement> create(SensorMeasurement sensorMeasurement);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<SensorMeasurement>> findAll();

}
