#!/bin/bash
set -e

git config --global user.email "josip.grgurica@gmail.com"
git config --global user.name "jkobejs"
git config --global push.default simple

sbt publishMicrosite
