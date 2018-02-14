import java.io.IOException;

import automataChecker.Distributer;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException{
		
		String inputFileName = "trace.txt";
		Distributer di = new Distributer();
		di.buildAutomata();
		di.specialCheck(inputFileName);
		di.startChecking();
		
	}
}