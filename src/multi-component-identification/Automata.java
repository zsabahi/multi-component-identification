package ir.ac.ut.bsproject;

import java.util.*;

import static ir.ac.ut.bsproject.Utils.*;
import static java.lang.Math.max;

public class Automata {
    public List<State> states;

    public Automata() {
        this.states = new ArrayList<>();
    }

    public void addState(State state){
        this.states.add(state);
    }

    public void addStates(List<State> states){
        this.states.addAll(states);
    }

    public State getStateByNumber(int number){
        for(State state:this.states){
            if(state.number == number)
                return state;
        }
        return null;
    }

    public ArrayList<Action> getActions(){
        ArrayList<Action> actions = new ArrayList<>();
        for(State state:this.states)
            actions.addAll(state.outActions);
        return actions;
    }

    public State getInitialState(){
        return this.states.get(0);
    }

    public State getNextState(int stateNumber, String actionName, String deltaTime, int config, String flowId){
        for(Action outAction:getStateByNumber(stateNumber).outActions){
            String outActionDeltaTime = outAction.getDeltaTime();
            if(outAction.name.equals(actionName))
                if((config == 1) ||
                        ((config == 2) && outAction.getFlowId().equals(flowId)) ||
                        ((config == 3) && isSubTime(outActionDeltaTime, deltaTime) && outAction.getFlowId().equals(flowId)))
                return outAction.dest;
        }
        return null;
    }

    public Pair<State, String> getNextStateAndFlowId(int stateNumber, String actionName){
        for(Action outAction:getStateByNumber(stateNumber).outActions){
            if(outAction.name.equals(actionName))
                return new Pair<>(outAction.dest, outAction.getFlowId());
        }
        return null;
    }

    public int getMaxFlowId(){
        int maxFlowId = 0;
        for(Action action: this.getActions()){
            String flowId = action.getFlowId();
            maxFlowId = max(maxFlowId, Integer.parseInt(flowId));
        }
        return maxFlowId;
    }

    public int getMaxStateNumber(){
        int maxStateNumber = 0;
        for(State state: this.states){
            int stateNo = state.number;
            maxStateNumber = max(maxStateNumber, stateNo);
        }
        return maxStateNumber;
    }

    public void mergeEqualStates(ArrayList<ArrayList<State>> equalStates) {
        if(equalStates.size()==0)
            return;

        LinkedList<Pair<State, State>> shouldBeMergedStates = getShouldBeMergedPairs(equalStates);

        HashMap<String, Pair<State, State>> mergedStates = new HashMap<>();
        while(!shouldBeMergedStates.isEmpty()){
            Pair<State, State> mergeStatePair = shouldBeMergedStates.remove();
            if(mergedStates.containsKey(mergeStatePair.toString()))
                continue;

            mergedStates.put(mergeStatePair.toString(), mergeStatePair);
            State mustMergeState = mergeStatePair.getRight();
            State mergedState = mergeStatePair.getLeft();
            mergeTwoState(mustMergeState, mergedState);
        }
        stateRenaming();
        determinization();
    }

    private void mergeTwoState(State mustMergeState, State mergedState) {
//        for(Action action: this.getActions()){
//            if(action.dest.number == mustMergeState.number)
//                action.dest = mergedState;
//            else if(action.source.number == mustMergeState.number)
//                action.source = mergedState;
//        }
        for(Action action: mustMergeState.inActions)
            action.dest = mergedState;
        for(Action action: mustMergeState.outActions)
            action.source = mergedState;

        mergedState.outActions.addAll(mustMergeState.outActions);
        mergedState.inActions.addAll(mustMergeState.inActions);
        mergedState.is_final = mergedState.is_final || mustMergeState.is_final;
        this.removeState(mustMergeState);
    }

