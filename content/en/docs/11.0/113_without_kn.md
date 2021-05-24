---
title: "11.3 Serverless without kn CLI"
linkTitle: "11.3 Serverless without kn CLI"
weight: 1130
onlyWhenNot: mobi
sectionnumber: 11.3
description: >
    OpenShift Serverless without kn CLI
---

## {{% param sectionnumber %}}.1: Knative Serving CRD

In the previous chapter we created a serverless application from scratch. We used the knative CLI `kn` to create our resources. In reality when managing your applications such tools will bring further complexity to your project after the initial startup. Altering resources without knowing about the underlying structure will become almost impossible. We will create the same serverless application without using the `kn` CLI in this short chapter.

As mentioned in the previous chapter the Knative project and OpenShift Serverless will provide a set of custom resource definitions (CRD) for you to maintain the state of your serverless application:

* Service: The `service.serving.knative.dev` (ksvc) CRD manages the life cycle of your workload and creates the network abstraction. It creates a route, a configuration, and a new revision for each change. Usually the ksvc is the most used and modified custom resource in a serverless application.
* Revision: The `revision.serving.knative.dev` CRD is a snapshot of the code and configuration for each modification made to the workload. Revisions are immutable objects.
* Route: The `route.serving.knative.dev` CRD maps a network endpoint to one or more revisions. You can manage the traffic in several ways, including functional traffic and named routes.
* Configuration: The `configuration.serving.knative.dev` CRD maintains the desired state of your deployment. It provides a clean separation between code and configuration. Modifying a configuration creates a new revision.


## {{% param sectionnumber %}}.2: Your application

As read from the descriptions above, the only custom resource we do have to maintain and define is the `service.serving.knative.dev` (ksvc). So let's inspect our service generated from the kn CLI.

```s

# Display the service in yaml output

$ oc get ksvc data-producer-serverless -o yaml

```

When you look at the resource definition you will recognize the similarity with resources you're already familiar with.

We can delete the knative service and all the connected resources will be cleaned up as well.

```s

# Delete the knative service

$ oc delete ksvc data-producer-serverless

 ```

Let us create the resource definition by hand. You will define a `service.serving.knative.dev` and the only two things you need to worry about defining are the image and the container port. Try to write the definition and create the resource on the cluster. Then test your API if it still works.

{{% details title="Hint" %}}

```yaml

apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: data-producer-serverless
spec:
  template:
    spec:
      containers:
        - image: quay.io/puzzle/quarkus-serverless-producer:rest
          ports:
            - containerPort: 8080

```

{{% /details %}}

Your API should deliver the same response as before. And that's how you can create and deploy your serverless application without relying on the Knative CLI `kn`.
