package automataChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AutomataFlowChecker {

	static ArrayList<Boolean> finalCheck;
	static final int patterntriggered = -2;
	static final int inputfinished = -1;
	static int idAssigner;

	public static ArrayList<Boolean> checkSuccessfulPatterns(Automata automata, ArrayList<Integer> trigdPtrnsNum, ArrayList<String> allMyInputs) {

		finalCheck = new ArrayList<Boolean>();
		for (int i = 0; i < trigdPtrnsNum.size(); i++)
			finalCheck.add(false);

		for (int i = 0; i < trigdPtrnsNum.size(); i++) {

			Pattern curPat = automata.patterns.get(trigdPtrnsNum.get(i));
			buildPossibleFlows(curPat, allMyInputs);
			
			for (int j = 0; j < curPat.flowTRoots.size(); j++) {
				
				for (int j2 = 0; j2 < curPat.flowTRoots.get(j).possibleFlowNodes
						.size(); j2++) {
					ArrayList<String> currStates2 = new ArrayList<>();
					// set first state
					Map<String, Flow> bindings = new HashMap<>();
					currStates2.add(curPat.initState.stateName);
					idAssigner = 0;

					matchesFlowPattern(automata, trigdPtrnsNum.get(i),
							curPat.flowTRoots.get(j),
							curPat.flowTRoots.get(j).possibleFlowNodes.get(j2),
							0, bindings, currStates2, finalCheck,
							trigdPtrnsNum, allMyInputs, -1);
				}
			}

		}
		return finalCheck;
	}

	public static void buildPossibleFlows(automataChecker.Pattern pattern,
			ArrayList<String> allInp) {

		String actionLine = "";

		for (int i = 0; i < allInp.size(); i++) {

			actionLine = Action.extractActName(allInp.get(i));

			if (Action.isInitializerAction(actionLine)) {

				String actProtocol = Action.extractProtocol(actionLine);

				for (int j = 0; j < pattern.flowTRoots.size(); j++)
					addToMatchingNodes(pattern.flowTRoots.get(j),
							allInp.get(i), actProtocol);

			}
		}

		setFinals(pattern);
	}

	private static void addToMatchingNodes(FlowTemplate ft, String inputLine,
			String actProtocol) {

		for (int i = 0; i < ft.childFlows.size(); i++) {
			if (ft.possibleFlowNodes.size() > 0) {
				addToMatchingNodes(ft.childFlows.get(i), inputLine, actProtocol);
			}
		}

		if (ft.protocol.equals(actProtocol)) {
			Flow f = Flow.createFlowFromAct(ft, inputLine);
			FlowNode newFl = new FlowNode(f, idAssigner);
			if (!ft.possibleFlowNodes.contains(newFl)) {
				ft.possibleFlowNodes.add(newFl);
			}
			idAssigner += 1;
		}

	}

	private static void setFinals(automataChecker.Pattern pattern) {
		for (int i = 0; i < pattern.flowTRoots.size(); i++)
			checkAndSet(pattern.flowTRoots.get(i));
	}

	private static void checkAndSet(FlowTemplate flowT) {
		for (int i = 0; i < flowT.childFlows.size(); i++)
			checkAndSet(flowT.childFlows.get(i));
		
		if (flowT.childFlows.size() == 0 || flowT.countGrandchildren() == 0) {
			flowT.setAsFinal();
		}
	}

	public static void matchesFlowPattern(Automata automata, int patternIndex, FlowTemplate curFt, FlowNode currnode, int lastmatchedindex, Map<String, Flow> bindings, ArrayList<String> currStates, ArrayList<Boolean> finalCheck, ArrayList<Integer> trigdPtrnsNum, ArrayList<String> allMyInputs, int pre_id){
		
		if(finalCheck.get(trigdPtrnsNum.indexOf(patternIndex)))
			return;
		
		if(currnode.nodeID < pre_id)
			return;
		
		bindings.put(currnode.data.getFlowName(), currnode.data);
		
		boolean isFinalFlowNode = curFt.isFinal();
		int result = findMatchingAndFinality(bindings, automata.patterns.get(patternIndex), currStates, lastmatchedindex, allMyInputs, isFinalFlowNode);

		if(result == patterntriggered){
			finalCheck.set(trigdPtrnsNum.indexOf(patternIndex), true);
//			System.out.println("The " + automata.patterns.get(patternIndex).patternName + " is reallly Triggered");
//			System.out.println(bindings);
			return;
		}else if(result == inputfinished)
			return;
		else
			lastmatchedindex = result;
		
		for (int i = 0; i < curFt.childFlows.size(); i++) {
			
			for (int j = 0; j < curFt.childFlows.get(i).possibleFlowNodes.size(); j++) {
				
				Map<String, Flow> tempBind = new HashMap<String, Flow>(bindings);
				ArrayList<String> currStemp = new ArrayList<String>(currStates);
				
				matchesFlowPattern(automata, patternIndex, curFt.childFlows.get(i), curFt.childFlows.get(i).possibleFlowNodes.get(j), lastmatchedindex, bindings, currStates, finalCheck, trigdPtrnsNum, allMyInputs,  currnode.nodeID);
				
				bindings = tempBind;
				currStates = currStemp;
			}
			
		}
	}

	public static int findMatchingAndFinality(Map<String, Flow> bindings,
			automataChecker.Pattern pattern, ArrayList<String> currStates,
			int startingIdx, ArrayList<String> allMyInputs,
			boolean isFinalFlowNode) {

		for (int i = startingIdx; i < allMyInputs.size(); i++) {
			boolean canggut = false;
			for (int j = currStates.size() - 1; j >= 0; j--) {

				automataChecker.State curSt = pattern.findState(currStates
						.get(j));

				ArrayList<String> nextSnames = pattern.nextStates(
						currStates.get(j),
						Action.extractActName(allMyInputs.get(i)));

				ArrayList<Flow> matchd = new ArrayList<>();
				if (nextSnames.size() > 0) {
					Action theAct = curSt.getMatchingAction(Action
							.extractActName(allMyInputs.get(i)));
					matchd = getTheLastBinded(bindings, theAct.flowName);
				}

				boolean matches = false;
				if (matchd.size() > 0) {
					if(!(Action.isInitializerAction(allMyInputs.get(i))))
						canggut = true;
					for (int k = 0; k < matchd.size(); k++) {
						matches = matchd.get(k).matchesAction(allMyInputs.get(i));
						if(matches)
							break;
					}
				} else if (i == startingIdx) {
					continue;
				} else {
//					here may call the new flows and set the index
					if (isFinalFlowNode)
						continue;
//					if we are at 0th input and no child is added
					if(canggut == false)
						return i;
				}

				for (int k = 0; k < nextSnames.size(); k++) {
					String nextSname = nextSnames.get(k);

					if (matches) {
						if (pattern.isFinal(nextSname))
							return patterntriggered;
						else {
							if (!currStates.contains(nextSname))
								currStates.add(nextSname);

							if (pattern.isOneWay(currStates.get(j))) {
								if (!pattern.isFirst(currStates.get(j))) {
									currStates.remove(currStates.get(j));
								}
							}
						}
					}
				}
			}
		}
		return inputfinished;
	}

	private static ArrayList<Flow> getTheLastBinded(Map<String, Flow> bindings,
			String flowName) {

		ArrayList<Flow> matched = new ArrayList<>();
		Flow newMatched = bindings.get(flowName);

		int cntr = 1;

		while (newMatched != null) {
			matched.add(newMatched);
			newMatched = bindings.get(flowName + "_" + cntr);
			cntr++;
		}
		
		return matched;
	}

}
