package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import dataStructure.TestFrame;

public class InitializerTF {
	
	private String csvFile;
	
	public InitializerTF(String path) {
		csvFile = path;
	}

    public ArrayList<TestFrame> readTestFrames(int key, int size) {
        BufferedReader br = null;
        String line = "";
        ArrayList<TestFrame> aux = new ArrayList<TestFrame>();

        try {

            br = new BufferedReader(new FileReader(csvFile));
            line = br.readLine();
            String[] parser = line.split(",");
//            System.out.println(parser[key]);
            double val = 0;
            String outcome;
            boolean fail=true;
            
            //per diminuire il costo assumo di riceverlo in input
            double occ = 1/(double)size;
            int i = 0;
            
            while ((line = br.readLine()) != null) {
            	i++;
                // use comma as separator
                parser = line.split(",");
                outcome = parser[1];
            	if(outcome.equals("Pass")) {
            		fail=false;
            	} else {
            		fail=true;
            	}
            	
            	val = Double.parseDouble(parser[key]);
            	
                if(key==3||key==6) {
                	val = 1-val;
//                	inserisco un epsilon alla confidenza.
//                	if(val == 0) {
//                		val = 0.000000001;
//                	}
                	
                	aux.add(new TestFrame(""+aux.size(), ""+aux.size(), val, occ, parser[2], fail));
                } else {
                	aux.add(new TestFrame(""+aux.size(), ""+aux.size(), val, occ, parser[2], fail));
                }
            }
            
            if(i!=size)
            	System.out.println("[WARNING] The size is lower/greater!!!");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
		return aux;

    }
    
    public double[][] weightedMatrixComputation_threshold(ArrayList<TestFrame> tf, int key, double threshold){
    	double[][] wm = new double[tf.size()][tf.size()];
    	double conf_j;
    	
    	
    	for(int i=0; i<tf.size(); i++) {
    		for(int j=0; j<tf.size(); j++) {
    			conf_j=tf.get(j).getFailureProb();
    			//if(tf.get(i).getOutput().equals(tf.get(j).getOutput()) && (conf_i-conf_j)>min && (conf_i-conf_j)<max){
    			if(conf_j > threshold){
    				wm[i][j] = conf_j;
    			} else {
    				wm[i][j] = 0;
    			}
    		}
    	}
    	
    	return wm;
    }
}