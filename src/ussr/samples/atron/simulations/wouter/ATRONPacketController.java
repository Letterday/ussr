package ussr.samples.atron.simulations.wouter;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.xml.internal.bind.v2.TODO;

import ussr.model.debugging.DebugInformationProvider;
import ussr.model.debugging.SimpleWindowedInformationProvider;
import ussr.physics.PhysicsFactory;
import ussr.physics.PhysicsParameters;
import ussr.samples.atron.ATRONController;

public abstract class ATRONPacketController extends ATRONController {
	
	DebugInformationProvider info;
	HashMap<Grouping,HashMap <Module,Byte[]>> neighbors = new HashMap <Grouping, HashMap <Module, Byte[]>>();
	HashMap<Module,Color[]> moduleColors = new HashMap <Module,Color[]>();
	int state = 0;
	int angle = 0;
	boolean hasReceivedBroadcast = false;
	private Color[] structureColors;
	
	
	

	public void rotate (Module m, int degrees) {
		if (getId() == m) {
			angle = degrees + angle;
			rotateToDegreeInDegrees (degrees);				
			while (isRotating()) {yield();}
			
			nextState();
		}
			
	}
	
	public void connectionOnConn(byte cSource, boolean makeConnection) {
		if (makeConnection) connect(cSource); else disconnect(cSource);
		info.addNotification("make (dis)connection on nr");
		while (makeConnection && !isConnected(cSource) || !makeConnection && isConnected(cSource)) {waiting();}
		info.addNotification("done (dis)connection on nr");
		nextState();
	}
	
	public byte getConnectorToNb (Module nb) {
		return neighbors.get(nb.getGrouping()).get(nb)[0];
	}
	
	public byte getConnectorFromNb (Module nb) {
		return neighbors.get(nb.getGrouping()).get(nb)[1];
	}
	
	public void connection (Module m1, Module m2, boolean makeConnection) {
		if (getId() == m1) {
			info.addNotification("#connection " + m1 + "," + m2);
			keepSending (m2);
			info.addNotification("done keepsending");
			byte conToNb = getConnectorToNb(m2);
			byte conFromNb = getConnectorFromNb(m2);
			if (conToNb%2 == 0 && conFromNb%2 == 1) {
				// Male wants to connect to female. No further action needed
				if (makeConnection) connect(conToNb); else disconnect(conToNb);
				info.addNotification("make (dis)connection");
				while (makeConnection && !isConnected(conToNb) || !makeConnection && isConnected(conToNb)) {waiting();}
				info.addNotification("done (dis)connection");
				nextState();
			}
			if (conToNb%2 == 1 && conFromNb%2 == 0) {
				// Female can not connect to male, so lets wait for other module
				while (makeConnection && !isConnected(conToNb) || !makeConnection && isConnected(conToNb)) {waiting();}
			}
			if (conToNb%2 == conFromNb%2) {
				// Same connector gender. Outch, one of the hemispheres needs to rotate!
				if (conToNb < conFromNb) {
					rotate(90);
					while (isRotating()) { waiting(); }
					neighbors.clear();
					keepSending (m2);
				}
				if (makeConnection) connect(conToNb); else disconnect(conToNb);
				while (makeConnection && !isConnected(conToNb) || !makeConnection && isConnected(conToNb)) {waiting();}
				nextState();
			}
		}
	}
	
	public void waiting () {
		yield();
	}
	
	public HashMap<Module,Byte[]> getNeighbors (Grouping s) {
		if (!neighbors.containsKey(s)) {
			neighbors.put(s,new HashMap<Module,Byte[]>());
		}

		return neighbors.get(s);
	}
	
	public void addNeighbor(Module nb, int conToNb, int conFromNb) {
		getNeighbors(nb.getGrouping()).put(nb, new Byte[] {(byte)conToNb,(byte)conFromNb});
	}
	
