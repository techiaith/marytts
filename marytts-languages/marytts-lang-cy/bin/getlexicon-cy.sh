#!/bin/bash

#DATA_SRC="http://techiaith.cymru/lts/"

DICT_URL=http://techiaith.cymru/lts/geiriadur-ynganu-bangor/bangordict.dict
LICENSE_URL=http://techiaith.cymru/lts/geiriadur-ynganu-bangor/LICENSE

LEXICON_ROOT="/home/marytts/marytts-languages/marytts-lang-cy/lib/modules/cy/lexicon"

MYDIR="$(dirname "${BASH_SOURCE[0]}")"

#
rm ${LEXICON_ROOT}/bangordict.dict
rm ${LEXICON_ROOT}/LICENSE

# Check if the executables needed for this script are present in the system
command -v wget >/dev/null 2>&1 ||\
 { echo "\"wget\" is needed but not found"'!'; exit 1; }

echo "--- Starting data download ..."
wget -P ${LEXICON_ROOT} -q ${DICT_URL} || { echo "WGET error"'!' ; exit 1 ; }
wget -P ${LEXICON_ROOT} -q ${LICENSE_URL} || { echo "WGET error"'!' : exit 1 ; } 

#
echo "--- Adapting lexicon for MaryTTS ... "
PYTHON_SCRIPT=${MYDIR}/adapt_lexicon.py

cat ${LEXICON_ROOT}/bangordict.dict | uniq | python3 ${PYTHON_SCRIPT} > ${LEXICON_ROOT}/cy.txt
mv phone_set ${LEXICON_ROOT}/bangordict.phones

echo "--- Completed preparing lexicon ---"

