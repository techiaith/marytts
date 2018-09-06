#!/bin/python3
# -*- coding:utf-8 -*-
import os
import os.path
import sys
import zipfile
from shutil import copyfile
import wget

DOWNLOAD_URL = 'http://techiaith.cymru/marytts/';

voicename = sys.argv[1]
marytts_version = sys.argv[2]
marytts_home = sys.argv[3]

zipfilename = 'voice-' + voicename + '-' + marytts_version + '.zip'
componentxmlfilename = 'voice-' + voicename + '-' + marytts_version + '-component.xml'

wget.download(DOWNLOAD_URL + zipfilename)
wget.download(DOWNLOAD_URL + componentxmlfilename)

source_dir = os.getcwd()

#
target_installation_dir = os.path.join(marytts_home, 'target', 'marytts-' + marytts_version)

zipfileloc = os.path.join(source_dir, 'voice-' + voicename + '-' + marytts_version + '.zip')
zippedfile = zipfile.ZipFile(zipfileloc)
zippedfile.extractall(target_installation_dir)

#
copyfile(os.path.join(source_dir, componentxmlfilename), os.path.join(target_installation_dir,'installed', 'voice-' + voicename + '-' + marytts_version + '-component.xml'))

