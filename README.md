# Quarkus Techlab

In this guided hands-on training, we learn how to implement microservices with help of the Quarkus framework.


## Content Sections

The training content resides within the [content](content) directory.

The main part are the labs, which can be found at [content/en/docs](content/en/docs).


## Hugo

This site is built using the static page generator [Hugo](https://gohugo.io/).

The page uses the [docsy theme](https://github.com/google/docsy) which is included as a Git Submodule.
Docsy is being enhanced using [docsy-plus](https://github.com/acend/docsy-plus/) as well as
[docsy-puzzle](https://github.com/puzzle/docsy-puzzle/)
for brand specific settings.

After cloning the main repo, you need to initialize the submodule like this:

```bash
git submodule update --init --recursive
```

The default configuration uses the Puzzle setup from [config/_default](config/_default/config.toml).
Alternatively you can use the Mobi setup from [config/mobi](config/mobi/config.toml), which is enabled with
`--environment mobi`.


### Docsy theme usage

* [Official docsy documentation](https://www.docsy.dev/docs/)
* [Docsy Plus](https://github.com/puzzle/docsy-plus/)


### Update submodules for theme updates

Run the following command to update all submodules with their newest upstream version:

```bash
git submodule update --remote
git pull --recurse-submodules
```


## Build using Docker

Build the image:

```bash
docker build [--build-arg TRAINING_HUGO_ENV=...] -t puzzle/quarkus-techlab:latest .
```

Run it locally:

```bash
docker run -i -p 8080:8080 puzzle/quarkus-techlab
```


### Using Buildah and Podman

Build the image:

```bash
buildah build-using-dockerfile -t puzzle/quarkus-techlab:latest .
```

Run it locally with the following command. Beware that `--rmi` automatically removes the built image when the container stops, so you either have to rebuild it or remove the parameter from the command.

```bash
podman run --rm --rmi --interactive --publish 8080:8080 localhost/puzzle/quarkus-techlab
```


## How to develop locally

To develop locally we don't want to rebuild the entire container image every time something changed, and it is also important to use the same hugo versions like in production.
We simply mount the working directory into a running container, where hugo is started in the server mode.

```bash
export HUGO_VERSION=$(grep "FROM klakegg/hugo" Dockerfile | sed 's/FROM klakegg\/hugo://g' | sed 's/ AS builder//g')
docker run --rm --interactive --publish 8080:8080 -v $(pwd):/src klakegg/hugo:${HUGO_VERSION} server -p 8080 --bind 0.0.0.0
```

use the following command to set the hugo environment

```bash
export HUGO_VERSION=$(grep "FROM klakegg/hugo" Dockerfile | sed 's/FROM klakegg\/hugo://g' | sed 's/ AS builder//g')
docker run --rm --interactive --publish 8080:8080 -v $(pwd):/src klakegg/hugo:${HUGO_VERSION} server --environment=<environment> -p 8080 --bind 0.0.0.0
```


## Linting of Markdown content

Markdown files are linted with <https://github.com/DavidAnson/markdownlint>.
Custom rules are in `.markdownlint.json`.
There's a GitHub Action `.github/workflows/markdownlint.yaml` for CI.
For local checks, you can either use Visual Studio Code with the corresponding extension, or the command line like this:

```shell script
npm install
npm run mdlint
```

Npm not installed? no problem

```bash
export HUGO_VERSION=$(grep "FROM klakegg/hugo" Dockerfile | sed 's/FROM klakegg\/hugo://g' | sed 's/ AS builder//g')
docker run --rm --interactive -v $(pwd):/src klakegg/hugo:${HUGO_VERSION}-ci /bin/bash -c "set -euo pipefail;npm install; npm run mdlint;"
```


## Github Actions


### Build

The [build action](.github/workflows/build.yaml) is fired on Pull Requests does the following

* builds all PR Versions (Linting and Docker build)
* deploys the built container images to the container registry
* Deploys a PR environment in a k8s test namespace with helm
* Triggers a redeployment
* Comments in the PR where the PR Environments can be found


### PR Cleanup

The [pr-cleanup action](.github/workflows/pr-cleanup.yaml) is fired when Pull Requests are closed and does the following

* Uninstalls PR Helm Release


### Push Main

The [push main action](.github/workflows/push-main.yaml) is fired when a commit is pushed to the main branch (eg. a PR is merged) and does the following, it's very similar to the Build Action

* builds main Versions (Linting and Docker build)
* deploys the built container images to the container registry
* Deploys the main Version on k8s using helm
* Triggers a redeployment


## Helm

Manually deploy the training Release using the following command:

```bash
helm install --repo https://acend.github.io/helm-charts/  <release> acend-training-chart --values helm-chart/values.yaml -n <namespace>
```

For debugging purposes use the `--dry-run` parameter

```bash
helm install --dry-run --repo https://acend.github.io/helm-charts/  <release> acend-training-chart --values helm-chart/values.yaml -n <namespace>
```


## Contributions

If you find errors, bugs or missing information please help us improve and have a look at the [Contribution Guide](CONTRIBUTING.md).
