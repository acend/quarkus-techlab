package ch.puzzle.quarkustechlab.restproducer.control;

import ch.puzzle.quarkustechlab.restproducer.entity.SensorMeasurement;
import io.opentracing.Tracer;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.JsonbBuilder;

@ApplicationScoped
@Traced
public class DataService {

    @Inject
    Tracer tracer;

    public SensorMeasurement createSensorMeasurementOrFail() throws Exception {
        if (Math.random() > 0.6)
            throw new Exception("Random failure");
        SensorMeasurement measurement = new SensorMeasurement();
        tracer.activeSpan().setBaggageItem("measurement", JsonbBuilder.create().toJson(measurement));
        return measurement;
    }
}
