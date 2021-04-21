---
title: "9.2. Extension functionality"
weight: 920
sectionnumber: 9.2
description: >
  Write the main extension functionality code.
---

Since we have created the extension we can now start to write the functionality that our extension will provide to an
application.


## Extension Functionality

Our extension should provide an endpoint returning some application details. The application is supposed to expose the
following information:

Information      | Source
-----------------|--------------------------------------------
`Build time`     | Collected at build time
`Create time`    | Collected at specific instance creation
`Startup time`   | Collected using Quarkus life cycle event `StartupEvent`
`Current time`   | That one is easy.
`Built for`      | Extension configuration value `quarkus.appinfo.built-for`
`Run by`         | Extension configuration value `quarkus.appinfo.run-by`
`Name`           | Quarkus application name `quarkus.application.name`
`Version`        | Quarkus application version `quarkus.application.version`
`Properties`     | Collected using `ConfigProvider`


### Task {{% param sectionnumber %}}.1 - Add dependencies
