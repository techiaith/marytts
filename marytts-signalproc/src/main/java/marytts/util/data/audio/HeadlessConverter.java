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


public class HeadlessConverter extends Thread {
		private File inDir;
		private File outDir;
		private FilenameFilter filenameFilter;
		private boolean bestOnly;
		private boolean stereoMono;
		private int channel;
		private boolean downSample;
		private int targetSampleRate;
		private String soxPath;
		private boolean highPassFilter;
		private boolean powerNormalise;
		private boolean maximiseAmplitude;
		private double targetMaxAmplitude;
		private boolean trimSilences;

		public HeadlessConverter(String inDirPath, String outDirPath, final boolean bestOnly, boolean stereoMono, int channel,
				         boolean downSample, int targetSampleRate, String soxPath, boolean highPassFilter, boolean powerNormalise,
				         boolean maximiseAmplitude, double targetMaxAmplitude, boolean trimSilences) throws IOException {

			inDir = new File(inDirPath);
			outDir = new File(outDirPath);
			if (!outDir.exists())
				outDir.mkdirs();
				
			filenameFilter = new FilenameFilter() {
				public boolean accept(File file, String filename) {
					if (!filename.endsWith(".wav"))
						return false;
					char lastCharInBasename = filename.charAt(filename.length() - 5);
					if (bestOnly && (lastCharInBasename < '0' || lastCharInBasename > '9')) {
						return false;
					}
					return true;
				}
			};
			
			this.bestOnly = bestOnly;
			this.stereoMono = stereoMono;
			this.channel = channel;
			this.downSample = downSample;
			this.targetSampleRate = targetSampleRate;
			this.soxPath = soxPath;
			this.highPassFilter = highPassFilter;
			this.powerNormalise = powerNormalise;
			this.maximiseAmplitude = maximiseAmplitude;
			this.targetMaxAmplitude = targetMaxAmplitude;
			this.trimSilences = trimSilences;

		}

