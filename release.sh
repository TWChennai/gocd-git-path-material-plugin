#!/bin/bash

source token.sh

filename="linux-amd64-github-release.tar.bz2"
os="linux"

if [ "$(uname)" == "Darwin" ]; then
    filename="darwin-amd64-github-release.tar.bz2"
    os="darwin"
fi

if [ ! -f  "./bin/${os}/amd64/github-release" ]; then
    if [ ! -f ${filename} ]; then
        wget https://github.com/aktau/github-release/releases/download/v0.6.2/${filename} -O ${filename}
    fi
    tar xf ${filename}
fi

VERSION="1.2.3"
GIT_HUB_RELEASE_BIN="./bin/${os}/amd64/github-release"

case "$1" in
  release)
    ${GIT_HUB_RELEASE_BIN} release \
      --user TWChennai \
      --repo gocd-git-path-material-plugin \
      --tag ${VERSION} \
      --name ${VERSION} \
      --pre-release;;
  upload)
    ${GIT_HUB_RELEASE_BIN} upload \
      --user TWChennai \
      --repo gocd-git-path-material-plugin \
      --tag ${VERSION} \
      --name "gocd-git-path-material-plugin-$VERSION.jar" \
      --file build/libs/gocd-git-path-material-plugin-${VERSION}.jar;;
  *)
    echo "usage release.sh release | upload"
esac
