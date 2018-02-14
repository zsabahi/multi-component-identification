package automataChecker;

import java.io.IOException;
import java.util.ArrayList;

public class FTReader {
	
	static ArrayList<String> stemps;
	public static void buildFlowTemplates(Automata automata) throws IOException{
		
	    for (int i = 0; i < automata.patterns.size(); i++){
	    	automata.patterns.get(i).resetFlowTemplates();
	    	stemps = new ArrayList<>();
	    	ArrayList<String> visited = new ArrayList<>();
	    	buildFlowTempTrees(automata.patterns.get(i), null, automata.patterns.get(i).initState, visited);
	    }
		
	}
	
	public static void buildFlowTempTrees(Pattern pattern, FlowTemplate curFT, State curSt, ArrayList<String> visited){
		
		if(visited.contains(curSt.stateName))
			return;
		visited.add(curSt.stateName);
		
		for (int i = 0; i < curSt.possibleActions.size(); i++) {

			Action eAct = curSt.possibleActions.get(i);
			
			if(!eAct.isInitializer)
				continue;
			
			FlowTemplate eFt = new FlowTemplate(eAct.flowName, eAct.protocol);
			ArrayList<State> nextSs = pattern.findStates(eAct.nextStateName);
			
			for (int j = 0; j < nextSs.size(); j++) {
				State nextS = nextSs.get(j);
				if(visited.contains(nextS.stateName))
					continue;
				
				int isVisited = isVisitedinHierarchy(curFT, eFt);
				
				if(pattern.isFirst(curSt.stateName)){
					pattern.flowTRoots.add(eFt);
				}
				else if(isVisited == -1){
					continue;
				}
				else if(isVisited == 0){
					eFt.setParent(curFT);
					curFT.childFlows.add(eFt);
				}
				else{
					String h = eFt.flowName;
					eFt.setFlowNameExtended(h+"_"+isVisited);
					eFt.setParent(curFT);
					curFT.childFlows.add(eFt);
				}
				
				ArrayList<String> neVisited = new ArrayList<>(visited);
				buildFlowTempTrees(pattern, eFt, nextS, neVisited);
			}
		}
	}
	
	public static int isVisitedinHierarchy(FlowTemplate ft, FlowTemplate childFt){
		
		if(ft == null)
			return 0;
		
		if(ft.childFlows.contains(childFt))
			return -1;
		
		int cnt = 0;
		while(ft != null){
			if(ft.flowName.equals(childFt.flowName))
				cnt++;
			ft = ft.parent;
		}
		
		return cnt;
	}
	
}
