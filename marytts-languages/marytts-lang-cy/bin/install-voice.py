#!/bin/python3
# -*- coding:utf-8 -*-
import os
import sys
import zipfile
import xml.etree.ElementTree as ET
from shutil import copyfile

voicename = sys.argv[1]
marytts_version = sys.argv[2]
marytts_home = sys.argv[3]

source_package_dir=os.path.join(marytts_home, 'voice-builder', voicename, 'mary', 'voice-' + voicename, 'target')
target_installation_dir=os.path.join(marytts_home, 'target', 'marytts-' + marytts_version)

zipfileloc = os.path.join(source_package_dir, 'voice-' + voicename + '-' + marytts_version + '.zip')

zippedfile = zipfile.ZipFile(zipfileloc)
zippedfile.extractall(target_installation_dir)

xml_component_file = os.path.join(source_package_dir, 'voice-' + voicename + '-' + marytts_version + '-component.xml')
tree = ET.parse(xml_component_file)
marytts_install = tree.getroot()

ns = {'ns0':'http://mary.dfki.de/installer'}
voice = marytts_install.find('ns0:voice', ns)
voice_files = voice.find('ns0:files', ns)

voice.set('name', voicename)
voice_files.text = (' ').join(zippedfile.namelist())

xml_component_out_file = os.path.join(source_package_dir, 'voice-' + voicename + '-' + marytts_version + '-component.mod.xml')

tree.write(open(xml_component_out_file,'w'), encoding='unicode')

copyfile(xml_component_out_file, \
         os.path.join(target_installation_dir,'installed', 'voice-' + voicename + '-' + marytts_version + '-component.xml'))
