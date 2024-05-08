package ch.puzzle.quarkustechlab.reactiverest.consumer.boundary;

import ch.puzzle.quarkustechlab.reactiverest.consumer.entity.SensorMeasurement;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/data")
public class DataResource {

    @Inject
    @RestClient
    DataService dataService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> findAll() {
        return dataService.findAll()
                .onItem().transform(list -> list != null ? Response.ok(list) : Response.serverError())
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> findById(@PathParam(value = "id") Long id) {
        return dataService.findById(id)
                .onItem().transform(item -> id != null ? Response.ok(item) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<SensorMeasurement> create(SensorMeasurement sensorMeasurement) {
        return dataService.create(sensorMeasurement);
    }
}
