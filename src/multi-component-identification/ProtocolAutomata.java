package ir.ac.ut.bsproject;

import java.util.ArrayList;
import java.util.HashMap;

public class ProtocolAutomata extends Automata {
    public String languageSet;
    
    public boolean hasAction(String actionName){
        return languageSet.contains(actionName + ",") || languageSet.contains("," + actionName);
    }

    public void addActionToLanguageSet(String actionName){
        if(this.languageSet == null)
            this.languageSet = actionName;
        else if(!this.hasAction(actionName))
                this.languageSet += "," + actionName;
    }

    public HashMap<Integer, ArrayList<String>> getSelfloops(){
        HashMap<Integer, ArrayList<String>> selfLoops = new HashMap<>();
        for(Action action: this.getActions()){
            if(action.source.number == action.dest.number){
                ArrayList<String> stateSelfLoops = new ArrayList<>();
                if(selfLoops.containsKey(action.source.number))
                    stateSelfLoops = selfLoops.get(action.source.number);
                stateSelfLoops.add(action.name);
                selfLoops.put(action.source.number, stateSelfLoops);
            }
        }
        return selfLoops;
    }
}
