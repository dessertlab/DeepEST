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

	/**********************Static Version************************/	
	/* Per poter eseguire l'algoritmo ho bisogno di diverse strutture dati, che in questo caso ci aspettiamo vengano fornite dall'esterno.
Dato che i Test Case vengono selezionati in maniera randomica, si suppone che essi vengano selezionati prima di eseguire l'algoritmo e vengano assegnati alla struttura dati 
dei Test Frame, tenendo conto dell'esito.
I parametri di input considerati sono:
	1) n, numero di campioni da selezionare;
	2) TestFrame, caratterizzato dall'ID (intero compreso tra [0, N-1]), esitoTestCase;
	3) weightsMatrix, matrice dei pesi;
	4) d, che rappresenta la probabilità con cui verrà preferito il campionamento basato su pesi rispetto a quello random.
	 */	
	public double[] selectAndRunTestCase(int n, ArrayList<TestFrame> testFrame, double[][] weightsMatrix, double d){
		failedRequest.clear();
		numfp = 0;

		//Verifica dei dati in input
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
		//		return this.estimatorBoCSPmodified(n, estimationX);
//		return this.estimatorCoc(testFrame.size(), n, estimationX);
		//		return this.estimatorErrorBased(testFrame.size(), n, estimationX);

	}

	/* Per poter eseguire l'algoritmo ho bisogno di diverse strutture dati, che in questo caso ci aspettiamo vengano fornite dall'esterno.
Dato che i casi di test vengono selezionati in maniera randomica si suppone che essi vengano selezionati prima di eseguire l'algoritmo e vengano assegnati alla struttura dati 
dei test frame, tenendo conto dell'esito.
I parametri di input considerati sono:
	1) n, numero di campioni da selezionare;
	2) TestFrame, caratterizzato dall'ID (intero compreso tra [0, N-1]), esitoTestCase;
	3) weightsMatrix, matrice dei pesi;
	4) d, che rappresenta la probabilità con cui verrà preferito il campionamento basato su pesi rispetto a quello random;
	5) maxInitialSampleSize, per definire la dimensione massima del campione iniziale.
	 */	
	public double[] selectAndRunTestCasev2(int n, ArrayList<TestFrame> testFrame, double[][] weightsMatrix, double d, int maxInitialSampleSize){
		numfp = 0;

		//		Verifica che il numero di campioni da estrarre dalla popolazione sia <= alla dimensione della popolazione o <= 0
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
		}

		//		Inizializzazione dell'ActiveSet
		ActiveSet ak = new ActiveSet(n, testFrame.size(), occurrenceProb);

		int k = 0, n0 = 0, randomNum = 0;
		int current_tf = 0;
		String name;
		boolean esito;


		//stima della variabile ausiliaria x (prodotto di prob di occorrenza per esito)
		double[] estimationX = new double[n];
		for(int i=0; i<estimationX.length; i++){
			estimationX[i] = 0;
		}

		//		System.out.println("[DEBUG] Passo "+k);
		//Selezione della prima osservazione mediante SRS
		randomNum = ThreadLocalRandom.current().nextInt(0, scompl.size());

		current_tf = scompl.get(randomNum);

		name = testFrame.get(current_tf).getTfID();
		esito = testFrame.get(current_tf).extractAndExecuteTestCase();	
		//				System.out.println("[DEBUG] il Test Frame selezionato è "+name+ " con esito " +esito);

		//aggiornamento dell'active set e dell'insime dei TF non ancora selezionati
		ak.activeSetUpdate(name , current_tf, esito, weightsMatrix);
		scompl.remove(randomNum);

		double y = 0;
		//si suppone esito positivo (true) se viene rilevato un fallimento.
		if (esito) {
			y = 1;
			numfp++;
		}

		//calcolo del primo valore necessario per la stima (t).
		estimationX[k] = (testFrame.size()/(double)n)*(occurrenceProb[randomNum]*y);
		k++;
		n0++;
		//		System.out.println("[DEBUG] z0X = "+estimationX[0]);

		//Costruzione del campione inziale
		//Nel caso di d=0, ovvero degenerazione dell'algoritmo nel SRS, si ha l'esecuzione del campionamento interamente all'interno di questo ciclo while.
		while((estimationX[k-1]==0 && k<maxInitialSampleSize) || (d==0 && k < n)){
			//			System.out.println("[DEBUG] Passo "+k);
			//Selezione della prima osservazione mediante SRS
			randomNum = ThreadLocalRandom.current().nextInt(0, scompl.size());

			current_tf = scompl.get(randomNum);

			name = testFrame.get(current_tf).getTfID();
			esito = testFrame.get(current_tf).extractAndExecuteTestCase();	
			//					System.out.println("[DEBUG] il Test Frame selezionato è "+name+ " con esito " +esito);

			//aggiornamento dell'active set e dell'insime dei TF non ancora selezionati
			ak.activeSetUpdate(name , current_tf, esito, weightsMatrix);


			y = 0;
			//si suppone esito positivo (true) se viene rilevato un fallimento.
			if (esito) {
				y = 1;
				numfp++;
			}

			//calcolo del primo valore necessario per la stima (t).
			estimationX[k] = (testFrame.size()/(double)n)*(occurrenceProb[scompl.get(randomNum)]*y);
			scompl.remove(randomNum);
			k++;
			n0++;
			//		System.out.println("[DEBUG] z0X = "+estimationX[0]);
		}


		int ck = 0;
		double ziX = 0, weightsSum = 0;
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

				//calcolo della zi
				ziX = ak.getOutcomeSumX();

				if(esito){
					//					System.out.println("[DEBUG] qi: "+ak.qi);
					ziX = ziX + occurrenceProb[current_tf]/ak.qi;
					numfp++;
				}
				//				System.out.println("[DEBUG] z"+k+" =" + zi);

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

				ziX = ak.getOutcomeSumX();
				//				System.out.println("[DEBUG] valore della somma dei precedenti "+k+" zX =" + ziX);

				if(esito){
					ak.qiCalculation(d, current_tf);
					ziX = ziX + (occurrenceProb[current_tf]/ak.qi);
					numfp++;
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

		/***************Stima***************/
		this.z = estimationX;
				return this.estimatorBoCSPv2(testFrame.size(), n, n0, d, estimationX);
		//		return this.estimatorBoCSPv2modified(testFrame.size(), n, n0, d, estimationX);
//		return this.estimatorCocv2(testFrame.size(), n, n0, d, estimationX);
	}

	/**********************Adaptive Version************************/	
	/* Per poter eseguire l'algoritmo ho bisogno di diverse strutture dati, che in questo caso ci aspettiamo vengano fornite dall'esterno.
Dato che i Test Case vengono selezionati in maniera randomica, si suppone che essi vengano selezionati prima di eseguire l'algoritmo e vengano assegnati alla struttura dati 
dei Test Frame, tenendo conto dell'esito.
I parametri di input considerati sono:
	1) n, numero di campioni da selezionare;
	2) TestFrame, caratterizzato dall'ID (intero compreso tra [0, N-1]), esitoTestCase;
	3) weightsMatrix, matrice dei pesi;
	4) d0, che rappresenta la probabilità con cui verrà preferito il campionamento basato su pesi rispetto a quello random.
In questa versione dell'algoritmo la d è modificata a runtime utilizzando due registri a scorrimento che fanno protendere la scelta del campionamento in base ai successi collezionati.
	 */	
	public double[] selectAndRunTestCaseAdaptive(int n, ArrayList<TestFrame> testFrame, double[][] weightsMatrix, double d0){
		numfp = 0;
		//		Verifica che il numero di campioni da estrarre dalla popolazione sia <= alla dimensione della popolazione
		if(n > testFrame.size() || n <= 0){
			double[] error =  {-1.0, -1.0};
			return error;
		}

		if(d0 <= 0 || d0 >= 1){
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
		//		System.out.println("[DEBUG] il Test Frame selezionato è "+name+ " con esito " +esito);

		//aggiornamento dell'active set e dell'insime dei TF non ancora selezionati
		ak.activeSetUpdate(name , randomNum, esito, weightsMatrix);
		scompl.remove(randomNum);

		double y = 0;
		//si suppone esito positivo (true) se viene rilevato un fallimento.
		if (esito) {
			y = 1;
			numfp++;
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

		double d = d0;
		//costruzione dei due registri a scorrimento per la definizone dell'algoritmo adattativo
		ArrayList<Integer> sR0 = new ArrayList<Integer>();
		ArrayList<Integer> sR1 = new ArrayList<Integer>();

		for(int f=0; f<4; f++){
			sR0.add(0);
			sR1.add(0);
		}

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

			//Calcolo della d
			d = d0 + (1-d0)*(sR0.get(3)*0.4 + sR0.get(2)*0.3 + sR0.get(1)*0.2 + sR0.get(0)*0.1) - (1-d0)*(sR1.get(3)*0.4 + sR1.get(2)*0.3 + sR1.get(1)*0.2 + sR1.get(0)*0.1);

			//per sopperire ad eventuali errori di calcolo dovuto alle operazioni con i double
			if(d > 1){
				d = 1;
			} else if(d < 0){
				d = 0;
			}


			//Valutazione della probabilità
			if(weightsSum == 0){
				prob = d + 0.1;
			} else{
				prob = Math.random();
				//				System.out.println("[DEBUG] valore di probabilità estratto: "+prob);
			}


			//			System.out.println("[DEBUG] d = "+d);
			//			System.out.println("[DEBUG] sR0 = "+sR0);
			//			System.out.println("[DEBUG] sR1 = "+sR1);

			if (prob <= d){
				//				System.out.println("[DEBUG] Ramo 1");
				//estrazione di un campione valutando il vettore dei pesi dell'active set
				current_tf = ak.testFrameExtraction(d);
				name = testFrame.get(current_tf).getTfID();
				esito = testFrame.get(current_tf).extractAndExecuteTestCase();
				//				System.out.println("[DEBUG] il Test Frame selezionato è "+name+" con esito "+esito);

				//calcolo della zi
				ziX = ak.getOutcomeSumX();
				sR0.remove(0);

				if(esito){
					//					System.out.println("[DEBUG] qi: "+ak.qi);
					ziX = ziX + occurrenceProb[current_tf]/ak.qi;
					sR0.add(1);
					numfp++;
				} else {
					sR0.add(0);
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

				ziX = ak.getOutcomeSumX();
				//				System.out.println("[DEBUG] valore della somma dei precedenti "+k+" zX =" + ziX);

				sR1.remove(0);

				if(esito){
					ak.qiCalculation(d, current_tf);
					ziX = ziX + (occurrenceProb[current_tf]/ak.qi);
					sR1.add(1);
					numfp++;
				} else {
					sR1.add(0);
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

		/***************Stima***************/
		this.z = estimationX;
		//		return this.estimatorBoCSP(n, estimationX);
		//		return this.estimatorBoCSPmodified(n, estimationX);
		//		return this.estimatorBoCSPmodifiedmedian(n, estimationX);
		//		return this.estimatorBoCSPoutlierselimination(n, estimationX);
		return this.estimatorCoc(testFrame.size(), n, estimationX);
	}

	/* Per poter eseguire l'algoritmo ho bisogno di diverse strutture dati, che in questo caso ci aspettiamo vengano fornite dall'esterno.
Dato che i casi di test vengono selezionati in maniera randomica si suppone che essi vengano selezionati prima di eseguire l'algoritmo e vengano assegnati alla struttura dati 
dei test frame, tenendo conto dell'esito.
I parametri di input considerati sono:
	1) n, numero di campioni da selezionare;
	2) TestFrame, caratterizzato dall'ID (intero compreso tra [0, N-1]), esitoTestCase;
	3) weightsMatrix, matrice dei pesi;
	4) d0, che rappresenta la probabilità con cui verrà preferito il campionamento basato su pesi rispetto a quello random;
	5) maxInitialSampleSize, per definire la dimensione massima del campione iniziale.
In questa versione dell'algoritmo la d è modificata a runtime utilizzando due registri a scorrimento che fanno protendere la scelta del campionamento in base ai successi collezionati.
	 */	
	public double[] selectAndRunTestCaseAdaptivev2(int n, ArrayList<TestFrame> testFrame, double[][] weightsMatrix, double d0, int maxInitialSampleSize){
		numfp = 0;
		//		Verifica che il numero di campioni da estrarre dalla popolazione sia <= alla dimensione della popolazione o <= 0
		if(n > testFrame.size() || n <= 0 || maxInitialSampleSize >= n){
			System.out.println("Input size ERROR");
			double[] error =  {-1.0, -1.0};
			return error;
		}

		if(d0 <= 0 || d0 >= 1){
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
		}

		//		Inizializzazione dell'ActiveSet
		ActiveSet ak = new ActiveSet(n, testFrame.size(), occurrenceProb);

		int k = 0, n0 = 0, randomNum = 0;
		int current_tf = 0;
		String name;
		boolean esito;


		//stima della variabile ausiliaria x (prodotto di prob di occorrenza per esito)
		double[] estimationX = new double[n];
		for(int i=0; i<estimationX.length; i++){
			estimationX[i] = 0;
		}

		//		System.out.println("[DEBUG] Passo "+k);
		//Selezione della prima osservazione mediante SRS
		randomNum = ThreadLocalRandom.current().nextInt(0, scompl.size());

		current_tf = scompl.get(randomNum);

		name = testFrame.get(current_tf).getTfID();
		esito = testFrame.get(current_tf).extractAndExecuteTestCase();	
		//		System.out.println("[DEBUG] il Test Frame selezionato è "+name+ " con esito " +esito);

		//aggiornamento dell'active set e dell'insime dei TF non ancora selezionati
		ak.activeSetUpdate(name , current_tf, esito, weightsMatrix);
		scompl.remove(randomNum);

		double y = 0;
		//si suppone esito positivo (true) se viene rilevato un fallimento.
		if (esito) {
			y = 1;
			numfp++;
		}

		//calcolo del primo valore necessario per la stima (t).
		estimationX[k] = (testFrame.size()/(double)n)*(occurrenceProb[randomNum]*y);
		k++;
		n0++;
		//		System.out.println("[DEBUG] z0X = "+estimationX[0]);


		while(estimationX[k-1]==0 && k<maxInitialSampleSize){
			//			System.out.println("[DEBUG] Passo "+k);
			//Selezione della prima osservazione mediante SRS
			randomNum = ThreadLocalRandom.current().nextInt(0, scompl.size());

			current_tf = scompl.get(randomNum);

			name = testFrame.get(current_tf).getTfID();
			esito = testFrame.get(current_tf).extractAndExecuteTestCase();	
			//		System.out.println("[DEBUG] il Test Frame selezionato è "+name+ " con esito " +esito);

			//aggiornamento dell'active set e dell'insime dei TF non ancora selezionati
			ak.activeSetUpdate(name , current_tf, esito, weightsMatrix);


			y = 0;
			//si suppone esito positivo (true) se viene rilevato un fallimento.
			if (esito) {
				y = 1;
				numfp++;
			}

			//calcolo del primo valore necessario per la stima (t).
			estimationX[k] = (testFrame.size()/(double)n)*(occurrenceProb[scompl.get(randomNum)]*y);
			scompl.remove(randomNum);

			k++;
			n0++;
			//		System.out.println("[DEBUG] z0X = "+estimationX[0]);
		}


		int ck = 0;
		double ziX = 0, weightsSum = 0;
		double prob = 0;

		double d = d0;
		//costruzione dei due registri a scorrimento per la definizone dell'algoritmo adattativo
		ArrayList<Integer> sR0 = new ArrayList<Integer>();
		ArrayList<Integer> sR1 = new ArrayList<Integer>();

		for(int f=0; f<4; f++){
			sR0.add(0);
			sR1.add(0);
		}

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

			//Calcolo della d
			d = d0 + (1-d0)*(sR0.get(3)*0.4 + sR0.get(2)*0.3 + sR0.get(1)*0.2 + sR0.get(0)*0.1) - (1-d0)*(sR1.get(3)*0.4 + sR1.get(2)*0.3 + sR1.get(1)*0.2 + sR1.get(0)*0.1);

			//per sopperire ad eventuali errori di calcolo dovuto alle operazioni con i double
			if(d > 1){
				d = 1;
			} else if(d < 0){
				d = 0;
			}

			//Valutazione della probabilità
			if(weightsSum == 0){
				//				d = 0;
				prob = d + 0.1;
			} else{
				prob = Math.random();
				//				System.out.println("[DEBUG] valore di probabilità estratto: "+prob);
			}

			//			System.out.println("[DEBUG] d = "+d);
			//			System.out.println("[DEBUG] sR0 = "+sR0);
			//			System.out.println("[DEBUG] sR1 = "+sR1);


			if (prob <= d){
				//				System.out.println("[DEBUG] Ramo 1");
				//estrazione di un campione valutando il vettore dei pesi dell'active set
				current_tf = ak.testFrameExtraction(d);
				name = testFrame.get(current_tf).getTfID();
				esito = testFrame.get(current_tf).extractAndExecuteTestCase();
				//				System.out.println("[DEBUG] il Test Frame selezionato è "+name+" con esito "+esito);

				//calcolo della zi e aggiornamento del relativo registro a scorrimento
				ziX = ak.getOutcomeSumX();
				sR0.remove(0);

				if(esito){
					//					System.out.println("[DEBUG] qi: "+ak.qi);
					ziX = ziX + occurrenceProb[current_tf]/ak.qi;
					sR0.add(1);
					numfp++;
				} else {
					sR0.add(0);
				}
				//				System.out.println("[DEBUG] z"+k+" =" + zi);

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
				//				System.out.println("[DEBUG] per il mapping dei valori mancanti bisogna sommare 1");


			} else {
				//				System.out.println("[DEBUG] Ramo 2");
				//selziono un campione random e ne prelevo l'id
				randomNum = ThreadLocalRandom.current().nextInt(0, scompl.size());

				current_tf = scompl.get(randomNum);

				name = testFrame.get(current_tf).getTfID();
				esito = testFrame.get(current_tf).extractAndExecuteTestCase();
				//				System.out.println("[DEBUG] il Test Frame selezionato è "+name+" con esito "+esito);

				//calcolo della zi e aggiornamento del relativo registro a scorrimento
				ziX = ak.getOutcomeSumX();
				//				System.out.println("[DEBUG] valore della somma dei precedenti "+k+" zX =" + ziX);
				sR1.remove(0);

				if(esito){
					ak.qiCalculation(d, current_tf);
					ziX = ziX + (occurrenceProb[current_tf]/ak.qi);
					sR1.add(1);
					numfp++;
				} else {
					sR1.add(0);
				}

				//				System.out.println("[DEBUG] qi =" + ak.qi);	
				//				System.out.println("[DEBUG] z"+k+" =" + ziX);

				ak.activeSetUpdate(name , current_tf, esito, weightsMatrix);
				scompl.remove(randomNum);
				//				System.out.println("[DEBUG] scompl" + scompl);
				//				System.out.println("[DEBUG] per il mapping dei valori mancanti bisogna sommare 1");
			}

			estimationX[k] = ziX;
			k++;
		}

		/***************Stima***************/
		this.z = estimationX;
		//		return this.estimatorBoCSPv2(testFrame.size(), n, n0, d0, estimationX);
		//		return this.estimatorBoCSPv2modified(testFrame.size(), n, n0, d, estimationX);
		return this.estimatorCocv2(testFrame.size(), n, n0, d, estimationX);
	}

	/**********************Estimators************************/
	//I due stimatori di seguito riportati sono influenzati dall'ordine con cui vengono prelevati i campioni.
	//stiamtore per n0=1
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

	//stimatore per n0>1
	private double[] estimatorBoCSPv2(int N, int n, int n0, double d, double[] estimationX){
		double sumX0 = 0, sumX = 0, sum = 0;

		for(int i=0; i<n; i++){
			//			System.out.println("[DEBUG] z"+(i)+") " + estimationX[i]);
			sum = sum + estimationX[i];
		}

		for(int i=0; i<n0; i++){
			sumX0 = sumX0 + estimationX[i];
		}

		//		System.out.println("[DEBUG] estimationX[n0-1] = " +estimationX[n0-1]);
		//		System.out.println("[DEBUG] sumX0 = " +sumX0);
		//		System.out.println("[DEBUG] x0 = " +x0);

		for(int i=n0; i<n; i++){
			sumX = sumX + estimationX[i];
		}

		//		System.out.println("[DEBUG]la somma degli zi è" + sum);

		double[] stima = new double[2];

		//Calcolo della stima.

		//		In stima[0] viene inserito il valore della stima calcolata come in Evoluzione dell'algoritmo.
		if(d==0 || n0 == n){
			stima[0] = sum; //(testFrame.size()/(double)n)*sum;
		}else{
			stima[0] = (1/(double)(n))*(n0*sumX0+sumX);
		}


		//		Stima della varianza
		//		Calcolo della varianza
		double num = 0;
		if (n0==1) {
			for(int i=1; i<n; i++){
				num = num + Math.pow((estimationX[i] - estimationX[0]), 2);
			}

			stima[1] = num/(double)(n*(n-1));
		} else {
			double m0 = 0, v1 = 0, v2 = 0, v = 0;

			for(int i=0; i<n0; i++){
				m0 = m0 + estimationX[i];
			}

			m0 = m0/(double)n0;
			//			System.out.println("[DEBUG] m0 = " + m0);

			for(int i=0; i<n0; i++){
				v = v + Math.pow(estimationX[i] - m0, 2);
				//				System.out.println("[DEBUG] v = " + v);
			}

			v = v/(double)(n0-1);
			//			System.out.println("[DEBUG] v = " + v);

			v1 = v*((N-n0))/(double)(N*n0);
			//			System.out.println("[DEBUG] v1 = " + v1);

			//			eseguo il calcolo di v2 se ci sono almeno due elementi, ovvero se non è degenerato in un SRS oppure se non ha campionato un singolo elemento dopo gli n0 iniziali
			//			quindi con varianza nulla.
			if(n-n0>1){
				m0 = sumX/(double)(n-n0);

				v = 0;

				for(int i=n0; i<n; i++){
					v = v + Math.pow(estimationX[i] - m0, 2);
				}

				v2 = v/(double)((n-n0)*(n-n0-1)*(Math.pow(N, 2)));
				//				System.out.println("[DEBUG] v2 = " + v2);

			}

			stima[1] = Math.pow(N, 2)*(Math.pow((n0/(double)(n)), 2)*v1 + (Math.pow(((n-n0)/(double)(n)), 2)*v2));
			//			System.out.println("[DEBUG] stima[2] primo termine = " +Math.pow((n0/(double)(n)), 2)*v1);
			//			System.out.println("[DEBUG] stima[2] secondo termine = " +(Math.pow(((n-n0)/(double)(n)), 2)*v2));
			//			System.out.println("[DEBUG] stima[2] = " + stima[2]);
		}

		return stima;
	}

	//modified estimator
	private double[] estimatorBoCSPmodified(int n, double[] estimationX){
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


		ArrayList<Double> values = new ArrayList<Double>();
		double diff = 0, percentvalue_pos = 0.9, percentvalue_neg = 0.1;
		sumX = 0;

		for(int i=0; i<n; i++){
			diff = estimationX[i]-stima[0];
			if(diff <= stima[0]*percentvalue_pos && diff >= -1*stima[0]*percentvalue_neg){
				//				System.out.println("[DEBUG] "+(i)+") " + estimationX[i]);
				values.add(estimationX[i]);
				sumX = sumX + estimationX[i];
			}
		}

		if(values.size() > 0){
			stima[0] = (1/(double)(values.size()))*sumX;
		}


		//		Calcolo della varianza
		double num = 0;

		for(int i=1; i<n; i++){
			num = num + Math.pow((estimationX[i] - estimationX[0]), 2);
		}

		//		La stima della varianza del totale si ottiene come il prodotto di N quadro per la stima della varianza della media
		stima[1]= num/(double)(n*(n-1));

		return stima;
	}

	private double[] estimatorBoCSPv2modified(int N, int n, int n0, double d, double[] estimationX){
		double sumX0 = 0, sumX = 0, sum = 0;

		for(int i=0; i<n; i++){
			//			System.out.println("[DEBUG] z"+(i)+") " + estimationX[i]);
			sum = sum + estimationX[i];
		}

		for(int i=0; i<n0; i++){
			sumX0 = sumX0 + estimationX[i];
		}

		for(int i=n0; i<n; i++){
			sumX = sumX + estimationX[i];
		}

		//		System.out.println("[DEBUG]la somma degli zi è" + sum);

		double[] stima = new double[2];

		//Calcolo della stima.

		//		In stima[0] viene inserito il valore della stima calcolata come in Evoluzione dell'algoritmo.
		if(d==0 || n0 == n){
			stima[0] = sum;
		}else{
			stima[0] = (1/(double)(n))*(n0*sumX0+sumX);
		}


		//		calcolo della stima in maniera alternativa
		if(!(d==0 || n0 == n)){
			ArrayList<Double> values = new ArrayList<Double>();
			double diff = 0, percentvalue_pos = 0.9, percentvalue_neg = 0.1;
			double somma = 0;

			diff = sumX0-stima[0];

			if(diff <= stima[0]*percentvalue_pos && diff >= -1*stima[0]*percentvalue_neg){
				//				System.out.println("[DEBUG] "+(i)+") " + estimationX[i]);
				values.add(sumX0);
				somma = somma + sumX0;
			}

			for(int i=n0; i<n; i++){
				diff = estimationX[i]-stima[0];
				if(diff <= stima[0]*percentvalue_pos && diff >= -1*stima[0]*percentvalue_neg){
					//					System.out.println("[DEBUG] "+(i)+") " + estimationX[i]);
					values.add(estimationX[i]);
					somma = somma + estimationX[i];
				}
			}

			if(values.size() > 0){
				stima[0] = (1/(double)(values.size()))*somma;
			}

		}


		//		Stima della varianza
		//		Calcolo della varianza
		double num = 0;
		if (n0==1) {
			for(int i=1; i<n; i++){
				num = num + Math.pow((estimationX[i] - estimationX[0]), 2);
			}

			stima[1] = num/(double)(n*(n-1));
		} else {
			double m0 = 0, v1 = 0, v2 = 0, v = 0;

			for(int i=0; i<n0; i++){
				m0 = m0 + estimationX[i];
			}

			m0 = m0/(double)n0;
			//			System.out.println("[DEBUG] m0 = " + m0);

			for(int i=0; i<n0; i++){
				v = v + Math.pow(estimationX[i] - m0, 2);
				//				System.out.println("[DEBUG] v = " + v);
			}

			v = v/(double)(n0-1);
			//			System.out.println("[DEBUG] v = " + v);

			v1 = v*((N-n0))/(double)(N*n0);
			//			System.out.println("[DEBUG] v1 = " + v1);

			//			eseguo il calcolo di v2 se ci sono almeno due elementi, ovvero se non è degenerato in un SRS oppure se non ha campionato un singolo elemento dopo gli n0 iniziali
			//			quindi con varianza nulla.
			if(n-n0>1){
				m0 = sumX/(double)(n-n0);

				v = 0;

				for(int i=n0; i<n; i++){
					v = v + Math.pow(estimationX[i] - m0, 2);
				}

				v2 = v/(double)((n-n0)*(n-n0-1)*(Math.pow(N, 2)));
				//				System.out.println("[DEBUG] v2 = " + v2);

			}

			stima[1] = Math.pow(N, 2)*(Math.pow((n0/(double)(n)), 2)*v1 + (Math.pow(((n-n0)/(double)(n)), 2)*v2));
			//			System.out.println("[DEBUG] stima[2] primo termine = " +Math.pow((n0/(double)(n)), 2)*v1);
			//			System.out.println("[DEBUG] stima[2] secondo termine = " +(Math.pow(((n-n0)/(double)(n)), 2)*v2));
			//			System.out.println("[DEBUG] stima[2] = " + stima[2]);
		}


		return stima;
	}


	//contribute estimator
	private double[] estimatorCoc(int N, int n, double[] estimationX){
		double nperc = n/(double)N;

		if(nperc >= 0.66){
			//			System.out.println("[DEBUG] Stimatore articolo "+nperc);
			return this.estimatorBoCSP(n, estimationX);
		} else if(nperc <= 0.34){
			//			System.out.println("[DEBUG] Stimatore n piccolo "+nperc);
			return this.estimatorBoCSPmodified(n, estimationX);
		} else {
			//			System.out.println("[DEBUG] Stimatore coc "+nperc);
			double[] estimation1, estimation2;
			double coc = (nperc - 0.34)*3;

			estimation1 = this.estimatorBoCSPmodified(n, estimationX);
			estimation2 = this.estimatorBoCSP(n, estimationX);

			estimation1[0] = (1-coc)*estimation1[0] + coc*estimation2[0];

			return estimation1;
		}

	}

	private double[] estimatorCocv2(int N, int n, int n0, double d, double[] estimationX){
		double nperc = n/(double)N;

		if(nperc >= 0.66){
			//			System.out.println("[DEBUG] Stimatore articolo "+nperc);
			return this.estimatorBoCSPv2(N, n, n0, d, estimationX);
		} else if(nperc <= 0.34){
			//			System.out.println("[DEBUG] Stimatore n piccolo "+nperc);
			return this.estimatorBoCSPv2modified(N, n, n0, d, estimationX);
		} else {
			//			System.out.println("[DEBUG] Stimatore coc "+nperc);
			double[] estimation1, estimation2;
			double coc = (nperc - 0.34)*3;

			estimation1 = this.estimatorBoCSPv2modified(N, n, n0, d, estimationX);
			estimation2 = this.estimatorBoCSPv2(N, n, n0, d, estimationX);

			estimation1[0] = (1-coc)*estimation1[0] + coc*estimation2[0];

			return estimation1;
		}

	}

}

