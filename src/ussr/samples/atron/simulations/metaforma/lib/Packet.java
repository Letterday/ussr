package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.gen.*;

import java.io.Serializable;
import java.math.BigInteger;

// There is chosen to use manual serialization instead of Java's built in serialization
// Reason: Much fewer bytes needed to transport on infrared!

public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final int HEADER_LENGTH = 6;

	private static MetaformaController ctrl;

	private Module source;				// 8bits
	
	private Module dest;				// 8bits
	
	private Type type = Type.DISCOVER;	// 3bits
	private byte sourceConnector = -1;	// 3bits
	private Dir dir = Dir.REQ;			// 1bit
	private boolean connectorConnected;	// 1bit
	
	private byte stateInstruction = -1;	// 5bits
	private IRole moduleRole;			// 3bits
	
	private byte metaBossId = 0; 	// 0 is for everyone
	
	private byte metaSourceId = 0;
	public byte[] data = new byte[]{};
	
	
	
	


	public Packet (Module s, Module d) {
		source = s;
		dest = d;
	}
	
	public Packet (Module s) {
		source = s;
		dest = Module.ALL;
	}
	
	
	public byte[] getBytes () {
		byte[] ret = new byte[data.length+HEADER_LENGTH];
		if (sourceConnector == -1) {
			throw new Error("sourceConnector = -1");
		}
		ret[0] = source.ord();
		ret[1] = dest.ord();
		ret[2] = (byte) (type.ord() | (sourceConnector<<3) | (dir.ord()<<6) |((connectorConnected?1:0)<<7));
		ret[3] = (byte) (stateInstruction | moduleRole.index()<<5);
		ret[4] = metaBossId;
		ret[5] = metaSourceId;

		for (int i=0; i<data.length; i++) {
			ret[i+HEADER_LENGTH] = data[i];
		}
		return ret;
	}
	
	public Packet (byte[] msg) {
		source = Module.values()[msg[0]];
		dest = Module.values()[msg[1]];
		
		type = Type.values()     	[(msg[2]&255>>0)%8];  
		sourceConnector = (byte) 	((msg[2]&255>>3)%8);
		dir = Dir.values()			[(msg[2]&255>>6)%1];
		connectorConnected =		((msg[2]&255>>7)==1);
		
		stateInstruction = (byte) ((msg[3]&255)%32);
		moduleRole =  ctrl.moduleRoleGet().fromByte((byte) ((msg[3]&255)>>5));
		
		metaBossId = msg[4];
		metaSourceId = msg[5];
		
		
		data = new byte[ msg.length-HEADER_LENGTH ];		
		
		for (int i=0; i<data.length; i++) {
			data[i] = msg[i+HEADER_LENGTH];
		}
	}
	
	public IRole getModRole () {
		return moduleRole;
	}
	
	public void setModRole (IRole p) {
		moduleRole = p;
	}
	
	
	public byte getStateInstruction() {
		return stateInstruction;
	}
	
	public Packet addState(MetaformaController c) {

		if (stateInstruction == -1) {
			stateInstruction = (byte)c.getStateInstruction();
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
		String header = getDir().toString() + " " + getType().toString() + " from: " + getSource().toString() + "(" + metaSourceId + " using "+metaBossId+")" + "(over " + sourceConnector + ") to: " + getDest().toString() + " - "  + " [" + "#" + getStateInstruction() + "] " + metaBossId + " ";
		String payload = "  ";
		if (getType() == Type.CONSENSUS) {
			payload = new BigInteger(data).bitCount() + " ";
		}
		else if (getType() == Type.GRADIENT) {
			payload = ctrl.varFromByteLocal(data[0]) + "," + data[1]  + " ";
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
	
	
	
	

	public Packet getAck() {
		return new Packet(dest,source).setDir(Dir.ACK);
	}

	public Module getDest() {
		return dest;
	}

	public Module getSource() {
		return source;
	}
	
	public byte getMetaBossId() {
		return metaBossId;
	}
	
	public void setMetaBossId(byte id) {
		metaBossId = id;
	}
	
	public void setMetaSourceId(byte id) {
		metaSourceId = id;
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

	public byte getMetaSourceId() {
		return metaSourceId;
	}
	
	
}
