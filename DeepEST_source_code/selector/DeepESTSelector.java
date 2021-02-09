package selector;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import dataStructure.*;

public class DeepESTSelector {
	//	numero di failing point
	private int numfp;

	//	stime passo passo
	private double[] z;

	//richieste fallite
	private ArrayList<String> failedRequest;
	
	public DeepESTSelector() {
		super();
		numfp = 0;
		failedRequest = new ArrayList<String>();
	}

	public double[] getZ() {
		return z;
	}
	
	public int getnumfp(){
		return numfp;
	}
	
	public ArrayList<String> getfailedRequest(){
		return failedRequest;
	}

	public double[] selectAndRunTestCase(int n, ArrayList<TestFrame> testFrame, double[][] weightsMatrix, double d){
		failedRequest.clear();
		numfp = 0;


		if(n > testFrame.size() || n <= 0){
			double[] error =  {-1.0, -1.0};
			return error;
		}
		
		if(d <= 0 || d >= 1){
			double[] error =  {-2.0, -2.0};
			return error;
		}

		/**************Inizializzazione****************/
		//Definizione delle strutture dati: scompl, valori non ancora selezionati; occurrenceProb, vettore delle probabilità di occorrenza
		ArrayList<Integer> scompl = new ArrayList<Integer>();
		double[] occurrenceProb = new double[testFrame.size()];

		for(int i = 0; i < testFrame.size(); i++){
			scompl.add(i);
			occurrenceProb[i] = testFrame.get(i).getOccurrenceProb();
			//			System.out.println(occurrenceProb[i]);
		}

		//		System.out.println("[DEBUG] Passo 0");

		//Selezione della prima osservazione mediante SRS
		int randomNum = ThreadLocalRandom.current().nextInt(0, testFrame.size());
		//		System.out.println("[DEBUG] Numero random estratto "+randomNum);

		//		Inizializzazione dell'ActiveSet
		ActiveSet ak = new ActiveSet(n, testFrame.size(), occurrenceProb);

		String name = testFrame.get(randomNum).getTfID();
		boolean esito = testFrame.get(randomNum).extractAndExecuteTestCase();
		String tc = testFrame.get(randomNum).getName();
		//		System.out.println("[DEBUG] il Test Frame selezionato è "+name+ " con esito " +esito);

		//aggiornamento dell'active set e dell'insime dei TF non ancora selezionati
		ak.activeSetUpdate(name , randomNum, esito, weightsMatrix);
		scompl.remove(randomNum);

		double y = 0;
		//si suppone esito positivo (true) se viene rilevato un fallimento.
		if (esito) {
			y = 1;
			numfp++;
			this.failedRequest.add(tc);
		}

		//Definizione dell'array contenente le stime passo passo:
		double[] estimationX = new double[n];

		//calcolo del primo valore necessario per la stima (t).
		estimationX[0] = (testFrame.size())*(occurrenceProb[randomNum]*y);
		//		System.out.println("[DEBUG] z0X = "+estimationX[0]);

		double ziX = 0;
		int current_tf = 0;
		int k = 1;
		int ck = 0;
		double weightsSum = 0;
		double prob = 0;

		/****************Campionamento*****************/
		while(k < n){
			//			System.out.println("[DEBUG] Passo "+k);
			weightsSum = 0;
			for(int i=0; i<scompl.size(); i++){
				//				System.out.println("[DEBUG] elemento: " + scompl.get(i)+1);
				//				System.out.println("[DEBUG] peso: " + ak.getWeights(scompl.get(i)));
				weightsSum = weightsSum + ak.getWeights(scompl.get(i));
			}

			//			System.out.println("[DEBUG] Somma dei pesi link uscenti da ak: "+weightsSum);

			//Valutazione della probabilità
			if(weightsSum == 0){
				prob = d + 0.1;
			} else{
				prob = Math.random();
				//				System.out.println("[DEBUG] valore di probabilità estratto: "+prob);
			}

			if (prob <= d){
				//				System.out.println("[DEBUG] Ramo 1");
				//estrazione di un campione valutando il vettore dei pesi dell'active set
				current_tf = ak.testFrameExtraction(d);
				name = testFrame.get(current_tf).getTfID();
				esito = testFrame.get(current_tf).extractAndExecuteTestCase();
				//				System.out.println("[DEBUG] il Test Frame selezionato è "+name+" con esito "+esito);

				//Calcolo della zi:
				//1. Calcolo della sommatoria
				ziX = ak.getOutcomeSumX();
				//2. Somma del valore campionato
				if(esito){
					//					System.out.println("[DEBUG] qi: "+ak.qi);
					ziX = ziX + occurrenceProb[current_tf]/ak.qi;
					numfp++;
					tc = testFrame.get(current_tf).getName();
					this.failedRequest.add(tc);
				}

				//aggiunta del TF all'active set
				ak.activeSetUpdate(name , current_tf, esito, weightsMatrix);

				//rimozione dell'elemento dall'insieme dei TF non ancora selezionati
				ck = 0;
				while(ck < scompl.size() && scompl.get(ck)!=current_tf){
					//					System.out.println("[DEBUG] valore scompl["+ck+"] = "+scompl.get(ck));
					ck++;
				}

				if(ck == scompl.size()){
					System.out.println("[ERROR] OUT OF RANGE scompl 'Ramo 1'");
				}else{
					scompl.remove(ck);
				}
				//				System.out.println("[DEBUG] scompl" + scompl);


			} else {
				//				System.out.println("[DEBUG] Ramo 2");
				//selziono un campione random e ne prelevo l'id
				randomNum = ThreadLocalRandom.current().nextInt(0, scompl.size());

				current_tf = scompl.get(randomNum);

				name = testFrame.get(current_tf).getTfID();
				esito = testFrame.get(current_tf).extractAndExecuteTestCase();
				//				System.out.println("[DEBUG] il Test Frame selezionato è "+name+" con esito "+esito);

				//Calcolo della zi:
				//1. Calcolo della sommatoria
				ziX = ak.getOutcomeSumX();
				//2. Somma del valore campionato
				if(esito){
					ak.qiCalculation(d, current_tf);
					ziX = ziX + (occurrenceProb[current_tf]/ak.qi);
					numfp++;
					tc = testFrame.get(current_tf).getName();
					this.failedRequest.add(tc);
				}

				//				System.out.println("[DEBUG] qi =" + ak.qi);	
				//				System.out.println("[DEBUG] z"+k+" =" + ziX);

				ak.activeSetUpdate(name , current_tf, esito, weightsMatrix);
				scompl.remove(randomNum);
				//				System.out.println("[DEBUG] scompl" + scompl);
			}

			estimationX[k] = ziX;
			k++;
		}

		//		ak.printSelectedTestFrame();
		/***************Stima***************/
		this.z = estimationX;
		return this.estimatorBoCSP(n, estimationX);

	}


	private double[] estimatorBoCSP(int n, double[] estimationX){
		double sumX = 0;

		//Calcolo della sommatoria degli zi necessaria per la stima, viene considerato anche il primo valore,
		//dato che si tratta di un singolo valore e quindi va moltiplicato per 1
		for(int i=0; i<n; i++){
//			System.out.println("[DEBUG] "+(i)+") " + estimationX[i]);
			sumX = sumX + estimationX[i];
		}
		//		System.out.println("[DEBUG]la somma deli zi è" + sum);

		double[] stima = new double[2];

		//Calcolo della stima.
		stima[0] = (1/(double)(n))*sumX;

		//		Calcolo della varianza
		double num = 0;

		for(int i=1; i<n; i++){
			num = num + Math.pow((estimationX[i] - estimationX[0]), 2);
		}

		//		La stima della varianza del totale si ottiene come il prodotto di N quadro per la stima della varianza della media
		stima[1]= num/(double)(n*(n-1));


		return stima;
	}



}

