/**
 * Copyright 2018 Prifysgol Bangor University 
 * Copyright 2007 DFKI GmbH.
 *
 * All Rights Reserved.  Use is subject to license terms.
 *
 * This file is part of MARY TTS.
 *
 * MARY TTS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package marytts.util.data.audio;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import marytts.signalproc.analysis.EnergyAnalyser;
import marytts.signalproc.analysis.EnergyAnalyser_dB;
import marytts.signalproc.analysis.F0TrackerAutocorrelationHeuristic;
import marytts.signalproc.analysis.FrameBasedAnalyser;
import marytts.signalproc.analysis.PitchFileHeader;
import marytts.signalproc.process.EnergyNormaliser;
import marytts.util.Pair;
import marytts.util.data.BufferedDoubleDataSource;
import marytts.util.data.DoubleDataSource;
import marytts.util.io.FileUtils;
import marytts.util.math.MathUtils;

/**
 * AudioConverterHeadless.java
 *
 */
public class AudioConverterHeadless {
	
	public AudioConverterHeadless() {}

	public static void main(String args[]) {

                try {
			String voice_name = args[0];
                        String inDirPath = "/home/marytts/voice-builder/" + voice_name + "/recordings";
                        String outDirPath = "/home/marytts/voice-builder/" + voice_name + "/wav";
                        boolean bestOnly = false;
                        boolean stereoMono = false;
                        int whichChannel = AudioPlayer.LEFT_ONLY;
                        boolean downSample = true;
                        int targetSampleRate = 16000;
                        String soxPath = "/usr/bin/sox";
                        boolean highPassFilter = false;
                        boolean powerNormalise = false;
                        boolean maximiseAmplitude = true;
                        Double targetMaxAmplitude = 0.9;
                        boolean trimSilences = true;
		
                        HeadlessConverter converter = new HeadlessConverter(inDirPath, outDirPath, bestOnly, stereoMono, whichChannel, downSample, targetSampleRate, soxPath,
                                                  highPassFilter, powerNormalise, maximiseAmplitude, targetMaxAmplitude, trimSilences);
                        
                        converter.start();

                } catch (IOException ioe) {
                        ioe.printStackTrace();
                }

        }
}

