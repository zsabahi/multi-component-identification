package automataChecker;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Distributer {

	public static Automata wholeAuto;
	Map<String, IPAtuomataChecker> checkers;
	
	Map<String, ArrayList<Boolean>> results;
	
	public Distributer() throws IOException, InterruptedException{
		
		wholeAuto = new Automata();
		checkers = new HashMap<>();
		
		results = new HashMap<String, ArrayList<Boolean>>();
		
	}
	
	public void buildAutomata() throws IOException{
		wholeAuto = AutomataReader.readAllPatterns(wholeAuto);
	}
	
	public boolean isInIpRange(String ip){
		return true;
	}
	
	public void specialCheck(String path) throws IOException{
		
		ArrayList<String> wholins = new ArrayList<>();
		BufferedReader bfr = new BufferedReader(new FileReader(path));
		String line = null;
		
		while( (line = bfr.readLine() ) != null){
			wholins.add(line);
		}
		bfr.close();
		
		for (int i = 0; i < wholins.size(); i++) {
			IPAtuomataChecker checker = new IPAtuomataChecker(wholeAuto, "this"+i, wholins.get(i));
			checkers.put("this"+i, checker);
		}

	}
	
	public void startChecking() throws IOException{
		for (Entry<String, IPAtuomataChecker> ckecker : checkers.entrySet()){
			FTReader.buildFlowTemplates(wholeAuto);
			ckecker.getValue().run();
			printResult();
		}
	}
	
	public void printResult() throws IOException{
		
		for (Entry<String, IPAtuomataChecker> ckecker : checkers.entrySet())
			ckecker.getValue().writeReults();
		
	}
	
}
