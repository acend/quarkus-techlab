package ch.puzzle.quarkustechlab.jmsconsumer.boundary;

import ch.puzzle.quarkustechlab.restconsumer.entity.SensorMeasurement;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.*;
import javax.json.bind.JsonbBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@ApplicationScoped
public class JmsDataConsumer implements Runnable {

    @Inject
    ConnectionFactory connectionFactory;

    private final Logger logger = Logger.getLogger(JMSConsumer.class.getName());
    private final ExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile SensorMeasurement lastData;

    public SensorMeasurement getLastData() {
        return lastData;
    }

    void onStart(@Observes StartupEvent event) {
        scheduler.submit(this);
    }

    void onShutDown(@Observes ShutdownEvent event) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(context.createQueue("data-inbound"));
            while (true) {
                Message message = consumer.receive();
                if (message == null) return;
                logger.info("Receieved data: " + message.getBody(String.class));
                lastData = JsonbBuilder.create().fromJson(message.getBody(String.class), SensorMeasurement.class);
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
