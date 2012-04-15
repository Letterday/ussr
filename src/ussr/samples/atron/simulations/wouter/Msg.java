package ussr.samples.atron.simulations.wouter;

import java.io.Serializable;




class Msg implements Serializable {
	private static final long serialVersionUID = 1L;
	enum Type {ROTATE,CONNECT,DISCONNECT,STATE, DISCOVER, FINISHED}
	enum Dir {REQ, ACK}
	
	
	final int HEADER_LENGTH = 4;
	
	Type type;
	private Module source = null;
	private Module dest = null;
	Dir dir;
	byte[] content;
	
	public Msg (int b1, int b2) {
		type = Type.STATE;
		dir = Dir.REQ;
		content = new byte[]{(byte)b1,(byte)b2};
	}
	
	public Msg (int b1) {
		type = Type.STATE;
		dir = Dir.REQ;
		content = new byte[]{(byte)b1};
	}
	
	
	public Msg (byte[] b) {		
		type = Type.values()[b[0]];
		dir = Dir.values()[b[1]];
		source = Module.values()[b[2]];
		dest = Module.values()[b[3]];
		
		content = new byte[b.length - HEADER_LENGTH];
		for (int i=0; i<b.length - HEADER_LENGTH; i++) {
			content[i] = b[i + HEADER_LENGTH];
		}	
	}
	
	public byte[] getBytes () {
		byte[] message = new byte[content.length + HEADER_LENGTH];
		message[0] = (byte)type.ordinal();
		message[1] = (byte)dir.ordinal();
		message[2] = (byte)source.ordinal();
		message[3] = (byte)dest.ordinal();
			
		
		for (int i=0; i<content.length; i++) {
			message[i + HEADER_LENGTH] = content[i];
		}
		return message;
	}
	
	public Module getSource() {
		return source;
	}
	
	public Module getDest() {
		return dest;
	}
	
	public Msg setType(Type t) {
		type = t;
		return this;
	}
	
	
	
	public Msg setSource(Module s) {
		source = s;
		return this;
	}
	
	public Msg setSource(String s) {
		source = Module.valueOf(s);
		return this;
	}
	
	public Msg setDest(Module destination) {
		dest = destination;
		return this;
	}
	
	public Msg setDest(String s) {
		dest = Module.valueOf(s);
		return this;
	}

	
	
	public String toString () {
		return dir + " " + type +  ": " + source + " -> " + dest + " (" + content[0] + ")";
	}

	public Msg setDir(Dir d) {
		dir = d;
		return this;
	}
	
	
}