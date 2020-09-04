# Reactive Messaging with Kafka in Quarkus

// TODO: KAFKA INTRODUCTION

## Kafka

In this Chapter we want to use Apache Kafka as our message broker. Kafka has some own concepts and introduces a ton of other functionality. But for starters were going to use it as a simple message broker.

### Define Kafka Cluster

In this techlab you are going to set up your own Kafka cluster which will handle your messages. Add the following resource definition to your infrastructure project under `quarkus-techlab-infrastructure/src/main/openshift/kafka`: 

```yaml

apiVersion: kafka.strimzi.io/v1beta1
kind: Kafka
metadata:
  name: quarkus-techlab-user$
spec:
  kafka:
    version: 2.5.0
    replicas: 1
    listeners:
      plain: {}
      tls: {}
    config:
      auto.create.topics.enable: false
      offsets.topic.replication.factor: 1
      transaction.state.log.replication.factor: 1
      transaction.state.log.min.isr: 1
      log.message.format.version: "2.5"
    storage:
      type: jbod
      volumes:
      - id: 0
        type: persistent-claim
        size: 100Gi
        deleteClaim: false
  zookeeper:
    replicas: 1
    storage:
      type: persistent-claim
      size: 100Gi
      deleteClaim: false
  entityOperator:
    topicOperator: {}
    userOperator: {}

```

For starters we need a simple Kafka Topic `manual` which we will use as communication channel to transfer data from one microservice to another. 

```yaml

apiVersion: kafka.strimzi.io/v1beta1
kind: KafkaTopic
metadata:
  name: manual
  labels:
    strimzi.io/cluster: quarkus-techlab-user$
spec:
  partitions: 1
  replicas: 1
  config:
    retention.ms: 7200000
    segment.bytes: 1073741824

```

If you apply these manifests you can see the Kafka cluster appear in your OpenShift project.

```s

oc apply -f quarkus-techlab-infrastructure/src/main/openshift/kafka

```