    private void determinization() {
        ArrayList<ArrayList<State>> mustMergeStates = new ArrayList<>();
        ArrayList<ArrayList<State>> finalEqualStates = new ArrayList<>();

        for(State state: this.states) {
            HashMap<String, HashSet<State>> categorizedActions = new HashMap<>();
            for (Action action : state.outActions) {
                if (categorizedActions.containsKey(action.name)) {
                    HashSet<State> equalStates = categorizedActions.get(action.name);
                    equalStates.add(action.dest);
                    categorizedActions.put(action.name, equalStates);
                } else {
                    HashSet<State> tempSet = new HashSet<>();
                    tempSet.add(action.dest);
                    categorizedActions.put(action.name, tempSet);
                }
            }
            for (Map.Entry<String, HashSet<State>> entry : categorizedActions.entrySet())
            {
                HashSet<State> equalStatesSet = entry.getValue();
                if(equalStatesSet.size() > 1) {
                    ArrayList<State> equalStatesArr = new ArrayList<>(equalStatesSet);
                    mustMergeStates.add(equalStatesArr);
                }
            }
        }
        Collections.sort(mustMergeStates, (a, b) -> {
            Collections.sort(a, new StateComparator());
            Collections.sort(b, new StateComparator());
            return (a.get(0).number) - (b.get(0).number);
        });


        int[] indices = new int[states.size()];
        int index = 0;
        for(int i=0; i<states.size(); i++)
            indices[i] = -1;
        for(ArrayList<State> mustMergeStatePair: mustMergeStates){
            State leftState = mustMergeStatePair.get(0);
            State rightState = mustMergeStatePair.get(1);
            if(indices[leftState.number] != -1){
                finalEqualStates.get(indices[leftState.number]).add(rightState);
                indices[rightState.number] = indices[leftState.number];
            }
            else{
                ArrayList<State> temp = new ArrayList<>();
                temp.add(leftState);
                temp.add(rightState);
                finalEqualStates.add(temp);
                indices[leftState.number] = index;
                indices[rightState.number] = index;
                index++;
            }
        }
        mergeEqualStates(finalEqualStates);
    }

    public Integer[] mergeEqualFlows(){
        ArrayList<ArrayList<String>> equalFlows = getEqualFlows();
        Collections.sort(equalFlows, (a, b) -> {
            Collections.sort(a);
            Collections.sort(b);
            return a.get(0).compareTo(b.get(0));
        });
        Integer[] newFlowIds = this.getNewFlowIdMap(equalFlows);
        this.updateFlowIds(newFlowIds);
        return newFlowIds;
    }

    public void removeRepetitiveActions(){
        for(State state: this.states) {
            HashMap<Pair<String, String>, ArrayList<Action>> categorizedActions = new HashMap<>();
            for (Action action : state.outActions) {
                String flowId = action.getFlowId();
                if (categorizedActions.containsKey(new Pair<>(action.name, flowId))) {
                    ArrayList<Action> repetitiveAction = categorizedActions.get(new Pair<>(action.name, flowId));
                    repetitiveAction.add(action);
                    categorizedActions.put(new Pair<>(action.name, flowId), repetitiveAction);
                } else {
                    ArrayList<Action> repetitiveAction = new ArrayList<>();
                    repetitiveAction.add(action);
                    categorizedActions.put(new Pair<>(action.name, flowId), repetitiveAction);
                }
            }
            for (Map.Entry<Pair<String, String>, ArrayList<Action>> entry : categorizedActions.entrySet()) {
                ArrayList<Action> equalActionSet = entry.getValue();
                if (equalActionSet.size() > 1) {
                    equalActionSet.remove(0);
                    state.outActions.removeAll(equalActionSet);
                }
            }
        }
    }

    private void updateFlowIds(Integer[] newFlowIds){
        for(Action action:this.getActions()){
            int curFlowId = Integer.parseInt(action.getFlowId());
            int newFlowId = newFlowIds[curFlowId-1];
            action.setFlowId(newFlowId);
        }
    }

    private Integer[] getNewFlowIdMap(ArrayList<ArrayList<String>> equalFlows){
        int maxFlowId = this.getMaxFlowId();
        Integer[] newFlowIds = new Integer[maxFlowId];
        for(int i=0;i<maxFlowId;i++)
            newFlowIds[i] = 0;
        int newFlowId = 1;
        for(ArrayList<String> equalFlowSet: equalFlows){
            int curFlowId = -1;
            for(String flowId: equalFlowSet){
                if(newFlowIds[Integer.parseInt(flowId)-1] != 0)
                    curFlowId = newFlowIds[Integer.parseInt(flowId)-1];
            }
            if(curFlowId == -1){
                curFlowId = newFlowId;
                newFlowId++;
            }
            for(String flowId: equalFlowSet)
                newFlowIds[Integer.parseInt(flowId)-1] = curFlowId;
        }
        for(int i=0;i<maxFlowId;i++) {
            if (newFlowIds[i] == 0) {
                newFlowIds[i] = newFlowId;
                newFlowId++;
            }
        }
        return newFlowIds;
    }

