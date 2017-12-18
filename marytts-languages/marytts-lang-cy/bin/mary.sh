#!/bin/bash 

VERSION=5.2

ps auxw | grep marytts-server | grep -v grep

if [ $? != 0 ]
then
	bash /home/pi/src/marytts/target/marytts-$VERSION/bin/marytts-server -Xmx1g
fi

