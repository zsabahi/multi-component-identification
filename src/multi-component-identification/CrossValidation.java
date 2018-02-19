package ir.ac.ut.bsproject;

import java.io.IOException;
import java.util.*;

import static ir.ac.ut.bsproject.Utils.getProtocols;

public class CrossValidation {

    public static Pair<String[][], String[][]> CreateKFold(int k, ArrayList<String> traces) {
        String[][] trainDataSet = new String[k][];
        String[][] testDataSet = new String[k][];
        int n = traces.size();

        int chunk = n / k;

        int[] index;
        index = new int[n];
        for (int i = 0; i < n; i++) {
            index[i] = i;
        }

        for (int i = 0; i < k; i++) {
            int start = chunk * i;
            int end = chunk * (i + 1);
            if (i == k-1) end = n;

            trainDataSet[i] = new String[n - end + start];
            testDataSet[i] = new String[end - start];
            for (int j = 0, p = 0, q = 0; j < n; j++) {
                if (j >= start && j < end) {
                    testDataSet[i][p++] = traces.get(index[j]);
                } else {
                    trainDataSet[i][q++] = traces.get(index[j]);
                }
            }
        }
        return new Pair<>(trainDataSet, testDataSet);
    }

    private static boolean testTrace(String testTrace, TraceAutomata dataTraceAutomata, int config, Integer[] newFlowIds) throws IOException, InterruptedException {
        State initialState = dataTraceAutomata.getInitialState();
        State topState = initialState;
        HashMap<String, String> traceFlowToModelFlowMap = new HashMap<>();
        List<String> actionList = Arrays.asList(testTrace.split("-"));
        for (String actionString : actionList) {
            String actionName = actionString.split("/")[0];
            String deltaTime = "[_]";
            String flowId = "";
            if (actionString.split("/").length > 1) {
                String paramListString = actionString.split("/")[1];
                List<Parameter> parameters = Utils.parseParamListString(paramListString);
                for (Parameter parameter : parameters) {
                    if (parameter.name.equals("dt"))
                        deltaTime = parameter.value;
                    else if (parameter.name.equals("fid"))
                        flowId = parameter.value;
                }
            }
            State nextState = null;
            if(config > 1) {
                String modelFlowId = traceFlowToModelFlowMap.get(flowId);
                if(modelFlowId != null)
                    nextState = dataTraceAutomata.getNextState(topState.number, actionName, deltaTime, config, modelFlowId);
                else{
                    // TODO: Track traceFlowToModelFlowMap is 1-1
                    Pair<State, String> nextStateAndFlowId= dataTraceAutomata.getNextStateAndFlowId(topState.number, actionName);
                    if(nextStateAndFlowId == null)
                        return false;
                    nextState = nextStateAndFlowId.getLeft();
                    modelFlowId = nextStateAndFlowId.getRight();
                    traceFlowToModelFlowMap.put(flowId, modelFlowId);
                }
            }
            else
                 nextState = dataTraceAutomata.getNextState(topState.number, actionName, deltaTime, config, flowId);

            if (nextState == null)
                return false;
            topState = nextState;
        }
        return (topState.is_final);
    }

    public static double getTestResult(String[] testTraces, TraceAutomata dataTraceAutomata, int config, Integer[] newFlowIds) throws IOException, InterruptedException {
        int passedTestsCount = 0;
        for (int j = 0; j< testTraces.length; j++) {
            String testTrace = testTraces[j];
            boolean testResultBoolean = testTrace(testTrace, dataTraceAutomata, config, newFlowIds);
            if(testResultBoolean)
                passedTestsCount++;
        }
        return (double)(passedTestsCount*100)/(testTraces.length);
    }

