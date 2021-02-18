---
title: "1.1 Twelve-Factor App"
linkTitle: "1.1 Twelve-Factor App"
weight: 110
sectionnumber: 1.1
description: >
  The basics of the Twelve-Factor App manifesto
---

With the move from classical applications to cloud native software-as-a-service applications, the underlying practices of software engineering should change as well. The twelve-factor applications provides a methodology for building modern software systems. The main goals are:

* Use declarative formats for setup automation, to minimize time and cost for new developers joining the project
* Have a clean contract with the underlying operating system, offering maximum portability between execution environments
* Are suitable for deployment on modern cloud platforms, obviating the need for servers and systems administration
* Minimize divergence between development and production, enabling continuous deployment for maximum agility
* And can scale up without significant changes to tooling, architecture, or development practices

The manifesto is held generic so it applies to software systems written in any language, using any tools. The manifesto is split up into 12 factors:


## {{% param sectionnumber %}}.1: Codebase

A twelve-factor application should always be tracked by a version control system (VCS), preferrably Git. Tracking versions and changes in a software system is key to it's success and crucial for deployments.

Individual applications should be held in a single repository which will provide benefits for continuous integration and delivery (CICD) approaches. Every codebasy can lead to multiple instances running on different environments coming from the same deployable.


## {{% param sectionnumber %}}.2: Dependencies

Most programming languages will provide a packaging system to manage your dependencies (Maven for example). A twelve-factor application declares all it's dependencies in their dependency manifest. It further uses a dependency isolation tool during execution to ensure that all dependencies were provided by the application itself and did not leak in from the surrounding system.


## {{% param sectionnumber %}}.3: Config

The twelve-factor app should be parameterizable by config. Deploys (staging, production, ...) should differ only in configuration. The application should have a strict seperation from config to code, config varies across deploys, code does not. Configuration will get overridden by environment variables on the running system.


## {{% param sectionnumber %}}.4: Backing services

Most twelve-factor applications will rely on backing services, examples might be databases, message brokers, SMTP services. These services should be attachable (and also detachable) by configuration. The application makes no distinction between local and third party services. Both are resources accessed via the configured URL or other credentials provided by the config.


## {{% param sectionnumber %}}.5: Build, release, run

The codebase of a twelve-factor application is transformed into a deploy through three stages:

* The build stage is a transform which converts a code repo into an executable bundle known as a build. Using a version of the code at a commit specified by the deployment process, the build stage fetches vendors dependencies and compiles binaries and assets.
* The release stage takes the build produced by the build stage and combines it with the deploy’s current config. The resulting release contains both the build and the config and is ready for immediate execution in the execution environment.
* The run stage (also known as “runtime”) runs the app in the execution environment, by launching some set of the app’s processes against a selected release.

Code becomes a build, which is combined with config to create a release.

The twelve-factor app uses strict separation between the build, release, and run stages. For example, it is impossible to make changes to the code at runtime, since there is no way to propagate those changes back to the build stage.


## {{% param sectionnumber %}}.6: Processes

The app is executed in the execution environment as one or more processes.

In the simplest case, the code is a stand-alone script, the execution environment is a developer’s local laptop with an installed language runtime, and the process is launched via the command line (for example, python my_script.py). On the other end of the spectrum, a production deploy of a sophisticated app may use many process types, instantiated into zero or more running processes.

Twelve-factor processes are stateless and share-nothing. Any data that needs to persist must be stored in a stateful backing service, typically a database.


## {{% param sectionnumber %}}.7: Port binding

Web apps are sometimes executed inside a webserver container. For example, PHP apps might run as a module inside Apache HTTPD, or Java apps might run inside Tomcat.

The twelve-factor app is completely self-contained and does not rely on runtime injection of a webserver into the execution environment to create a web-facing service. The web app exports HTTP as a service by binding to a port, and listening to requests coming in on that port.

In a local development environment, the developer visits a service URL like `http://localhost:5000/` to access the service exported by their app. In deployment, a routing layer handles routing requests from a public-facing hostname to the port-bound web processes.

This is typically implemented by using dependency declaration to add a webserver library to the app, such as Tornado for Python, Thin for Ruby, or Jetty for Java and other JVM-based languages. This happens entirely in user space, that is, within the app’s code. The contract with the execution environment is binding to a port to serve requests.


