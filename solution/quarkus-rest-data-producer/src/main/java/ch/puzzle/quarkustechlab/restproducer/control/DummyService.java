package ch.puzzle.quarkustechlab.restproducer.control;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DummyService {

    public String dummy() {
        return "dummy";
    }
}