		public void run() {
			int numSteps = 1;
			int stepsComplete = 0;
			if (maximiseAmplitude)
				numSteps++;
			if (powerNormalise)
				numSteps++;
			int interProgress = 0;
			int stepProgress = 100 / numSteps;

			try {
				File[] wavFiles = inDir.listFiles(filenameFilter);
				System.out.println("Number of wave files to convert: " + wavFiles.length);

				double[] amplitudeFactors = new double[wavFiles.length];
				Arrays.fill(amplitudeFactors, 1); // factor 1 = no change
				if (powerNormalise) {
					Arrays.sort(wavFiles, new Comparator<File>() {
						public int compare(File f1, File f2) {
							long t1 = f1.lastModified();
							long t2 = f2.lastModified();
							if (t1 < t2)
								return -1;
							if (t1 > t2)
								return 1;
							return 0;
						}

						public boolean equals(Object obj) {
							return false;
						}
					});
					List<Pair<Integer, Integer>> sessions = new ArrayList<Pair<Integer, Integer>>();
					List<Double> sessionEnergies = new ArrayList<Double>();
					double maxEnergy = Double.NEGATIVE_INFINITY;
					// each session pair is the index numbers delimiting the session in typical java fashion,
					// i.e. from the first element to one higher than the last element.
					// e.g., (3,6) includes wavFiles[3],wavFiles[4],wavFiles[5]
					int currentStart = 0;
					for (int i = 0; i < wavFiles.length - 1; i++) {
						long ti = wavFiles[i].lastModified();
						long ti1 = wavFiles[i + 1].lastModified();
						// System.out.printf(Locale.US, wavFiles[i].getName()+" %tc\n", ti);
						assert ti1 >= ti; // we sorted it, didn't we
						if (ti1 - ti > 600000) { // 600.000 ms = 10 min
							// System.out.println("Break after "+wavFiles[i].getName());
							System.out.println();
							sessions.add(new Pair<Integer, Integer>(currentStart, i + 1));
							currentStart = i + 1;
						}
						if (i == wavFiles.length - 2) {
							// System.out.printf(Locale.US, wavFiles[i+1].getName()+" %tc\n", ti1);
						}
					}
					sessions.add(new Pair<Integer, Integer>(currentStart, wavFiles.length));

					for (int i = 0; i < sessions.size(); i++) {
						Pair<Integer, Integer> session = sessions.get(i);
						double avgEnergySession = computeAverageEnergy(wavFiles, session.getFirst(), session.getSecond());
						System.out.printf(Locale.US, "Session at %tc: %d files, avg. Energy: %f\n",
								wavFiles[session.getFirst()].lastModified(), session.getSecond() - session.getFirst(),
								avgEnergySession);
						sessionEnergies.add(avgEnergySession);
						if (avgEnergySession > maxEnergy)
							maxEnergy = avgEnergySession;
					}

					// Amplitude factors:
					// db1 = 10 * log10(A1^2), db2 = 10 * log10(A2^2)
					// => energy difference db2 - db1 corresponds to amplitude factor:
					// factor = sqrt(10^((db2 - db1)/10))
					for (int s = 0, max = sessions.size(); s < max; s++) {
						Pair<Integer, Integer> session = sessions.get(s);
						double energy = sessionEnergies.get(s);
						if (maxEnergy - energy < 1.e-15) { // energy == maxEnergy
							continue;
						}
						double factor = Math.sqrt(Math.pow(10., (maxEnergy - energy) / 10.));
						System.out.println("Session " + s + " scaling factor: " + factor);
						for (int i = session.getFirst(); i < session.getSecond(); i++) {
							amplitudeFactors[i] = factor;
						}
					}
					stepsComplete++;
				}

				// Find global maximum amplitude
				if (maximiseAmplitude) {
					double globalMaxAmplitude = 0.;
					int globalMaxIndex = -1;
					for (int i = 0; i < wavFiles.length; i++) {
						double maxAmplitude = getMaxAbsAmplitude(wavFiles[i]) * amplitudeFactors[i];
						if (maxAmplitude > globalMaxAmplitude) {
							globalMaxAmplitude = maxAmplitude;
							globalMaxIndex = i;
						}
					}
					System.out.println("Maximum amplitude of " + globalMaxAmplitude
							+ (powerNormalise ? " (after normalisation)" : "") + " found in file "
							+ wavFiles[globalMaxIndex].getName());
					System.out.println("Target maximum amplitude: " + targetMaxAmplitude);
					double scalingFactor = targetMaxAmplitude / globalMaxAmplitude;
					System.out.println("Applying scaling factor of " + scalingFactor + " to all files");
					for (int i = 0; i < wavFiles.length; i++) {
						amplitudeFactors[i] *= scalingFactor;
					}
					stepsComplete++;
				}

				for (int i = 0; i < wavFiles.length; i++) {
					String wavFileName = wavFiles[i].getName();
					System.out.println(wavFileName);

					File outFile = new File(outDir, wavFileName);
					if (outFile.exists()) {
						outFile.delete();
					}

					AudioInputStream ais = AudioSystem.getAudioInputStream(wavFiles[i]);

					// Enforce PCM_SIGNED encoding
					if (!ais.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
						ais = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, ais);
					}

					if (stereoMono) {
						ais = new AudioConverterUtils.Stereo2Mono(channel).apply(ais);
					}

					// If Audio is Mono then only remove Low Frequency Noise
					if (highPassFilter && ais.getFormat().getChannels() == 1) {
						ais = new AudioConverterUtils.HighPassFilter(50, 40).apply(ais);
					}

					if (powerNormalise || maximiseAmplitude) {
						double factor = amplitudeFactors[i];
						if (factor != 1.) {
							ais = new EnergyNormaliser(factor * factor).apply(ais);
						}
					}

					AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outFile);
					ais.close();

					if (trimSilences) {
						trimSilences(outFile);
					}

					if (downSample) {
						samplingRateConverter(outFile.getAbsolutePath(), targetSampleRate);
					}
				}

