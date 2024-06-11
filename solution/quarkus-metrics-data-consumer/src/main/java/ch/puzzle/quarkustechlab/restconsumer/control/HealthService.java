package ch.puzzle.quarkustechlab.restconsumer.control;

import jakarta.enterprise.context.ApplicationScoped;

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