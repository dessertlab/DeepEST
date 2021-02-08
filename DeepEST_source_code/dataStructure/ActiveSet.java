package dataStructure;

import java.util.ArrayList;

public class ActiveSet {
	private ArrayList<String> testFrame;
	private int[] id;
	private boolean[] outcome; 
	private double[] weights;
	private int outcomeSum;
	private double outcomeSumX;
	private double[] occurrenceProb;
	
	//la scelta di inserire qi in questo modo è stata effettuata per una questione di ottimizzazione,
	//in quanto il metodo testFrameExtraction già calcola la somma dei pesi e quindi mi consente di risparmiare 
	//un ciclo che in caso di numero molto elevato di test frame può essere molto costoso.
	public double qi;
	
	public int getOutcomeSum() {
		return outcomeSum;
	}
	
	public double getOutcomeSumX() {
		return outcomeSumX;
	}
	
	
	public double getWeights(int k){
		return weights[k];
	}

	// il valore N è il numero di Test Case che si desidera estrarre con l'algoritmo.
	// il valore T è il numero totale di Test Frame.
	public ActiveSet(int N, int T, double[] occurrenceP) {
		super();
		testFrame = new ArrayList<String>();
		id = new int[N];
		outcome = new boolean[N];
		for (int i=0; i <N; i++){
			id[i]=0;
			outcome[i]=false;
		}
		weights = new double[T];
		occurrenceProb = new double[T];
		for (int i=0; i <T; i++){
			weights[i]=0;
			occurrenceProb[i] = occurrenceP[i];
		}
		
		outcomeSum = 0;
		outcomeSumX = 0;
	}
	
	//metodo di aggiornamento dell'active set
	public void activeSetUpdate(String tF, int tFnumber, boolean y, double[][] upweights){
		testFrame.add(tF);
		id[testFrame.size()-1] = tFnumber;
		outcome[testFrame.size()-1] = y;
		
		for(int i=0; i< weights.length; i++){
			weights[i] = upweights[tFnumber][i] + weights[i];
		}
		
		for(int i=0; i< testFrame.size(); i++){
			weights[id[i]] = 0;
		}
		
//		System.out.println("[DEBUG] testFrame:"+testFrame);
//		for(int i=0; i< weights.length; i++){
//			System.out.println("[DEBUG] "+(i)+") "+weights[i]);
//		}
		
//se è stato rilevato un fallimento incremento la somma degli esiti positivi e la somma delle variabili ausiliarie
		if(y){
			outcomeSum++;
//			System.out.println("[DEBUG ak.activeSetUpdate] Prima) outcomeSumX = " + outcomeSumX);
//			System.out.println("[DEBUG ak.activeSetUpdate] occurrenceProb = " + occurrenceProb[tFnumber]);
			outcomeSumX = outcomeSumX + occurrenceProb[tFnumber];
//			System.out.println("[DEBUG ak.activeSetUpdate] Dopo) outcomeSumX = " + outcomeSumX);
		}
		
	}
	
/********************Experimental Version**********************/	
	//metodo di aggiornamento dell'active set, versione sperimentale
	public void activeSetUpdateExp(String tF, int tFnumber, double y, double[][] upweights){
		testFrame.add(tF);
		id[testFrame.size()-1] = tFnumber;
		
		for(int i=0; i< weights.length; i++){
			weights[i] = upweights[tFnumber][i] + weights[i];
		}
		
		for(int i=0; i< testFrame.size(); i++){
			weights[id[i]] = 0;
		}
		
		if(y>0){
			outcomeSumX = outcomeSumX + occurrenceProb[tFnumber]*y;
		}
		
	}
/*****************************************/
	
//calcolo del testFrame da estrarre, richiede in ingresso la probabilità d e una variabile di tipo double in cui 
//verrà inserito il valore relativo alla probabilità di estrazione del TF in questione.
	public int testFrameExtraction(double d){
		double somma = weights[0];
		for(int i=1; i< weights.length; i++){
			somma = somma + weights[i];
		}
		
		double rand = Math.random();
		
//		System.out.println("[DEBUG ak.testFrameExtraction] numero random: "+rand);
//		for(int i=0; i< weights.length; i++){
//			System.out.println("[DEBUG] "+(i+1)+") "+weights[i]);
//		}
		
		int k = 0;
		double peso = weights[0]/somma;
		
		while(k < weights.length-1 && (rand >= peso)){
			k++;
			peso = peso + weights[k]/somma;
		}
		
		
		qi= d*(weights[k]/somma) + (1-d)*(1/(double)(weights.length-testFrame.size()));
		
//		System.out.println("[DEBUG ak.testFrameExtraction] numero estratto: "+k);
		
		return k;
	}
	
	public void qiCalculation(double d, int k){
		double somma = weights[0];
		for(int i=1; i< weights.length; i++){
			somma = somma + weights[i];
		}
//		System.out.println("[DEBUG ak.qiCalculation] somma = "+somma);
		if(somma!=0){
			qi = d*(weights[k]/somma) + (1-d)*(1/(double)(weights.length-testFrame.size()));
		} else{
			qi = (1/(double)(weights.length-testFrame.size()));
		}
//		System.out.println("[DEBUG ak.qiCalculation] id.lenght = "+id.length);
//		System.out.println("[DEBUG ak.qiCalculation] testFrame = "+testFrame.size());
//		System.out.println("[DEBUG ak.qiCalculation] qi = "+qi);
		
	}
	
	public void printSelectedTestFrame(){
		System.out.println("Selected Test Frame are :" + this.testFrame);
	}
	
	
}
