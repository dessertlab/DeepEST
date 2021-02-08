package dataStructure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TestCase{
	private String name;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
	
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestCase other = (TestCase) obj;
		if (tcID == null) {
			if (other.tcID != null)
				return false;
		} else if (!tcID.equals(other.tcID))
			return false;
		return true;
	}
	


	private String tcID;
	private int numberOfCommands;
	private ArrayList<String> listOfCommands;
	private ArrayList<?> inputs;  //possono essere stringhe (nomi di file),numeri, o parametri
	private int maxNumberOfInputs; //un caso di test quanti input al massimo può avere
	private Path pathRootDirectory; //direcotry dove i comnadi del test case saranno eseguiti
	enum Outcome {ok, notOK, undefined};  
	public enum ExecutionState {executed, notExecuted};
	private boolean outcome;
	private ExecutionState executionState;
	private static String stringOk = "" ;
	private static String stringNotOk = "different results";
	private double expectedOccurrenceProbability;
	private double realOccurrenceProbability;
	private double expectedFailureLikelihood;
	
	
	public double getExpectedFailureLikelihood() {
		return expectedFailureLikelihood;
	}

	public void setExpectedFailureLikelihood(double expectedFailureLikelihood) {
		this.expectedFailureLikelihood = expectedFailureLikelihood;
	}

	public TestCase(String _name, String _tcID, Path rootDirectory){
		name = _name; 
		tcID= _tcID; 
		outcome = false;
		executionState = ExecutionState.notExecuted;
		pathRootDirectory = rootDirectory;
	};
	
	public TestCase(TestCase tc){
		name = new String(tc.getName()); 
		tcID = new String (tc.getNumber()); 
		outcome = tc.getOutcome();
		numberOfCommands = tc.getNumberOfCommands();
		listOfCommands = new ArrayList<String>(tc.getListOfCommands());
		inputs=  tc.getInputs();  //CAMBIARE
		this.maxNumberOfInputs = tc.getMaxNumberOfInputs();
		this.expectedOccurrenceProbability = tc.getExpectedOccurrenceProbability();
		this.realOccurrenceProbability= tc.getRealOccurrenceProbability();
		//ALTRI ATTRIBUTI 
	};
	
	public TestCase(String _name, String _tcID){
		name = _name; 
		tcID = _tcID; 
		outcome = false;
		executionState = ExecutionState.notExecuted;
		pathRootDirectory = Paths.get(System.getProperty("user.dir")); 
		
	};
	//FARE CLASSE ECCEZIONE test case non eseguito, il metodo sotto lancia questa eccezione
	
	public TestCase(String _id) {
		tcID = _id; 
	}

	public ExecutionState getExecutionState() {
		return executionState;
	}

	public void setExecutionState(ExecutionState executionState) {
		this.executionState = executionState;
	}

	public boolean runTestCase(String string) {
		outcome = this.getOutcome();
		executionState = ExecutionState.executed;
		return outcome; 
	}
		
	
	
	public boolean runTestCase() throws IOException, InterruptedException {
		 
		//create a temporary script
		File tempScript = createTempScript(this.pathRootDirectory);
				
		
		System.out.println("\n\n***** Running Test "+this.name+". ******\nExecuted Temporary Script: "+tempScript.toString());
		int execState=-1;
		String TCPrintedOutput=""; 
		String TCPrintedError = "";
		try {
		     ProcessBuilder pb = new ProcessBuilder(tempScript.toString());
		     //pb.inheritIO();
		     pb.directory(this.pathRootDirectory.toFile());
		     Process process = pb.start();
		     BufferedReader reader = 
		                new BufferedReader(new InputStreamReader(process.getInputStream()));
		     StringBuilder builder = new StringBuilder();
		     String line = null;
		        while ( (line = reader.readLine()) != null) {
		    	 builder.append(line);
		    	 builder.append(System.getProperty("line.separator"));
		     }
		        
		     BufferedReader reader2 = 
		                new BufferedReader(new InputStreamReader(process.getErrorStream()));
		     StringBuilder builder2 = new StringBuilder();
		     String line2 = null;
		        while ( (line2 = reader2.readLine()) != null) {
		    	 builder2.append(line2);
		    	 builder2.append(System.getProperty("line.separator"));
		     }
		    
		        //FARE REDIRECT DELLO STANDARD ERROR
		     process.waitFor();
				
		     TCPrintedOutput = builder.toString();
		     System.out.println("\n'Standard Output' Printed by the test case execution "+this.name+": "+TCPrintedOutput);
		     TCPrintedError = builder2.toString();
		     //System.out.println("\n'Standard Error' Printed by the test case execution "+this.name+": "+TCPrintedError);  
		     
		     execState = process.exitValue();
		     
		 } finally {
			tempScript.delete();			 
			 }
		
		System.out.println("\n ** Test " +this.name+ " teriminato **\n");
		
		if(execState==0){
			executionState = ExecutionState.executed;}
		else{
			executionState =ExecutionState.notExecuted;
			System.out.println("\n'Standard Error' Printed by the test case execution "+this.name+": "+TCPrintedError);
		     System.out.println("\nExecution State: "+execState);
		     }
			
		//PER l'outcome si dovrebbe poter leggere anche da un LOG nel caso in cui i risutalti vengono scritti nel log
		if(TCPrintedOutput.equals(stringNotOk))
			outcome=false;
		else 
			if (TCPrintedOutput.equals(stringOk))
				outcome = true;				
		
		return outcome;
		
	};

	private File createTempScript(Path directoryPath) throws IOException {
	    
		File tempScript = File.createTempFile("script", null, new File(directoryPath.toString()));
		String cmdPerm = "chmod +x "+tempScript;
		String[] cmds = {"bash","-c", cmdPerm};
		try {
			Process process2 = Runtime.getRuntime().exec(cmds);//this.listOfCommands.get(0));
			process2.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		FileWriter fileout = new FileWriter(tempScript); 
		BufferedWriter filebuf = new BufferedWriter(fileout); 
		
		//Writer streamWriter = new OutputStreamWriter(new FileOutputStream(      tempScript));
	    PrintWriter printWriter = new PrintWriter(filebuf);

	    printWriter.println("#!/bin/bash");
	    for(int i = 0; i<this.listOfCommands.size();i++)
	    	printWriter.println(this.listOfCommands.get(i));
	   // printWriter.println("echo END COMMANDS");
	    printWriter.close();
	    return tempScript;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNumber() {
		return tcID;
	}
	public void setNumber(String number) {
		this.tcID = number;
	}
	public int getNumberOfCommands() {
		return numberOfCommands;
	}
	public void setNumberOfCommands(int numberOfCommands) {
		this.numberOfCommands = numberOfCommands;
	}
	public ArrayList<String> getListOfCommands() {
		return listOfCommands;
	}
	public void setListOfCommands(ArrayList<String> listOfCommands) {
		this.listOfCommands = listOfCommands;
	}
	
	
	public double getExpectedOccurrenceProbability() {
		return expectedOccurrenceProbability;
	}
	public void setExpectedOccurrenceProbability(
			double expectedOccurrenceProbability) {
		this.expectedOccurrenceProbability = expectedOccurrenceProbability;
	}
	public double getRealOccurrenceProbability() {
		return realOccurrenceProbability;
	}
	public void setRealOccurrenceProbability(double realOccurrenceProbability) {
		this.realOccurrenceProbability = realOccurrenceProbability;
	}
	
	public void setOutcome(boolean outcome) {
		this.outcome = outcome;
	}
	public boolean getOutcome() {
		return this.outcome;
	}
	public ArrayList<?> getInputs() {
		return inputs;
	}
	public void setInputs(ArrayList<?> inputs) {
		this.inputs = inputs;
	}
	public int getMaxNumberOfInputs() {
		return maxNumberOfInputs;
	}
	public void setMaxNumberOfInputs(int maxNumberOfInputs) {
		this.maxNumberOfInputs = maxNumberOfInputs;
	}

	public String getTcID() {
		return tcID;
	}

	public void setTcID(String tcID) {
		this.tcID = tcID;
	}
	
}