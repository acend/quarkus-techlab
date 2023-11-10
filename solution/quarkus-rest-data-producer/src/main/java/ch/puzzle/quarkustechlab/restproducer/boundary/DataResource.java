package ch.puzzle.quarkustechlab.restproducer.boundary;

import ch.puzzle.quarkustechlab.restproducer.control.DummyService;
import ch.puzzle.quarkustechlab.restproducer.entity.SensorMeasurement;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

@Path("/data")
public class DataResource {

    private static final Logger logger = LoggerFactory.getLogger(DataResource.class.getName());

    Random random = new Random();

    @Inject
    DummyService dummyService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement hello() throws InterruptedException {
        logger.info("RestCall");
        logger.info("getSensorMeasurement called!");
        Thread.sleep(random.nextInt(1000));
        return new SensorMeasurement();
    }

    @GET
    @Path("/dummy")
    public String dummy() {
        return dummyService.dummy();
    }
}
