package automataChecker;

import java.util.ArrayList;

public class FlowTemplate {

	String protocol;
	String flowName;
	String flowNameExtended;
	boolean aFinalFlow;
	ArrayList<FlowTemplate> childFlows;
	ArrayList<FlowNode> possibleFlowNodes;
	FlowTemplate parent;
	
	public FlowTemplate(String flowName, String protocol) {
		
		this.flowName = flowName;
		this.protocol = protocol;
		this.aFinalFlow = false;
		childFlows = new ArrayList<FlowTemplate>();
		possibleFlowNodes = new ArrayList<FlowNode>();
		
	}
	
	public String toString(){
		if(parent != null)
			return "\n flowName: " + flowName + " final? " + aFinalFlow + " parent: " + parent.flowName + " children size: " + possibleFlowNodes.size() + " " + childFlows;
		else
			return "\n flowName: " + flowName + " final? " + aFinalFlow + " parent eobseo , children: " + possibleFlowNodes.size() + " " + childFlows;
	}
	
	public void setAsFinal(){
		this.aFinalFlow = true;
	}
	
	public boolean isFinal(){
		return aFinalFlow;
	}

	public void addChild(String flowName, String protocol){
		if(!isExistingFlowName(flowName))
			childFlows.add(new FlowTemplate(flowName, protocol));		
	}
	
	public boolean isExistingFlowName(String flowName){
		for (int i = 0; i < childFlows.size(); i++)
			if(childFlows.get(i).flowName.equals(flowName))
				return true;
		return false;
	}
	
	public void setParent(FlowTemplate parent) {
		this.parent = parent;
	}
	
	@Override
	public boolean equals(Object obj){
		
		if(!this.getClass().isAssignableFrom(obj.getClass()))
			return false;
		
		FlowTemplate igeo = (FlowTemplate) obj;
		if(this.protocol.equals(igeo.protocol) && this.flowName.equals(igeo.flowName))
			return true;
			
		return false;
	}
	
	public int countGrandchildren(){
		int numOfGrands = 0;
		
		for (int i = 0; i < childFlows.size(); i++){
			numOfGrands += childFlows.get(i).possibleFlowNodes.size();
		}
		
		return numOfGrands;
	}
	
	public void setFlowNameExtended(String flowNameExtended) {
		this.flowNameExtended = flowNameExtended;
	}
	
}
