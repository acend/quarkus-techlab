package ch.puzzle.quarkustechlab.opentelemetry.jaeger.boundary;

import ch.puzzle.quarkustechlab.opentelemetry.jaeger.control.TracedService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class TracedResource {

    @Inject
    TracedService tracedService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @WithSpan
    public String hello() {
        Span span = Span.current();
        span.setAttribute("Additional information key", "Additional information value");
        return tracedService.hello();
    }
}
