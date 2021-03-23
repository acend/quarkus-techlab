package ch.puzzle.consumer.boundary;

import ch.puzzle.consumer.entity.SensorMeasurement;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

@ApplicationScoped
public class ReactiveDataConsumer {

    private static final Logger log = Logger.getLogger(ReactiveDataConsumer.class.getName());

    @Incoming("data-inbound")
    @Outgoing("in-memory-stream")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public SensorMeasurement consume(SensorMeasurement sensorMeasurement) {
        return sensorMeasurement;
    }
}
