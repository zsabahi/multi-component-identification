package ir.ac.ut.bsproject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DOTParser {
    public static ProtocolAutomata parseDOTFile(String filePath) {
        String nodeRegex = "node \\[shape=(?<shape>([A-Za-z]+)), color=black, style=filled, fillcolor=(?<color>([a-z]+))\\] (?<nodeList>(([0-9]*\\s*)+));";
        String actionRegex = "(?<sourceStateNumber>([0-9]+)) -> (?<destStateNumber>([0-9]+))\\[label=\"(?<actionName>([A-Za-z0-9\\.]+))\\((?<paramList>(([a-z]+=[\\w]+,+)+))\\);\"\\];";
        Pattern nodePattern = Pattern.compile(nodeRegex);
        Pattern actionPattern = Pattern.compile(actionRegex);

        ProtocolAutomata automata = new ProtocolAutomata();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = br.readLine()) != null) {
                Matcher nodeMatcher = nodePattern.matcher(line);
                if (nodeMatcher.matches()) {
                    String shape = nodeMatcher.group("shape");
                    String color = nodeMatcher.group("color");
                    String[] nodeList = nodeMatcher.group("nodeList").trim().split(" ");
                    List<State> states = getStatesByShapeAndColor(automata, shape, color, nodeList);
                    automata.addStates(states);
                }
                Matcher actionMatcher = actionPattern.matcher(line);
                if (actionMatcher.matches()) {
                    State sourceState = automata.getStateByNumber(Integer.parseInt(actionMatcher.group("sourceStateNumber")));
                    State destState = automata.getStateByNumber(Integer.parseInt(actionMatcher.group("destStateNumber")));
                    String actionName = actionMatcher.group("actionName");
                    automata.addActionToLanguageSet(actionName);
                    String paramListString = actionMatcher.group("paramList").trim();
                    List<Parameter> parameters = Utils.parseParamListString(paramListString);
                    Action action = new Action(actionName, parameters, sourceState, destState);
                    sourceState.addOutAction(action);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return automata;
    }

    public static List<State> getStatesByShapeAndColor(ProtocolAutomata automata, String shape, String color, String[] nodeList) {
        List<State> states = new ArrayList<>();
        boolean is_initial = false;
        boolean is_final = false;

        if (shape.equals("Mcircle") && color.equals("white"))
            is_initial = true;
        else if (color.equals("yellow")) {
            is_final = true;
        }

        for (String nodeNumber : nodeList) {
            if(!nodeNumber.equals("")) {
                int nodeNo = Integer.parseInt(nodeNumber);
                if(automata.getStateByNumber(nodeNo) == null) {
                    State state = new State(nodeNo, is_initial, is_final);
                    states.add(state);
                }
            }
        }
        return states;
    }
}
