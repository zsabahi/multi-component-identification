package automataChecker;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Action {
	
	String actionName;
	String protocol;
	boolean isInitializer;
	ArrayList<String> nextStateName = new ArrayList<>();
	String flowName;
	int packetLength;
	
	public Action(String actionName, String nextStateName, String flowName, String protocol, String packetLen, boolean isInitializer) {
		this.actionName = actionName;
		this.isInitializer = isInitializer;
		this.nextStateName.add(nextStateName);
		this.protocol = protocol;
		
		if(Config.checkFlowParameters && Config.checkPacketLengths){
			this.flowName = flowName;
			this.packetLength = Integer.parseInt(packetLen);
		}
		else if(Config.checkPacketLengths){
			this.flowName = "none";
			this.packetLength = Integer.parseInt(packetLen);
		}
		else if(Config.checkFlowParameters){
			this.flowName = flowName;
			this.packetLength = 0;
		}
		else{
			this.flowName = "none";
			this.packetLength = 0;
		}
		
	}
	
	public Action(String actionName, String nextStateName) {
		this.actionName = actionName;
		this.nextStateName.add(nextStateName);
	}
	
	public void addToAction(String nextStateName) {
		if(!this.nextStateName.contains(nextStateName))
			this.nextStateName.add(nextStateName);
	}
	
	public String toString(){
		return actionName + " >> " + nextStateName + " , " + protocol + " " + flowName + " init?: " + isInitializer;
	}
	
	public static String extractActName(String actionLine){
		
		String regex = getCorrectRegex();
	    Pattern format = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		
		String action = "";
	
		Matcher m = format.matcher(actionLine);
	    if (m.find())		    	
	    	action = m.group(1);
		
	    return action;
	}
	
	public static boolean isInitializerAction(String actionLine) {
		return actionLine.contains("init") || actionLine.contains("Req");
	}

	public static String extractProtocol(String actionLine){
		
		if(actionLine.contains("TCP"))
			return "TCP";
		else if(actionLine.contains("UDP"))
			return "UDP";
		else if(actionLine.contains("HTTP"))
			return "HTTP";
		else if(actionLine.contains("SSH"))
			return "SSH";
		else if(actionLine.contains("SSLv2"))
			return "SSLv2";
		else if(actionLine.contains("TLSv1.2"))
			return "TLSv1.2";
		else if(actionLine.contains("SSL"))
			return "SSL";
		else if(actionLine.contains("TLSv1"))
			return "TLSv1";
		else
			return "OTHER";
	}
	
	public static String getCorrectRegex(){
	    String re1="(.*?)";	// Word 1
	    String re2="(\\/)";	// Any Single Character 1

	    return re1+re2;
	}
	
	public String getActionName(){
		return actionName;
	}
	
	public String getFlowName(){
		return flowName;
	}
	
	public int getPacketLength() {
		return packetLength;
	}
	
	@Override
	public boolean equals(Object obj){
		
		if(!this.getClass().isAssignableFrom(obj.getClass()))
			return false;
		
		Action igeo = (Action) obj;
		if (this.actionName.equals(igeo.actionName) && this.flowName.equals(igeo.flowName))
			return true;
			
		return false;
	}
	
}
