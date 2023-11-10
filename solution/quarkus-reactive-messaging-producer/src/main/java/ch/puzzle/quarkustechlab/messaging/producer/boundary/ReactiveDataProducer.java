package ch.puzzle.quarkustechlab.messaging.producer.boundary;

import ch.puzzle.quarkustechlab.messaging.producer.entity.SensorMeasurement;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@ApplicationScoped
public class ReactiveDataProducer {

    @Outgoing("data")
    public Multi<SensorMeasurement> produceData() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
                .onItem().transform(i -> new SensorMeasurement(new Random().nextDouble(), Instant.now()));
    }
}
