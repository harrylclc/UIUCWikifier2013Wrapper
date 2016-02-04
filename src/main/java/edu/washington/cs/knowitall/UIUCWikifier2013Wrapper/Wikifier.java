package edu.washington.cs.knowitall.UIUCWikifier2013Wrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.wikifier.common.GlobalParameters;
import edu.illinois.cs.cogcomp.wikifier.inference.InferenceEngine;
import edu.illinois.cs.cogcomp.wikifier.models.LinkingProblem;
import edu.illinois.cs.cogcomp.wikifier.models.ReferenceInstance;

public class Wikifier {

	private InferenceEngine inference;

	public Wikifier(String pathToWikifierResources, String configName)
			throws Exception {
		if (configName.equals(WikificationConfiguration.STAND_ALONE_GUROBI
				.toString())) {
			WikificationConfigurationSettings
					.standAloneGurobiSettings(pathToWikifierResources);
		} else if (configName
				.equals(WikificationConfiguration.STAND_ALONE_NO_INFERENCE
						.toString())) {
			WikificationConfigurationSettings
					.standAloneNoInferenceSettings(pathToWikifierResources);
		} else if (configName.equals(WikificationConfiguration.FULL.toString())) {
			WikificationConfigurationSettings
					.fullSettings(pathToWikifierResources);
		}

		inference = new InferenceEngine(false);
	}

	public WikifiedDocument wikify(String documentString, String documentName)
			throws Exception {
		TextAnnotation ta = GlobalParameters.curator
				.getTextAnnotation(documentString);
		LinkingProblem problem = new LinkingProblem(documentName, ta,
				new ArrayList<ReferenceInstance>());
		inference.annotate(problem, null, false, false, 0);
		WikifiedDocument wd = new WikifiedDocument(documentName,
				documentString, problem);
		return wd;
	}

	public WikifiedDocument wikifyFile(String filePath, String documentName)
			throws Exception {
		String docString = IOUtils.toString(new FileReader(new File(filePath)));
		return wikify(docString, documentName);
	}

	/**
	 * args[0] is path to Wikifier Resource Dir args[1] is configuration setting
	 * that must match a WikificationConfiguration args[2] is the input
	 * directory, all files at the first level will be processed args[3] is the
	 * output directory
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String pathToWikifierDir = args[0];
		String confString = args[1];
		boolean validConfigString = false;
		for (WikificationConfiguration wc : WikificationConfiguration.values()) {
			if (confString.equals(wc.toString())) {
				validConfigString = true;
			}
		}
		if (!validConfigString) {
			throw new IllegalArgumentException("Invalid Conf string.");
		}
		String pathToInputFiles = args[2];
		String pathToOutputFiles = args[3];

		Wikifier wikifier = new Wikifier(pathToWikifierDir, confString);

		File inputDir = new File(pathToInputFiles);
		// for(File f : inputDir.listFiles()){
		// WikifiedDocument wd =
		// wikifier.wikifyFile(f.getAbsolutePath(),f.getName());
		// File outputFile = new File(pathToOutputFiles+"/"+f.getName()+".out");
		// BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		// bw.write(wd.getWikifiedDocumentString());
		// bw.close();
		// }

		ArrayList<File> files = new ArrayList<>();
		System.out.println("Starting to get all files under '"
				+ pathToInputFiles + "'");
		Iterator<File> it = FileUtils.iterateFiles(inputDir, null, true);
		while (it.hasNext())
			files.add(it.next());
		System.out.println("Done. Total " + files.size() + " files");
		int nThreads = Integer.valueOf(args[4]);
		ArrayList<File>[] fileParts = new ArrayList[nThreads];
		for (int i = 0; i < nThreads; i++)
			fileParts[i] = new ArrayList<File>();
		for (int i = 0; i < files.size(); i++)
			fileParts[i % nThreads].add(files.get(i));
		WikifyThread[] workers = new WikifyThread[nThreads];
		for (int i = 0; i < nThreads; i++)
			workers[i] = new WikifyThread(i, wikifier, fileParts[i],
					pathToOutputFiles);
		for (int i = 0; i < nThreads; i++)
			workers[i].run();
	}

	public static enum WikificationConfiguration {
		STAND_ALONE_GUROBI, STAND_ALONE_NO_INFERENCE, FULL
	}

}

class WikifyThread extends Thread {
	int id;
	Wikifier wikifier;
	ArrayList<File> docs;
	String pathToOutputFiles;

	public WikifyThread(int id, Wikifier wikifier, ArrayList<File> docs,
			String pathToOutputFiles) {
		this.id = id;
		this.wikifier = wikifier;
		this.docs = docs;
		this.pathToOutputFiles = pathToOutputFiles;
	}

	public void run() {
		System.out.println("Thread " + id + " docs: " + docs.size());
		try {
			for (int i = 0; i < docs.size(); i++) {
				if (i % 100 == 0) {
					System.out.println("===========Thread " + id + " docs:" + i
							+ "/" + docs.size() + "===========");
				}
				File f = docs.get(i);
				WikifiedDocument wd = wikifier.wikifyFile(f.getAbsolutePath(),
						f.getName());
				File outputFile = new File(pathToOutputFiles + "/"
						+ f.getName() + ".out");
				BufferedWriter bw = new BufferedWriter(new FileWriter(
						outputFile));
				bw.write(wd.getWikifiedDocumentString());
				bw.close();
			}
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
	}
}