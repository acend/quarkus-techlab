---
title: "4.2 OpenShift"
linkTitle: "4.2 OpenShift"
weight: 31
sectionnumber: 4.2
description: >
   Bring our microservices to the OpenShift plattform.
---

## {{% param sectionnumber %}}.1 Defining Resources

To build our simple applications we need for each part an ImageStream, DeploymentConfig and Service. To define the needed resources create a template folder in your applications repository (src/main/openshift/templates) and add a new file to it `data-producer.yml` / `data-consumer.yml`: 

```yml
-- data-consumer.yml

apiVersion: v1
kind: List
metadata:
  labels:
    application: quarkus-techlab
items:

  - apiVersion: v1
    kind: ImageStream
    metadata:
      labels:
        application: quarkus-techlab
      name: data-consumer
  
  - apiVersion: v1
    kind: DeploymentConfig
    metadata:
      labels:
        application: quarkus-techlab
      name: data-consumer
    spec:
      replicas: 1
      selector:
        deploymentConfig: data-consumer
      strategy:
        type: Recreate
      template:
        metadata:
          labels:
            application: quarkus-techlab
            deploymentConfig: data-consumer
        spec:
          containers:
            - image: data-consumer
              imagePullPolicy: Always
              livenessProbe:
                failureThreshold: 5
                httpGet:
                  path: /health
                  port: 8080
                  scheme: HTTP
                initialDelaySeconds: 3
                periodSeconds: 20
                successThreshhold: 1
                timeoutSeconds: 15
              readinessProbe:
                failureThreshold: 5
                httpGet:
                  path: /health
                  port: 8080
                  scheme: HTTP
                initialDelaySeconds: 3
                periodSeconds: 20
                successThreshold: 1
                timeoutSeconds: 15
              name: data-consumer
              port:
                - containerPort: 8080
                  name: http
                  protocol: TCP
              resources:
                limits:
                  cpu: 1
                  memory: 500Mi
                requests:
                  cpu: 50m
                  memory: 100Mi
      triggers:
        - imageChangeParams:
            automatic: true
            containerNames: 
              - data-consumer
            from:
              kind: ImageStreamTag
              name: data-consumer:latest
          type: ImageChange
        - type: ConfigChange
  
  - apiVersion: v1
    kind: Service
    metadata:
      labels:
        application: quarkus-techlab
      name: data-consumer
    spec:
      ports:
        - name: data-consumer-http
          port: 8080
          protocol: TCP
          targetPort: 8080
      selector:
        deploymentConfig: data-consumer
      sessionAffinity: None
      type: ClusterIP

```

This defines a list of OpenShift resources containing the ImageStream, DeploymentConfig and Service for the application. To read more about [OpenShift](https://docs.openshift.com/container-platform/4.5/welcome/index.html) or [Kubernetes](https://kubernetes.io/docs/home/) resources please see the official documentations. 


## {{% param sectionnumber %}} Applying our resources

To continue we will log into our cluster: 

```bash

oc login --server=https://${OCP_URL}>:${OCP_PORT}

```

We then create a new project

```bash

oc new-project quarkus-techlab-userXY
oc project quarkus-techlab-userXY

```

Then we will apply our defined resources with:

```bash

oc apply -f data-consumer/src/main/openshift/templates

```

The output should confirm that the three resources have been generated successfully. 

## Start a deployment

Due to the fact that we defined an trigger on ImageChange in the DeploymentConfig a deployment starts whenever we push or tag a new image to our ImageStream. To deploy our application we simply tag and push our images to the registry.

```bash
// Tag images
docker tag data-producer:native $REGISTRY/$OPENSHIFT_PROJECT/data-producer:latest
docker tag data-consumer:native $REGISTRY/$OPENSHIFT_PROJECT/data-consumer:latest

// TODO: Insert correct registry link
docker login -u $USERNAME $REGISTRY -p $(oc whoami -t)

// Push images
docker push $REGISTRY/$OPENSHIFT_PROJECT/data-producer:latest
docker push $REGISTRY/$OPENSHIFT_PROJECT/data-consumer:latest

```

When the image is pushed OpenShift will automatically rollout your application. To verify your deployment use the oc tool to watch the pods in your project:

```bash

oc get pods -w

```

You will see that two pods will start `data-producer-1-*****` and `data-consumer-1-*****`. After a short time the readiness probes should succeed and both pods will change their status to `Running`, which means your application is ready to use!

Create a route to the data-consumer service with: 

```bash

oc expose svc data-consumer

```

Now test your data-consumer application with:

```bash

 curl http://$(oc get route data-consumer -o go-template --template='{{.spec.host}}')/data

``` 

You should see the response given from the application in the expected format. 

Nice, our application now runs in the clouds!
