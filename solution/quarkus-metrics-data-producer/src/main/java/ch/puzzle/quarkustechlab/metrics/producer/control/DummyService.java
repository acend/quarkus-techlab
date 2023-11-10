package ch.puzzle.quarkustechlab.metrics.producer.control;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DummyService {

    public String dummy() {
        return "dummy";
    }
}