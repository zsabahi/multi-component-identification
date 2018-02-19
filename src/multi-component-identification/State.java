package ir.ac.ut.bsproject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class State {
    public int number;
    public List<Action> outActions;
    public List<Action> inActions;
    public boolean is_initial;
    public boolean is_final;

    public State(int number, boolean is_initial, boolean is_final) {
        this.number = number;
        this.outActions = new ArrayList<>();
        this.inActions = new ArrayList<>();
        this.is_initial = is_initial;
        this.is_final = is_final;
    }

    public void addOutAction(Action outAction){
        this.outActions.add(outAction);
    }

    public void addInActions(Action inAction) { this.inActions.add(inAction); }

    public Action getOutAction(State dest){
        for(Action action: this.outActions){
            if(action.dest.equals(dest))
                return action;
        }
        return null;
    }

    public boolean hasAction(String actionName){
        for(Action action: this.outActions){
            if(action.name.equals(actionName))
                return true;
        }
        return false;
    }


    @Override
    public String toString() {
        return Integer.toString(this.number);
    }
}

class StateComparator implements Comparator<State> {
    @Override
    public int compare(State s1, State s2) {
        return s1.number - s2.number;
    }
}