    public static void crossValidation(int k, int config, ArrayList<String> traces, String protocolsDirPath) throws IOException, InterruptedException {
        ArrayList<ProtocolAutomata> protocols = getProtocols(protocolsDirPath);

        Pair<String[][], String[][]> kFold = CreateKFold(k, traces);
        String [][] trainDataSet = kFold.getLeft(), testDataSet = kFold.getRight();

        int stepsStateSize [][] = new int [4][k], stepsActionSize [][] = new int [4][k];
        double step1TestResult = 0.0, step2TestResult = 0.0, step3TestResult = 0.0, step4TestResult = 0.0;
        long totalStep1TestTime=0, totalStep1TrainTime=0, totalStep2TestTime=0, totalStep2TrainTime=0,
             totalStep3TestTime=0, totalStep3TrainTime=0, totalStep4TestTime=0, totalStep4TrainTime=0;

        Integer[] flowIds = new Integer[0];

        for(int i = 0; i < k; i++) {
            long startTime = System.currentTimeMillis();
            // Step 1
            TraceAutomata dataTraceAutomata = TraceParser.parseTraceArray(trainDataSet[i]);
            stepsStateSize[0][i] = dataTraceAutomata.states.size();
            stepsActionSize[0][i] = dataTraceAutomata.getActions().size();
            long step1TrainTime = System.currentTimeMillis() - startTime;
            totalStep1TrainTime += step1TrainTime;
            step1TestResult += getTestResult(testDataSet[i], dataTraceAutomata, config, flowIds);
            long step1TestTime = System.currentTimeMillis() - step1TrainTime - startTime;
            totalStep1TestTime += step1TestTime;
            // Step 2
            dataTraceAutomata.generalizingByCounterAbstraction(protocols, config);
            stepsStateSize[1][i] = dataTraceAutomata.states.size();
            stepsActionSize[1][i] = dataTraceAutomata.getActions().size();
            HashMap<State, HashMap<String, Pair<String, Integer>>> flowVector = StateEqualityChecker.buildFlowVector(dataTraceAutomata, protocols, config);
            if (config > 1) // config > 1 ----> with flows - with flows and with time
                flowIds = dataTraceAutomata.mergeEqualFlows();
            dataTraceAutomata.removeRepetitiveActions();
            long step2TrainTime = System.currentTimeMillis() - step1TestTime - startTime;
            totalStep2TrainTime += step2TrainTime;
            long step2TestTime = System.currentTimeMillis() - step2TrainTime - startTime;
            totalStep2TestTime += step2TestTime;
            step2TestResult += getTestResult(testDataSet[i], dataTraceAutomata, config, flowIds);
            // Step 3
            dataTraceAutomata.generalizingByCompletingTransitions(flowVector, protocols, flowIds);
            stepsStateSize[2][i] = dataTraceAutomata.states.size();
            stepsActionSize[2][i] = dataTraceAutomata.getActions().size();
            long step3TrainTime = System.currentTimeMillis() - step2TestTime - startTime;
            totalStep3TrainTime += step3TrainTime;
            step3TestResult += getTestResult(testDataSet[i], dataTraceAutomata, config, flowIds);
            long step3TestTime = System.currentTimeMillis() - step3TrainTime - startTime;
            totalStep3TestTime += step3TestTime;
            // Step 4
            dataTraceAutomata.generalizingByRelaxingUnnecessaryOrders();
            stepsStateSize[3][i] = dataTraceAutomata.states.size();
            stepsActionSize[3][i] = dataTraceAutomata.getActions().size();
            long step4TrainTime = System.currentTimeMillis() - step3TestTime - startTime;
            totalStep4TrainTime += step4TrainTime;
            step4TestResult += getTestResult(testDataSet[i], dataTraceAutomata, config, flowIds);
            long step4TestTime = System.currentTimeMillis() - step4TrainTime - startTime;
            totalStep4TestTime += step4TestTime;
        }

        for(int i=0; i<4; i++){
            int totalStepSize = 0;
            int totalActionSize = 0;
            for(int j=0; j<k; j++){
                totalStepSize += stepsStateSize[i][j];
                totalActionSize += stepsActionSize[i][j];
            }
            System.out.println("State size of step " + (i+1) + " is: " + ((double)totalStepSize/k));
            System.out.println("State size of action " + (i+1) + " is: " + ((double)totalActionSize/k));
        }

        System.out.println("Final accuracy of step 1 is: " + (step1TestResult/k));
        System.out.println("total train time of step 1 is: " + (totalStep1TrainTime));
        System.out.println("total test time of step 1 is: " + (totalStep1TestTime));
        System.out.println("Final accuracy of step 2 is: " + (step2TestResult/k));
        System.out.println("total train time of step 2 is: " + (totalStep2TrainTime));
        System.out.println("total test time of step 2 is: " + (totalStep2TestTime));
        System.out.println("Final accuracy of step 3 is: " + (step3TestResult/k));
        System.out.println("total train time of step 3 is: " + (totalStep3TrainTime));
        System.out.println("total test time of step 3 is: " + (totalStep3TestTime));
        System.out.println("Final accuracy of step 4 is: " + (step4TestResult/k));
        System.out.println("total train time of step 4 is: " + (totalStep4TrainTime));
        System.out.println("total test time of step 4 is: " + (totalStep4TestTime));
    }
}
