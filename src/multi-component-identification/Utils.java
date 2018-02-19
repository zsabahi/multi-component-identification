package ir.ac.ut.bsproject;

import java.io.*;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.max;

public final class Utils{

    public static List<Parameter> parseParamListString(String paramListString) {
        List<String> params = Arrays.asList(paramListString.split(","));
        ArrayList<Parameter> parameters = new ArrayList<>();

        for (String param : params) {
            String paramName = param.split("=")[0];
            String paramValue = param.split("=")[1];
            Parameter parameter = new Parameter(paramName, paramValue);
            parameters.add(parameter);
        }
        return parameters;
    }

    public static LinkedList<Pair<State, State>> getShouldBeMergedPairs(ArrayList<ArrayList<State>> equalStates) {
        LinkedList<Pair<State, State>> ShouldBeMergedStates = new LinkedList<>();
        for(ArrayList<State> equalStateSet:equalStates){
            Collections.sort(equalStateSet, new StateComparator());
            for(int i=1; i<equalStateSet.size(); i++)
                ShouldBeMergedStates.add(new Pair<>(equalStateSet.get(0), equalStateSet.get(i)));
        }
        return ShouldBeMergedStates;
    }

    public static ArrayList<ArrayList<Pair<String, String>>> permute(ArrayList<Pair<String, String>> arr){
        ArrayList<ArrayList<Pair<String, String>>> permutations = new ArrayList<>();
        permute(arr, 0, permutations);
        return permutations;
    }

    private static void permute(ArrayList<Pair<String, String>> arr, int k, ArrayList<ArrayList<Pair<String, String>>> permutations){
        for(int i = k; i < arr.size(); i++){
            java.util.Collections.swap(arr, i, k);
            permute(arr, k+1, permutations);
            java.util.Collections.swap(arr, k, i);
        }
        if (k == arr.size() -1){
            ArrayList<Pair<String, String>> arrClone = (ArrayList<Pair<String, String>>) arr.clone();
            permutations.add(arrClone);
        }
    }

    public static ProtocolAutomata getProperActionProtocol(String actionName){
        String protocolsDirPath = "/home/mohammad/testcases/ProtocolsAut/";

        ArrayList<ProtocolAutomata> protocols = getProtocols(protocolsDirPath);
        for(ProtocolAutomata protocol:protocols)
            if(protocol.hasAction(actionName))
                return protocol;
        return null;
    }

    public static ArrayList<String> createTraceFileFromPSML(String baseDir) throws IOException {
        File folder = new File(baseDir);
        File[] listOfFiles = folder.listFiles();

        HashMap<PacketTransferInfo, String> flowIds = new HashMap<>();
        StringBuilder trace = new StringBuilder();
        int flowId = 0;
        Double maxDeltaTime = 0.0;
        for (int i = 0; i < listOfFiles.length; i++) {
            ArrayList<PacketTransferInfo> illegalPacketFlows = new ArrayList<>();
            String psmlFile = listOfFiles[i].getName();
            ArrayList<Packet> packetList = PSMLParser.parsePSML(baseDir + psmlFile);
            int j;
            for(j=0;j<packetList.size();j++){
                boolean shouldAddFlow = true;
                Packet packet = packetList.get(j);
                String actionName = packet.getName();
                String packetFlowId = "";
                if(flowIds.containsKey(packet.getFlowIdentifier()))
                    packetFlowId = flowIds.get(packet.getFlowIdentifier());
                else{
                    if(!packet.isInit() || illegalPacketFlows.contains(packet.getFlowIdentifier())) {
                        illegalPacketFlows.add(packet.getFlowIdentifier());
                        shouldAddFlow = false;
                    }
                    else {
                        flowId++;
                        packetFlowId = Integer.toString(flowId);
                        flowIds.put(packet.getFlowIdentifier(), packetFlowId);
                    }
                }
                if(shouldAddFlow) {
                    trace.append(actionName);
                    trace.append("/fid=");
                    trace.append(packetFlowId);
                    trace.append("/");
//                    trace.append(",dt=");
//                    Double lastTime = Double.parseDouble(packet.getTime());
//                    if (j != 0) {
//                        Packet lastPacket = packetList.get(j - 1);
//                        lastTime = Double.parseDouble(lastPacket.getTime());
//                    }
//                    Double curTime = Double.parseDouble(packet.getTime());
//
//                    Double timeDiff = curTime - lastTime;
//                    maxDeltaTime = max(timeDiff, maxDeltaTime);
//                    DecimalFormat df = new DecimalFormat("#.#####");
//                    trace.append(df.format(timeDiff));
                    trace.append("-");
                }
            }
            trace.append("\n");
        }
        String traceString = trace.toString();
//        DecimalFormat df = new DecimalFormat("#.#####");
//        traceString = traceString.replaceAll("dt=([0-9])+(\\.*([0-9])*)", "dt=[$1$2_" + df.format(maxDeltaTime)+ "]");
        List<String> traceArr = Arrays.asList(traceString.split("\n"));
        return new ArrayList<>(traceArr);
    }

    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 1;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    public static ArrayList<ProtocolAutomata> getProtocols(String dirPath){
        ArrayList<ProtocolAutomata> protocols = new ArrayList<>();

        File folder = new File(dirPath);
        File[] listOfFiles = folder.listFiles();

        for(File file: listOfFiles){
            String fileName = file.getName();
            ProtocolAutomata protocol = DOTParser.parseDOTFile(dirPath + fileName);
            protocols.add(protocol);
        }
        return protocols;
    }

    public static boolean isSubTime(String time1, String time2) {
        // time format: [lower-bound_upper-bound]

        Double time1LowerBound = 0.0, time1UpperBound = 0.0, time2LowerBound = 0.0, time2UpperBound = 0.0;
        String timeRegex = "\\[(?<lower_bound>([0-9]+(\\.[0-9])*))_(?<upper_bound>([0-9]+(\\.[0-9])*))\\]";
        Pattern timePattern = Pattern.compile(timeRegex);
        Matcher timeMatcher = timePattern.matcher(time1);
        if (timeMatcher.matches()) {
            time1LowerBound = Double.parseDouble(timeMatcher.group("lower_bound"));
            time1UpperBound = Double.parseDouble(timeMatcher.group("upper_bound"));
        }
        timeMatcher = timePattern.matcher(time2);
        if (timeMatcher.matches()) {
            time2LowerBound = Double.parseDouble(timeMatcher.group("lower_bound"));
            time2UpperBound = Double.parseDouble(timeMatcher.group("upper_bound"));
        }

        return ((time1LowerBound <= time2LowerBound) && (time1UpperBound >= time2UpperBound));
    }

    public static ArrayList<String> readFileLines(String filePath){
        ArrayList<String> lines = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }
}