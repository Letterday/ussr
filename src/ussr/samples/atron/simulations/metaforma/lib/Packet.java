package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.gen.*;

import java.io.Serializable;


public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	Type type = Type.DISCOVER;
	private Module source;
	private Module dest;
	private byte sourceConnector = -1;
	Dir dir = Dir.REQ;
	private byte stateOperation = -1;
	private byte stateInstruction = -1;
	private byte statePending = -1;
	public byte[] data = new byte[3];

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
	
	public byte getStateOperation() {
		return stateOperation;
	}
	
	public byte getStateInstruction() {
		return stateInstruction;
	}
	
	public byte getStatePending() {
		return statePending;
	}
	
	public Packet addState(MetaformaController c) {
		if (stateOperation == -1) {
			stateOperation = (byte)c.getStateOperation();
		}
		if (stateInstruction == -1) {
			stateInstruction = (byte)c.getStateInstruction();
		}
		if (statePending == -1) {
			statePending = (byte)c.getStatePending();
		}

		return this;
	}
	
	
	
	
	public byte[] getData() {
		return data;
	}

	public String toString () {
		return "(" + getDir().toString() + ") from: " + getSource().toString() + "(over: " + sourceConnector + ") to: " + getDest().toString() + " - " + getType().toString() + " [" + getStateOperation() + "," + getStateInstruction() + "," + getStatePending() + "]" + (data[0] != 0 ? "(" + data[0] + "," + data[1] + "," + data[2] + ")" : "");
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
	
	public Packet setState (byte operation, byte instruction, byte pending) {
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
		return new byte[]{type.ord(),source.ord(),sourceConnector,dest.ord(),dir.ord(),stateOperation,stateInstruction,statePending,data[0],data[1],data[2]};
	}
	
	public Packet (byte[] msg) {
		type = Type.values()[msg[0]];
		source = Module.values()[msg[1]];
		sourceConnector = msg[2];
		dest = Module.values()[msg[3]];
		dir = Dir.values()[msg[4]];
		stateOperation = msg[5];
		stateInstruction = msg[6];
		statePending = msg[7];
		data = new byte[] {msg[8],msg[9],msg[10]};
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

	public Packet setData(int d) {
		return setData(d,0);
	}
	
	public Packet setData(int d1, int d2) {
		data[0] = (byte)d1;
		data[1] = (byte)d2;
		return this;
	}
	
	public Packet setData(int d1, int d2, int d3) {
		data[0] = (byte)d1;
		data[1] = (byte)d2;
		data[2] = (byte)d3;
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
