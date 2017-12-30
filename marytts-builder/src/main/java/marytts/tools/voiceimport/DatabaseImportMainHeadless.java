/**
 * Portions Copyright 2018 Prifysgol Bangor University
 * Portions Copyright 2006 DFKI GmbH.
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute, 
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * Permission is hereby granted, free of charge, to use and distribute
 * this software and its documentation without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of this work, and to
 * permit persons to whom this work is furnished to do so, subject to
 * the following conditions:
 * 
 * 1. The code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 * 2. Any modifications must be clearly marked as such.
 * 3. Original authors' names are not deleted.
 * 4. The authors' names are not used to endorse or promote products
 *    derived from this software without specific prior written
 *    permission.
 *
 * DFKI GMBH AND THE CONTRIBUTORS TO THIS WORK DISCLAIM ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS, IN NO EVENT SHALL DFKI GMBH NOR THE
 * CONTRIBUTORS BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 * PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS
 * ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package marytts.tools.voiceimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import marytts.util.io.BasenameList;

/**
 * The single purpose of the DatabaseImportMainHeadless class is to provide a main which executes the sequence of database import and
 * conversion operations.
 * 
 * @author sacha, anna
 * 
 */
public class DatabaseImportMainHeadless {

	protected VoiceImportComponent[] components;
	protected String[][] groups2Comps;
	protected DatabaseLayout db = null;
	protected BasenameList bnl = null;
	protected String currentComponent;

	public DatabaseImportMainHeadless(String title, VoiceImportComponent[] components, DatabaseLayout db, String[][] groups2Comps) {
		
		this.components = components;
		this.db = db;
		this.bnl = db.getBasenames();
		this.groups2Comps = groups2Comps;
		currentComponent = "global properties";		
	}


	/**
	 * Run the selected components in a different thread.
	 * 
	 */
	protected void runSelectedComponents() {
		new Thread("RunSelectedComponentsThread") {
			public void run() {
				try {
					for (int i = 0; i < components.length; i++) {

						boolean success = false;
						final VoiceImportComponent oneComponent = components[i];

						try {
							success = oneComponent.compute();
						} 
						catch (Exception exc) {
							throw new Exception("A component produced the following exception: ", exc);
						} 

						if (success) {
							System.out.println("*** SUCCESS ***");
						} else {
							System.out.println("*** FAILURE ***");
							System.exit(0);
						}

					}
				} catch (Throwable e) {
					e.printStackTrace();
				} 
			}
		}.start();
	}


	public static String[][] readComponentList(InputStream fileIn) throws IOException {
		List<String> groups = new ArrayList<String>();
		Map<String, String> groups2Names = new HashMap<String, String>();
		Map<String, List<String>> groups2Components = new HashMap<String, List<String>>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(fileIn, "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#") || line.equals(""))
					continue;
				
				System.out.println(line);
				
