package automataChecker;
import java.util.ArrayList;

public class Automata {
	
	ArrayList<Pattern> patterns;
	
	public Automata(){
		patterns = new ArrayList<>();
	}
	
	public String toString(){
		return patterns.toString();
	}
	
	public Pattern getPattern(int index){
		if(patterns.size() >= index)
			return patterns.get(index);
		return null;
	}
	
	public int countPatterns(){
		return patterns.size();
	}
	
}
