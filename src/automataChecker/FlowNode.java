package automataChecker;

public class FlowNode{

	Flow data;
	int nodeID;
	
	public FlowNode(Flow f, int t){
		data = f;
		nodeID = t;
	}
	
	public String toString(){
		return data.toString() + "\n";
	}
	
	@Override
	public boolean equals(Object obj){
		
		if(!this.getClass().isAssignableFrom(obj.getClass()))
			return false;
		
		FlowNode igeo = (FlowNode) obj;
		if (this.data.flowNameEqui.equals(igeo.data.flowNameEqui) && this.data.fid.equals(igeo.data.fid))
			return true;
			
		return false;
	}
	
}
