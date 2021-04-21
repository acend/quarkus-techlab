---
title: "5.1 Defining pipelines"
linkTitle: "5.1 Defining pipelines"
weight: 510
sectionnumber: 5.1
description: >
   Defining pipelines for our microservices.
---

In this example we are going to build a CI/CD pipeline to apply our current manifests (OpenShift Resources), build our image and push it into our docker-registry. To define a minimal pipeline we need at least a Task, a Pipeline and PipelineResources which are going to be used by our Pipeline.


## {{% param sectionnumber %}}.1: Define our first Task

Let's define our first Task resource. We talked about applying our manifests to our existing OpenShift namespace. Our Task to apply our manifests will look something like this:

```yaml

apiVersion: tekton.dev/v1alpha1
kind: Task
metadata:
  name: apply-manifests
spec:
  resources:
    inputs:
      - type: git
        name: source
  params:
    - name: manifest_dir
      description: The directory in source that contains yaml manifests
      type: string
      default: "src/main/openshift/templates"
  steps:
    - name: apply
      image: appuio/oc:v4.3
      workingDir: /workspace
      command: ["/bin/bash", "-c"]
      args:
        - |-
          echo Applying manifests in $(inputs.params.manifest_dir) directory
          oc apply -f source/$(inputs.params.manifest_dir)
          echo -----------------------------------

```

We define that we will need a input Resource (PipelineResource) of type git (Git-Repository), we can use a parameter called 'manifest_dir' to define where our templates reside which we are going to apply. In the specification (`spec:`) we define a set of steps which are executed in this specific Task.

In this example Task we can see that we will use a container `appuio/oc:v4.3` which provides us an image with provided OpenShift CLI (`oc`) available. We start the container with the defined arguments which are going to apply our manifests on the used namespace.


## {{% param sectionnumber %}}.2: Define the Pipeline

After we have taken a look at the Task we are going to execute we will take a look at the entire Pipeline. The Pipeline will look like this:

```yaml

apiVersion: tekton.dev/v1alpha1
kind: Pipeline
metadata:
  name: apply-and-build
spec:
  resources:
  - name: git-repo
    type: git
  - name: image
    type: image
  params:
  - name: deployment-name
    type: string
    description: Name of the deployment to be patched
  - name: docker-file
    description: Path to the Dockerfile
    type: string
    default: src/main/docker/Dockerfile.multistage.jvm
  tasks:
  - name: apply-manifests
    taskRef:
      name: apply-manifests
    resources:
      inputs:
      - name: source
        resource: git-repo
  - name: build-image
    taskRef:
      name: buildah
      kind: ClusterTask
    resources:
      inputs:
      - name: source
        resource: git-repo
      outputs:
      - name: image
        resource: image
    runAfter:
    - apply-manifests
    params:
    - name: TLSVERIFY
      value: "false"
    - name: DOCKERFILE
      value: $(params.docker-file)


```

We can see that we are going to use two PipelineResources, a git repository (`{type: git, name: git-repo}`) and a image reference (`{type: image, name: image}`). We can parameterize our Pipeline with the parameters `deployent-name` which will specify the microservice we will build and deploy and the parameter `docker-file` which will be passed into the second step to define where our Dockerfile's location is.
In the `tasks` specification we define which Tasks the Pipeline will execute. We can define the behaviour of execution of these Tasks with additional flags, for example we define that the Task `build-image` will be runned after `apply-manifests` with the `runAfter` element.
You can see now that the workflow passes the defined PipelineResources for one Task to another as defined inputs and outputs.
The Task `build-image` is a predefined ClusterTask which comes with the Tekton Operator installation on our OpenShift cluster.


## {{% param sectionnumber %}}.3: Define PipelineResources

We already defined the Pipeline and Tasks which we are going to use to build and deploy our Quarkus application. The missing parts are the PipelineResources we used in our Pipeline defined in the specification of our Pipeline and Tasks. Let's create these PipelineResources so we can test our defined Pipeline.

