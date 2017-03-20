##!/bin/sh

# EXIT ERROR settings 
set -o errexit

DESCRIPTION="Install built voice into a MaryTTS system"
MYDIR="$(dirname "${BASH_SOURCE[0]}")"

MARYTTS_VERSION="5.2"
MARYTTS_HOME="/home/marytts"
MARYTTS_CY_HOME="/home/marytts/marytts-languages/marytts-lang-cy"


NUMARG=1
if [ $# -ne $NUMARG ]
then
  echo "NAME:
        `basename $0`

DESCRIPTION:
    $DESCRIPTION

USAGE: 
        `basename $0` [voice_name] 
        
        voice_name: name of voice  

EXAMPLE:
        `basename $0` macsen" 
  exit 1
fi


BINDIR="`dirname "$0"`"
export MARY_BASE="`(cd "$BINDIR"/.. ; pwd)`"

VOICE_NAME=$2

PYTHON_SCRIPT="${MYDIR}/install-voice.py"

python3 ${PYTHON_SCRIPT} \
	${VOICE_NAME} \
        ${MARYTTS_VERSION} \
        ${MARYTTS_HOME}	
