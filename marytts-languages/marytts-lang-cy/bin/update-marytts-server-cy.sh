#!/bin/bash

mvn install

cp -v ${MARYTTS_CY_HOME}/target/marytts-lang-cy-${MARYTTS_VERSION}.jar ${MARYTTS_HOME}/target/marytts-${MARYTTS_VERSION}/lib
cp -v ${MARYTTS_CY_HOME}/target/marytts-lang-cy-${MARYTTS_VERSION}.jar ${MARYTTS_HOME}/target/marytts-builder-${MARYTTS_VERSION}/lib
cp -v ${MARYTTS_CY_HOME}/marytts-lang-cy-${MARYTTS_VERSION}-component.xml ${MARYTTS_HOME}/target/marytts-${MARYTTS_VERSION}/installed

