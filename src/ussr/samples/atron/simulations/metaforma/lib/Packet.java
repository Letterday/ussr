package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.gen.*;

import java.io.Serializable;


public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	Type type = Type.DISCOVER;
	private Module source;
	private byte sourceConnector = -1;
	private Module dest;
	Dir dir = Dir.REQ;
	public byte[] data = new byte[2];

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
	
	
	public byte[] getData() {
		return data;
	}

	public String toString () {
		return hashCode() + "(" + getDir().toString() + ") from: " + getSource().toString() + "(over: " + sourceConnector + ") to: " + getDest().toString() + " - " + getType().toString() + " (" + data[0] + "," + data[1] + ")";
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
		return new byte[]{type.ord(),source.ord(),sourceConnector,dest.ord(),dir.ord(),data[0],data[1]};
	}
	
	public Packet (byte[] msg) {
		type = Type.values()[msg[0]];
		source = Module.values()[msg[1]];
		sourceConnector = msg[2];
		dest = Module.values()[msg[3]];
		dir = Dir.values()[msg[4]];
		data = new byte[] {msg[5],msg[6]};
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

	public byte getSourceConnector() {
		return sourceConnector;
	}

	public Packet setDest(Module m) {
		dest = m;
		return this;
	}
	
}
