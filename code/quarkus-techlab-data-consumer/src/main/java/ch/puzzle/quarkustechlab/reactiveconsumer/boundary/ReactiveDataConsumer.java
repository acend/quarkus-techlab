package ch.puzzle.quarkustechlab.reactiveconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.JsonbBuilder;
import java.util.logging.Logger;

@ApplicationScoped
public class ReactiveDataConsumer {

    private final Logger logger = Logger.getLogger(ReactiveDataConsumer.class.getName());

    @Incoming("data")
    public void consumeStream(SensorMeasurement sensorMeasurement) {
        logger.info("Received reactive message: " + JsonbBuilder.create().toJson(sensorMeasurement));
    }
}
