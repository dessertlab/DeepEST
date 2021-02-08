package main;

import java.util.ArrayList;

import dataStructure.TestFrame;
import selector.DeepESTSelector;

public class MainJar {

	public static void main(String[] args) {
		String help = "Please specify:\n"
				+ "	- dataset path\n"
				+ "	- auxiliary variable: confidence, lsa, dsa, combo\n"
				+ "	- threshold\n"
				+ "	- budget";
		
		if (args.length < 4) {
			System.out.println(help);
			return; 
		}

		InitializerTF csvR = new InitializerTF(args[0]);
		String key_s = args[1];
		Double threshold = Double.parseDouble(args[2]);
		int budget = Integer.parseInt(args[3]);
		
		int key = 4;
		if(key_s.equalsIgnoreCase("confidence")) {
			key = 3;
			threshold = 1-threshold;
		} else if(key_s.equalsIgnoreCase("dsa")) {
			key = 4;
		} else if(key_s.equalsIgnoreCase("lsa")) {
			key = 5;
		} else {
			key = 6;
			threshold = 1-threshold;
		}
		
		System.out.println("Approach execution on "+args[0]+" with auxiliary variable "+args[1]+" and budget "+args[3]);
		
		int rep = 30;
		
		ArrayList<TestFrame> tf = csvR.readTestFrames(key, 10000);
		
//		System.out.println(tf.get(0).getFailureProb());
		
		DeepESTSelector aws= new DeepESTSelector();
		
		double[][] weightsMatrix = csvR.weightedMatrixComputation_threshold(tf, key, threshold);
		double[] rel; 
		double[] rel_arr = new double[rep];
		int[] num_fp = new int[rep];
				
		for(int i=0; i<rep; i++) {
			rel = aws.selectAndRunTestCase(budget, tf, weightsMatrix, 0.8);
			rel_arr[i] = 1-rel[0];
			num_fp[i] = aws.getnumfp();
		}
		
		for(int i=0; i<rep; i++) {
			System.out.println("Repetition "+(i+1)+") Estimated Accuracy: " +rel_arr[i]+" | Number of failed tests: " + num_fp[i]);
		}

	}

}
