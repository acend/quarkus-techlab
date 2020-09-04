# Deployment using Tekton 

Tekton is a Kubernetes-native, continuous integration and delivery (CI/CD) framework that enables you to create containerized, composable, and configurable workloads declaratively through CRDs. 
In this section we are going to define and create pipelines using Tekton to apply our defined manifests, build and deploy our images.

## Pipelines

With OpenShift 4 pipelines come in as a tech-preview feature. They represent a cloud-native CI/CD solution based on Kubernetes resources. Under the hood it uses Tekton building blocks to automate deployment by abstracting away the underlaying implementation details. Pipelines are a serverless system which runs pipelines with all required dependencies isolated in containers. 

Let's dive in and look at the resources. 

OpenShift pipelines provide a set of standard Custom Resource Definitions (CRDs) that act as the building blocks for your pipeline the important concepts and resources are:

### Task

A Task is the smallest configurable unit in a Pipeline. It is essentially a function of inputs and outputs that form the Pipeline build. It can run individually or as a part of a Pipeline. A Pipeline includes one or more Tasks, where each Task consists of one or more steps. Steps are a series of commands that are sequentially executed by the Task.

### Pipeline

A Pipeline consists of a series of Tasks that are executed to construct complex workflows that automate the build, deployment, and delivery of applications. It is a collection of PipelineResources, parameters, and one or more Tasks. A Pipeline interacts with the outside world by using PipelineResources, which are added to Tasks as inputs and outputs.

### PipelineRun

A PipelineRun is the running instance of a Pipeline. A PipelineRun initiates a Pipeline and manages the creation of a TaskRun for each Task being executed in the Pipeline.

### TaskRun

A TaskRun is automatically created by a PipelineRun for each Task in a Pipeline. It is the result of running an instance of a Task in a Pipeline. It can also be manually created if a Task runs outside of a Pipeline.

### PipelineResource

A PipelineResource is an object that is used as an input and output for Pipeline Tasks. For example, if an input is a Git repository and an output is a container image built from that Git repository, these are both classified as PipelineResources. PipelineResources currently support Git resources, Image resources, Cluster resources, Storage Resources and CloudEvent resources.

### Trigger

A Trigger captures an external event, such as a Git pull request and processes the event payload to extract key pieces of information. This extracted information is then mapped to a set of predefined parameters, which trigger a series of tasks that may involve creation and deployment of Kubernetes resources. You can use Triggers along with Pipelines to create full-fledged CI/CD systems where the execution is defined entirely through Kubernetes resources.

### Condition

A Condition refers to a validation or check, which is executed before a Task is run in your Pipeline. Conditions are like if statements which perform logical tests, with a return value of True or False. A Task is executed if all Conditions return True, but if any of the Conditions fail, the Task and all subsequent Tasks are skipped. You can use Conditions in your Pipeline to create complex workflows covering multiple scenarios.

## Creating our pipelines

In this example we are going to build a CI/CD pipeline to apply our current manifests (OpenShift Resources), build our image and push it into our docker-registry. To define a minimal pipeline we need at least a Task, a Pipeline and PipelineResources which are going to be used by our Pipeline.

### Define our first Task

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

### Define the Pipeline

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
    default: src/main/docker/Dockerfile.multistage
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

### Define PipelineResources

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

### Apply the Pipeline

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

### Start and run the pipeline

To run your newly defined Pipeline you will use the Tekton binary (`tkn`). Simply run the following command to start a PipelineRun of your defined Pipeline. 

```s

tkn pipeline start apply-and-build -r git-repo=data-consumer-repo -r image=data-consumer-image -p deployment-name=data-consumer -p docker-file=src/main/docker/Dockerfile.multistage.jvm -s pipeline

```

Tekton will prompt you with a command to follow the logs of the started PipelineRun. If you enter the prompted command you will follow the PipelineRuns logs on your command line. 

## What did just happen

We declared in this chapter our first own CI/CD Pipelines as Kubernetes-native resources. We defined a Pipeline which uses multiple PipelineResources in it's Tasks to apply our manifests, build and deploy our applications. 

Update your microservices, change the infrastructure declaration and test your deployment again!
