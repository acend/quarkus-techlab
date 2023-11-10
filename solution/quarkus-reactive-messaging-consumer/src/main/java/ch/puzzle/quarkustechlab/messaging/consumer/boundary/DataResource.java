package ch.puzzle.quarkustechlab.messaging.consumer.boundary;

import ch.puzzle.quarkustechlab.messaging.consumer.entity.SensorMeasurement;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("/data")
public class DataResource {

    @Inject
    @Channel("in-memory-stream")
    Multi<SensorMeasurement> channel;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorMeasurement> stream() {
        return channel;
    }
}
