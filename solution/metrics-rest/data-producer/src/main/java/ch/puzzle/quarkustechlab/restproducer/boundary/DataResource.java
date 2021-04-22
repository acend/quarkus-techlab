package ch.puzzle.quarkustechlab.restproducer.boundary;

import ch.puzzle.quarkustechlab.restproducer.entity.SensorMeasurement;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@Path("/data")
public class DataResource {

    private Long gauge = 0L;
    private final MeterRegistry registry;
    private final Logger logger = Logger.getLogger(DataResource.class.getName());

    public DataResource(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("example_gauge", this, DataResource::getGauge);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(value = "GET_data", description = "Timer for the endpoint /data", extraTags = {"producer", "GET"})
    public SensorMeasurement getMeasurement() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextLong(0, 5_000));
        return new SensorMeasurement();
    }

    @GET
    @Counted
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public String incrementCounter() {
        return "+1";
    }

    @GET
    @Path("/gauge")
    @Produces(MediaType.APPLICATION_JSON)
    public Long incrementGauge() {
        return ++gauge;
    }

    Long getGauge() {
        return gauge;
    }
}