    private ArrayList<ArrayList<String>> getEqualFlows() {
        ArrayList<ArrayList<String>> equalFlows = new ArrayList<>();
        for(State state: this.states) {
            HashMap<String, HashSet<String>> categorizedActions = new HashMap<>();
            for (Action action : state.outActions) {
                if (categorizedActions.containsKey(action.name)) {
                    HashSet<String> equalFlowsSet = categorizedActions.get(action.name);
                    equalFlowsSet.add(action.getFlowId());
                    categorizedActions.put(action.name, equalFlowsSet);
                } else {
                    HashSet<String> equalFlowsSet = new HashSet<>();
                    equalFlowsSet.add(action.getFlowId());
                    categorizedActions.put(action.name, equalFlowsSet);
                }
            }
            for (Map.Entry<String, HashSet<String>> entry : categorizedActions.entrySet()) {
                HashSet<String> equalFlowsSet = entry.getValue();
                if (equalFlowsSet.size() > 1) {
                    ArrayList<String> equalFlowsArr = new ArrayList<>(equalFlowsSet);
                    equalFlows.add(equalFlowsArr);
                }
            }
        }
        return equalFlows;
    }

    public void generalizingByCounterAbstraction(ArrayList<ProtocolAutomata> protocols, int config) {
        ArrayList<ArrayList<State>> equalStates = StateEqualityChecker.getEqualStates((TraceAutomata) this, protocols, config);
        mergeEqualStates(equalStates);
    }


    public void generalizingByCompletingTransitions(HashMap<State, HashMap<String, Pair<String, Integer>>> stateFlowVector,
                                                    ArrayList<ProtocolAutomata> protocols, Integer[] newFlowIds) {
        HashMap<String, HashMap<Integer, ArrayList<String>>> protocolSelfLoops = new HashMap<>();
        for (ProtocolAutomata protocol : protocols)
            protocolSelfLoops.put(protocol.languageSet, protocol.getSelfloops());

        for (Map.Entry<State, HashMap<String, Pair<String, Integer>>> entry : stateFlowVector.entrySet()) {
            State state = entry.getKey();
            HashMap<String, Pair<String, Integer>> flowVector = entry.getValue();
            for(Map.Entry<String, Pair<String, Integer>> flowVectorEntry : flowVector.entrySet()){
                int flowId = Integer.parseInt(flowVectorEntry.getKey());
                Pair<String, Integer> protocolState = flowVectorEntry.getValue();
                HashMap<Integer, ArrayList<String>> selfLoops = protocolSelfLoops.get(protocolState.getLeft());
                if(selfLoops.containsKey(protocolState.getRight())){
                    for(String actionName: selfLoops.get(protocolState.getRight())){
                        if(!state.hasAction(actionName)) {
                            if(newFlowIds.length > 0)
                                flowId = newFlowIds[flowId-1];
                            Parameter flowParameter = new Parameter("fid", Integer.toString(flowId));
                            ArrayList<Parameter> parameters = new ArrayList<>();
                            parameters.add(flowParameter);
                            Action action = new Action(actionName, parameters, state, state);
                            state.addOutAction(action);
                        }
                    }
                }
            }
        }
    }

    private HashMap<State, HashSet<State>> getStateParents(){
        HashMap<State, HashSet<State>> stateParents = new HashMap<>();

        for(State state: this.states){
            for(Action action: state.outActions){
                if(action.source != action.dest){
                    HashSet<State> parents;
                    if(stateParents.containsKey(action.dest))
                        parents = stateParents.get(action.dest);
                    else
                        parents = new HashSet<>();
                    parents.add(action.source);
                    stateParents.put(action.dest, parents);
                }
            }
        }
        return stateParents;
    }

