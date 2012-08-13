package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.gen.*;

import java.io.Serializable;
import java.math.BigInteger;


public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final int HEADER_LENGTH = 9;

	private static MetaformaController ctrl;

	Type type = Type.DISCOVER;
	private Module source;
	private Module dest;
	private byte sourceConnector = -1;
	Dir dir = Dir.REQ;
	private byte stateInstruction = -1;
	public byte[] data = new byte[]{};
	private byte metaId = 0; // 0 is for everyone
	private boolean connectorConnected;
	private byte metaSourceId;
	
	
	private IMetaRole metaRole;
	


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

		stateInstruction = p.getStateInstruction();
		metaId = p.getMetaId();
		metaSourceId = p.getMetaSourceId();
	}
	
	public byte[] getBytes () {
		byte[] ret = new byte[data.length+HEADER_LENGTH];
		ret[0] = (byte) (sourceConnector + ((dir.ord() == 1) ? 16 : 0) + (connectorConnected ? 32 : 0));
		ret[1] = source.ord();
		ret[2] = metaRole.index();
		ret[3] = dest.ord();
		ret[4] = type.ord();
		ret[5] = stateInstruction;
		ret[6] = metaId;
		ret[7] = metaSourceId;
		ret[8] = (byte)data.length;

		for (int i=0; i<data.length; i++) {
			ret[i+HEADER_LENGTH] = data[i];
		}
		return ret;
	}
	
	public Packet (byte[] msg) {
		dir = Dir.values()[(msg[0]&16)==16?1:0];
		connectorConnected = (msg[0]&32)==32;
		source = Module.values()[msg[1]];
		sourceConnector = (byte) (msg[0]%8);
		metaRole =  ctrl.getMetaRole().fromByte(msg[2]);
		dest = Module.values()[msg[3]];
		type = Type.values()[msg[4]];  
		stateInstruction = msg[5];
		metaId = msg[6];
		metaSourceId = msg[7];
		
		byte payloadLength = msg[8]; 			// the length of the payload is stored in msg[8]
		data = new byte[payloadLength];		
		
		for (int i=0; i<payloadLength; i++) {
			data[i] = msg[i+HEADER_LENGTH];
		}
	}
	
	public IMetaRole getMetaRole () {
		return metaRole;
	}
	
	public void setMetaRole (IMetaRole p) {
		metaRole = p;
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
		String header = getDir().toString() + " " + getType().toString() + " from: " + getSource().toString() + "(" + metaSourceId + " using "+metaId+")" + "(over " + sourceConnector + ") to: " + getDest().toString() + " - "  + " [" + "#" + getStateInstruction() + "] " + metaId + " ";
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
	
	public byte getMetaId() {
		return metaId;
	}
	
	public void setMetaId(byte id) {
		metaId = id;
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
