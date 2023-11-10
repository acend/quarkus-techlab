package ch.puzzle.quarkustechlab.messaging.consumer.boundary;

import ch.puzzle.quarkustechlab.messaging.consumer.entity.SensorMeasurement;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ReactiveDataConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReactiveDataConsumer.class.getName());

    @Incoming("data")
    @Outgoing("in-memory-stream")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public SensorMeasurement consume(SensorMeasurement sensorMeasurement) {
        return sensorMeasurement;
    }
}
