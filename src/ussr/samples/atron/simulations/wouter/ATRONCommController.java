package ussr.samples.atron.simulations.wouter;


import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import ussr.samples.atron.ATRONController;
import ussr.samples.atron.simulations.wouter.Msg.Dir;
import ussr.samples.atron.simulations.wouter.Msg.Type;


class Msg implements Serializable {
	private static final long serialVersionUID = 1L;
	enum Type {ROTATE,STATE}
	enum Dir {REQ, ACK}
	

	Type type;
	private char source;
	private char dest;
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
		source = (char)b[2];
		dest = (char)b[3];
		content = new byte[b.length - 4];
		for (int i=0; i<b.length-4; i++) {
			content[i] = b[i + 4];
		}	
	}
	
	public byte[] getBytes () {
		byte[] message = new byte[content.length + 4];
		message[0] = (byte)type.ordinal();
		message[1] = (byte)dir.ordinal();
		message[2] = (byte)source;
		message[3] = (byte)dest;
		for (int i=0; i<content.length; i++) {
			message[i+4] = content[i];
		}
		return message;
	}
	
	public char getSource() {
		return source;
	}
	
	public Msg setType(Type t) {
		type = t;
		return this;
	}
	
	
	
	public Msg setSource(char s) {
		source = s;
		return this;
	}
	
	public Msg setDest(char destination) {
		dest = destination;
		return this;
	}

	public char getDest() {
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

public class ATRONCommController extends ATRONController {

	HashMap <Byte,Byte> neighbors = new HashMap <Byte, Byte>();
	HashMap <Byte,Msg> buffer = new HashMap <Byte,Msg>(); 
	
	int state = 1;
	
	public static void main( String[] args ) {
        new ATRONCommSimulator().main();
    }
	
	@Override
	public void activate() {
		setup();
		
		
		 while(true) {
		
			module.getConnectors().get(0).setColor(Color.RED);
			module.getConnectors().get(1).setColor(Color.BLACK);
			module.getConnectors().get(2).setColor(Color.BLUE);
			module.getConnectors().get(3).setColor(Color.WHITE);
			module.getConnectors().get(4).setColor(Color.RED);
			module.getConnectors().get(5).setColor(Color.BLACK);
			module.getConnectors().get(6).setColor(Color.BLUE);
			module.getConnectors().get(7).setColor(Color.WHITE);
			 
			
			
			if (state == 1) {
				if (getName () == "M") {
					if (send(new Msg(1).setType(Msg.Type.ROTATE),'R').content[0] == 99) {
						System.out.println("I will rotate!");
						rotateDo (90);
						while (!getModule().getActuators().get(0).isActive()) {
							yield();
						}
						System.out.println("DONE.");
						state++;
						broadcast (new Msg(state).setType(Msg.Type.STATE));
					}
					else {
						//send(new Msg(2).setType(Msg.Type.ROTATE),'R');
						System.out.println("I will NOT rotate!");
					}
					
				}
				
				
			}
			if (state == 2) {
				System.out.println(getName() + " state2");
				state++;
			}
			
			if (state == 3) {
				if (getName () == "L") {
					getModule().getComponent(0).setModuleComponentColor(Color.yellow);
					state++;
					broadcast (new Msg(state).setType(Msg.Type.STATE));
				}
				
			}
			
			
			 yield();
		 }
	}
	
	public void broadcast(Msg m) {
		for (byte c=0; c<8; c++) {
			send (m.setSource(getName().charAt(0)).setDest('*').getBytes(),c);
		}
	}

	public void rotateDo (int deg) {
		rotate(deg);
		while(getModule().getActuators().get(0).isActive()) {
			yield();
		}
	}
	
	public void broadcast (byte[] bs) {
		System.out.println(getName() + ".broadcast(" + bs[0] + ") ");
		for (byte c=0; c<8; c++) {
			send (bs,c);
		}
	}
	
	public void send(byte[] bs, int connector) {
		if (connector < 0 || connector > 7) {
			System.err.println("Connector has invalid nr " + connector);
		}
		System.out.println(getName() + ".send(" + bs[0] + ") over " + connector);
		
		sendMessage(bs, (byte)bs.length, (byte)connector);
	}

	
	
	public Msg send (Msg m, char dest) {
		m.setDest(dest);
		m.setSource(getName().charAt(0));

		System.out.println(getName() + ".send: " + m.toString());
		
		if (neighbors.containsKey((byte)dest)) {
			send(m.getBytes(),neighbors.get((byte)dest));
		}
		else {
			broadcast(m.getBytes());
		}
		
	
		if(m.dir == Msg.Dir.REQ) {

			while (!buffer.containsKey((byte)dest)) {
				yield();
				System.out.println(getName() + ".waiting + " + buffer.toString());
			}

			Msg msg = buffer.get((byte)dest);
		
			return msg;
		}
		return null;
	}
	
	public void handleMessage(byte[] b, int messageLength, int c) {
		System.out.println(getName() + ".handleMessage(" + b.length + ") over " + c);
		

		receive(new Msg(b),(byte)c);
	}
	
	public void receive (Msg m, byte c) {
		System.out.println(getName() + ".receive: " + m.toString() );
		addNeighbor((byte)m.getSource(), c);
		if (m.getDest() != '*' && getName().charAt(0) != (m.getDest())) {
			return;
		}
		System.out.println(m.type);
		if (m.type == Msg.Type.STATE && state != m.content[0]) {
			System.out.println(getName() + ".receive state" + m.content[0]);
			state++;
			for (byte i=0; i<8; i++) {
				if (i != c) {
					send (m.setSource(getName().charAt(0)).getBytes(),i);
					
				}
			}
		}
		
		if (m.dir == Msg.Dir.ACK) {
			buffer.put((byte)m.getSource(), m);
		}
		
		if (state == 1){
			if (getName () == "R") {
	
				if (m.getSource() == 'M') {
					
					send(new Msg(99).setType(Msg.Type.ROTATE).setDir(Msg.Dir.ACK),'M');
				}
			}
		}
		
	}

	private void addNeighbor(byte source, byte c) {
		neighbors.put(source,c);
		
	}
	
	
	
}
