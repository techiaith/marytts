#!/bin/sh

##################################
# audio_converter_GUI.sh
# Author: Fabio Tesser
# Email: fabio.tesser@gmail.com
##################################

BINDIR="`dirname "$0"`"
export MARY_BASE="`(cd "$BINDIR"/.. ; pwd)`"
echo $MARY_BASE

java -showversion -Xmx1024m -Dmary.base="$MARY_BASE" -cp "$MARY_BASE/lib/*" marytts.util.data.audio.AudioConverterHeadless $1
