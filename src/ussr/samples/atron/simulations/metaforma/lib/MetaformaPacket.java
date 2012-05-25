package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.gen.*;

import java.io.Serializable;


public class MetaformaPacket implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	Type type = Type.DISCOVER;
	Dir dir = Dir.REQ;
	private Module source;
	private Module dest;
	private byte sourceConnector = -1;
	public byte data;
	public int t;

	public MetaformaPacket (Module s, Module d) {
		source = s;
		dest = d;
	}
	
	public MetaformaPacket (MetaformaPacket p) {
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
		return hashCode() + "(" + getDir().toString() + ") from: " + getSource().toString() + "(over: " + sourceConnector + ") to: " + getDest().toString() + " - " + getType().toString() + " (" + data + ")";
	}
	
	public MetaformaPacket setSourceConnector (byte c) {
		if (c != -1){
			sourceConnector = c;
		}
		else {
			System.err.println("sourceConnector " + c);
		}
		return this;
	}
	
	public MetaformaPacket setType(Type t) {
		type = t;
		return this;
	}
	
	public MetaformaPacket setDir(Dir d) {
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
	
	public MetaformaPacket (byte[] msg) {
		type = Type.values()[msg[0]];
		source = Module.values()[msg[1]];
		sourceConnector = msg[2];
		dest = Module.values()[msg[3]];
		data = msg[4];
		dir = Dir.values()[msg[5]];
	}

	public MetaformaPacket getAck() {
		return new MetaformaPacket(dest,source).setDir(Dir.ACK);
	}

	public Module getDest() {
		return dest;
	}

	public Module getSource() {
		return source;
	}


	public MetaformaPacket setSource(Module id) {
		source = id;
		return this;
	}

	public MetaformaPacket setData(int d) {
		data = (byte)d;
		return this;
	}

	public byte getSourceConnector() {
		return sourceConnector;
	}

	public MetaformaPacket setDest(Module m) {
		dest = m;
		return this;
	}
	
}
