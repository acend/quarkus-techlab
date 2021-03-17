package ch.puzzle.producer.boundary;

import ch.puzzle.producer.entity.SensorMeasurement;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

@Path("/data")
public class DataResource {

    private final PgPool client;

    public DataResource(PgPool client) {
        this.client = client;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> findAll() {
        return SensorMeasurement.findAll(client);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<SensorMeasurement> findById(@PathParam(value = "id") Long id) {
        return SensorMeasurement.findById(client, id);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<SensorMeasurement> create(SensorMeasurement sensorMeasurement) {
        return sensorMeasurement.save(client);
    }
}