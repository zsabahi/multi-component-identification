package automataChecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class IPAtuomataChecker{
	
	String myIP;
	Automata automata;
	ArrayList<Integer> firstLevelTrigdPtrnsNum;
	ArrayList<Boolean> finallyTriggeredPatterns;
	String allMyInputs;
	
	public IPAtuomataChecker(Automata ati, String ip, String inputs){
		automata = ati;
		this.myIP = ip;
		firstLevelTrigdPtrnsNum = new ArrayList<Integer>();
		finallyTriggeredPatterns = new ArrayList<Boolean>();
		allMyInputs = inputs;
	}
	
	public ArrayList<String> getActs(String whole){
		
		ArrayList<String> acts = new ArrayList<>();
		
	    String txt = whole;
	    StringTokenizer st = new StringTokenizer(txt, "-");
		String cur = "";				
		while (st.hasMoreTokens()) {
			cur = st.nextToken();
			acts.add(cur);
		}
	    
	    return acts;
	}
	
	public void run(){
		
		ArrayList<String> actions = new ArrayList<>();
		actions = getActs(allMyInputs);
		
		try {
			firstLevelTrigdPtrnsNum = AutomataActionChecker.readAndMatch(automata, actions);
			finallyTriggeredPatterns = AutomataFlowChecker.checkSuccessfulPatterns(automata, firstLevelTrigdPtrnsNum, actions);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void writeReults() throws IOException{
		
		String finalResult = "";
		if(Config.checkFlowParameters){
			for (int i = 0; i < automata.patterns.size(); i++) {
				if(firstLevelTrigdPtrnsNum.contains(i)){
					if(finallyTriggeredPatterns.get(firstLevelTrigdPtrnsNum.indexOf(i)))
						finalResult += "True";
					else
						finalResult += "False";
				}else
					finalResult += "False";
			}
			
			System.out.println(finalResult);
		}
		
	}
	
}
