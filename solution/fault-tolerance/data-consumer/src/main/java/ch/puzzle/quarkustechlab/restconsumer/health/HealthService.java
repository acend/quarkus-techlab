package ch.puzzle.quarkustechlab.restconsumer.health;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;

@ApplicationScoped
public class HealthService {

    Instant lastMessageTime;

    public void registerMessageFetch() {
        this.lastMessageTime = Instant.now();
    }

    public Instant getLastMessageTime() {
        return lastMessageTime;
    }
}