	public byte getNbConnector (Module nb) {
		return neighbors.get(nb.getGrouping()).get(nb)[0];
	}
	
	public void disconnect (Module m1, Grouping  s) {
		info.addNotification(".Disconnect("+m1+","+s+")");
		
		int initialstate = state;
		if (m1 == getId()) {
			if (getNeighbors(s).isEmpty()) {
				broadcast (new Packet(getId(),Module.ALL).setType(Type.DISCOVER));
				delay(1000);
			}
			for (Module m2 : getNeighbors(s).keySet()) {
				connection (m1,m2,false);
				// TODO: This only works when there exists 1 neighbor, as there are no pending states yet.
				// After disconnecting 1 neighbor it continues to next state instead of waiting for all to complete
			}
		}
		else {
			broadcast (new Packet(getId(),Module.ALL).setType(Type.DISCOVER));
			delay(1000);
			// Note: We have to wait here for some time, since we can not know how much modules in the structure are connected. For sure not all of them, so a timed wait is the only solution here.

			if (getId().getGrouping() == s) {
				info.addNotification(getId().getGrouping() + " == " + s);
				if (hasNeighbor(m1)) {
					connection (getId(),m1,false);
				}
			}
			else {
				info.addNotification(getId().getGrouping() + " != " + s);
			}
		}
		
			
		
		while (state == initialstate) {yield();}
	}
	
	public void disconnect (Module m1, Module m2) {
		// info.addNotification(".disconnect("+m1+","+m2+")");
		connection (m1,m2,false);
		connection (m2,m1,false);
	}
	
	public void connect (Module m1, Module m2) {
		connection (m1,m2,true);
		connection (m2,m1,true);
	}
	
	public void connect (Module m, Grouping s) {
		// to be implemented
	}
	
	private void refresh () {
		rotateToDegreeInDegrees(angle);
	}
	
	
	
	public abstract void handleStates ();
	public abstract void setColors ();
	
	public void activate ()	{
		setup();
		info = this.getModule().getDebugInformationProvider();
		setColors();
		
		
		float t = time();
		while (true) {
			refresh();
			
			handleStates();
			yield();
		}
	}
	
	
//	public void activateOLD() {
//		setup();
//		info = this.getModule().getDebugInformationProvider();
//		
//		float t = time();
//		while (true) {
//			refresh();
//			
//			switch (state) {
//				case 0:
//					disconnect (Module.Walker_Right, Grouping.Structure);
//				break;
//				
//				case 1:
//					disconnect (Module.Structure_0,Module.Structure_2);
//				break;
//
//				case 2:
//					rotate (Module.Structure_0,90);
//				break;
//				
//				case 3:
//					rotate (Module.Walker_Left,90);
//				break;
//				
//				case 4:
//					// The call below does not work because MR does not receive messages from F3 (F3 does receive from MR (STRANGE?))
//					// connect (Module.MR,Module.F3);
//					// But connecting by specifying the connection number does work
//					// However, we want the protocol to figure out this number itself.
//					if (getId() == Module.Walker_Right) {
//						connectionOnConn ((byte)6,true);
//					}
//				break;
//				
//			
//				
//				case 5:
//					disconnect (Module.Walker_Left, Grouping.Structure);
//				break;
//					
//				case 6:
//					rotate (Module.Walker_Right, 180);
//				break;
//					
//				case 7:
//					rotate(Module.Walker_Left,90);
//				break;
//				
//				case 8:
//					// connect (Module.ML,Module.F6);
//					if (getId() == Module.Walker_Left) {
//						connectionOnConn ((byte)2,true);
//						connectionOnConn ((byte)4,true);
//						connectionOnConn ((byte)6,true);
//						connectionOnConn ((byte)0,true);
//					}
//				break;
//				
//				case 9:
//					disconnect (Module.Walker_Right,Module.Structure_3);
//				break;
//				
//				case 10:
//					rotate (Module.Walker_Right,90);
//				break;
//				
//				case 11:
//					//kill();
//				break;
//					
//			}
//
//			bus.execAt(Module.F0,1).con.disconnect(Module.MR).next();
//			bus.execAt(Module.F0,2).con.disconnect(Module.F2).next();
//			bus.execAt(Module.F0,3).rotateDegrees(90).next();
//			bus.execAt(Module.ML,4).rotateDegrees(90).next();
//			bus.execAt(Module.MR,5).con.connect(Module.F3).next();
//			bus.execAt(Module.ML,6).con.disconnect(Module.F0).next();
//			
			
			
			
//			yield();
//			
//
//			if (time() - 5 > t) {
//				broadcast(new Packet(getId(), Module.ALL).setType(Type.DISCOVER));
//				t = time();
//			}
//		}
//		
//	}
	
