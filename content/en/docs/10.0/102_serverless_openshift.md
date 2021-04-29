---
title: "10.2 OpenShift Serverless with kn"
linkTitle: "10.2 OpenShift Serverless with kn"
weight: 102
sectionnumber: 10.2
description: >
    OpenShift Serverless with the kn CLI
---

## {{% param sectionnumber %}}.1: OpenShift Serverless

The OpenShift Serverless API is built upon the Knative project and provides us a Kubernetes native way to deploy applications and container workloads. For in-depth information consider checking the official [Knative](https://knative.dev/) or [OpenShift Serverless](https://www.openshift.com/learn/topics/serverless) documentations!

Our main goal is to deploy our application as a serverless service waiting for an incoming event, starting and serving our application on demand.

We are going to test two ways how to deploy our serverless applications: First we are trying the Knative `kn` client (CLI) and in a further step we will write our serverless resources by ourselves.


## {{% param sectionnumber %}}.2: Deploy with Knative client `kn`

If you do not already have your Knative client `kn` installed on your machine (you can verify that by testing `$ kn version` in your terminal), download and install the Knative client via your OpenShift GUI.

Start by building a native compiled docker-image of your producer and push the image to your preferred image registry. If you don't have your Dockerhub or Quay account ready you can use our prebuilt image `quay.io/puzzle/quarkus-serverless-producer:rest`.

{{% details title="Hint" %}}

```s

# Build native application

$ ./mvnw clean package -Pnative -Dquarkus.native.container-build=true

# Build docker container

$ docker build -f src/main/docker/Dockerfile.native -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} .

# Push image to registry

$ docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}

```

{{% /details %}}

Make sure you create a new project / namespace for the example below. It will be easier for you to understand the resources created by the `kn` CLI by starting off with a new project.

```s

oc new-project <username>-serverless

```

After releasing your docker image to your preferred docker registry - or not - we start creating our serverless service on OpenShift with the `kn` CLI. Creating a serverless service is pretty straight forward:

```s

$ kn service create <service_name> --image <image> --env <key=value>

# This will create OpenShift resources named <service_name> and referencing the <image>
# The environment variables will be added to your deployment

```

The kn CLI will create all the resources needed for you, so let's try this!

Make sure you are in your desired workspace / project in OpenShift and create your first serverless application! The output should look something like this:

```s

Creating service <service_name> in namespace <project>:

  0.017s The Configuration is still working to reflect the latest desired specification.
  0.062s The Route is still working to reflect the latest desired specification.
  0.107s Configuration <service_name> is waiting for a Revision to become ready.
 15.667s ...
 15.670s Ingress has not yet been reconciled.
 15.671s Waiting for load balancer to be ready
 16.383s Ready to serve.

Service <service_name> created to latest revision <service_name>-revision_number is available at URL: 
<route>

```

You can verify what the `kn` CLI created for you by checking the resources in your project. Inspect all the resources created for your serverless application!

{{% details title="Hint" %}}

```s

# Get all resources in the namespace

$ oc get all

# Describe resource in namespace

$ oc describe <resource_type> <resource_name>

```

{{% /details %}}

You will see that the knative resources are all very similar to the native kubernetes resources. The service (svc) is now a kservice (ksvc), the route will become a serving.knative.dev route (rt) and so on.

Let's test your API: Open two terminals. In the first terminal we can watch either the status of our pods or deplyoments and in the other we can curl against our new serverless data-producer!

```s

# Watch the pods

$ oc get pods -w

# Curl against your API

$ curl $(oc get rt data-producer-serverless -o go-template='{{.status.url}}')/data

```

You will see that as soon as the traffic comes from your request, the knative application will spawn a pod on demand and handle your request. After some delay your application will return a `SensorMeasurement`.

Congratulations, this was your first successful serverless deployment! In the next chapter we are going to try the same thing without relying on our `kn` CLI, stay tuned!
