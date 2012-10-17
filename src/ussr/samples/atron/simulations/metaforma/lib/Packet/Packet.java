package ussr.samples.atron.simulations.metaforma.lib.Packet;

import ussr.samples.atron.simulations.metaforma.lib.Dir;
import ussr.samples.atron.simulations.metaforma.lib.IModule;
import ussr.samples.atron.simulations.metaforma.lib.IMetaPart;
import ussr.samples.atron.simulations.metaforma.lib.MfController;
import ussr.samples.atron.simulations.metaforma.lib.Module;
import ussr.samples.atron.simulations.metaforma.lib.Orientation;
import ussr.samples.atron.simulations.metaforma.lib.State;


// Chosen to use manual serialization instead of Java's built in serialization
// Reason: Much fewer bytes needed to transport on infra-red!

public abstract class Packet extends PacketBase {

	public Packet(MfController c) {
		super(c);
	}

	private static final int HEADER_LENGTH = 7;
	

	protected IModule source;				// 8bits
	
//	private byte type = 0;	// 4bits
	public byte connSource = -1;	// 3bits
	public byte connDest = -1;	// Not in payload
	private Dir dir = Dir.REQ;			// 1bit
	private boolean connectorConnected;	// 1bit
	
	private IMetaPart metaPart;			// 3bits
	
	public byte regionID = 0; 	// 0 is for everyone
	
	public byte metaID = 0;

	private State state; // operation: 3 bits, counter: 5 bits, // instruction 5bits
	
	
	public final Packet deserialize (byte[] b,byte connector) {
		byte[] payload = deserializeHeader(b,connector);
		deserializePayload(payload);
		return this;
	}
	
	public final byte[] serialize () {
		return serializeHeader(serializePayload());
	}
	
	public abstract byte[] serializePayload ();
//	{
//		return new byte[0];
//	}
//	
	
	public abstract Packet deserializePayload (byte payload[]);
//	{
//		if (payload.length > 0) {
//			ctrl.getVisual().error("Payload length " + payload.length + " but not implemented!");
//		}
//		return null;
//	}
	
	
	private byte[] serializeHeader (byte data[]) {
		byte[] ret = new byte[data.length+HEADER_LENGTH];
//		if (connSource == -1) {
//			throw new Error("sourceConnector = -1");
//		}
		// 0 is used to determine difference between packet and metapacket
		ret[0] = (byte) ((state.getOrientation().ordinal()%8)|((type%16)<<3));
		ret[1] = (byte) (source.ord());
		ret[2] = (byte) ( ((connSource%8)<<3) | ((dir.ord()%2)<<6) |((connectorConnected?1:0)<<7));
		ret[3] = (byte) (state.getInstruction()%32 | (metaPart.index()%8)<<5);
		ret[4] = regionID;
		ret[5] = metaID;
		ret[6] = (byte) ((state.getOperation().ord()%8) | (state.getOperationCounter()<<3));

		for (int i=0; i<data.length; i++) {
			ret[i+HEADER_LENGTH] = data[i];
		}
		return ret;
	}
	
	public byte[] deserializeHeader (byte[] msg,byte connector) {		
		connDest 					= connector;
		
		source = 					Module.value(msg[1]&255);
		
		
		type = 						getType(msg);
		
		
		connSource = 		(byte) 	(((msg[2]&255)>>3)%8);
		dir = Dir.values()			[((msg[2]&255)>>6)%2];
		connectorConnected =		(((msg[2]&255)>>7)==1);
		

		metaPart =  ctrl.getMetaPart().fromByte((byte) ((msg[3]&255)>>5));
		
		regionID = msg[4];
		metaID = msg[5];
		state = new State(ctrl.getStateInit().fromByte((byte) ((msg[6]&255)%8)),(byte) (((msg[6]&255)>>3)%32),(byte) ((msg[3]&255)%32),Orientation.values()[(msg[0]&255)%8]);
		
		byte[] payload = new byte[ msg.length-HEADER_LENGTH ];		
		
		for (int i=0; i<payload.length; i++) {
			payload[i] = msg[i+HEADER_LENGTH];
		}
		return payload;
	}
	
	public IMetaPart getMetaPart () {
		if (metaPart == null) {
			throw new Error("Packet " + this + " has null metapart!");
		}
		return metaPart;
	}
	
	public Packet setMetaPart (IMetaPart r) {
		
		metaPart = r;
		return this;
	}
		
	public Packet addState(State s) {
		state = s;
		return this;
	}
		
	
	private String toStringHeader () {
		String ret = "";
		ret += getDir().toString() + " ";
		ret += getClass().getSimpleName().replaceAll("Packet", "") + " ";
		ret += " from: " + getSource() + "(" + metaID + " using "+regionID+")" + "(over " + connSource + " to " + connDest + ") " + state + " " + " ";
		return ret;
	}
	
	public abstract String toStringPayload ();
		
	public String toString () {
		return toStringHeader() + toStringPayload();
	}
	
	public Packet setSourceConnector (byte c) {
		if (c != -1){
			connSource = c;
		}
		else {
			System.err.println("sourceConnector " + c);
		}
		return this;
	}
	
	
	public Packet setType(int t) {
		type = (byte) t;
		return this;
	}
	
	public Packet setDir(Dir d) {
		dir = d;
		return this;
	}
		
	public Dir getDir () {
		return dir;
	}
	

	public IModule getSource() {
		if (source == null) {
			throw new Error("Packet " + this + " has null source!");
		}
		return source;
	}
	
	public byte getRegionID() {
		return regionID;
	}
	
	public void setRegionID(byte id) {
		regionID = id;
	}
	
	


	public Packet setSource(IModule id) {
		source = id;
		return this;
	}

	
	public byte getSourceConnector() {
		return connSource;
	}


	public boolean getConnectorConnected() {
		return connectorConnected;
	}
	
	public void setConnectorConnected(boolean c) {
		connectorConnected = c;
	}

	public byte getMetaID() {
		return metaID;
	}
	
	public void setMetaID(int id) {
		metaID = (byte) id;
	}
	

	public State getState() {
		return state;
	}

	public static byte getType(byte[] msg) {
		return (byte) (((msg[0]&255)>>3)%16);
	}

	
	
}