	public void nextState () {
		neighbors.clear();
		hasReceivedBroadcast = false;
		colorize();
		info.putStateInformation(Integer.toString(state), "");
		state++;
		broadcast(new Packet(getId(),Module.ALL).setType(Type.STATE).setData(state));
	}
	
	public float time () {
		return getModule().getSimulation().getTime();
	}

	private void keepSending(Module dest) {
		info.addNotification(".keepsending to " + dest);
		if (dest == Module.ALL) {
			System.err.println("keepsending all not allowed");
			System.exit(0);
		}
	
		
		float t = time();
		if (!hasNeighbor(dest)) {
			broadcast(new Packet(getId(), dest));
			while (!hasNeighbor(dest)) {
				waiting();
				delay(500);
			
				if (time() - 4 > t) {
					broadcast(new Packet(getId(), dest).setData(99));
				}
			}
		}
		//info.addNotification("GOT nr " + dest + ": " + neighbors.get(dest)[0] + "," + neighbors.get(dest)[1]);
	}
	
	
	private boolean hasNeighbor(Module n) {
		if (getNeighbors(n.getGrouping()).containsKey(n)) {
			info.addNotification(".hasNeighbor " + n);
			return true;
		}
		else {
			info.addNotification(".hasNeighbor " + n + " NOT");
			return false;
		}
			
	}
	
	private HashMap <Module,Byte[]>  getNbConnectors() {
		return neighbors.get(getId().getGrouping());
	}
	
	public String getNeighborsString() {
		String r = "  \n";
		
		for (Map.Entry<Grouping, HashMap<Module,Byte[]>> e : neighbors.entrySet()) {
			if (!getNeighbors(e.getKey()).isEmpty()) {
				for (Map.Entry<Module,Byte[]> f : e.getValue().entrySet()) {
					r += f.getKey() + " [" + f.getValue()[0] + ", " + f.getValue()[1] + "], ";
				}
				r = r.substring(0, r.length()-2);
			}
			r += "\n ";
		}
		return r.substring(0, r.length()-2);
	}


	public void delay(int ms) {
    	float stopTime = module.getSimulation().getTime()+ms/1000f;
    	info.addNotification("start waiting");
    	while(stopTime>module.getSimulation().getTime()) {
    		
    		yield();
    	}
    	info.addNotification("end waiting");
	}


	public Module getId() {
		return Module.valueOf(getName());
	}
	
	public void setId(Module id) {
		info.addNotification(getId() + " renamed to " + id);
		getModule().setProperty("name",id.name());
		colorize();
	}
	
	public void colorize() {
		getModule().getComponent(0).setModuleComponentColor(getColors()[0]);
		getModule().getComponent(1).setModuleComponentColor(getColors()[1]);
		
		colorizeConnectors();
	}

