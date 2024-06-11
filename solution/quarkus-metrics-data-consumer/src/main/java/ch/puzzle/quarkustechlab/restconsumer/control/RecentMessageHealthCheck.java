package ch.puzzle.quarkustechlab.restconsumer.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import java.time.Instant;

@Liveness
@ApplicationScoped
public class RecentMessageHealthCheck implements HealthCheck {

    @Inject
    HealthService healthService;

    @Override
    public HealthCheckResponse call() {
        Instant lastMessageTime = healthService.getLastMessageTime();

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("Last message check")
                .status(lastMessageTime == null || ((lastMessageTime.toEpochMilli() + 60000) >= Instant.now().toEpochMilli()));

        if(lastMessageTime != null) {
            responseBuilder.withData("lastMessageTime", lastMessageTime.toEpochMilli())
                    .withData("ageInMs", (Instant.now().toEpochMilli() - lastMessageTime.toEpochMilli()));
        }

        return responseBuilder.build();
    }
}