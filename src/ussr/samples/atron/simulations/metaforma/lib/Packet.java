package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.gen.*;

import java.io.Serializable;
import java.math.BigInteger;


public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final int HEADER_LENGTH = 10;

	private static MetaformaController ctrl;

	Type type = Type.DISCOVER;
	private Module source;
	private Module dest;
	private byte sourceConnector = -1;
	Dir dir = Dir.REQ;
	private IStateOperation stateOperation = null;
	private byte stateInstruction = -1;
	public byte[] data = new byte[]{};
	private byte metaId = 0; // 0 is for everyone
	private boolean connectorConnected;
	


	public Packet (Module s, Module d) {
		source = s;
		dest = d;
	}
	
	public Packet (Module s) {
		source = s;
		dest = Module.ALL;
	}
	
	public Packet (Packet p) {
		type = p.getType();
		source = p.getSource();
		sourceConnector = p.getSourceConnector();
		dest = p.getDest();
		data = p.getData();
		dir = p.getDir();
		stateOperation = p.getStateOperation();
		stateInstruction = p.getStateInstruction();
		metaId = p.getMetaId();
	}
	
	public Packet (byte[] msg) {
		type = Type.values()[msg[0]];
		source = Module.values()[msg[1]];
		sourceConnector = msg[2];
		dest = Module.values()[msg[3]];
		dir = Dir.values()[msg[4]];  
		stateOperation = ctrl.IstateOperation.fromByte(msg[5]);
		stateInstruction = msg[6];
		metaId = msg[7];
		connectorConnected = msg[8] == 1;
		
		byte payloadLength = msg[9]; 			// the length of the payload is stored in msg[8]
		data = new byte[payloadLength];		
		
		for (int i=0; i<payloadLength; i++) {
			data[i] = msg[i+HEADER_LENGTH];
		}
	}
	
	public IStateOperation getStateOperation() {
		return stateOperation;
	}
	
	public byte getStateInstruction() {
		return stateInstruction;
	}
	
	public Packet addState(MetaformaController c) {
		if (stateOperation == null) {
			stateOperation = c.getStateOperation();
			if (stateInstruction == -1) {
				stateInstruction = (byte)c.getStateInstruction();
			}
		}
		return this;
	}
		
	
	public byte[] getData() {
		return data;
	}
	
	public BigInteger getDataAsInteger() {
		return new BigInteger(data);
	}

		
	public String toString () {
		String header = getDir().toString() + " " + getType().toString() + " from: " + getSource().toString() + "(" + metaId + ")" + "(over " + sourceConnector + ") to: " + getDest().toString() + " - "  + " [" + getStateOperation() + "#" + getStateInstruction() + "] " + metaId + " ";
		String payload = "  ";
		if (getType() == Type.CONSENSUS) {
			payload = new BigInteger(data).bitCount() + " ";
		}
		else if (getType() == Type.GRADIENT) {
			payload = ctrl.Ivar.fromByte(data[0]) + "," + data[1]  + " ";
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
	
	public Packet setState (IStateOperation operation, byte instruction, byte pending) {
		stateOperation = operation;
		stateInstruction = instruction;
		return this;
	}
	
	public Packet setType(Type t) {
		type = t;
		return this;
	}
	
	public Packet setDir(Dir d) {
		dir = d;
		return this;
	}
	
	public Type getType () {
		return type;
	}
	
	public boolean isType (Type t) {
		return t.equals(type);
	}
	
	public Dir getDir () {
		return dir;
	}
	
	public byte[] getBytes () {
		byte[] ret = new byte[data.length+HEADER_LENGTH];
		ret[0] = type.ord();
		ret[1] = source.ord();
		ret[2] = sourceConnector;
		ret[3] = dest.ord();
		ret[4] = dir.ord();
		ret[5] = stateOperation.ord();
		ret[6] = stateInstruction;
		ret[7] = metaId;
		ret[8] = (byte) (connectorConnected ? 1 : 0);
		ret[9] = (byte)data.length;

		for (int i=0; i<data.length; i++) {
			ret[i+HEADER_LENGTH] = data[i];
		}
		return ret;
	}
	
	

	public Packet getAck() {
		return new Packet(dest,source).setDir(Dir.ACK);
	}

	public Module getDest() {
		return dest;
	}

	public Module getSource() {
		return source;
	}
	
	public byte getMetaId() {
		return metaId;
	}
	
	public void setMetaId(byte id) {
		metaId = id;
	}


	public Packet setSource(Module id) {
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

	public Packet setDest(Module m) {
		dest = m;
		return this;
	}

	public boolean isReq() {
		return getDir() == Dir.REQ;
	}

	public static void setController (MetaformaController c) {
		ctrl = c;
	}

	public boolean getConnectorConnected() {
		return connectorConnected;
	}
	
	public void setConnectorConnected(boolean c) {
		connectorConnected = c;
	}
	
	
}
