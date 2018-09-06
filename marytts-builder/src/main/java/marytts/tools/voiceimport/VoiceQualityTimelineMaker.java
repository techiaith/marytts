/**
 * Copyright 2010 DFKI GmbH.
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

package marytts.tools.voiceimport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import marytts.signalproc.analysis.VoiceQuality;
import marytts.unitselection.data.FloatArrayDatagram;

/**
 * Make a Timeline from a directory of voice quality parameter files
 * 
 * @author steiner
 * 
 */
public class VoiceQualityTimelineMaker extends AbstractTimelineMaker {

	/**
	 * get the name of this {@link VoiceImportComponent}
	 * 
	 * @return the name
	 */
	@Override
	public String getName() {
		return "VoiceQualityTimelineMaker";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getType() {
		return "vq";
	}

	/**
	 * load a {@link VoiceQualityDataFile}
	 * 
	 * @param file
	 *            to load
	 * @return the VoiceQualityDataFile
	 */
	@Override
	protected VoiceQualityDataFile loadDataFile(File file) {
		return new VoiceQualityDataFile(file);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 *             IOException
	 * @return header string
	 */
	@Override
	protected String getProcessingHeader() throws IOException {
		Properties properties = new Properties();
		properties.setProperty("paramNames", Arrays.toString(getParamNames()));

		// awkward code to Stringify differently than toString():
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		properties.store(stream, null);
		String header = stream.toString("latin1");
		return header;
	}

	/**
	 * get names of voice quality parameters as a String array
	 * 
	 * @return paramNames
	 */
	public String[] getParamNames() {
		// TODO this should preferably call an appropriate getter in VoiceQuality...
		String[] paramNames = new String[] { "OQG", "GOG", "SKG", "RCG", "IC" };
		return paramNames;
	}

	/**
	 * Extension of {@link AbstractDataFile} for voice quality parameter files generated by {@link VoiceQuality#writeVqFile}
	 * 
	 * @author steiner
	 * 
	 */
	public class VoiceQualityDataFile extends AbstractDataFile {

		protected int numParams;

		/**
		 * main constructor
		 * 
		 * @param file
		 *            to load
		 */
		public VoiceQualityDataFile(File file) {
			super(file);
		}

		/**
		 * load the file
		 * 
		 * @param file
		 *            file
		 */
		@Override
		protected void load(File file) {
			// load the file as a VoiceQuality instance:
			String fileName = file.getAbsolutePath();
			VoiceQuality vq = new VoiceQuality(fileName);
			sampleRate = vq.params.samplingRate;
			frameSkip = vq.params.skipsize;
			numFrames = vq.params.numfrm;
			numParams = vq.params.dimension;
			frameDuration = (int) (sampleRate * frameSkip);

			// add data frames with values for each VQ parameter (as floats):
			datagrams = new FloatArrayDatagram[numFrames];
			for (int f = 0; f < numFrames; f++) {
				float[] frameData = new float[numParams];
				for (int p = 0; p < numParams; p++) {
					frameData[p] = (float) vq.vq[p][f];
				}
				FloatArrayDatagram datagram = new FloatArrayDatagram(frameDuration, frameData);
				datagrams[f] = datagram;
			}
		}
	}
}
