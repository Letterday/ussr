package ussr.samples.atron.simulations.wouter;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ussr.samples.atron.ATRONController;
import ussr.samples.atron.simulations.wouter.Msg.Dir;


public class ATRONCommController extends ATRONController {

	HashMap <String,Byte> neighbors = new HashMap <String, Byte>();
	HashMap <String,Msg> buffer = new HashMap <String,Msg>(); 
	HashSet <Integer> busy = new HashSet <Integer>();
	
	int state = 0;
	private boolean finishedForwarded;
	private String name;
	private String name_orig;
	
	public static void main( String[] args ) {
        new ATRONCommSimulator().main();
    }
	
	@Override
	public void activate() {
		setup();
		setName(super.getName());
		name_orig = name;
		assignRoles(true);
		
		
		System.out.println(getName() + "==============================");
		
	
		
	
		 while(true) {
		
			 colorize();
			if (state == 1) {
				if (getName().equals("f0")) {
					busy.add(1);
					System.out.println("START f2.");
					disconnectTarget("f2");
					System.out.println("DONE f2.");
				
					while (!isFinished ()) {
						System.out.println(getName() + ".waiting for other");
						System.out.println(busy);
						yield();
					}
					getModule().getComponent(0).setModuleComponentColor(Color.CYAN);
					nextState();
					
				}
				if (getName().equals("f0")) {
					System.out.println("START m3.");
					disconnectTarget("m3");
					System.out.println("DONE m3.");
					finished(1);
					while (state == 1) {
						System.out.println(getName() + ".waiting for new state");
						module.getSimulation().waitForPhysicsStep(false);
					}				}
				
			}
			
			if (state == 3) {
				if (getName().equals("f0")) {
					rotateDo(-90);
					nextState();
				}
			}
			
//			if (state == 3) {
//				if (getName().equals("m2")) {
//					rotateDo(90);
//					nextState();
//				}
//			}
//			
//			if (state == 4) {
//				if (getName().equals("m3")) {
//					connectTarget("f4");
//					nextState();
//				}
//			}
//			
//			
//			if (state == 2) {
//				if (getName().equals("m3")) {
//					System.out.println("START m3.");
//					disconnectTarget("f0");
//					System.out.println("DONE m3.");
//					//finished(1);
//					while (state == 1) {
//						System.out.println(getName() + ".waiting for new state");
//						yield();
//					}
//					nextState();
//				}
//				
//			}
			
			
			 yield();
		 }
	}
	
	

	private void assignRoles(boolean firstTime) {
		Map <String,String> trans = new HashMap<String,String>();
		trans.put("f0", "m1");
		trans.put("f1", "m3");
		trans.put("f2", "m2");
		trans.put("f3", "f0");
		trans.put("f4", "f1");
		trans.put("f5", "f2");
		trans.put("f6", "f3");
		
		if (firstTime) {
			trans.put("f7", "f4");
			trans.put("f8", "f5");
			trans.put("f9", "f6");
		}
		else {
			trans.put("m1", "f6");
			trans.put("m2", "f5");
			trans.put("m3", "f4");
		}
		
		for (Map.Entry<String, String> entry : trans.entrySet()) {
		   if (getName().equals(entry.getKey())) {
			   System.out.println(entry.getKey() + " renamed to " + entry.getValue());
			   setName(entry.getValue());
			   state++;
				  
			   
			   return;
		   }
		   
		}
		colorize();
	
	} 
	
	public void colorize() {
		
		if (getName().equals("m1")) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#00FFFF"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#FFFF00"));
		}
		else if (getName().equals("m2")) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#008888"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#888800"));
		}
		else if (getName().equals("m3")) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#003333"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#333300"));
		}
		else {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#0000FF"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#FF0000"));
		}
		colorizeConnectors();
	}
	
	private void colorizeConnectors() {
		module.getConnectors().get(0).setColor(Color.BLUE);
		module.getConnectors().get(1).setColor(Color.BLACK);
		module.getConnectors().get(2).setColor(Color.RED);
		module.getConnectors().get(3).setColor(Color.WHITE);
		module.getConnectors().get(4).setColor(Color.BLUE);
		module.getConnectors().get(5).setColor(Color.BLACK);
		module.getConnectors().get(6).setColor(Color.RED);
		module.getConnectors().get(7).setColor(Color.WHITE);
		
	}

	private void finished(int nr) {
		broadcast (new Msg(nr).setType(Msg.Type.FINISHED));
		
	}

	private boolean isFinished() {
		return busy.isEmpty();
	}

	public void nextState () {
		System.out.println(getName() + ".nextState(curr="+state+")");
		finishedForwarded = false;
		state++;
		broadcast (new Msg(state).setType(Msg.Type.STATE));
	}
	
	public void broadcast(Msg m) {
		for (byte c=0; c<8; c++) {
			send (m.setSource(getName()).setDest("*").getBytes(),c);
		}
	}

	public void rotateDo (int deg) {
		rotate(deg);
		while(getModule().getActuators().get(0).isActive()) {
			yield();
		}
	}
	
	
	
	public void disconnectTarget (String target) {
		System.out.println(getName() + ".disconnect " + target);
		int c = targetToConnector(target);
		if (c%2 == 0) {
			disconnect (c);
			while(isConnected(c)) {
				System.out.println(getName() + ".disconnecting " + c);
				yield();
			}
		}
		else {
			send(new Msg(0).setType(Msg.Type.DISCONNECT),target);
		}
	}
		
	private int targetToConnector (String target) {
		System.out.println(getName() + ".targetToConnector("+target+")");
		if (!neighbors.containsKey(target)) {
			System.out.println(getName() + ".neighbors.NOTcontainsKey " + target);
			send(new Msg(0).setType(Msg.Type.DISCOVER),target);
		}	
		System.out.println(getName() + ".neighbors.containsKey " + target);
		int c = neighbors.get(target);
		return c;
	}
	
	private void connectTarget(String target) {
		System.out.println(getName() + ".connect " + target);
		int c = targetToConnector(target);
		if (c%2 == 0) {
			connect (c);
			while(!isConnected(c)) {
				System.out.println(getName() + ".connecting " + c);
				yield();
			}
		}
		else {
			send(new Msg(0).setType(Msg.Type.CONNECT),target);
		}
		
	}
	
