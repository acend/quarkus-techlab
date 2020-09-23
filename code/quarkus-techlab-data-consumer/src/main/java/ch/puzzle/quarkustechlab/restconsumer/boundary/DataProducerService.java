package ch.puzzle.quarkustechlab.restconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/data")
@RegisterRestClient(configKey = "data-producer-api")
public interface DataProducerService {

    @GET
    @Produces("application/json")
    @Timeout(500)
    @Fallback(fallbackMethod = "getDefaultMeasurement")
    SensorMeasurement getSensorMeasurement();

    default SensorMeasurement getDefaultMeasurement() {
        return new SensorMeasurement();
    }
}
