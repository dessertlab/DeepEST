package dataStructure;




public class TestFrame{

	private String name;
	private String tfID;
	private double failureProb;
	private double occurrenceProb;
	private String output;
	private boolean fail;

	public TestFrame(String _name, String _tfID, double _failureProb, double _occurrenceProb, String _output, boolean _fail) {
		super();
		this.name = _name;
		this.tfID = _tfID;
		this.failureProb = _failureProb;
		this.occurrenceProb = _occurrenceProb;
		this.output = _output;
		this.fail = _fail;
	}


	//	Metodo per l'esecuzione di un caso di Test estratto dal Test Frame
	public boolean extractAndExecuteTestCase(){
		return this.fail;
	}

	public String getOutput() {
		return output;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTfID() {
		return tfID;
	}

	public void setTfID(String tfID) {
		this.tfID = tfID;
	}

	public double getFailureProb() {
		return failureProb;
	}

	public void setFailureProb(double failureProb) {
		this.failureProb = failureProb;
	}

	public double getOccurrenceProb() {
		return occurrenceProb;
	}

	public void setOccurrenceProb(double occurrenceProb) {
		this.occurrenceProb = occurrenceProb;
	}

}
