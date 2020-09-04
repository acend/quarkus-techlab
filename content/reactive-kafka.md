# Reactive Messaging with Kafka

// TODO: Messaging basics

## Setup Kafka Cluster

To setup the Kafka cluster we use the Strimzi operator. First we specify the cluster resource. Create a new file infrastructure/src/main/openshift/kafka/cluster.yml with the follwing content:

```yaml

apiVersion: kafka.strimzi.io/v1beta1
kind: Kafka
metadata:
  name: subzero
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
      transaction.state.log.min.isr: 2
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

This creates a cluster with an active kafka and zookeeper replicas. 

To use kafka as a message broker we need to set up a topic to which we can send and from which we can consume data. Create another resource file infrastructure/src/main/openshift/kafka/manual-topic.yml:

```yaml

apiVersion: kafka.strimzi.io/v1beta1
kind: KafkaTopic
metadata:
  name: manual
  labels:
    strimzi.io/cluster: subzero
spec:
  partitions: 1
  replicas: 1
  config:
    retention.ms: 7200000
    segment.bytes: 1073741824

```







### READ MESSAGES KAFKA


```bash 

oc rsh quarkus-techlab-kafka-0

./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic twitter-inbound --from-beginning

```