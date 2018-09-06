#!/bin/bash

sudo apt-get update
sudo apt-get install -y oracle-java8-jdk maven 

export MARYTTS_VERSION="5.2"
export MARYTTS_HOME="/home/pi/src/marytts"
export MARYTTS_CY_HOME="${MARYTTS_HOME}/marytts-languages/marytts-lang-cy"
export PATH="${PATH}:${MARYTTS_HOME}/target/marytts-builder-5.2/bin:${MARYTTS_CY_HOME}/bin"

