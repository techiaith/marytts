/**
 * Copyright 2018 Prifysgol Bangor University
 * Copyright 2000-2009 DFKI GmbH.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import marytts.util.data.BufferedDoubleDataSource;
import marytts.util.data.ESTTrackWriter;
import marytts.util.data.audio.AudioDoubleDataSource;
import marytts.util.data.audio.DDSAudioInputStream;
import marytts.util.data.text.ESTTextfileDoubleDataSource;
import marytts.util.io.FileUtils;
import marytts.util.string.PrintfFormat;

/**
 * For the given texts, compute unit features and align them with the given unit labels.
 * 
 * @author schroed
 *
 */
public class LabelledFilesInspectorHeadless extends VoiceImportComponent {

	protected File wavDir;
	protected File phoneLabDir;
	protected File pmDir;

	protected DatabaseLayout db = null;


	protected String extractedDir;
	protected String extractedWavDir;
	protected String extractedLabDir;
	protected String extractedPmDir;

	public final String PMDIR = "db.pmDir";
	public final String PMEXT = "db.pmExtension";

	public String getName() {
		return "LabelledFilesInspectorHeadless";
	}

	@Override
	protected void initialiseComp() {
		extractedDir = db.getProp(db.TEMPDIR);
		extractedWavDir = extractedDir + "wav/";
		extractedLabDir = extractedDir + "lab/";
		extractedPmDir = extractedDir + "pm/";
	}

	public SortedMap getDefaultProps(DatabaseLayout db) {
		this.db = db;
		if (props == null) {
			props = new TreeMap();
		}
		return props;
	}

	protected void setupHelp() {
		props2Help = new TreeMap();
	}

	public boolean compute() throws IOException {
		//quit = false;
		wavDir = new File(db.getProp(db.WAVDIR));
		if (!wavDir.exists())
			throw new IOException("No such directory: " + wavDir);

		phoneLabDir = new File(db.getProp(db.LABDIR));
		pmDir = new File(db.getProp(PMDIR));

		File extractedDirFile = new File(extractedDir);
		if (!extractedDirFile.exists())
			extractedDirFile.mkdir();
		File extractedWavDirFile = new File(extractedWavDir);
		if (!extractedWavDirFile.exists())
			extractedWavDirFile.mkdir();
		File extractedLabDirFile = new File(extractedLabDir);
		if (!extractedLabDirFile.exists())
			extractedLabDirFile.mkdir();
		File extractedPmDirFile = new File(extractedPmDir);
		if (!extractedPmDirFile.exists())
			extractedPmDirFile.mkdir();

		System.out.println("Skipping manually inspecting " + bnl.getLength() + " files ");
		return true;
	}
	

	/**
	 * Provide the progress of computation, in percent, or -1 if that feature is not implemented.
	 * 
	 * @return -1 if not implemented, or an integer between 0 and 100.
	 */
	public int getProgress() {
		return -1;
	}

}
