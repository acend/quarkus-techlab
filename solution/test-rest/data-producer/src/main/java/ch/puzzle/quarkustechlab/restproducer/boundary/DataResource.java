package ch.puzzle.quarkustechlab.restproducer.boundary;

import ch.puzzle.quarkustechlab.restproducer.control.DummyService;
import ch.puzzle.quarkustechlab.restproducer.entity.SensorMeasurement;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/data")
public class DataResource {

    @Inject
    DummyService dummyService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement data() {
        return new SensorMeasurement() ;
    }

    @GET
    @Path("/dummy")
    public String dummy() {
        return dummyService.dummy();
    }
}