				System.out.println("Completed Audio Conversion successfully... Done.");
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				System.err
						.println("Audio conversion failed for ArrayIndexOutOfBoundsException. Probably this is due because the file lenght is not a multiple of 1024/2048 samples.");
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Audio conversion failed.");
			} finally {
			}

		}

		/**
		 * To choose a global value to get Best Dynamic Bits
		 *
		 * @param wavFileNames
		 * @param targetBitsPerSample
		 * @return globalBestShift
		 * @throws Exception
		 */
		@Deprecated
		// using volume scaling integrated with energy normalisation instead.
		private int bestShiftBits(File[] wavFiles, int targetBitsPerSample) throws Exception {

			int globalBestShift = 0;

			for (int i = 0; i < wavFiles.length; i++) {

				AudioInputStream ais = AudioSystem.getAudioInputStream(wavFiles[i]);

				if (!ais.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
					ais = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, ais);
				}
				if (stereoMono) {
					ais = new AudioConverterUtils.Stereo2Mono(channel).apply(ais);
				}

				// If Audio is Mono then only remove Low Frequency Noise
				if (highPassFilter && ais.getFormat().getChannels() == 1) {
					ais = new AudioConverterUtils.HighPassFilter(50, 40).apply(ais);
				}

				int[] samples = AudioConverterUtils.getSamples(ais);
				int maxBitPos = 0;
				int valueAfterShift;
				int bitsPerSample = ais.getFormat().getSampleSizeInBits();
				for (int k = 0; k < samples.length; k++) {
					for (int j = bitsPerSample; j >= 1; j--) {
						valueAfterShift = Math.abs(samples[k]) >> j;
						if (valueAfterShift != 0) {
							if (maxBitPos < j)
								maxBitPos = j;
							break;
						}
					}
				}

				ais.close();
				int bestShift = maxBitPos - targetBitsPerSample + 2;
				if (bestShift > globalBestShift) {
					globalBestShift = bestShift;
				}

			}

			return globalBestShift;
		}

		/**
		 * Sampling Rate Conversion doing with SOX.
		 *
		 * @param outpath
		 * @param targetSamplingRate
		 * @throws IOException
		 */
		private void samplingRateConverter(String waveFile, int targetSamplingRate) throws IOException {

			Runtime rtime = Runtime.getRuntime();

			String usrHome = System.getProperty("user.home");
			File outFile = new File(usrHome, "tempOut.wav");
			
			String soxCommandLine = soxPath + " " + waveFile + " -r " + targetSamplingRate + " " + outFile.getAbsolutePath();

			Process process = rtime.exec(soxCommandLine);
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (!outFile.renameTo(new File(waveFile)))
				FileUtils.copy(outFile.getAbsolutePath(), waveFile);
		}

		private double computeAverageEnergy(File[] wavFiles, int pos, int end)
				throws UnsupportedAudioFileException, IOException {
			int len = end - pos;

			double[] energies = new double[len];
			for (int i = 0; i < len; i++) {
				energies[i] = computeAverageEnergy(wavFiles[pos + i]);
			}

			return MathUtils.median(energies);
		}

		private double computeAverageEnergy(File wavFile) throws UnsupportedAudioFileException, IOException {
			AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile);
			int sampleRate = (int) ais.getFormat().getSampleRate();
			int framelength = (int) (0.01 /* seconds */* sampleRate);
			double[] audioData = new AudioDoubleDataSource(ais).getAllData();
			DoubleDataSource audio = new BufferedDoubleDataSource(audioData);
			EnergyAnalyser energyAnalyser = new EnergyAnalyser_dB(audio, framelength, sampleRate);
			PitchFileHeader f0TrackerParams = new PitchFileHeader();
			f0TrackerParams.windowSizeInSeconds = energyAnalyser.getFrameLengthTime();
			f0TrackerParams.skipSizeInSeconds = energyAnalyser.getFrameShiftTime();
			f0TrackerParams.fs = sampleRate;
			F0TrackerAutocorrelationHeuristic f0Tracker = new F0TrackerAutocorrelationHeuristic(f0TrackerParams);
			audio = new BufferedDoubleDataSource(audioData);
			f0Tracker.pitchAnalyze(audio);
			double[] f0Contour = f0Tracker.getF0Contour();
			// Now determine speech stretches, and compute average frame energy within those
			int energyBufferLength = 20;
			double speechStartLikelihood = 0.1; // count as speech if at least 10% of recent frames are above threshold
			double speechEndLikelihood = 0.1; // count as non-speech if up to 10% of recent frames are below threshold
			double shiftFromMinimumEnergyCenter = 0.1; // threshold is at 10% of the distance between smallest and biggest cluster
			int numClusters = 4;

			double[][] speechStretches = energyAnalyser.getSpeechStretchesUsingEnergyHistory(energyBufferLength,
					speechStartLikelihood, speechEndLikelihood, shiftFromMinimumEnergyCenter, numClusters);

			FrameBasedAnalyser.FrameAnalysisResult<Double>[] energies = energyAnalyser.analyseAllFrames();
			int numSpeechFrames = 0;
			int numFrames = Math.min(energies.length, f0Contour.length);
			// an upper bound for the number of speech frames is numFrames:
			double[] speechFrameEnergies = new double[numFrames];
			int iCurrentFrame = 0;
			for (int i = 0; i < speechStretches.length; i++) {
				// System.out.println("Speech stretch "+i+": "+speechStretches[i][0]+" -> "+speechStretches[i][1]);
				while (iCurrentFrame < numFrames && energies[iCurrentFrame].getStartTime() < speechStretches[i][0]) {
					iCurrentFrame++;
				}
				while (iCurrentFrame < numFrames && energies[iCurrentFrame].getStartTime() < speechStretches[i][1]) {
					// Current frame is speech frame
					// If it is also voiced, use its energy:
					if (f0Contour[iCurrentFrame] > 10) { // voiced
						speechFrameEnergies[numSpeechFrames] = energies[iCurrentFrame].get();
						numSpeechFrames++;
					}
					iCurrentFrame++;
				}
			}
			if (numSpeechFrames == 0) {
				System.err.println("No speech found in file " + wavFile.getAbsolutePath());
				return 0;
			}
			double medianSpeechFrameEnergy = MathUtils.median(speechFrameEnergies, 0, numSpeechFrames);
			double medianSpeechSampleEnergy = medianSpeechFrameEnergy / energyAnalyser.getFrameLengthSamples();
			return medianSpeechSampleEnergy;
		}

		private double getMaxAbsAmplitude(File wavFile) throws UnsupportedAudioFileException, IOException {
			double maxAmplitude = 0.;
			AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile);
			double[] audioData = new AudioDoubleDataSource(ais).getAllData();
			for (int i = 0; i < audioData.length; i++) {
				if (audioData[i] > maxAmplitude || -audioData[i] > maxAmplitude) {
					maxAmplitude = Math.abs(audioData[i]);
				}
			}
			ais.close();
			return maxAmplitude;
		}

		private void trimSilences(File wavFile) throws UnsupportedAudioFileException, IOException {
			// We hard-code the values here. Use marytts.tools.voiceimport.EndpointDetector if you want to tune them.
			int energyBufferLength = 20;
			double speechStartLikelihood = 0.1;
			double speechEndLikelihood = 0.1;
			double shiftFromMinimumEnergyCenter = 0.0;
			int numClusters = 4;
			double minimumStartSilenceInSeconds = 0.5;
			double minimumEndSilenceInSeconds = 0.5;
			String usrdir = System.getProperty("user.home");
			System.out.println("tmpAudio created in " + usrdir);	
			File tmpFile = new File(usrdir, "tmpAudio.wav");
			AudioConverterUtils.removeEndpoints(wavFile.getAbsolutePath(), tmpFile.getAbsolutePath(), energyBufferLength,
					speechStartLikelihood, speechEndLikelihood, shiftFromMinimumEnergyCenter, numClusters,
					minimumStartSilenceInSeconds, minimumEndSilenceInSeconds);

			if (!tmpFile.renameTo(wavFile))
				FileUtils.copy(tmpFile.getAbsolutePath(), wavFile.getAbsolutePath());
		}
}