    private void stateRenaming(){
        int stateNumber = 1;
        for(State state: this.states){
            if(state.is_initial)
                state.number = 0;
            else{
                state.number = stateNumber;
                stateNumber++;
            }
        }
    }


    public State getLowerCommonAncestor(State s1, State s2, HashMap<State, HashSet<State>> stateParents){
        Boolean[] isVisited = new Boolean[2*stateParents.size()];
        for(int i=0;i<2*stateParents.size();i++)
            isVisited[i] = false;
        Boolean[] isVisited2 = new Boolean[2*stateParents.size()];
        for(int i=0;i<2*stateParents.size();i++)
            isVisited2[i] = false;


        State topState;
        LinkedList<State> queue = new LinkedList<>();
        queue.add(s1);
        while(!queue.isEmpty()){
            topState = queue.remove();
            HashSet<State> parents = stateParents.get(topState);
            if(parents != null) {
                for (State parent : parents) {
                    if (!isVisited[parent.number]) {
                        isVisited[parent.number] = true;
                        queue.add(parent);
                    }
                }
            }
        }
        queue.add(s2);
        while(!queue.isEmpty()){
            topState = queue.remove();
            HashSet<State> parents = stateParents.get(topState);
            if(parents != null) {
                for(State parent: parents) {
                    if (isVisited[parent.number])
                        return parent;
                    else if(!isVisited2[parent.number]) {
                        queue.add(parent);
                        isVisited2[parent.number] = true;
                    }
                }
            }
        }
        return this.getInitialState();
    }

    public ArrayList<Pair<State, State>> getCycles(HashMap<State, HashSet<State>> stateParents){
        ArrayList<Pair<State, State>> cyclesSourceAndDest = new ArrayList<>();
        for (Map.Entry<State, HashSet<State>> entry : stateParents.entrySet()) {
            HashSet<State> parents = entry.getValue();
            State cycleEndState = entry.getKey();
            if(parents.size()>1){
                LinkedList<State> queue = new LinkedList<>(parents);
                while(queue.size()>1) {
                    State s1 = queue.remove();
                    State s2 = queue.remove();
                    State lowerCommonAncestor = getLowerCommonAncestor(s1, s2, stateParents);
//                    System.out.println("Parents of " + s1 + " and " + s2 + " is " + lowerCommonAncestor);
                    queue.add(lowerCommonAncestor);
                }
                State cycleStartState = queue.remove();
                if(!stateParents.get(cycleEndState).contains(cycleStartState) && !cycleEndState.equals(cycleStartState)) {
//                    System.out.println("We should analyse path from " + cycleStartState + " to " + cycleEndState);
                    cyclesSourceAndDest.add(new Pair<>(cycleStartState, cycleEndState));
                }
            }
        }
        return cyclesSourceAndDest;
    }

    public HashMap<String, HashSet<Pair<Pair<String, String>, Pair<String, String>>>> getPairActionsSeqMap(HashSet<ArrayList<State>> paths,
                                                                               ArrayList<ArrayList<Pair<String, String>>> curPaths){
        HashMap<String, HashSet<Pair<Pair<String, String>, Pair<String, String>>>> pairActionsSeqMap = new HashMap<>();
        for(ArrayList<State> foundedPath: paths){
            ArrayList<Action> actionList = new ArrayList();
            ArrayList<Pair<String, String>> curPath = new ArrayList<>();
            for(int i=foundedPath.size()-1; i>0; i--){
                State actionSource = foundedPath.get(i);
                State actionDest = foundedPath.get(i-1);
                //TODO: we should add all of the outActions from source state to dest
                Action action = actionSource.getOutAction(actionDest);
                actionList.add(action);
                curPath.add(new Pair<>(action.name, action.getFlowId()));
            }
            curPaths.add(curPath);

            for(int i=0; i<actionList.size()-1; i++){
                for(int j=i+1; j<actionList.size(); j++){
                    Action firstAction = actionList.get(i);
                    Action secondAction = actionList.get(j);
                    if(firstAction.name.equals(secondAction.name) && firstAction.getFlowId().equals(secondAction.getFlowId()))
                        continue;
                    String hashKey = firstAction.name + firstAction.getFlowId() + secondAction.name + secondAction.getFlowId();
                    if((firstAction.name + firstAction.getFlowId()).hashCode() < (secondAction.name + secondAction.getFlowId()).hashCode())
                        hashKey = secondAction.name + secondAction.getFlowId() + firstAction.name + firstAction.getFlowId();

                    HashSet<Pair<Pair<String, String>, Pair<String, String>>> actionPairsSet = new HashSet<>();
                    if(pairActionsSeqMap.containsKey(hashKey))
                        actionPairsSet = pairActionsSeqMap.get(hashKey);

                    actionPairsSet.add(new Pair<>(new Pair<>(firstAction.name, firstAction.getFlowId()),
                            new Pair<>(secondAction.name, secondAction.getFlowId())));
                    pairActionsSeqMap.put(hashKey, actionPairsSet);
                }
            }
        }
        return pairActionsSeqMap;
    }