	public void handleMessage(byte[] message, int messageLength, int connector) {
		
		Packet p = new Packet(message);
		
		if (p.getData() == 99) {
			// Indicates that sender is waiting for an answer, but it does not come. So try a broadcast.
			broadcast(new Packet(getId(), p.getSource()).setType(Type.BROADCAST).setData(p.getSourceConnector()));
			info.addNotification("Answer to "+p.getSource()+"does not arrive, so try broadcast.");
		}
		
		if (p.getType() == Type.BROADCAST && hasReceivedBroadcast == false) {
			hasReceivedBroadcast = true;
			if (p.getDest() != getId()) {
				info.addNotification("spread broadcast|!");
				for (int i=0; i<8; i++) {
					if (i != connector) {
						send(p.getBytes(),i);
					}
				}
			}
		}
		
		
		if (p.getType() == Type.BROADCAST) {
			if (p.getDest() == getId())	{
				addNeighbor(p.getSource(), p.getData(), 1);
			}
		}
		else {
			addNeighbor (p.getSource(),connector,p.getSourceConnector());
		
			info().addNotification("rm from "+p.getSource()+"!");
			
			if (p.getSource() == getId()) {
				try {
					throw new Exception("Source cannot be myself (" + getId() + ")!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(0);
				}
			}
			
			if (p.getDest() == getId() || p.getDest() == Module.ALL) {
				if (p.getType() == Type.STATE && state < p.getData()) {
					state = p.getData();
					neighbors.clear();
					info.addNotification("New state " + state);
					for (byte c=0; c<8; c++) {
						if (c != connector)
							send (new Packet (p).setData(state).setType(Type.STATE).setDest(Module.ALL).getBytes(), c);
					}
				}
			
				if (p.getSource() != Module.ALL) {
					info.addNotification("add " + p.getSource() + " " + connector+ "," + p.getSourceConnector());
					addNeighbor(p.getSource(), connector, p.getSourceConnector());
				}
				
				info.addNotification(".handleMessage = " + p.toString() + " over " + connector);
				
				if (p.getType() == Type.DISCOVER && p.getDir() == Dir.REQ) {
					send(p.getAck().getBytes(),connector);
					
				}
			}
		}
	}
	
	

	public void broadcast (Packet p) {
		info.addNotification(".broadcast packet (" + p.toString() + ") ");
		for (byte c=0; c<8; c++) {
			send (p.getBytes(),c);
		}
	}
	
	public void send (Packet p) {
		info.addNotification(".send packet (" + p.toString() + ") ");
		byte c = getNbConnector(p.getDest());
		send(p.setSourceConnector(c).getBytes(), c);
	}
	
	
	
	public void send(byte[] bs, int connector) {
		Packet p = new Packet(bs);
		if (p.getType() != Type.BROADCAST) {
			p.setSourceConnector((byte)connector);
			p.setSource(getId());
		}
		
		info.addNotification(".send = " + p.toString() + " over " + connector);
		if (connector < 0 || connector > 7) {
			System.err.println("Connector has invalid nr " + connector);
		}
		
		sendMessage(p.getBytes(), (byte)p.getBytes().length, (byte)connector);
	}
	
	

	public DebugInformationProvider info() {
		if (info == null) {
			info = this.getModule().getDebugInformationProvider();
		}
		return info;
	}


	public int getState() {
		return state;
	}


	

	public int getAngle() {
		return angle;
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


//		for (int i=3; i<=9; i++) {
//			renameFromTo (Module.getOnNumber(Structure.Structure,i),Module.getOnNumber(Structure.Structure,i-3));
//		}
//		colorize();
		
	
	public void addStructureColors(Color[] colors) {
		structureColors = colors;
	}
	
	public void setModuleColors(Module m, Color[] colors) {
		moduleColors.put(m, colors);
	}

	
	public Color[] getColors () {
		if (moduleColors.containsKey(getId())) {
			return moduleColors.get(getId());
		}
		else {
			return structureColors;
		}
			
	}
	
	public void renameFromTo (Module from, Module to) {
		if (getId() == from) {
			setId(to);
			state++;
			return;
		}
	}

}
