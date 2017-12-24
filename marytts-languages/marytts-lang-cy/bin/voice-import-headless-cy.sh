#!/bin/bash

MARYTTS_VERSION="5.2"
MARYTTS_HOME="/home/marytts"
MARYTTS_CY_HOME="/home/marytts/marytts-languages/marytts-lang-cy"

${MARYTTS_HOME}/target/marytts-${MARYTTS_VERSION}/bin/marytts-server &

voiceimport.sh PraatPitchmarker 
voiceimport.sh MCEPMaker
voiceimport.sh Festvox2MaryTranscripts
voiceimport.sh AllophnesExtractor
voiceimport.sh HTKLabeler
voiceimport.sh LabelPauseDeleter
#voiceimport.sh LabelledFilesInspector
voiceimport.sh PhoneUnitLabelComputer
voiceimport.sh HalfPhoneUnitLabelComputer
voiceimport.sh TranscriptionAligner
#voiceimport.sh FeatureSelection
voiceimport.sh PhoneUnitFeatureComputer
voiceimport.sh HalfPhoneUnitFeatureComputer
voiceimport.sh PhoneLabelFeatureAligner
voiceimport.sh HalfPhoneLabelFeatureAligner
voiceimport.sh WaveTimelineMaker
voiceimport.sh BasenameTimelineMaker
voiceimport.sh MCepTimelineMake
voiceimport.sh PhoneUnitfileWriter
voiceimport.sh PhoneFeatureFileWriter
voiceimport.sh DurationCARTTrrainer
voiceimport.sh F0CARTTrainer
voiceimport.sh HalfPhoneUnitFileWriter
voiceimport.sh HalfPhoneFeatureWriter
voiceimport.sh F0olynomialFeatureFielWriter
voiceimport.sh AcousticFeatureFileWriter
voiceimport.sh JoinCostFielMaker
voiceimport.sh CARTBuilder
voiceimport.sh VoiceCompiler

