package ch.puzzle.producer.boundary;

import ch.puzzle.producer.entity.SensorMeasurement;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@ApplicationScoped
public class ReactiveDataProducer {

    @Outgoing("data-inbound")
    public Multi<SensorMeasurement> produceData() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
                .onItem().transform(i -> new SensorMeasurement(new Random().nextDouble(), Instant.now()));
    }
}
