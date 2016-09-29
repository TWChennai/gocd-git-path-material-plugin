# gocd-git-path-material-plugin

GoCD plugin to introduce a material, that watches on a sub-directory of git repository. 


[![Build Status](https://snap-ci.com/TWChennai/gocd-git-path-material-plugin/branch/master/build_image)](https://snap-ci.com/TWChennai/gocd-git-path-material-plugin/branch/master)


### Installation

Have a look [here](https://docs.go.cd/current/extension_points/plugin_user_guide.html)

**Known bug/feature:** After plugin installation, you will be able to see *GitPathMaterial* as a material type only in edit. So add a git material when you add new pipeline, edit to add new *GitPathMaterial* and remove the old one. 


### Contributing

#### Build

execute the following command to build the plugin

```bash
./gradlew clean build
```

#### Vagrant

You can quickly test the plugin by using vagrant, ensure you have installed `vagrant`, refer [vagrant installation guide](https://www.vagrantup.com/docs/installation/) for installing vagrant for different environments


### Ansible

Ansible is used to provision the the vagrant machine with [go-server](https://www.go.cd/) along with [git](https://git-scm.com/) and [gocd-git-path-material-plugin](https://github.com/TWChennai/gocd-git-path-material-plugin) 


Execute to start the go-server

```bash
vagrant up --provision
```

You can now access the [go-server via port 8153](http://localhost:8153)


### reload

If you like to reload the go-server with new build run,

```
./gradlew clean reload
```
