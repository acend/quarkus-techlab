package ch.puzzle.quarkustechlab.reactiverest.producer.boundary;

import ch.puzzle.quarkustechlab.reactiverest.producer.entity.SensorMeasurement;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.time.Duration;

@Path("/data")
public class DataResource {


    @Inject
    PgPool client;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> findAll() {
        return SensorMeasurement.findAll(client);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> findById(@PathParam(value = "id") Long id) {
        return SensorMeasurement.findById(client, id)
                .onItem().transform(sensorMeasurement -> sensorMeasurement != null ? Response.ok(sensorMeasurement) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @GET
    @Path("/latest")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> latest() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
                .onItem().transform(i -> SensorMeasurement.getLatest(client).await().indefinitely());
    }

    @GET
    @Path("/average")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> average() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
                .onItem().transform(i -> SensorMeasurement.getAverage(client).await().indefinitely());
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<SensorMeasurement> create(SensorMeasurement sensorMeasurement) {
        return sensorMeasurement.save(client);
    }
}
