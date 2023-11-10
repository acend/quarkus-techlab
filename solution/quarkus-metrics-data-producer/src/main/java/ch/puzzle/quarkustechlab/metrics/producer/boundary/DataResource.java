package ch.puzzle.quarkustechlab.metrics.producer.boundary;

import ch.puzzle.quarkustechlab.metrics.producer.control.DummyService;
import ch.puzzle.quarkustechlab.metrics.producer.entity.SensorMeasurement;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Path("/data")
public class DataResource {

    private static final Logger logger = LoggerFactory.getLogger(DataResource.class.getName());

    Random random = new Random();

    @Inject
    DummyService dummyService;

    private Long gauge = 0L;
    private final MeterRegistry registry;

    public DataResource(MeterRegistry registry) {
        this.registry = registry;
        registry.gauge("example_gauge", this, DataResource::getGauge);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed(value = "GET_data", description = "Timer for the endpoint /data", extraTags = {"producer", "GET"})
    public SensorMeasurement hello() throws InterruptedException {
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

    @GET
    @Path("/dummy")
    public String dummy() {
        return dummyService.dummy();
    }
}