## {{% param sectionnumber %}}.8: Concurrency

Any computer program, once run, is represented by one or more processes. Web apps have taken a variety of process-execution forms. For example, PHP processes run as child processes of Apache, started on demand as needed by request volume. Java processes take the opposite approach, with the JVM providing one massive uberprocess that reserves a large block of system resources (CPU and memory) on startup, with concurrency managed internally via threads. In both cases, the running process(es) are only minimally visible to the developers of the app.

Scale is expressed as running processes, workload diversity is expressed as process types.

In the twelve-factor app, processes are a first class citizen. Processes in the twelve-factor app take strong cues from the unix process model for running service daemons. Using this model, the developer can architect their app to handle diverse workloads by assigning each type of work to a process type. For example, HTTP requests may be handled by a web process, and long-running background tasks handled by a worker process.


## {{% param sectionnumber %}}.9: Disposability

The twelve-factor app’s processes are disposable, meaning they can be started or stopped at a moment’s notice. This facilitates fast elastic scaling, rapid deployment of code or config changes, and robustness of production deploys.

Processes should strive to minimize startup time. Ideally, a process takes a few seconds from the time the launch command is executed until the process is up and ready to receive requests or jobs. Short startup time provides more agility for the release process and scaling up; and it aids robustness, because the process manager can more easily move processes to new physical machines when warranted.

Processes shut down gracefully when they receive a SIGTERM signal from the process manager. For a web process, graceful shutdown is achieved by ceasing to listen on the service port (thereby refusing any new requests), allowing any current requests to finish, and then exiting. Implicit in this model is that HTTP requests are short (no more than a few seconds), or in the case of long polling, the client should seamlessly attempt to reconnect when the connection is lost.


## {{% param sectionnumber %}}.10: Dev/prod parity

Historically, there have been substantial gaps between development (a developer making live edits to a local deploy of the app) and production (a running deploy of the app accessed by end users). These gaps manifest in three areas:

* The time gap: A developer may work on code that takes days, weeks, or even months to go into production.
* The personnel gap: Developers write code, ops engineers deploy it.
* The tools gap: Developers may be using a stack like Nginx, SQLite, and OS X, while the production deploy uses Apache, MySQL, and Linux.

The twelve-factor app is designed for continuous deployment by keeping the gap between development and production small. Looking at the three gaps described above:

* Make the time gap small: a developer may write code and have it deployed hours or even just minutes later.
* Make the personnel gap small: developers who wrote code are closely involved in deploying it and watching its behavior in production.
* Make the tools gap small: keep development and production as similar as possible.


## {{% param sectionnumber %}}.11: Logs

Logs provide visibility into the behavior of a running app. In server-based environments they are commonly written to a file on disk (a “logfile”); but this is only an output format.

Logs are the stream of aggregated, time-ordered events collected from the output streams of all running processes and backing services. Logs in their raw form are typically a text format with one event per line (though backtraces from exceptions may span multiple lines). Logs have no fixed beginning or end, but flow continuously as long as the app is operating.

A twelve-factor app never concerns itself with routing or storage of its output stream. It should not attempt to write to or manage logfiles. Instead, each running process writes its event stream, unbuffered, to stdout. During local development, the developer will view this stream in the foreground of their terminal to observe the app’s behavior.


## {{% param sectionnumber %}}.12: Admin processes

The process formation is the array of processes that are used to do the app’s regular business (such as handling web requests) as it runs. Separately, developers will often wish to do one-off administrative or maintenance tasks for the app, such as:

* Running database migrations (e.g. manage.py migrate in Django, rake db:migrate in Rails).
* Running a console (also known as a REPL shell) to run arbitrary code or inspect the app’s models against the live database. Most languages provide a REPL by running the interpreter without any arguments (e.g. python or perl) or in some cases have a separate command (e.g. irb for Ruby, rails console for Rails).
* Running one-time scripts committed into the app’s repo (e.g. php scripts/fix_bad_records.php).

One-off admin processes should be run in an identical environment as the regular long-running processes of the app. They run against a release, using the same codebase and config as any process run against that release. Admin code must ship with application code to avoid synchronization issues.
