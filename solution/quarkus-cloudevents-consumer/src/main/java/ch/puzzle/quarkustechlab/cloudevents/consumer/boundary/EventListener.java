package ch.puzzle.quarkustechlab.cloudevents.consumer.boundary;

import ch.puzzle.quarkustechlab.cloudevents.SensorMeasurement;
import io.smallrye.reactive.messaging.ce.IncomingCloudEventMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class EventListener {

    private final Logger logger = LoggerFactory.getLogger(EventListener.class);

    @Incoming("measurements")
    public CompletionStage<Void> consume(Message<SensorMeasurement> message) {
        IncomingCloudEventMetadata cloudEventMetadata = message.getMetadata(
                IncomingCloudEventMetadata.class).orElseThrow(() -> new IllegalArgumentException("Expected a CloudEvent!"));
        logger.info("Received Cloud Events (spec-version: {}): id: '{}', source:  '{}', type: '{}', subject: '{}', payload-message: '{}' ",
                cloudEventMetadata.getSpecVersion(),
                cloudEventMetadata.getId(),
                cloudEventMetadata.getSource(),
                cloudEventMetadata.getType(),
                cloudEventMetadata.getSubject().orElse("no subject"),
                message.getPayload());
        return message.ack();
    }
}