//	public byte targetToConnector (String target) {
//		return 
//	}
	
	public void setName (String n) {
		name = n;
	}
	
	public String getName () {
		return name;
	}
	
	public void connectTo (String target) {
		send(new Msg(0).setType(Msg.Type.CONNECT),target);
		
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
		System.out.println(getName() + ".send(" + bs[bs.length - 1] + ") over " + connector);
		
		sendMessage(bs, (byte)bs.length, (byte)connector);
	}

	
	
	public Msg send (Msg m, String dest) {
		m.setDest(dest);
		m.setSource(getName());

		System.out.println(getName() + ".send: " + m.toString());
		
		if (neighbors.containsKey(dest)) {
			send(m.getBytes(),neighbors.get(dest));
		}
		else {
			broadcast(m.getBytes());
		}
		
	
		if(m.dir == Msg.Dir.REQ) {

			while (!buffer.containsKey(dest)) {
				System.out.println(getName() + ".waiting for " + dest + " = " + buffer.toString());
				yield();
			}

			Msg msg = buffer.get(dest);
		
			return msg;
		}
		return null;
	}
	
	public void handleMessage(byte[] b, int messageLength, int c) {
		System.out.println(getName() + ".handleMessage(" + b[b.length-1] + ") over " + c + " orig: "+name_orig);
//		for (int i=0; i< b.length;i++)
//		System.out.print(b[i] + " ");

		receive(new Msg(b),(byte)c);
	}
	
	public void receive (Msg m, byte c) {
		System.out.println(getName() + ".receive: " + m.toString() );
		addNeighbor(m.getSource(), c);
		
		
		if (m.type == Msg.Type.STATE && state < m.content[0]) {
			// state update, so broadcast
			System.out.println(getName() + ".receive state " + m.content[0]);
			state = m.content[0];
			for (byte i=0; i<8; i++) {
				if (i != c) {
					send (m.setSource(getName()).getBytes(),i);
					
				}
			}
		}
		
		if (m.type == Msg.Type.FINISHED) {
			// state update, so broadcast
			busy.remove((byte)m.content[0]); //ERR?
			busy.clear();
			System.out.println(getName() + " " + busy + " " + m.content[0]);
			System.out.println(getName() + ".receive finished" + m.content[0]);
			if (!finishedForwarded) {
				finishedForwarded = true;
				for (byte i=0; i<8; i++) {
					if (i != c) {
						send (m.setSource(getName()).getBytes(),i);
						
					}
				}
			}
		}
		
		if (!m.getDest().equals("*") && !getName().equals(m.getDest())) {
			// this message is not for me
			return;
		}
		
		if (m.dir == Msg.Dir.ACK) {
			buffer.put(m.getSource(), m);
		}
		
		
		
		if (m.type == Msg.Type.DISCONNECT) {
			if (m.dir == Msg.Dir.REQ) {
				int c2 = neighbors.get(m.getSource());
				if (c2%2 == 0) {
					disconnect (c2);
					while(isConnected(c2)) {
						System.out.println(getName() + ".disconnectingggg " + c2 );
						// module.getSimulation().waitForPhysicsStep(false);
						yield();
						
						
					}
					send(new Msg(0).setType(Msg.Type.DISCONNECT).setDir(Dir.ACK),m.getSource());
					
				}
				else {
					System.err.println("Odd connector nr " + c2);
				}
			}
		}
		
		if (m.type == Msg.Type.CONNECT) {
			if (m.dir == Msg.Dir.REQ) {
				int c2 = neighbors.get(m.getSource());
				if (c2%2 == 0) {
					connect (c2);
					while(!isConnected(c2)) {
						System.out.println(getName() + ".connectingggg " + c2 );
						module.getSimulation().waitForPhysicsStep(false);
						
					}
					send(new Msg(0).setType(Msg.Type.CONNECT).setDir(Dir.ACK),m.getSource());
					
				}
				else {
					System.err.println("Odd connector nr " + c2);
				}
			}
		}
		
		if (m.type == Msg.Type.DISCOVER && m.dir == Msg.Dir.REQ) {
			send (new Msg(0).setDir(Msg.Dir.ACK).setType(Msg.Type.DISCOVER),m.getSource());
		}
		
	}

	private void addNeighbor(String source, byte c) {
		neighbors.put(source,c);
		
	}
	
	public void disconnect (int c) {
		System.out.println(getName() + ".disconnect("+c+")");
		super.disconnect(c);
	}
	
	
}