```yaml

apiVersion: v1
kind: List
metadata:
  labels:
    application: quarkus-techlab
items:
- apiVersion: tekton.dev/v1alpha1
  kind: PipelineResource
  metadata:
    name: data-producer-repo
  spec:
    type: git
    params:
    - name: url
      value: https://github.com/g1raffi/quarkus-techlab-data-producer.git
- apiVersion: tekton.dev/v1alpha1
  kind: PipelineResource
  metadata:
    name: data-producer-image
  spec:
    type: image
    params:
    - name: url
      value: image-registry.openshift-image-registry.svc:5000/quarkus-techlab/data-producer:latest
- apiVersion: tekton.dev/v1alpha1
  kind: PipelineResource
  metadata:
    name: data-consumer-repo
  spec:
    type: git
    params:
    - name: url
      value: https://github.com/g1raffi/quarkus-techlab-data-consumer.git
- apiVersion: tekton.dev/v1alpha1
  kind: PipelineResource
  metadata:
    name: data-consumer-image
  spec:
    type: image
    params:
    - name: url
      value: image-registry.openshift-image-registry.svc:5000/quarkus-techlab/data-consumer:latest

```

This defines a List of PipelineResources with four elements, for each application we define a git-repository of type `git` and an image reference of type `image`.


## {{% param sectionnumber %}}.4: Apply the Pipeline

Create a directory / module to hold your infrastructure objects `quarkus-techlab-infrastructure` and save these resource definitions under `quarkus-techlab-infrastructure/src/main/openshift/tekton`. Make sure you are in your defined namespace and apply these resources:

```s

oc apply -f quarkus-techlab-infrastructure/src/main/openshift/tekton

```

You will see that the the resources will be created.

```s

$ oc apply -f quarkus-techlab-infrastructure/src/main/openshift/tekton

pipeline.tekton.dev/apply-and-build created
task.tekton.dev/apply-manifests created
pipelineresource.tekton.dev/data-producer-repo created
pipelineresource.tekton.dev/data-producer-image created
pipelineresource.tekton.dev/data-consumer-repo created
pipelineresource.tekton.dev/data-consumer-image created

```


## {{% param sectionnumber %}}.5: Start and run the pipeline

To run your newly defined Pipeline you will use the Tekton binary (`tkn`). Simply run the following command to start a PipelineRun of your defined Pipeline.

First add a multistage Dockerfile for JVM builds to your two repositories:

```Dockerfile
# quarkus-techlab-data-consumer/src/main/docker/Dockerfile.multistage.jvm

## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/centos-quarkus-maven:20.1.0-java11 AS build
COPY pom.xml /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml -B de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies
COPY src /usr/src/app/src
USER root
RUN chown -R quarkus /usr/src/app
USER quarkus
RUN mvn -f /usr/src/app/pom.xml clean package

## Stage 2 : create the docker final image
FROM registry.access.redhat.com/ubi8/ubi-minimal:8.1

ARG JAVA_PACKAGE=java-11-openjdk-headless
ARG RUN_JAVA_VERSION=1.3.8

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# Install java and the run-java script
# Also set up permissions for user `1001`
RUN microdnf install curl ca-certificates ${JAVA_PACKAGE} \
    && microdnf update \
    && microdnf clean all \
    && mkdir /deployments \
    && chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments \
    && curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o /deployments/run-java.sh \
    && chown 1001 /deployments/run-java.sh \
    && chmod 540 /deployments/run-java.sh \
    && echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/lib/security/java.security

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

COPY --from=build /usr/src/app/target/lib/* /deployments/lib/
COPY --from=build /usr/src/app/target/*-runner.jar /deployments/app.jar

EXPOSE 8080
USER 1001

ENTRYPOINT [ "/deployments/run-java.sh" ]

```

```s

tkn pipeline start apply-and-build -r git-repo=data-consumer-repo -r image=data-consumer-image -p deployment-name=data-consumer -p docker-file=src/main/docker/Dockerfile.multistage.jvm -s pipeline

```

Tekton will prompt you with a command to follow the logs of the started PipelineRun. If you enter the prompted command you will follow the PipelineRuns logs on your command line.


## {{% param sectionnumber %}}.6: What did just happen

We declared in this chapter our first own CI/CD Pipelines as Kubernetes-native resources. We defined a Pipeline which uses multiple PipelineResources in it's Tasks to apply our manifests, build and deploy our applications.

Update your microservices, change the infrastructure declaration and test your deployment again!
