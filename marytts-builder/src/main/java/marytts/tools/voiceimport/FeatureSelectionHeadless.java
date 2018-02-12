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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.SortedMap;
import java.util.TreeMap;

import marytts.client.http.MaryHttpClient;
import marytts.util.http.Address;
import marytts.util.io.FileUtils;

/**
 * For the given texts, compute unit features and align them with the given unit labels.
 * 
 * @author schroed
 *
 */
public class FeatureSelectionHeadless extends VoiceImportComponent {

	protected File featureFile;
	protected String features;
	protected String locale;
	protected MaryHttpClient mary;
	protected boolean success = true;

	protected DatabaseLayout db = null;

	public String FEATUREFILE = "FeatureSelectionHeadless.featureFile";
	public String MARYSERVERHOST = "FeatureSelectionHeadless.maryServerHost";
	public String MARYSERVERPORT = "FeatureSelectionHeadless.maryServerPort";

	public String getName() {
		return "FeatureSelectionHeadless";
	}

	@Override
	protected void initialiseComp() {
		locale = db.getProp(db.LOCALE);

		mary = null; // initialised only if needed
		featureFile = new File(getProp(FEATUREFILE));
		if (featureFile.exists()) {
			System.out.println("Loading features from file " + getProp(FEATUREFILE));
			try {
				features = FileUtils.getFileAsString(featureFile, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public SortedMap<String, String> getDefaultProps(DatabaseLayout theDb) {
		this.db = theDb;
		if (props == null) {
			props = new TreeMap<String, String>();
			props.put(FEATUREFILE, db.getProp(db.CONFIGDIR) + "features.txt");
			props.put(MARYSERVERHOST, "localhost");
			props.put(MARYSERVERPORT, "59125");
		}
		return props;
	}

	protected void setupHelp() {
		props2Help = new TreeMap<String, String>();
		props2Help.put(FEATUREFILE, "file containing the list of features to use."
				+ "Will be created by querying MARY server if it does not exist");
		props2Help.put(MARYSERVERHOST, "the host were the Mary server is running, default: \"localhost\"");
		props2Help.put(MARYSERVERPORT, "the port were the Mary server is listening, default: \"59125\"");
	}

	public MaryHttpClient getMaryClient() throws IOException {
		if (mary == null) {
			try {
				mary = new MaryHttpClient(new Address(getProp(MARYSERVERHOST), Integer.parseInt(getProp(MARYSERVERPORT))));
			} catch (IOException e) {
				throw new IOException("Could not connect to Maryserver at " + getProp(MARYSERVERHOST) + " "
						+ getProp(MARYSERVERPORT));
			}
		}
		return mary;
	}

	protected void saveFeatures(String newFeatures) {

		System.out.println("Saving features to " + featureFile.getAbsolutePath());
		features = newFeatures;
		if (!features.contains(PhoneUnitFeatureComputer.PHONEFEATURE)) {
			System.out.println("WARNING: Important feature missing. The features '" + PhoneUnitFeatureComputer.PHONEFEATURE 
					+ "' is not present.\nThis will lead to problems in the further processing.");			
		}
		try {
			PrintWriter pw = new PrintWriter(featureFile, "UTF-8");
			pw.println(features);
			pw.close();
		} catch (IOException e) {
			System.err.println("Cannot save features:");
			e.printStackTrace();
			success = false;
		}
	}

	protected void setSuccess(boolean val) {
		success = val;
	}

	public boolean compute() throws IOException {

		if (features == null) {
			mary = getMaryClient();
			features = mary.getDiscreteFeatures(locale);
			features = features.replaceAll(" ", "\n");
		}

		try {
			saveFeatures(features);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Error("Error defining replacements");
		}

		return success;

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
