package ch.puzzle.quarkustechlab.opentelemetry.jaeger.control;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TracedService {

    @WithSpan
    public String hello() {
        return "hello";
    }
}
