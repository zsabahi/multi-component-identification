package ir.ac.ut.bsproject;

import java.util.List;

public class Action {
    public String name;
    List<Parameter> parameters;
    public State source;
    public State dest;

    public Action(String name, List<Parameter> parameters, State source, State dest) {
        this.name = name;
        this.source = source;
        this.dest = dest;
        this.parameters = parameters;
    }

    @Override
    public String toString(){ return  source + "->" + dest + "(" + name + ")";}

    public String getFlowId(){
        for(Parameter parameter:this.parameters){
            if(parameter.name.equals("fid"))
                return parameter.value;
        }
        return null;
    }
    public void setFlowId(int flowId){
        for(Parameter parameter:this.parameters){
            if(parameter.name.equals("fid"))
                parameter.value = Integer.toString(flowId);
        }
    }

    public String getDeltaTime(){
        for(Parameter parameter:this.parameters){
            if(parameter.name.equals("dt"))
                return parameter.value;
        }
        return null;
    }
}
