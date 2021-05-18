---
title: "6.1 ActiveMQ Artemis"
linkTitle: "6.1 ActiveMQ Artemis"
weight: 610
sectionnumber: 6.1
onlyWhen: artemis
description: >
   Messaging with ActiveMQ Artemis in Quarkus.
---

## {{% param sectionnumber %}}.1: ActiveMQ Artemis

In order to make our microservices to communicate through messaging we need to setup a message broker first. In this first example we are going to use [Artemis ActiveMQ](https://activemq.apache.org/components/artemis/) as our message broker. Artemis provides us a simple message broker which comes with a few handy features which we are going to make use of.

To setup our Artemis ActiveMQ instance we are going to use a docker image and run it locally:

```s

docker run -it --rm -p 8161:8161 -p 61616:61616 -p 5672:5672 -e ARTEMIS_USERNAME=quarkus -e ARTEMIS_PASSWORD=quarkus vromero/activemq-artemis:2.11.0-alpine

```

If you have your container up and running you can log into the web UI on [http://localhost:8161/console](http://localhost:8161/console) and click yourself through the interface.


## {{% param sectionnumber %}}.2: Microservices

Create two new microservices 'quarkus-jms-producer' and 'quarkus-jms-consumer' and create the `SensorMeasurement` class in both microservices:

```java

public class SensorMeasurement {

    public Double data;
    public Instant time;

    public SensorMeasurement() {
        this.data = Math.random();
        this.time = Instant.now();
    }

    public SensorMeasurement(Double data, Instant time) {
        this.data = data;
        this.time = time;
    }
}


```

Add the 'quarkus-artemis-jms' and 'quarkus-jsonb' extensions to your projects.

To ensure the connection to the message broker add the following config to your application.properties file:

```s

# Configures the Artemis JMS properties.
quarkus.artemis.url=tcp://artemis-activemq:61616
%dev.quarkus.artemis.url=tcp://localhost:61616
quarkus.artemis.username=quarkus
quarkus.artemis.password=quarkus

```

Let's start with producing data to a Queue:


### {{% param sectionnumber %}}.2.1: Producing data

We are going to implement a new class called DataProducer `...producer.boundary.JmsDataProducer` which implements the interface Runnable. We setup a simple scheduler which triggers the production of a message every five seconds with a random SensorMeasurement.

```java

@ApplicationScoped
public class JmsDataProducer implements Runnable {

    @Inject
    ConnectionFactory connectionFactory;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    void onStart(@Observes StartupEvent event) {
        scheduler.scheduleWithFixedDelay(this, 0L, 5L, TimeUnit.SECONDS);
    }

    void onStop(@Observes ShutdownEvent event) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        try(JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            context.createProducer().send(context.createQueue("data-inbound"), JsonbBuilder.create().toJson(new SensorMeasurement()));
        }
    }
}

```

In the `run` method we create a new context with our `ConnectionFactory` and create a producer which  sends a message to the `data-inbound` message queue.


### {{% param sectionnumber %}}.2.2: Consuming data

On the consumer side we create a new Class `...consumer.boundary.JmsDataConsumer` which also implements the Interface Runnable:

```java

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

```

If you run both applications you will see that the consumer receives an Message every five seconds with new data from the message broker. If you check the Artemis UI you can see under 'Diagram' your Queue with an active consumer connected to it. When you head over to the 'Queues' tab you can read or manipulate your Queues manually.

Feel free to dive and explore the JMS interface a bit more. If you have seen enought, head over to the next chapter where we will get reactive again!
