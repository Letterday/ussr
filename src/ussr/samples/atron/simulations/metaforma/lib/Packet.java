package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.gen.*;
import java.math.BigInteger;

// There is chosen to use manual serialization instead of Java's built in serialization
// Reason: Much fewer bytes needed to transport on infrared!

public class Packet extends PacketBase {

	private static final int HEADER_LENGTH = 6;

	

	protected IModule source;				// 8bits
	
	private IPacketType type = PacketCoreType.DISCOVER;	// 3bits
	private byte sourceConnector = -1;	// 3bits
	private Dir dir = Dir.REQ;			// 1bit
	private boolean connectorConnected;	// 1bit
	
	private byte stateInstruction = -1;	// 5bits
	private IRole IModuleRole;			// 3bits
	
	private byte metaBossId = 0; 	// 0 is for everyone
	
	private byte metaSourceId = 0;

	private IStateOperation stateOperation; // 3 bits
	private byte stateOperationCounter;	// 5bits
	
	
	public Packet (IModule s) {
		source = s;
	}
	
	
	public byte[] getBytes () {
		byte[] ret = new byte[data.length+HEADER_LENGTH];
		if (sourceConnector == -1) {
			throw new Error("sourceConnector = -1");
		}
		ret[0] = (byte) (source.ord() % 128);
		ret[1] = (byte) (type.ord()%8 | ((sourceConnector%8)<<3) | ((dir.ord()%2)<<6) |((connectorConnected?1:0)<<7));
		ret[2] = (byte) (stateInstruction%32 | (IModuleRole.index()%8)<<5);
		ret[3] = metaBossId;
		ret[4] = metaSourceId;
		ret[5] = (byte) ((stateOperation.ord()%8) | (stateOperationCounter<<3));

		for (int i=0; i<data.length; i++) {
			ret[i+HEADER_LENGTH] = data[i];
		}
		return ret;
	}
	
	public Packet (byte[] msg) {
		if (!isPacket(msg)) {
			System.err.println("NOT a packet!");
		}
		source = Module.value(msg[0]&255>>0%128);
		
		type = PacketCoreType.values()     	[((msg[1]&255)>>0)%8];  
		sourceConnector = (byte) 	(((msg[1]&255)>>3)%8);
		dir = Dir.values()			[((msg[1]&255)>>6)%2];
		connectorConnected =		(((msg[1]&255)>>7)==1);
		
		stateInstruction = (byte) ((msg[2]&255)%32);
		
		IModuleRole =  ctrl.moduleRoleGet().fromByte((byte) ((msg[2]&255)>>5));
		
		metaBossId = msg[3];
		metaSourceId = msg[4];
		stateOperation = ctrl.getStateMngr().getState().getOperation().fromByte((byte) ((msg[5]&255)%8));
		stateOperationCounter = (byte) (((msg[5]&255)>>3)%32);
		
		data = new byte[ msg.length-HEADER_LENGTH ];		
		
		for (int i=0; i<data.length; i++) {
			data[i] = msg[i+HEADER_LENGTH];
		}
	}
	
	public IRole getModRole () {
		return IModuleRole;
	}
	
	public Packet setModRole (IRole p) {
		IModuleRole = p;
		return this;
	}
	
	
	public byte getStateInstruction() {
		return stateInstruction;
	}
	
	public IStateOperation getStateOperation() {
		return stateOperation;
	}
	
	public byte getStateOperationCounter() {
		return stateOperationCounter;
	}
	
	
	public Packet addState(State s) {
		if (stateInstruction == -1) {
			stateInstruction = s.getInstruction();
		}
		
		stateOperation = s.getOperation();
		stateOperationCounter = s.getOperationCounter();
		
		return this;
	}
		
	
	public byte[] getData() {
		return data;
	}
	
	public BigInteger getDataAsInteger() {
		return new BigInteger(data);
	}

		
	public String toString () {
		String header = getDir().toString() + " " + getType().toString() + " from: " + getSource().toString() + "(" + metaSourceId + " using "+metaBossId+")" + "(over " + sourceConnector + ")" + " [" + stateOperation  + "(" + stateOperationCounter + ") # " + getStateInstruction() + "] " + metaBossId + " ";
		String payload = "  ";
		if (getType() == PacketCoreType.CONSENSUS) {
			payload = new BigInteger(data).bitCount() + " - " + Module.fromBits(new BigInteger(data)) + " ";
		}
		else if (getType() == PacketCoreType.GRADIENT) {
			payload = ctrl.varFromByteLocal(data[0]) + "," + data[1]  + " ";
		}
		else if (getType() == PacketCoreType.META_VAR_SYNC) {
			for (int i=0; i<data.length;i=i+3) {
				payload += ctrl.varInitFromBytes(data[i]) + "=" + data[i+1]  + " (" + data[i+2] + ") , ";
			}
		}
		else {
			for (int i=0; i<data.length; i++) {
				payload += data[i] + ", ";
			}
		}
		return header + " (" + payload.substring(0,payload.length()-1) + ")";
	}
	
	public Packet setSourceConnector (byte c) {
		if (c != -1){
			sourceConnector = c;
		}
		else {
			System.err.println("sourceConnector " + c);
		}
		return this;
	}
	
	
	public Packet setType(PacketCoreType t) {
		type = t;
		return this;
	}
	
	public Packet setDir(Dir d) {
		dir = d;
		return this;
	}
	
	public IPacketType getType () {
		return type;
	}
	
	public boolean isType (PacketCoreType t) {
		return t.equals(type);
	}
	
	public Dir getDir () {
		return dir;
	}
	

	public IModule getSource() {
		return source;
	}
	
	public byte getMetaBossId() {
		return metaBossId;
	}
	
	public void setMetaBossId(byte id) {
		metaBossId = id;
	}
	
	public void setMetaSourceId(int id) {
		metaSourceId = (byte) id;
	}


	public Packet setSource(IModule id) {
		source = id;
		return this;
	}

	public Packet setData(IStateOperation o) {
		data = new byte[]{o.ord()};
		return this;
	}
	
	public Packet setData(byte b) {
		data = new byte[]{b};
		return this;
	}
	
	public Packet setData(byte[] b) {
		data = b;
		return this;
	}
	

	
	public Packet setData(BigInteger bi) {
		data = bi.toByteArray();
		return this;
	}

	
	public byte getSourceConnector() {
		return sourceConnector;
	}


	public boolean isReq() {
		return getDir() == Dir.REQ;
	}


	public boolean getConnectorConnected() {
		return connectorConnected;
	}
	
	public void setConnectorConnected(boolean c) {
		connectorConnected = c;
	}

	public byte getMetaSourceId() {
		return metaSourceId;
	}
	
	public static boolean isPacket(byte[] msg) {
		return (msg[0]&255>>7)%1 == 0;
	}


	public State getState() {
		return new State(stateOperation,stateOperationCounter,stateInstruction);
	}
}
