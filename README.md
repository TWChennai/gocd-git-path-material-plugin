# gocd-git-path-material-plugin

GoCD plugin to introduce a material, that watches on a sub-directory of git repository a.k.a GoCD plugin to support for [Monorepos](https://developer.atlassian.com/blog/2015/10/monorepos-in-git/)

[![Build Status](https://travis-ci.org/TWChennai/gocd-git-path-material-plugin.svg?branch=master)](https://travis-ci.org/TWChennai/gocd-git-path-material-plugin)

![gocd-git-path-material-plugin-add](docs/add-material.png)

![gocd-git-path-material-plugin-popup](docs/gitmaterial-popup.png)

### Installation

* Download from [releases](https://github.com/chadlwilson/gocd-git-path-material-plugin/releases/)
* Follow the installation instructions [here](https://docs.go.cd/current/extension_points/plugin_user_guide.html#installing-and-uninstalling-of-plugins)

### Usage

### Via UI

**Known bug/feature:** After plugin installation, you will *not* be able to see *Git Path* material as a material type
when creating a new pipeline. So add a dummy git material, then edit to add a new *Git Path* material and remove the old one. 


### Via pipelines-as-code

For GoCD `>= 19.2.0` via pluggable materials configuration with [gocd-yaml-config-plugin](https://github.com/tomzo/gocd-yaml-config-plugin#pluggable)
```yaml
materials:
  path-filtered-material:
    plugin_configuration:
      id: git-path
    options:
      url: https://github.com/chadlwilson/gocd-git-path-sample.git
      username: username # optional
      path: path1, path2/subpath
      shallow_clone: false # optional
    destination: destDir
```

You can see a sample [here](samples/sample-pipelines.gocd.yaml).

### Contributing

#### Build

execute the following command to build the plugin

```bash
./gradlew clean build
```

#### Docker

You can quickly test the plugin using Docker, ensure you have installed docker, refer [docker installation guide](https://www.docker.com/products/overview) for installing docker for different environments

Execute the following gradle task to start the go-server
```bash
./gradlew clean startGoCd
```

You can now access the [go-server via port 8153](http://localhost:8153)

#### reload

If you like to reload the go-server with new build run,
```bash
./gradlew clean restartGoCd
```

#### stop

You can stop the running docker instance with the following gradle task
```bash
./gradlew clean stopGoCd
```
