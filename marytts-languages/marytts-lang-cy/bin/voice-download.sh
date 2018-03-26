#!/bin/bash

# EXIT ERROR settings 
set -o errexit

DESCRIPTION="Download a pre-built Welsh voice"
MYDIR="$(dirname "${BASH_SOURCE[0]}")"


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
        `basename $0` wispr" 
  exit 1
fi


VOICE_NAME=$1

PYTHON_SCRIPT="${MYDIR}/download-voice.py"

python3 ${PYTHON_SCRIPT} \
	${VOICE_NAME} \
        ${MARYTTS_VERSION} \
        ${MARYTTS_HOME}	

