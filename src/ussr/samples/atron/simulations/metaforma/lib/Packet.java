package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.gen.*;

import java.io.Serializable;
import java.math.BigInteger;


public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final int HEADER_LENGTH = 9;
	public static IOperation operationHolder;
	
	Type type = Type.DISCOVER;
	private Module source;
	private Module dest;
	private byte sourceConnector = -1;
	Dir dir = Dir.REQ;
	private IOperation stateOperation = null;
	private byte stateInstruction = -1;
	private byte statePending = -1;
	public byte[] data = new byte[]{};


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
		statePending = p.getStatePending();
	}
	
	public Packet (byte[] msg) {
		type = Type.values()[msg[0]];
		source = Module.values()[msg[1]];
		sourceConnector = msg[2];
		dest = Module.values()[msg[3]];
		dir = Dir.values()[msg[4]];  
		stateOperation = operationHolder.fromByte(msg[5]);
		stateInstruction = msg[6];
		statePending = msg[7];
		
		byte payloadLength = msg[8]; 			// the length of the payload is stored in msg[8]
		data = new byte[payloadLength];		
		
		for (int i=0; i<payloadLength; i++) {
			data[i] = msg[i+HEADER_LENGTH];
		}
	}
	
	public IOperation getStateOperation() {
		return stateOperation;
	}
	
	public byte getStateInstruction() {
		return stateInstruction;
	}
	
	public byte getStatePending() {
		return statePending;
	}
	
	public Packet addState(MetaformaController c) {
		if (stateOperation == null) {
			stateOperation = c.getStateOperation();
			if (stateInstruction == -1) {
				stateInstruction = (byte)c.getStateInstruction();
			}
			if (statePending == -1) {
				statePending = (byte)c.getStatePending();
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
		String header = getDir().toString() + " " + getType().toString() + " from: " + getSource().toString() + "(over " + sourceConnector + ") to: " + getDest().toString() + " - "  + " [" + getStateOperation() + "," + getStateInstruction() + "," + getStatePending() + "]";
		String payload = " ";
		for (int i=0; i<data.length; i++) {
			payload += Byte.toString(data[i]) + ",";
		}
		return header + " (" + payload.substring(payload.length()-1) + ")";
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
	
	public Packet setState (IOperation operation, byte instruction, byte pending) {
		stateOperation = operation;
		stateInstruction = instruction;
		statePending = pending;
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
		ret[7] = statePending;
		ret[8] = (byte)data.length;

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


	public Packet setSource(Module id) {
		source = id;
		return this;
	}

	public Packet setData(IOperation o) {
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

	
	
}
