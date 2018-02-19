package ir.ac.ut.bsproject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class TraceParser {
    public static Action parseActionString(String actionString, State source, State dest){
        // Format: actionName/param1Name=param1Value, ...

        Action action;
        String actionName = actionString.split("/")[0];
        if(actionString.split("/").length <= 1) {
            action = new Action(actionName, new ArrayList<>(), source, dest);
        } else {
            String paramListString = actionString.split("/")[1];
            List<Parameter> parameters = Utils.parseParamListString(paramListString);
            action = new Action(actionName, parameters, source, dest);
        }
        return action;
    }


    public static TraceAutomata parseTraceFile(String traceFilePath) {
        TraceAutomata automata = new TraceAutomata();

        try(BufferedReader br = new BufferedReader(new FileReader(traceFilePath))) {
            int stateNumber = 0;
            State initialState = new State(0, true, false);
            automata.addState(initialState);
            String line;
            while((line = br.readLine()) != null) {
                State prevState = initialState;
                String[] actions = line.split("-");
                int i;
                for(i=0; i<actions.length-1; i++){
                    String actionString = actions[i];
                    stateNumber++;
                    State curState = new State(stateNumber, false, false);
                    Action action = parseActionString(actionString, prevState, curState);
                    prevState.addOutAction(action);
                    curState.addInActions(action);
                    automata.addState(curState);
                    prevState = curState;
                }
                stateNumber++;
                State finalState = new State(stateNumber, false, true);
                automata.addState(finalState);
                Action action = parseActionString(actions[i], prevState, finalState);
                prevState.addOutAction(action);
                finalState.addInActions(action);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return automata;
    }

    public static TraceAutomata parseTraceArray(String[] traceLines) {
        TraceAutomata automata = new TraceAutomata();

        int stateNumber = 0;
        State initialState = new State(0, true, false);
        automata.addState(initialState);
        for(String traceLine: traceLines){
            State prevState = initialState;
            String[] actions = traceLine.split("-");
            int i;
            for(i=0; i<actions.length-1; i++){
                String actionString = actions[i];
                stateNumber++;
                State curState = new State(stateNumber, false, false);
                Action action = parseActionString(actionString, prevState, curState);
                prevState.addOutAction(action);
                curState.addInActions(action);
                automata.addState(curState);
                prevState = curState;
            }
            stateNumber++;
            State finalState = new State(stateNumber, false, true);
            automata.addState(finalState);
            Action action = parseActionString(actions[i], prevState, finalState);
            prevState.addOutAction(action);
            finalState.addInActions(action);
        }
        return automata;
    }
}