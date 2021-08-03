package ch.puzzle.quarkustechlab.restconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/data")
@RegisterRestClient(configKey = "data-producer-api")
public interface DataProducerService {

    @GET
    @Produces("application/json")
    @Retry(maxRetries = 10)
    SensorMeasurement getSensorMeasurement();

    @GET
    @Path("/slow")
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout(500)
    SensorMeasurement getSlowMeasurement();

    @GET
    @Path("/slow")
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout(500)
    @Fallback(fallbackMethod = "getDefaultMeasurement")
    SensorMeasurement getFallbackMeasurement();

    default SensorMeasurement getDefaultMeasurement() {
        return new SensorMeasurement();
    }
}
