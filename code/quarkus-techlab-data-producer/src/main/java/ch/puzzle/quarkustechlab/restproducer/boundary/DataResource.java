package ch.puzzle.quarkustechlab.restproducer.boundary;

import ch.puzzle.quarkustechlab.restproducer.entity.SensorMeasurement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Random;
import java.util.logging.Logger;

@Path("/data")
public class DataResource {

    private static Logger logger = Logger.getLogger(DataResource.class.getName());

    Random random = new Random();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement getSensorMeasurement() throws InterruptedException {
        logger.info("getSensorMeasurement called!");
        Thread.sleep(random.nextInt(1000));
        return new SensorMeasurement();
    }
}