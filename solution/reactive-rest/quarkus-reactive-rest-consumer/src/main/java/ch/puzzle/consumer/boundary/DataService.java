package ch.puzzle.consumer.boundary;

import ch.puzzle.consumer.entity.SensorMeasurement;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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

    @GET
    @Path("/latest")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<SensorMeasurement> getLatest();
}
