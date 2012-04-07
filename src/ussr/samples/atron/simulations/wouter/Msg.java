package ussr.samples.atron.simulations.wouter;

import java.io.Serializable;

class Msg implements Serializable {
	private static final long serialVersionUID = 1L;
	enum Type {ROTATE,CONNECT,DISCONNECT,STATE, DISCOVER, FINISHED}
	enum Dir {REQ, ACK}
	

	Type type;
	private String source = "";
	private String dest = "";
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
		for (int i=0; i<10; i++) {
			if (b[i+2] > 0)
				source += (char)b[i+2];
			if (b[i+12] > 0)
				dest += (char)b[i+12];
		}
		
		content = new byte[b.length - 22];
		for (int i=0; i<b.length-22; i++) {
			content[i] = b[i + 22];
		}	
	}
	
	public byte[] getBytes () {
		byte[] message = new byte[content.length + 22];
		message[0] = (byte)type.ordinal();
		message[1] = (byte)dir.ordinal();
		for (int i=0; i<source.length() && i<10; i++) {
			message[i+2] = (byte)source.charAt(i);
		}
		for (int i=0; i<dest.length() && i<10; i++) {
			message[i+12] = (byte)dest.charAt(i);
		}
		
		
		for (int i=0; i<content.length; i++) {
			message[i + 22] = content[i];
		}
		return message;
	}
	
	public String getSource() {
		return source;
	}
	
	public Msg setType(Type t) {
		type = t;
		return this;
	}
	
	
	
	public Msg setSource(String s) {
		source = s;
		return this;
	}
	
	public Msg setDest(String destination) {
		dest = destination;
		return this;
	}

	public String getDest() {
		return dest;
	}
	
	public String toString () {
		return dir + " " + type +  ": " + source + " -> " + dest + " (" + content[0] + ")";
	}

	public Msg setDir(Dir d) {
		dir = d;
		return this;
	}
	
	
}