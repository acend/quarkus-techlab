# User IDE

IDE for techlab users containing all needed tools. It is based on [codercom/code-server](https://hub.docker.com/r/codercom/code-server), a [VS Code](https://github.com/Microsoft/vscode) accessible through the browser.


## Build

```bash
buildah bud -t user-ide infra/user-ide/
```


## Run

```bash
podman run -d -p 8888:8080 --name=user-ide --rm localhost/user-ide
```

Get the pwd:

```bash
podman exec user-ide /bin/bash -c 'cat ~/.config/code-server/config.yaml'
```

Login to your container using the pwd: <http://localhost:8888/>


## Stop

```bash
podman stop user-ide
```
