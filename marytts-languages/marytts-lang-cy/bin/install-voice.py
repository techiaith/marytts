#!/bin/python3
# -*- coding:utf-8 -*-
import os
import os.path
import sys
import zipfile
import xml.etree.ElementTree as ET
from shutil import copyfile

DOWNLOAD_URL = 'http://techiaith.cymru/marytts/';

voicename = sys.argv[1]
marytts_version = sys.argv[2]
marytts_home = sys.argv[3]

zipfilename = 'voice-' + voicename + '-' + marytts_version + '.zip'
componentxmlfilename = 'voice-' + voicename + '-' + marytts_version + '-component.xml'

source_package_dir = os.path.join(marytts_home, 'mary', 'voice-' + voicename, 'target')

print ("Chwilio am ffeiliau llais yn : %s" % source_package_dir)

#
target_installation_dir = os.path.join(marytts_home, 'target', 'marytts-' + marytts_version)

print ("Gosod llais yn : %s " % target_installation_dir)

zipfileloc = os.path.join(source_package_dir, zipfilename)
zippedfile = zipfile.ZipFile(zipfileloc)
zippedfile.extractall(target_installation_dir)

#
source_xml_component_file = os.path.join(source_package_dir, 'voice-' + voicename + '-' + marytts_version + '-component.xml')
tree = ET.parse(source_xml_component_file)
marytts_install = tree.getroot()

ns = {'ns0':'http://mary.dfki.de/installer'}
voice = marytts_install.find('ns0:voice', ns)
voice_license = voice.find('ns0:license', ns)
voice_package = voice.find('ns0:package', ns)
package_location = voice_package.find('ns0:location', ns)
voice_files = voice.find('ns0:files', ns)

voice.set('name', voicename)
voice_license.set('href', 'https://creativecommons.org/licenses/by/4.0/')
package_location.set('href', DOWNLOAD_URL)
voice_files.text = (' ').join(zippedfile.namelist())

#
tree.write(open(source_xml_component_file,'w'), encoding='unicode')

target_installation_xml_component_file = os.path.join(target_installation_dir, 'installed', 'voice-' + voicename + '-' + marytts_version + '-component.xml')
print ("Sgwennu manylion llais i %s " % (target_installation_xml_component_file))  
copyfile(source_xml_component_file, target_installation_xml_component_file)

