package ch.puzzle.quarkustechlab.restproducer.control;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DummyService {

    public String dummy() {
        return "dummy";
    }
}