    public void addValidPath(State cycleStartState, State cycleEndState, ArrayList<ArrayList<Pair<String, String>>> validPaths){
        int maxStateNumber = getMaxStateNumber();
        int stateNo = maxStateNumber +1;

        for(ArrayList<Pair<String, String>> validPath: validPaths){
            State curStartState = cycleStartState;
            for(int i=0; i<validPath.size(); i++) {
                Pair<String, String> actionNameFlowId = validPath.get(i);
                String actionName = actionNameFlowId.getLeft();
                String flowId = actionNameFlowId.getRight();
                Parameter flowParameter = new Parameter("fid", flowId);
                ArrayList<Parameter> parameters = new ArrayList<>();
                parameters.add(flowParameter);
                State curDestState = cycleEndState;
                if (i<validPath.size()-1) {
                    curDestState = new State(stateNo, false, false);
                    this.addState(curDestState);
                    stateNo++;
                }
                Action action = new Action(actionName, parameters, curStartState, curDestState);
                curStartState.addOutAction(action);
                curStartState = curDestState;
            }
        }
    }

    public void generalizingByRelaxingUnnecessaryOrders() {
        HashMap<State, HashSet<State>> stateParents = getStateParents();
        ArrayList<Pair<State, State>> cyclesSourceAndDest = getCycles(stateParents);
        for (Pair<State, State> cycleSourceAndDest : cyclesSourceAndDest) {
            State cycleStartState = cycleSourceAndDest.getLeft();
            State cycleEndState = cycleSourceAndDest.getRight();
            Boolean[] isVisited = new Boolean[2*stateParents.size()];
            for (int i = 0; i < 2*stateParents.size(); i++)
                isVisited[i] = false;
            ArrayList<State> path = new ArrayList<>();
            isVisited[cycleEndState.number] = true;
            path.add(cycleEndState);
            HashSet<ArrayList<State>> paths = new HashSet<>();
            findPaths(cycleStartState, cycleEndState, stateParents, isVisited, path, paths);
//            System.out.println("Paths: " + paths);

            // TODO: Convert States list to actions list is not the best way
            ArrayList<ArrayList<Pair<String, String>>> curPaths = new ArrayList<>();
            HashMap<String, HashSet<Pair<Pair<String, String>, Pair<String, String>>>> pairActionsSeqMap = getPairActionsSeqMap(paths, curPaths);
            HashSet<Pair<String, String>> cycleActionsLangSet = new HashSet<>();
            HashSet<Pair<Pair<String, String>, Pair<String, String>>> rules = new HashSet<>();
            for (Map.Entry<String, HashSet<Pair<Pair<String, String>, Pair<String, String>>>> entry : pairActionsSeqMap.entrySet()){
                HashSet<Pair<Pair<String, String>, Pair<String, String>>> actionPairsSet = entry.getValue();
                ArrayList<Pair<Pair<String, String>, Pair<String, String>>> actionPairsArr = new ArrayList<>(actionPairsSet);
                if(actionPairsArr.size() == 1)
                    rules.add(actionPairsArr.get(0));
                for(Pair<Pair<String, String>, Pair<String, String>> actionPair: actionPairsArr){
                    cycleActionsLangSet.add(new Pair<>(actionPair.getLeft().getLeft(), actionPair.getLeft().getRight()));
                    cycleActionsLangSet.add(new Pair<>(actionPair.getRight().getLeft(), actionPair.getRight().getRight()));
                }
            }
//            System.out.println("Rules: " + rules);
            ArrayList<Pair<String, String>> cycleActionsLang = new ArrayList<>(cycleActionsLangSet);
            ArrayList<ArrayList<Pair<String, String>>> validPaths = getValidPaths(rules, cycleActionsLang);
            validPaths.removeAll(curPaths);
//            System.out.println("Paths should be add to Automata: " + validPaths);
            addValidPath(cycleStartState, cycleEndState, validPaths);
        }
    }

