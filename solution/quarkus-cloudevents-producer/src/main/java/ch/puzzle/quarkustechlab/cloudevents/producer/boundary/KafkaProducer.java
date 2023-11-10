package ch.puzzle.quarkustechlab.cloudevents.producer.boundary;

import ch.puzzle.quarkustechlab.cloudevents.SensorMeasurement;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;

@ApplicationScoped
public class KafkaProducer {

    private final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    @Channel("measurements")
    @Inject
    Emitter<SensorMeasurement> sensorMeasurementEmitter;

    @ConfigProperty(name = "quarkus.uuid")
    String uuid;

    @ConfigProperty(name = "quarkus.application.name")
    String applicationName;

    public void emitEvent(SensorMeasurement sensorMeasurement) {
        OutgoingCloudEventMetadata<Object> metadata = OutgoingCloudEventMetadata.builder()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create(applicationName+"-"+uuid))
                .withType("measurement-emitted")
                .withSubject("subject-123")
                .build();

        logger.info("Producing Cloud Event, (spec-version: {}): id: '{}', source:  '{}', type: '{}', subject: '{}', payload-message: '{}' ",
                metadata.getSpecVersion(),
                metadata.getId(),
                metadata.getSource(),
                metadata.getType(),
                metadata.getSubject().orElse("no subject"),
                sensorMeasurement);

        sensorMeasurementEmitter.send(Message.of(sensorMeasurement).addMetadata(metadata));
    }
}