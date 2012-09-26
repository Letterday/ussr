//package ussr.samples.atron.simulations.metaforma.lib.Packet;
//
//
//public class MetaPacket extends PacketBase {
//	
//	private static final int HEADER_LENGTH = 3;
//	public byte dest;		//8bits
//	public byte lastHop;	//8bits
//	public byte source;	//8bits
//	public byte groupSize;
//	
//	
//	public void setLastHop (byte h) {
//		lastHop = h;
//	}
//	
//	public byte getLastHop() {
//		return lastHop;
//	}
//	
//
//	public MetaPacket (byte s, byte d) {
//		if (s != 0) {
//			source = s;
//			dest = d;
//		}
//		else {
//			throw new Error("Source can not be 0!");
//		}
//		
//	}
//	
//	public MetaPacket (byte[] msg) {
//		super();
//		groupSize = (byte) ((msg[0]&255) % 128);
//		source = msg[1];
//		dest = msg[2];
//		lastHop = msg[3];
//	}
//
//	public byte[] getBytes () {
//		byte[] ret = new byte[HEADER_LENGTH];
//		ret[0] = (byte) (((source&255)%128)+128);
//		ret[1] = dest;
//		ret[2] = lastHop;
//		
//		return ret;
//	}
//	
//	
//	public MetaPacket setType (int i) {
//		type = (byte) i;
//		return this;
//	}
//	
//	
//	public String toString () {
//		String header = "MP from: " + getSource() + " to: " + getDest() + " via " + getLastHop();
//			
//		return header;
//	}
//	
//	
//	
//	
//	public byte getDest() {
//		return dest;
//	}
//
//	public byte getSource() {
//		return source;
//	}
//	
//
//	public MetaPacket setSource(byte id) {
//		source = id;
//		return this;
//	}
//	
//	
//	public MetaPacket setDest(byte m) {
//		dest = m;
//		return this;
//	}
//	
//	
//}
