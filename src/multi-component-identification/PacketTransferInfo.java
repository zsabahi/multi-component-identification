package ir.ac.ut.bsproject;

public class PacketTransferInfo {
    private String srcIP;
    private int srcPort;
    private String dstIP;
    private int dstPort;
    private String protocol;

    public PacketTransferInfo(String newSrcIP, int newSrcPort, String newDstIP, int newDstPort, String newProtocol) {
        this.srcIP = newSrcIP;
        this.srcPort = newSrcPort;
        this.dstIP = newDstIP;
        this.dstPort = newDstPort;
        this.protocol = newProtocol;
    }

    @Override
    public int hashCode(){
        return this.srcIP.hashCode()+this.srcPort+this.dstIP.hashCode()+this.dstPort+this.protocol.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if ( !(obj instanceof PacketTransferInfo) )
            return false;

        return ((this.srcIP.equals(((PacketTransferInfo)obj).srcIP)) && (this.srcPort==((PacketTransferInfo)obj).srcPort)
                && (this.dstIP.equals(((PacketTransferInfo)obj).dstIP)) && (this.dstPort==((PacketTransferInfo)obj).dstPort)
                ||(this.srcIP.equals(((PacketTransferInfo)obj).dstIP)) && (this.srcPort==((PacketTransferInfo)obj).dstPort)
                && (this.dstIP.equals(((PacketTransferInfo)obj).srcIP)) && (this.dstPort==((PacketTransferInfo)obj).srcPort))
                && (this.protocol.equals(((PacketTransferInfo)obj).protocol));
    }
}
