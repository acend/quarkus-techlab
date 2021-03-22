package ch.puzzle.producer.boundary;

import ch.puzzle.producer.entity.SensorMeasurement;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import org.jboss.resteasy.reactive.RestSseElementType;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

@Path("/data")
public class DataResource {

    private final PgPool client;

    public DataResource(PgPool client) {
        this.client = client;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<SensorMeasurement>> findAll() {
        return SensorMeasurement.findAll(client).collect().asList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> findById(@PathParam(value = "id") Long id) {
        return SensorMeasurement.findById(client, id)
                .onItem().transform(sensorMeasurement -> sensorMeasurement != null ? Response.ok(sensorMeasurement) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<SensorMeasurement> create(SensorMeasurement sensorMeasurement) {
        return sensorMeasurement.save(client);
    }

    @GET
    @Path("/latest")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> latest() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
                .onItem().transform(i -> SensorMeasurement.getLatest(client).await().indefinitely());
    }

    @GET
    @Path("/average")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> average() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
                .onItem().transform(i -> SensorMeasurement.getAverage(client).await().indefinitely());
    }
}