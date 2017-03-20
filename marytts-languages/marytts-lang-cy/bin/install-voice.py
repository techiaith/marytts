#!/bin/python3
# -*- coding:utf-8 -*-
import os
import sys
import zipfile
from shutil import copyfile

voicename = sys.argv[1]
marrytts_version = sys.argv[2]
marytts_home=sys.argv[3]

source_package_dir=os.path.join(marytts_home, 'voice_builder', voicename, 'mary', 'voice-' + voicename, 'target')
target_installation_dir=os.path.join(marytts_home, 'target', 'marytts-' + marytts_version)

zippedfile = zipfile.ZipFile(os.path.join(source_package_dir ,'voice-' + voicename + '-' + marytts_version + '.zip'))
zippedfile.extractall(os.path.join(target_installation_dir,'lib'))

copyfile(os.path.join(source_package_dir,'voice-' + voicename + '-' + marytts_version + '-component.xml'), os.path.join(target_installation_dir,'installed'))
#@todo - diweddaru'r XML oddi fewn
