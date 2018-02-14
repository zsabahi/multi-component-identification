package automataChecker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Pattern {
	
	String patternName;
	Map<String, State> states;
	ArrayList<FlowTemplate> flowTRoots;
	State initState;
	
	public Pattern(String patName){
		
		states = new HashMap<>();
		patternName = patName;
		flowTRoots = new ArrayList<>();
		
	}
	
	public void resetFlowTemplates(){
		flowTRoots = new ArrayList<>();
	}
	
	public State getOrNewState(String stName){

		State temp = findState(stName);
		if(temp == null){
			temp = new State(stName);
		}
		return temp;
		
	}
	
	public void setInitialState(State st){
		initState = st;
	}
	
	public State getInitialState(){
		return initState;
	}
	
	public String getPatternName(){
		return patternName;
	}
	
	public void addAction(String sName, String action, String nextState, String flowName, String protocol, String packetLen, boolean isInitializer){
		
		State cur = findState(sName);
		
		Action temp = new Action(action, nextState, flowName, protocol, packetLen, isInitializer);
		boolean flag = true;
		for (int i = 0; i < cur.possibleActions.size(); i++) {
			if(cur.possibleActions.get(i).actionName.equals(temp.actionName)){
				cur.possibleActions.get(i).addToAction(nextState);
				flag = false;
			}
		}
		
		if(flag)
			cur.possibleActions.add(temp);
		
	}
	
	public void addState(State newState){
		
		State temp = findState(newState.getStateName());
		if(temp == null){
			states.put(newState.getStateName(), newState);
		}else if(newState.isFinal){
			temp.isFinal = true;
		}
	}
	
	public String toString(){
		return "\nPattern: " + patternName + "\ninitial state: ---" + initState + "---\n" + states.toString() + "\n----tempTree-----\n" + flowTRoots;
	}
	
	public ArrayList<String> nextStates(String currSName, String action){
		
		ArrayList<String> possibleNext = new ArrayList<>();
		
		State currState = findState(currSName);
		
		if(currState == null)
			return null;
		
		for (int k = 0; k < currState.possibleActions.size(); k++) {
			
			if(currState.possibleActions.get(k).actionName.equals(action)){
				possibleNext = currState.possibleActions.get(k).nextStateName;
			}
		}

		return possibleNext;
	}
	
	public State findState(String stateName){
		return states.get(stateName);
	}
	
	public ArrayList<State> findStates(ArrayList<String> stateNames){
		
		ArrayList<State> sth = new ArrayList<>(); 
		for (int i = 0; i < stateNames.size(); i++) {
			sth.add(states.get(stateNames.get(i)));
		}
		return sth;
	}
	
	public boolean isFinal(String sName){
		State t = findState(sName);
		if(t != null)
			return t.isFinal;
		else
			return false;
	}
	
	public boolean isDeadEnd(String sName){
		State t = findState(sName);
		if(t != null){
			if(t.possibleActions.size() == 0)
				return true;
			else
				return false;
		}
		else
			return false;
	}
	
	public boolean isOneWay(String sName){
		State t = findState(sName);
		if(t != null){
			if(t.possibleActions.size() == 1)
				return true;
			else
				return false;
		}
		else
			return false;
	}
	
	public boolean isFirst(String sName){
		return sName == initState.stateName;
	}
	
	public void printTemplates(){
		for (int i = 0; i < flowTRoots.size(); i++) {
			System.out.println(flowTRoots.get(i));
		}
	}
	
	public Action getTheAction(State st, int ActionIndex){
		if(st.possibleActions.size() >= ActionIndex)
			return st.possibleActions.get(ActionIndex);
		else{
			System.out.println("TraceBuilder Asked For Unavailable Action");
			return null;
		}
	}
	
	public void printNodeStuff(){
		for (int i = 0; i < flowTRoots.size(); i++) {
			printme(flowTRoots.get(i));
		}
	}

	public void printme(FlowTemplate cur){
		for (int i = 0; i < cur.childFlows.size(); i++) {
			printme(cur.childFlows.get(i));
		}
	}
	
}
