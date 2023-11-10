package ch.puzzle.quarkustechlab.restconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/data")
@RegisterRestClient(configKey = "data-producer-api")
public interface DataProducerService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timeout(500)
    @Fallback(fallbackMethod = "getDefaultMeasurement")
    SensorMeasurement getSensorMeasurement();

    default SensorMeasurement getDefaultMeasurement() {
        return new SensorMeasurement();
    }
}
