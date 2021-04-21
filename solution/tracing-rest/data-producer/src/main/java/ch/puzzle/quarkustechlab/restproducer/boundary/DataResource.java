package ch.puzzle.quarkustechlab.restproducer.boundary;

import ch.puzzle.quarkustechlab.restproducer.control.DataService;
import ch.puzzle.quarkustechlab.restproducer.entity.SensorMeasurement;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/data")
public class DataResource {

    @Inject
    DataService dataService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorMeasurement hello() throws Exception {
        return dataService.createSensorMeasurementOrFail();
    }
}