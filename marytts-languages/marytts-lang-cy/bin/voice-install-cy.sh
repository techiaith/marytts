#!/bin/bash
# EXIT ERROR settings 
set -o errexit

DESCRIPTION="Install built voice into a MaryTTS system"
MYDIR="$(dirname "${BASH_SOURCE[0]}")"

NUMARG=1
if [ $# -ne $NUMARG ]
then
  echo "NAME:
        `basename $0`

DESCRIPTION:
    Install built voice into a MaryTTS system

USAGE: 
        `basename $0` [voice_name] 
        
        voice_name: name of voice  

EXAMPLE:
        `basename $0` macsen" 
  exit 1
fi

MYDIR="$(dirname "${BASH_SOURCE[0]}")"
BINDIR="`dirname "$0"`"

VOICE_NAME=$1

PYTHON_SCRIPT="${MYDIR}/python/install-voice.py"

python3 ${PYTHON_SCRIPT} \
	${VOICE_NAME} \
        ${MARYTTS_VERSION} \
        ${MARYTTS_HOME} \
        ${MARYTTS_VOICES_HOME}	
