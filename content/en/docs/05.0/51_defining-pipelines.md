---
title: "5.1 Defining pipelines"
linkTitle: "5.1 Defining pipelines"
weight: 510
sectionnumber: 5.1
description: >
   Defining pipelines for our microservices.
---

It is time to automate the deployment of our Quarkus application to OpenShift by using OpenShift Pipelines. OpenShift Pipelines are based on [Tekton](https://tekton.dev/).


## Task {{% param sectionnumber %}}.1: Basic Concepts

Tekton makes use of several Kubernetes [custom resources (CRD)](https://kubernetes.io/docs/concepts/extend-kubernetes/api-extension/custom-resources/).

These CRDs are:

* *[Task](https://github.com/tektoncd/pipeline/blob/master/docs/tasks.md)*: A collection of steps that perform a specific task.
* *[Pipeline](https://github.com/tektoncd/pipeline/blob/master/docs/pipelines.md)*: A series of tasks, combined to work together in a defined (structured) way
* *[TaskRun](https://github.com/tektoncd/pipeline/blob/master/docs/taskruns.md)*: The execution and result of running an instance of a task
* *[PipelineRun](https://github.com/tektoncd/pipeline/blob/master/docs/pipelineruns.md)*: The actual execution of a whole Pipeline, containing the results of the pipeline (success, failed...)

Pipelines and tasks should be generic and must never define possible variables - such as 'input git repository' - directly in their definition. The concrete PipelineRun will get the parameters, that are being used inside the pipeline.

[Workspaces](https://redhat-scholars.github.io/tekton-tutorial/tekton-tutorial/workspaces.html) are used to share the data between Tasks and Steps.

![Static Pipeline Definition](../pipeline-static-definition.png)
*Static definition of a Pipeline*

For each task, a pod will be allocated and for each step inside this task, a container will be used.

![Pipeline Runtime View](../pipeline-runtime-view.png)
*Runtime view of a Pipeline showing mapping to pods and containers*


## Task {{% param sectionnumber %}}.3: Tekton CLI tkn

For additional features, we are going to add another CLI that eases access to the Tekton resources and gives you more direct access to the OpenShift Pipeline semantics:

Verify tkn version by running:

```bash
tkn version
```

```
Client version: 0.13.1
Pipeline version: unknown
Triggers version: unknown
```


## Task {{% param sectionnumber %}}.4: Create Pipeline tasks

A Task is the smallest block of a Pipeline, which by itself can contain one or more steps. These steps are executed to process a specific element. For each task, a pod is allocated and each step is running in a container inside this pod. Tasks are reusable by other Pipelines. _Input_ and _Output_ specifications can be used to interact with other tasks.

{{% alert title="Note" color="primary" %}}
You can find more examples of reusable tasks in the [Tekton Catalog](https://github.com/tektoncd/catalog) and [OpenShift Catalog](https://github.com/openshift/pipelines-catalog) repositories.
{{% /alert %}}

Let's examine the task that does a deployment. Create the local file `<workspace>/deploy-task.yaml` with the following content:

{{< highlight yaml >}}{{< readfile file="solution/tekton/pipeline/deploy-task.yaml" >}}{{< /highlight >}}

[source](https://raw.githubusercontent.com/puzzle/quarkus-techlab/master/solution/tekton/pipeline/deploy-task.yaml)

Let's create the task.

{{% details title="command hint" mode-switcher="normalexpertmode" %}}

```bash
oc apply -f deploy-task.yaml
```

{{% /details %}}

Expected output:

```
tkn task.tekton.dev/apply-manifests created
```

Using the Tekton CLI, verify that the task has been created:

```bash
tkn task ls
```

```
NAME              DESCRIPTION   AGE
apply-manifests                 19 seconds ago
```


## Task {{% param sectionnumber %}}.5: Create a Pipeline

A pipeline is a set of tasks, which should be executed in a defined way to achieve a specific goal.

It first uses the Task *git-clone*, which is a default task the OpenShift operator created automatically. This task will check out the defined git repository with the needed Dockerfile. The next task *buildah* will build the image. The resulted image is pushed to an image registry, defined by the *image-name* parameter. After that, the created tasks *apply-manifest* is executed. The execution order of these tasks is defined with the *runAfter* Parameter in the YAML definition.

{{% alert title="Note" color="primary" %}}
The Pipeline should be reusable across multiple projects or environments, that's why the resources (git-repo and image) are not defined here. When a Pipeline is executed, these resources will get defined by the parameters.
{{% /alert %}}

Create the following pipeline `<workspace>/deploy-pipeline.yaml`:

{{< highlight yaml >}}{{< readfile file="solution/tekton/pipeline/deploy-pipeline.yaml" >}}{{< /highlight >}}

[source](https://raw.githubusercontent.com/puzzle/quarkus-techlab/master/solution/tekton/pipeline/deploy-pipeline.yaml)

Create the Pipeline.

{{% details title="command hint" mode-switcher="normalexpertmode" %}}

```bash
oc apply -f deploy-pipeline.yaml
```

{{% /details %}}

which will result in: `pipeline.tekton.dev/build-and-deploy created`

Using the Tekton CLI, verify that the Pipeline has been created:

```bash
tkn pipeline ls
```

```
NAME               AGE              LAST RUN   STARTED   DURATION   STATUS
build-and-deploy   19 seconds ago   ---        ---       ---        ---
```


## Task {{% param sectionnumber %}}.6: Prepare persistent workspace

The data for the tasks is shared by a common workspace. We use a [Persistent Volume](https://kubernetes.io/docs/concepts/storage/persistent-volumes/), short PV, to back our workspace and make it persistent. The PV is requested by a [Persistent Volume Claim](https://kubernetes.io/docs/concepts/storage/volumes/#persistentvolumeclaim), short PVC.

Create the following resource definition for a PVC inside `<workspace>/workspaces-pvc.yaml`:

{{< highlight yaml >}}{{< readfile file="solution/tekton/pipeline/workspaces-pvc.yaml" >}}{{< /highlight >}}

[source](https://raw.githubusercontent.com/puzzle/quarkus-techlab/master/solution/tekton/pipeline/workspaces-pvc.yaml)

Create the PVC.

{{% details title="command hint" mode-switcher="normalexpertmode" %}}

```bash
oc apply -f workspaces-pvc.yaml
```

{{% /details %}}

which will result in: `persistentvolumeclaim/pipeline-workspace created`

Now we are ready to build and deploy our new microservice using a Tekton pipeline.


## Task {{% param sectionnumber %}}.7: Trigger Pipeline

After the Pipeline has been created, it can be triggered to execute the tasks.
Since the Pipeline is generic, we have to provide the concrete values.

Following parameters are needed to configure the pipeline to deploy the data-consumer component:

* git-url: Git repository
* git-revision: revision (branch/tag) of the repository
* docker-file: path to the Dockerfile inside the repository
* image-name: image name (incl. registry) of the resulting image
* manifest-dir: path to directory inside the repository that contains the yaml manifests


### Create PipelineRun Resources

Creating PipelineRun Resources will trigger the pipeline.

{{% alert title="Note" color="primary" %}}
We use a template to adapt the image registry URL to match to your project.
{{% /alert %}}

Create the following openshift template `<workspace>/pipeline-run-template.yaml`:

{{< highlight yaml >}}{{< readfile file="solution/tekton/pipeline/pipeline-run-template.yaml" >}}{{< /highlight >}}

Create the PipelineRun by processing the template and creating the generated resources:

```bash
oc process -f pipeline-run-template.yaml \
  --param=PROJECT_NAME=$(oc project -q) \
| oc apply -f-
```

which will result in: `pipelinerun.tekton.dev/build-and-deploy-run-1 created`

This will create and execute a PipelineRun. Use the command `tkn pipelinerun logs build-and-deploy-run-1 -f -n $NAMESPACE` to display the logs.

The PipelineRuns can be listed with:

```bash
tkn pipelinerun ls
```

```
NAME                     STARTED          DURATION    STATUS
build-and-deploy-run-1   3 minutes ago    1 minute    Succeeded
```

Moreover, the logs can be viewed with the following command and selecting the appropriate Pipeline and PipelineRun:

```bash
tkn pipeline logs
```


### Execute Pipelines using tkn

Alternatively we can also trigger a Pipeline using the tkn cli.

Start the Pipeline for the data-consumer:

```bash

tkn pipeline start build-and-deploy \
  -p git-url='https://github.com/puzzle/quarkus-techlab.git' \
  -p git-revision='master' \
  -p docker-file='src/main/docker/Dockerfile.multistage.jvm' \
  -p image-name="image-registry.openshift-image-registry.svc:5000/$(oc project -q)/data-consumer:latest" \
  -p manifest-dir='solution/tekton/manifests/data-consumer.yaml' \
  -p deployment-name=data-consumer \
  -s pipeline \
  -w name=source-workspace,claimName=pipeline-workspace

```

This will create and execute a PipelineRun. Use the same commands as listed above to check the progress of the run.


## Task {{% param sectionnumber %}}.8: OpenShift WebUI

Go tho the developer view of the WebUI of OpenShift and select your pipeline project.

Do you remember that you did not create any Deployment for your application? That has been done by your Tekton pipeline.

With the OpenShift Pipeline operator, a new menu item is introduced to the WebUI of OpenShift named Pipelines. All Tekton CLI commands, which are used above, could be replaced with the web interface. The big advantage is the graphical presentation of Pipelines and their lifetime.


### Checking your application

Check the logs of your data-consumer microservice. You will see that your microservice started. Restart the pipeline with the correct parameters to deploy the data-producer as well!

{{% details title="command hint" mode-switcher="normalexpertmode" %}}

```bash

tkn pipeline start build-and-deploy \
  -p git-url='https://github.com/puzzle/quarkus-techlab.git' \
  -p git-revision='master' \
  -p docker-file='src/main/docker/Dockerfile.multistage.jvm' \
  -p image-name="image-registry.openshift-image-registry.svc:5000/$(oc project -q)/data-producer:latest" \
  -p manifest-dir='solution/tekton/manifests/data-producer.yaml' \
  -p deployment-name=data-producer \
  -s pipeline \
  -w name=source-workspace,claimName=pipeline-workspace
  
```

{{% /details %}}


## High quality and secure Pipeline

This was just an example for a pipeline, that builds and deploys a container image to OpenShift. There are lots of security features missing.

Check out the Puzzle [delivery pipeline concept](https://github.com/puzzle/delivery-pipeline-concept) for further information.


## Links and Sources

* [Tekton](https://tekton.dev/)
* [Understanding OpenShift Pipelines](https://docs.openshift.com/container-platform/latest/pipelines/understanding-openshift-pipelines.html)
* [Creating CI/CD solutions for applications using OpenShift Pipelines](https://docs.openshift.com/container-platform/latest/pipelines/creating-applications-with-cicd-pipelines.html)
* [Pipeline-Tutorial](https://github.com/openshift/pipelines-tutorial/)
* [Interactive OpenShift Pipelines tutorial](https://learn.openshift.com/middleware/pipelines/)on [learn.openshift.com](https://learn.openshift.com/)


## Solution

The needed resource files are available inside the folder [solution/tekton/pipeline/](https://github.com/puzzle/quarkus-techlab/tree/master/solution/tekton/pipeline/) of the techlab [github repository](https://github.com/puzzle/quarkus-techlab).

If you weren't successful, you can update your project with the solution by cloning the Techlab Repository `git clone https://github.com/puzzle/quarkus-techlab.git` and executing this command:

```s
oc apply -f solution/tekton/pipeline
```
