package ussr.samples.atron.simulations.metaforma.lib;




public class MetaPacket extends PacketBase {
	
	private static final int HEADER_LENGTH = 4;

	private static final byte INITIAL_TTL = 5;

	private IPacketType type;	//2bits
	private byte ttl;		//3bits
	
	private byte lastHop;	//8bits
	private byte dest;		//8bits

	private byte source;	//8bits
	

	public IPacketType getType () {
		return type;
	}
	
	public void setLastHop (byte h) {
		lastHop = h;
	}
	
	public byte getLastHop() {
		return lastHop;
	}
	
	public byte getTTL() {
		return ttl;
	}

	public boolean isDying() {
		return ttl <= 0;
	}

	public MetaPacket (byte s, byte d) {
		if (s != 0) {
			source = s;
			dest = d;
			ttl = INITIAL_TTL;
			data = new byte[0];
		}
		else {
			throw new Error("Source can not be 0!");
		}
		
	}
	
	public MetaPacket (byte[] msg) {
		super();
		source = (byte) ((msg[0]&255) % 128);
		dest = msg[1];
		lastHop = msg[2];
		type = ctrl.getMetaPacketType(((msg[3]&255)>>0)%4);   
		ttl = (byte) 				(((msg[3]&255)>>2)%8);
	    
		
		data = new byte[ msg.length-HEADER_LENGTH ];
		
		for (int i=0; i<data.length; i++) {
			data[i] = msg[i+HEADER_LENGTH];
		}

	}

	public byte[] getBytes () {
		byte[] ret = new byte[data.length+HEADER_LENGTH];
		ret[0] = (byte) (((source&255)%128)+128);
		ret[1] = dest;
		ret[2] = lastHop;
		ret[3] = (byte) (type.ord() | (ttl<<2) | 15<<5);
				
		for (int i=0; i<data.length; i++) {
			ret[i+HEADER_LENGTH] = data[i];
		}
		
		return ret;
	}
	
	
	public MetaPacket setType (IPacketType t) {
		type = t;
		return this;
	}
	
	
	public byte[] getData() {
		return data;
	}
	
		
	public String toString () {
		String header = type + " from: " + getSource() + " to: " + getDest() + " via " + getLastHop() + " - ttl " + ttl;
		String payload = "  ";
		for (int i=0; i<data.length; i++) {
			payload += data[i] + ", ";
		}
	
		return header + " (" + payload.substring(0,payload.length()-1) + ")";
	}
	
	
	
	
	public byte getDest() {
		return dest;
	}

	public byte getSource() {
		return source;
	}
	

	public MetaPacket setSource(byte id) {
		source = id;
		return this;
	}

	
	public MetaPacket setData(byte[] b) {
		data = b;
		return this;
	}
	
	public MetaPacket setData(int b1) {
		return setData(new byte[]{(byte) b1});
	}
	
	public MetaPacket setData(int b1,int b2) {
		return setData(new byte[]{(byte) b1,(byte) b2});
	}
	
	
	public MetaPacket setDest(byte m) {
		dest = m;
		return this;
	}

	
	public void decreaseTTL() {
		ttl--;
	}
	
	
}
