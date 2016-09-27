# gocd-git-path-material-plugin

GoCD plugin to introduce a material, that watches on a sub-directory of git repository. 


[![Build Status](https://snap-ci.com/TWChennai/gocd-git-path-material-plugin/branch/master/build_image)](https://snap-ci.com/TWChennai/gocd-git-path-material-plugin/branch/master)


### Installation

Have a look [here](https://docs.go.cd/current/extension_points/plugin_user_guide.html)

**Known bug/feature:** After plugin installation, you will be able to see *GitPathMaterial* as a material type only in edit. So add a git material when you add new pipeline, edit to add new *GitPathMaterial* and remove the old one. 