				String[] lineSplit = line.split(" ");
				if (lineSplit[0].equals("group")) {
					// we have a group
					// line looks like "group basic_data basic data files"
					groups.add(lineSplit[1]);
					StringBuilder nameBuf = new StringBuilder();
					for (int i = 2; i < lineSplit.length; i++) {
						nameBuf.append(lineSplit[i] + " ");
					}
					groups2Names.put(lineSplit[1], nameBuf.toString().trim());
				} else {
					// we have a component
					// line looks like
					// "marytts.tools.voiceimport.WaveformTimelineMaker basic_data"
					if (groups2Components.containsKey(lineSplit[1])) {
						List<String> comps = groups2Components.get(lineSplit[1]);
						comps.add(lineSplit[0]);
					} else {
						List<String> comps = new ArrayList<String>();
						comps.add(lineSplit[0]);
						groups2Components.put(lineSplit[1], comps);
					}
				}
			}
			in.close();
		} catch (IOException e) {
			throw new IOException("Problem reading list of voice import components -- importMain.config seems broken", e);
		}
		String[][] result = new String[groups.size()][];
		for (int i = 0; i < groups.size(); i++) {
			String groupKey = groups.get(i);
			String groupName = groups2Names.get(groupKey);
			List<String> components = groups2Components.get(groupKey);
			if (components == null) // group is empty
				continue;
			String[] group = new String[components.size() + 1];
			group[0] = groupName;
			for (int j = 0; j < components.size(); j++) {
				group[j + 1] = components.get(j);
			}
			result[i] = group;
		}
		return result;
	}


	public static void main(String[] args) throws Exception {
		File voiceDir = determineVoiceBuildingDir(args);
		if (voiceDir == null) {
			throw new IllegalArgumentException("Cannot determine voice building directory.");
		}
		File wavDir = new File(voiceDir, "wav");
		//System.out.println(System.getProperty("user.dir")+System.getProperty("file.separator")+"wav");
		assert wavDir.exists() : "no wav dir at " + wavDir.getAbsolutePath();

		/* Read the list of components */
		File importMainConfigFile = new File(voiceDir, "importMain.config");
		if (!importMainConfigFile.exists()) {
			FileUtils.copyInputStreamToFile(DatabaseImportMainHeadless.class.getResourceAsStream("importMain.config"),
					importMainConfigFile);
		}
		assert importMainConfigFile.exists();

		String[][] groups2comps = readComponentList(new FileInputStream(importMainConfigFile));

		VoiceImportComponent[] components = createComponents(groups2comps);

		/* Load DatabaseLayout */
		File configFile = new File(voiceDir, "database.config");

		DatabaseLayout db = new DatabaseLayout(configFile, components);
		if (!db.isInitialized())
			return;

		String voicename = db.getProp(db.VOICENAME);				
		System.out.println("Building " + voicename + " voice");

		DatabaseImportMainHeadless importer = new DatabaseImportMainHeadless("Database import: " + voicename, components, db, groups2comps);
		importer.runSelectedComponents();

	}


	/**
	 * @param groups2comps
	 *            groups2comps
	 * @return compsList.toArray(new VoiceImportComponent[compsList.size()])
	 * @throws InstantiationException
	 *             InstantiationException
	 * @throws IllegalAccessException
	 *             IllegalAccessException
	 * @throws ClassNotFoundException
	 *             ClassNotFoundException
	 */
	private static VoiceImportComponent[] createComponents(String[][] groups2comps) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		/* Create component classes */
		List<VoiceImportComponent> compsList = new ArrayList<VoiceImportComponent>();
		// loop over the groups
		for (int i = 0; i < groups2comps.length; i++) {
			// get the components for this group
			String[] nextComps = groups2comps[i];
			// loop over the components (first element is group name; ignore)
			for (int j = 1; j < nextComps.length; j++) {
				// get the class name of this component
				String className = nextComps[j];
				//System.out.println(className);
				// create a new instance of this class and store in compsList
				compsList.add((VoiceImportComponent) Class.forName(className).newInstance());
				// remove "de.dfki...." from class name and store in groups2comps
				nextComps[j] = className.substring(className.lastIndexOf('.') + 1);
			}
		}
		return compsList.toArray(new VoiceImportComponent[compsList.size()]);
	}


	private static File determineVoiceBuildingDir(String[] args) {
		
		// Determine the voice building directory in the following order:

		// 1. System property "user.dir"
		// 2. First command line argument
		// 3. current directory
		
		// Do a sanity check -- do they exist, do they have a wav/ subdirectory?

		String voiceBuildingDir = null;
		Vector<String> candidates = new Vector<String>();
		
		candidates.add(System.getProperty("user.dir"));
		
		if (args.length > 0)
			candidates.add(args[0]);
		
		candidates.add("."); // current directory
		
		for (String dir : candidates) {
			if (dir != null && new File(dir).isDirectory() && new File(dir + "/wav").isDirectory()) {
				voiceBuildingDir = dir;
				break;
			}
		}
		
		System.setProperty("user.dir", voiceBuildingDir);
		if (voiceBuildingDir != null) {
			return new File(voiceBuildingDir);
		} else {
			return null;
		}

	}

}
