package ussr.samples.atron.simulations.wouter;

import java.io.Serializable;

enum Module {ALL, MC,ML,MR,F0,F1,F2,F3,F4,F5,F6,F7,F8,F9,F10;
	public byte ord() {
		return (byte)ordinal();
	}
}
enum Type {ROTATE,CONNECT,DISCONNECT,STATE,DISCOVER;
	public byte ord() {
		return (byte)ordinal();
	}
}

enum Dir {REQ,ACK;
	public byte ord() {
		return (byte)ordinal();
	}
}

class Packet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	Type type = Type.DISCOVER;
	Dir dir = Dir.REQ;
	private Module source;
	private Module dest;
	private byte sourceConnector = -1;
	public byte data;  

	public Packet (Module s, Module d) {
		source = s;
		dest = d;
	}
	
	public Packet (Packet p) {
		type = p.getType();
		source = p.getSource();
		sourceConnector = p.getSourceConnector();
		dest = p.getDest();
		data = p.getData();
		dir = p.getDir();
	}
	
	
	public byte getData() {
		return data;
	}

	public String toString () {
		return "(" + getDir().toString() + ") from: " + getSource().toString() + "(on: " + sourceConnector + ") to: " + getDest().toString() + " - " + getType().toString() + " (" + data + ")";
	}
	
	public Packet setSourceConnector (byte c) {
		System.out.println(".setSourceConnector " + c);
		if (c != -1){
			sourceConnector = c;
		}
		else {
			System.err.println("sourceConnector " + c);
		}
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
	
	public Dir getDir () {
		return dir;
	}
	
	public byte[] getBytes () {
		return new byte[]{type.ord(),source.ord(),sourceConnector,dest.ord(),data,dir.ord()};
	}
	
	public Packet (byte[] msg) {
		type = Type.values()[msg[0]];
		source = Module.values()[msg[1]];
		sourceConnector = msg[2];
		dest = Module.values()[msg[3]];
		data = msg[4];
		dir = Dir.values()[msg[5]];
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
		data = (byte)d;
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
