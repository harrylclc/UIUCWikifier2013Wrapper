package edu.washington.cs.knowitall.UIUCWikifier2013Wrapper;

import java.io.File;
import java.util.ArrayList;

public class Test {

	private void getAllFiles(File folder, ArrayList<File> results) {
		File[] files = folder.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				getAllFiles(f, results);
			} else {
				System.out.println(f.getAbsolutePath());
				results.add(f);
			}
		}
	}

	public static void main(String[] args) {
		Test t = new Test();
		String pathToInputFolder = "/home/cul226/workspace/UIUCWikifier2013Wrapper";
		File inputFolder = new File(pathToInputFolder);
		ArrayList<File> results = new ArrayList<>();
		System.out.println("Starting to get all files under '"
				+ pathToInputFolder + "'");
		t.getAllFiles(inputFolder, results);
		System.out.println("Done. Total " + results.size() + " files");
//		int nThreads = 3;
//		ArrayList<File>[] fileParts = new ArrayList[nThreads];
//		for (int i = 0; i < nThreads; i++)
//			fileParts[i] = new ArrayList<File>();
//		for (int i = 0; i < results.size(); i++)
//			fileParts[i % nThreads].add(results.get(i));
//		WikifyThread[] workers = new WikifyThread[nThreads];
//		for (int i = 0; i < nThreads; i++)
//			workers[i] = new WikifyThread(i, fileParts[i]);
//		for (int i = 0; i < nThreads; i++)
//			workers[i].run();
	}
}

//class WikifyThread extends Thread {
//	int id;
//	ArrayList<File> docs;
//
//	public WikifyThread(int id, ArrayList<File> docs) {
//		this.id = id;
//		this.docs = docs;
//	}
//
//	public void run() {
//		System.out.println("Thread " + id + " docs: " + docs.size());
//
//	}
//}