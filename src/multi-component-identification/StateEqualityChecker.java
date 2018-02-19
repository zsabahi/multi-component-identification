package ir.ac.ut.bsproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class StateEqualityChecker {
    private static HashMap<String, ProtocolAutomata> getFlowProtocol(TraceAutomata traceAutomata, ArrayList<ProtocolAutomata> protocols){
        HashMap<String, ProtocolAutomata> flowProtocol = new HashMap<>();

        for(Action action:traceAutomata.getActions()){
            String flowId = action.getFlowId();
            if(flowId!=null && flowProtocol.get(flowId)==null){
                for(ProtocolAutomata protocol:protocols){
                    if(protocol.hasAction(action.name))
                        flowProtocol.put(flowId, protocol);
                }
            }
        }
        return flowProtocol;
    }

    public static HashMap<State, HashMap<String, Pair<String, Integer>>> buildFlowVector(TraceAutomata traceAutomata, ArrayList<ProtocolAutomata> protocols, int config){
        HashMap<String, ProtocolAutomata> flowProtocol = getFlowProtocol(traceAutomata, protocols);
        HashMap<State, HashMap<String, Pair<String, Integer>>> flowVector = new HashMap<>();
        HashMap<String, Pair<String, Integer>> tempFlowVector = new HashMap<>();

        //initial tempFlowVector for initial state
        for (Map.Entry<String, ProtocolAutomata> entry : flowProtocol.entrySet())
        {
            ProtocolAutomata protocol = entry.getValue();
            String flowId = entry.getKey();
            tempFlowVector.put(flowId, new Pair<>(protocol.languageSet, protocol.getInitialState().number));
        }

        Boolean[] isVisited = new Boolean[traceAutomata.states.size()];
        for(int i=0; i<traceAutomata.states.size(); i++)
            isVisited[i] = false;

        LinkedList<State> queue = new LinkedList<>();
        State initialState = traceAutomata.getInitialState();
        flowVector.put(initialState, tempFlowVector);
        queue.add(initialState);
        while(!queue.isEmpty()){
            State topState = queue.remove();
            if(!isVisited[topState.number]) {
                isVisited[topState.number] = true;
                for (Action action : topState.outActions) {
                    if(!action.dest.equals(topState))
                        queue.add(action.dest);
                    String flowId = action.getFlowId();
                    String deltaTime = action.getDeltaTime();
                    ProtocolAutomata protocol = flowProtocol.get(flowId);
                    State nextState = protocol.getNextState(flowVector.get(topState).get(flowId).getRight(), action.name, deltaTime, 1, flowId);
                    HashMap<String, Pair<String, Integer>> tmpFlowVector;
                    HashMap<String, Pair<String, Integer>> parentFlowVector = new HashMap<>(flowVector.get(topState));
                    HashMap<String, Pair<String, Integer>> selfFLowVector = flowVector.get(action.dest);
                    if(selfFLowVector != null)
                        tmpFlowVector = updateFlowVector(selfFLowVector, parentFlowVector.get(flowId), flowId);
                    else
                        tmpFlowVector = parentFlowVector;
                    if(nextState != null) {
                        tmpFlowVector.put(flowId, new Pair<>(protocol.languageSet, nextState.number));
                        flowVector.put(action.dest, tmpFlowVector);
                    }
                    else {
//                        System.out.println(topState);
//                        System.out.println(action);
//                        System.out.println(flowId);
                    }
                }
            }
        }
        return flowVector;
    }

    private static HashMap<String, Pair<String, Integer>> updateFlowVector(HashMap<String, Pair<String, Integer>> selfFlowVector,
                                                                           Pair<String, Integer> parentProtocolState, String flowId){
            HashMap<String, Pair<String, Integer>> flowVector = selfFlowVector;
            if(!parentProtocolState.getRight().equals(flowVector.get(flowId).getRight()) &&
                    parentProtocolState.getRight() > 0)
                flowVector.put(flowId, parentProtocolState);

            return flowVector;
        }

    private static ArrayList<Integer> getProtocolStateCounts(HashMap<String, Pair<String, Integer>> stateFlowVector, ArrayList<Pair<String, Integer>> protocolStates){
        ArrayList<Integer> protocolStateCounts = new ArrayList<>();
        HashMap<Pair<String, Integer>, Integer> protocolStateCountsMap = new HashMap<>();
        // initial HashMap
        for(Pair<String, Integer> protocolState:protocolStates)
            protocolStateCountsMap.put(protocolState, 0);

        for(Pair<String, Integer> protocolState:stateFlowVector.values())
        {
            int count = protocolStateCountsMap.get(protocolState);
            protocolStateCountsMap.put(protocolState, count+1);
        }

        for(Pair<String, Integer> protocolState:protocolStates) {
            int protocolStateCount = protocolStateCountsMap.get(protocolState);
            protocolStateCounts.add(protocolStateCount);
        }

        return protocolStateCounts;
    }

    private static HashMap<ArrayList<Integer>, ArrayList<State>> buildCountVector(TraceAutomata traceAutomata, ArrayList<ProtocolAutomata> protocols, int config){
        HashMap<ArrayList<Integer>, ArrayList<State>> countVector = new HashMap<>();
        ArrayList<Pair<String, Integer>> protocolStates = new ArrayList<>();
        for(ProtocolAutomata protocol:protocols){
            for(State state: protocol.states) {
                Pair<String, Integer> protocolState = new Pair<>(protocol.languageSet, state.number);
                protocolStates.add(protocolState);
            }
        }

        HashMap<State, HashMap<String, Pair<String, Integer>>> flowVector = buildFlowVector(traceAutomata, protocols, config);

        for (Map.Entry<State, HashMap<String, Pair<String, Integer>>> entry : flowVector.entrySet())
        {
            State state = entry.getKey();
            HashMap<String, Pair<String, Integer>> stateFlowVector = entry.getValue();
            ArrayList<Integer> protocolStateCounts = getProtocolStateCounts(stateFlowVector, protocolStates);
            ArrayList<State> states = countVector.get(protocolStateCounts);
            if(states == null)
               states = new ArrayList<>();
            states.add(state);
            countVector.put(protocolStateCounts, states);
        }
        return countVector;
    }

    public static ArrayList<ArrayList<State>> getEqualStates(TraceAutomata traceAutomata, ArrayList<ProtocolAutomata> protocols, int config){
        ArrayList<ArrayList<State>> equalStates = new ArrayList<>();
        HashMap<ArrayList<Integer>, ArrayList<State>> countVector = buildCountVector(traceAutomata, protocols, config);

        for (ArrayList<State> countVectorStateList: countVector.values())
        {
            if(countVectorStateList.size()>1)
                equalStates.add(countVectorStateList);
        }
        return equalStates;
    }
}
