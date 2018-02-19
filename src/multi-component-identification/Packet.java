package ir.ac.ut.bsproject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Packet {

	private int NO;
	private String time;
	private String srcIP;
	private int srcPort;
	private String dstIP;
	private int dstPort;
	private Integer length;
	private String protocol;
	private String info;
	private String name;
	private String Direction;
	public static int sessionId=1;

	public Packet() {}

	public Packet(int newNO, String newTime, String newSrcIP, int newSrcPort, String newDstIP, int newDstPort, int newLength, String newProtocol, String newInfo) throws UnknownHostException {
		this.NO =newNO;
		this.time = newTime; 
		this.srcIP = newSrcIP; 
		this.srcPort = newSrcPort; 
		this.dstIP = newDstIP; 
		this.dstPort = newDstPort;
		this.length = newLength;
		this.protocol = newProtocol;
		this.info = newInfo;
		this.Direction = getPacketDirection();
		this.name = mapPacketToActionName();
	}

	public String getName(){
		return this.name;
	}

	public boolean isInit(){ return this.name.contains("init");}

	public String getTime(){
		return this.time;
	}

	public PacketTransferInfo getFlowIdentifier(){
		PacketTransferInfo flowIdentifier = new PacketTransferInfo(this.srcIP, this.srcPort,
																   this.dstIP, this.dstPort, this.protocol);
		return flowIdentifier;
	}

	private String getPacketDirection() throws UnknownHostException {
		// use private ips as source
		InetAddress address = InetAddress.getByName(this.srcIP);
		if(address.isSiteLocalAddress())
			return "I";
		return "O";
	}

	public boolean isValid(){
		if ((info.contains("[TCP")) || (protocol.contains("XML")))
			return false;
		if(this.name.contains("ERRRRRR"))
			return false;
		return (! this.name.equals(this.protocol + this.Direction));
	}

	public String mapPacketToActionName() {
		if (protocol.equals("TCP"))
			return forTCP(); 
		
		else if((protocol.contains("TLS")) || (protocol.contains("SSL")))
			return forCipher();

		else if(protocol.equals("HTTP"))
			return forHTTP();

		return protocol+Direction;
	}


	private String forHTTP() {
		String str = "";
		if(info.contains("GET") || info.contains("Get") || info.contains("POST") || info.contains("Post"))
			str="init";
		else
			str = "data";
			
		return "HTTP".concat(str+Direction);
	}

	private String forCipher() {
		String str = "";

		if(info.contains("Client Hello"))				 str="init";
		if(info.contains("Server Hello"))				 str="init";
		if(info.contains("Certificate"))				 str="init";
		if(info.contains("Client Key Exchange"))		 str="init";
		if(info.contains("Server Key Exchange"))		 str="init";
		if(info.contains("New Session Ticket"))  		 str="init";
		if(info.contains("Change Cipher Spec"))  		 str="init";
		if(info.contains("Hello Request"))  			 str="init";
		if(info.contains("Unknown CA)")) 				 str="init";
		if(info.contains("Protocol Version"))   		 str="init";
		if(info.contains("Encrypted Alert"))   			 str="data";
		if(info.contains("Application Data"))  			 str="data";
		if(info.contains("Continuation Data")) 			 str="data";
		if(info.contains("Encrypted Data"))				 str="data";
		if(info.contains("Encrypted Handshake Message")) str="init";

		if(str.equals(""))
			str = "ERRRRRRRR"+info;
		if(protocol.equals("SSLv3"))
				protocol = "SSL";

		return protocol.concat(str+Direction);
	}

	private String forTCP() {
		String[] sInfo = info.split(" ");
		String str = "";

		for(Integer i=0 ; i<sInfo.length ; i++)
			if(!sInfo[i].equals(""))
				if(!sInfo[i].contains("="))
				{
					if(sInfo[i].contains("[SYN"))	str="init";
					if(sInfo[i].contains("ACK]"))	if(str.equals("")) str="ack";
					if(sInfo[i].contains("PSH"))	str="data";
					if(sInfo[i].contains("RST"))	str="fin";
					if(sInfo[i].contains("FIN"))	str="fin";
					if(sInfo[i].contains("URG"))	str="data";
				}
		return protocol.concat(str+Direction);
	}

	
	@Override
	public int hashCode(){
		//name is not a independent attribute 
		return this.NO+this.time.hashCode()+this.srcIP.hashCode()+this.srcPort+this.dstIP.hashCode()+this.dstPort+this.length+this.protocol.hashCode()+this.info.hashCode();
	}

	@Override
	public boolean equals(Object obj){
		if ( !(obj instanceof Packet) )
			return false;

		//should not compare NO and time -> they are obviously different;
		boolean res = (this.srcIP.equals(((Packet)obj).srcIP)) &&
				(this.srcPort==((Packet)obj).srcPort) && (this.dstIP.equals(((Packet)obj).dstIP)) && (this.dstPort==((Packet)obj).dstPort) &&
				(this.length==((Packet)obj).length) && (this.protocol.equals(((Packet)obj).protocol)) && (this.info.equals(((Packet)obj).info));

		return res;
	}

	@Override
	public String toString() { return this.NO + this.time + this.srcIP + this.srcPort + this.dstIP + this.dstPort
			+ this.length + this.protocol + this.info + this.Direction + this.name;
	}
}
