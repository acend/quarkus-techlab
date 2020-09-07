# Quarkus Techlab

In this guided hands-on training, we learn how to implement microservices with help of the Quarkus framework.


## Content Sections

The training content resides within the [content](content) directory.

The main part are the labs, which can be found at [content/en/docs](content/en/docs).


## Hugo

This site is built using the static page generator [Hugo](https://gohugo.io/).

The page uses the [docsy theme](https://github.com/google/docsy) which is included as a Git Submodule.

After cloning the main repo, you need to initialize the submodule like this:

```s
git submodule update --init --recursive
```


## Build using Docker

Build the image:

```s
docker build -t puzzle/quarkus-techlab:latest .
```

Run it locally:

```s
docker run -i -p 8080:8080 puzzle/quarkus-techlab
```


### Using Buildah and Podman

Build the image:

```s
buildah build-using-dockerfile -t puzzle/quarkus-techlab:latest .
```

Run it locally with the following command. Beware that `--rmi` automatically removes the built image when the container stops, so you either have to rebuild it or remove the parameter from the command.

```s
podman run --rm --rmi --interactive --publish 8080:8080 localhost/puzzle/quarkus-techlab
```


## How to develop locally

To develop locally we don't want to rebuild the entire container image every time something changed, and it is also important to use the same hugo versions like in production.
We simply mount the working directory into a running container, where hugo is started in the server mode.

```s
export HUGO_VERSION=<version-in-dockerfile>
docker run \
  --rm --interactive \
  --publish 8080:8080 \
  -v $(pwd):/opt/app/src \
  -w /opt/app/src acend/hugo:${HUGO_VERSION} \
  hugo server -p 8080 --bind 0.0.0.0
```


## Linting of Markdown content

Markdown files are linted with <https://github.com/DavidAnson/markdownlint>.
Custom rules are in `.markdownlint.json`.
There's a GitHub Action `.github/workflows/markdownlint.yaml` for CI.
For local checks, you can either use Visual Studio Code with the corresponding extension (markdownlint), or the command line like this:

```s
npm install
npm run mdlint
```


## Contributions

If you find errors, bugs or missing information please help us improve and have a look at the [Contribution Guide](CONTRIBUTING.md).
