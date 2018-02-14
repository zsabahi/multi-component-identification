package automataChecker;
import java.util.ArrayList;

public class State {

	String stateName;
	ArrayList<Action> possibleActions;
	boolean isFinal;
	
	public State(String sname) {
		possibleActions = new ArrayList<Action>();
		stateName = sname;
		isFinal = false;
	}
	
	public boolean isFinalState(){
		return isFinal;
	}
	
	public void setAsFinalState(){
		isFinal = true;
	}
	
	public String getStateName() {
		return stateName;
	}
	
	public int countPossibleActions(){
		return possibleActions.size();
	}
	
	public void defineNewEvent(String act, String nexst){
		
		Action temp = new Action(act, nexst);
		possibleActions.add(temp);
		
	}
	
	public String toString(){
		return "\n" + stateName + ":\n" + possibleActions.toString() + "\n" + "final?: " + isFinal;
	}
	
	public Action getMatchingAction(String actionName){
		for (int i = 0; i < possibleActions.size(); i++) {
			if(possibleActions.get(i).actionName.equals(actionName))
				return possibleActions.get(i);
		}
		return null;
	}
	
}
