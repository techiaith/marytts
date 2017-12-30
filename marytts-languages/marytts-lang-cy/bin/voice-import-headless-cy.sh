#!/bin/bash

MARYTTS_VERSION="5.2"
MARYTTS_HOME="/home/marytts"
MARYTTS_CY_HOME="/home/marytts/marytts-languages/marytts-lang-cy"

${MARYTTS_HOME}/target/marytts-${MARYTTS_VERSION}/bin/marytts-server &

voiceimport_headless.sh