    public ArrayList<ArrayList<Pair<String, String>>> getValidPaths(HashSet<Pair<Pair<String, String>, Pair<String, String>>> rules,
                                                      ArrayList<Pair<String, String>> cycleActionsLang) {
        ArrayList<ArrayList<Pair<String, String>>> permutations = permute(cycleActionsLang);
        ArrayList<ArrayList<Pair<String, String>>> validPaths = new ArrayList<>();
        for(ArrayList<Pair<String, String>> permutation: permutations){
            boolean isValid = true;
            for(Pair<Pair<String, String>, Pair<String, String>> rule: rules){
                if(permutation.indexOf(rule.getLeft()) > permutation.indexOf(rule.getRight()) || !isValid)
                    isValid = false;
            }
            if(isValid)
                validPaths.add(permutation);
        }
        return validPaths;
    }

    public ArrayList<State> findPaths(State startNode, State endNode, HashMap<State, HashSet<State>> stateParents,
                          Boolean[] isVisited, ArrayList<State> path, HashSet<ArrayList<State>> paths) {

        if (startNode.number == endNode.number)
            return path;

        HashSet<State> parents = stateParents.get(endNode);
        if(parents == null)
            return null;
        for (State parent : parents) {
            if (!isVisited[parent.number]) {
                Boolean[] isVisitedClone = isVisited.clone();
                isVisitedClone[parent.number] = true;
                ArrayList<State> pathClone = new ArrayList<>(path);
                pathClone.add(parent);
                ArrayList<State> newPath = findPaths(startNode, parent, stateParents, isVisitedClone, pathClone, paths);
                if(newPath != null)
                    paths.add(newPath);
            }
        }
        return null;
    }

    private void removeState(State state){
        this.states.remove(state);
    }

    public String generateDOTFile() {
        StringBuilder DOTOutput = new StringBuilder("digraph G {\n");
//        String initialNode = "node [shape=Mcircle, color=black, style=filled, fillcolor=white] 0;\n";
        String initialNode = "node [shape = Mcircle , color=black, style=filled, fillcolor=white] 0;\n";
//        StringBuilder midNodes = new StringBuilder("node [shape=circle, color=black, style=filled, fillcolor=white] ");
        StringBuilder midNodes = new StringBuilder("node [shape = circle , color=black , style=filled , fillcolor=white] ");
//        StringBuilder finalNodes = new StringBuilder("node [shape=circle, color=black, style=filled, fillcolor=yellow] ");
        StringBuilder finalNodes = new StringBuilder("node [shape = circle , color=black , style=filled , fillcolor=yellow] ");
        StringBuilder transitions = new StringBuilder("");

        for(State state:this.states) {
            if(state.is_final)
                finalNodes.append(state.number + " ");
            else
                midNodes.append(state.number + " ");

            for(Action action:state.outActions){
                transitions.append(state.number + " -> " + action.dest.number);
//                transitions.append("[label=\"" + action.name + "(");
                transitions.append(" [label=\"" + action.name + ";");

                for(Parameter param:action.parameters)
//                    transitions.append(param.name + "=" + param.value + ",");
                    transitions.append(param.value);
                transitions.append("\"];\n");
//                transitions.append(");\"];\n");
            }
        }
        midNodes.append(";\n");
        finalNodes.append(";\n");
        DOTOutput.append(initialNode);
        DOTOutput.append(midNodes.toString());
        DOTOutput.append(finalNodes.toString());
        DOTOutput.append(transitions.toString());
        DOTOutput.append("}");
        return DOTOutput.toString();
    }
}