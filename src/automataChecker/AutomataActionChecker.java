package automataChecker;

import java.io.IOException;
import java.util.ArrayList;

public class AutomataActionChecker {

	public static ArrayList<Integer> readAndMatch(Automata automata, ArrayList<String> allMyInputs) throws IOException, InterruptedException{
		
		ArrayList<Integer> trigdPtrnsNum = new ArrayList<Integer>();
		ArrayList<ArrayList<String>> currStates = new ArrayList<ArrayList<String>>();
		
		setTheInitials(automata, currStates);
		String action = "";
		
		for (int k = 0; k < allMyInputs.size(); k++) {
			
			action = allMyInputs.get(k);
			action = Action.extractActName(action);
			
			for (int i = 0; i < currStates.size(); i++) {
				
				ArrayList<String> newStates;
				ArrayList<String> temp = new ArrayList<>();
				
				for (int j = 0; j < currStates.get(i).size(); j++) {
					
					newStates = automata.patterns.get(i).nextStates(currStates.get(i).get(j), action);
					
					for (int j2 = 0; j2 < newStates.size(); j2++) {
						
						String newState = newStates.get(j2);
						
						if(!(isDuplicate(currStates.get(i), newState)) && !(isDuplicate(temp, newState)))
							temp.add(newState);
						
						if(automata.patterns.get(i).isFinal(newState)){
							
							if(!trigdPtrnsNum.contains(i))
								trigdPtrnsNum.add(i);
							
							break;
						}
						
						if(automata.patterns.get(i).isOneWay(currStates.get(i).get(j))){
							if(!automata.patterns.get(i).isFirst(currStates.get(i).get(j))){
								currStates.get(i).remove(currStates.get(i).get(j));
								j--;
							}
						}
						
					}

				}
				
				currStates.get(i).addAll(temp);
			}
		}
		return trigdPtrnsNum;
		
	}

	public static void setTheInitials(Automata automata, ArrayList<ArrayList<String>> currStates){
		
		ArrayList<String> tempState;
		for (int i = 0; i < automata.patterns.size(); i++) {
			
			tempState = new ArrayList<>();
			String firstState = automata.patterns.get(i).initState.getStateName();
			
			tempState.add(firstState);
			
			currStates.add(tempState);
		}
	}
	
	public String toString(Automata automata){
		return automata.patterns.toString();
	}
	
	public static boolean isDuplicate(ArrayList<String> currStates, String sName){
		return currStates.contains(sName);
	}

}
