package ch.puzzle.consumer.boundary;

import ch.puzzle.consumer.entity.SensorMeasurement;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestSseElementType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/data")
public class DataResource {

    @Inject
    @Channel("in-memory-stream")
    Multi<SensorMeasurement> channel;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> stream() {
        return channel;
    }
}