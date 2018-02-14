package automataChecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

public class AutomataReader {

	public static Automata readAllPatterns(Automata automata) throws IOException{
		
		File folder = new File("patterns");
		File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile())
	    	  buildPatternFromFile(automata, "patterns/" + listOfFiles[i].getName());
	    }
	    return automata;
	}
	
	public static void buildPatternFromFile(Automata automata, String fileName) throws IOException{
		
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		
		String patNameRegex = ".*?(?:[a-z][a-z]+).*?((?:[a-z][a-z]+))";
		
		java.util.regex.Pattern regPattern = java.util.regex.Pattern.compile(patNameRegex,java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
	    Matcher matcher = regPattern.matcher(fileName);
	    String patternName = fileName;
	    if (matcher.find())
	    	patternName = matcher.group(1);

		Pattern curPat = new Pattern(patternName);
		
		String curLine = "";
		
		while(true){
			
			curLine = reader.readLine();
			if(curLine.contains("}") || curLine == null)
				break;
			
			if(curLine.contains("digraph"))
				continue;
			
			if(curLine.contains("node")){
				
				StringTokenizer tknizer = new StringTokenizer(curLine, "[,];");
				String curToken = "";
				String typeIndic = "";
				String nodeType = "";
				String nodeNums = "";
				
				while (tknizer.hasMoreTokens()) {
					
					curToken = tknizer.nextToken();

					if(curToken.contains("shape")){
						typeIndic = curToken.substring(curToken.indexOf("=")+1 , curToken.length()).trim();
						if(typeIndic.charAt(0) == 'M'){
							nodeType = "initial";
						}
					}
					else if(curToken.contains("fillcolor")){
						
						typeIndic = curToken.substring(curToken.indexOf("=")+1 , curToken.length()).trim();
						if("yellow".equals(typeIndic.toLowerCase()))
							nodeType = "final";
						else if("white".equals(typeIndic.toLowerCase())){
							if("initial".equals(nodeType))
								continue;
							nodeType = "middle";
						}
					}
				}
				
				nodeNums = curToken.trim();
				if(nodeType != ""){
					createNodes(curPat, nodeType, nodeNums);
				}
			}else{ //actions and states
				
				String curS = null;
		        String nextS = null;
		        String action = null;
		        String flowNum = null;
		        String protocol = null;
		        String flowName = null;
		        boolean isInit = false;
				
				String actionPattern = "(\\d+).*?(\\d+).*?(?:[a-z][a-z]+)(=\")(.*?)(;)(\\d+)";

			    regPattern = java.util.regex.Pattern.compile(actionPattern,java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
			    matcher = regPattern.matcher(curLine);
			    if (matcher.find())
			    {
			        curS = matcher.group(1);
			        nextS = matcher.group(2);
			        action = matcher.group(4);
			        
			        flowNum = matcher.group(6);
			        
			        protocol = Action.extractProtocol(action);
			        
			        if(protocol.isEmpty())
			        	System.out.println("Unpredicted Protocol, Null so...");
			        
			        flowName = protocol + "-" + flowNum;
			        
			        isInit = Action.isInitializerAction(action);
			    }
			    
			    curPat.addAction(curS, action, nextS, flowName, protocol, "0", isInit);
			} 
		}
		
		automata.patterns.add(curPat);
		
		reader.close();
	}
	
	private static void createNodes(Pattern pt, String nodeType, String nodeNums) {
		
		StringTokenizer tknizer = new StringTokenizer(nodeNums);
		State st;
		while(tknizer.hasMoreTokens()){
			
			st = new State(tknizer.nextToken());
			
			if("final".equals(nodeType))
				st.isFinal = true;
			if("initial".equals(nodeType))
				pt.setInitialState(st);
			
			pt.addState(st);
		}
	}

}
