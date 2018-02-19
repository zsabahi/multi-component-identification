package ir.ac.ut.bsproject;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;

public class PSMLParser {
    public static ArrayList<Packet> parsePSML(String filePath) {
        ArrayList<Packet> packetsList = new ArrayList<>();
        try {
            File PSMLFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(PSMLFile);

            doc.getDocumentElement().normalize();

            NodeList packetNodes = doc.getElementsByTagName("packet");
            for (int i=0;i<packetNodes.getLength();i++) {
                Node packetNode = packetNodes.item(i);
                if (packetNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) packetNode;

                    NodeList sectionNodes = eElement.getElementsByTagName("section");
                    int NO = Integer.parseInt(sectionNodes.item(0).getTextContent());
                    String time = sectionNodes.item(1).getTextContent();
                    String srcIP = sectionNodes.item(2).getTextContent();
                    int srcPort = Integer.parseInt(sectionNodes.item(3).getTextContent());
                    String dstIP = sectionNodes.item(4).getTextContent();
                    int dstPort = Integer.parseInt(sectionNodes.item(5).getTextContent());
                    int length = Integer.parseInt(sectionNodes.item(6).getTextContent());
                    String protocol = sectionNodes.item(7).getTextContent();
                    String info = sectionNodes.item(8).getTextContent();

                    if (!(info.contains("[TCP")) && (!protocol.contains("XML"))){
                        if(protocol.equals("TCP") && info.contains(" > ")){
                            String[] sInfo = info.split(" ");

                            String ret = "";
                            for(int j=3 ; j<sInfo.length ; j++)
                            {
                                ret = ret.concat(" "+sInfo[j]);
                            }
                            info = ret;
                        }
                        Packet packet = new Packet(NO, time, srcIP, srcPort, dstIP, dstPort, length, protocol, info);
                        if(packetsList.size()>0 && packet.isValid()){

                            if (!packetsList.get(packetsList.size() - 1).equals(packet))
                                packetsList.add(packet);
                        }
                        else if(packet.isValid())
                            packetsList.add(packet);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packetsList;
    }
}