package ussr.samples.atron.simulations.wouter;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import ussr.model.debugging.DebugInformationProvider;
import ussr.samples.atron.ATRONController;

public class ATRONPacketController extends ATRONController {
	
	DebugInformationProvider info;
	HashMap <Module,Byte[]> neighbors = new HashMap <Module, Byte[]>();
	int state = 0;
	int angle = 0;
	
	public static void main(String[] args) {
		
		ATRONPacketSimulation.main(args);
	}

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
	
	public void connection (Module m1, Module m2, boolean makeConnection) {
		if (getId() == m1) {
			keepSending (m2);
			info.addNotification("done keepsending");
			byte cSource = neighbors.get(m2)[0];
			byte cDest = neighbors.get(m2)[1];
			if (cSource%2 == 0 && cDest%2 == 1) {
				// Male wants to connect to female. No further action needed
				if (makeConnection) connect(cSource); else disconnect(cSource);
				info.addNotification("make (dis)connection");
				while (makeConnection && !isConnected(cSource) || !makeConnection && isConnected(cSource)) {waiting();}
				info.addNotification("done (dis)connection");
				nextState();
			}
			if (cSource%2 == 1 && cDest%2 == 0) {
				// Female can not connect to male, so lets wait for other module
				while (makeConnection && !isConnected(cSource) || !makeConnection && isConnected(cSource)) {waiting();}
			}
			if (cSource%2 == cDest%2) {
				// Same connector gender. Outch, one of the hemispheres needs to rotate!
				if (cSource < cDest) {
					rotate(90);
					while (isRotating()) { waiting(); }
					neighbors.clear();
					keepSending (m2);
				}
				if (makeConnection) connect(cSource); else disconnect(cSource);
				while (makeConnection && !isConnected(cSource) || !makeConnection && isConnected(cSource)) {waiting();}
				nextState();
			}
		}
	}
	
	public void waiting () {
		yield();
	}
	
	public void disconnect (Module m1, Module m2) {
		connection (m1,m2,false);
		connection (m2,m1,false);
	}
	
	public void connect (Module m1, Module m2) {
		connection (m1,m2,true);
		connection (m2,m1,true);
	}
	
	public void activate() {
		setup();
		info = this.getModule().getDebugInformationProvider();
		
		assignRoles(true);
		

		while (true) {
			
			switch (state) {
				case 0:
					disconnect (Module.F0,Module.MR);
				break;
				
				case 1:
					disconnect (Module.F0,Module.F2);
				break;
				
				case 2:
					rotate (Module.F0,90);
				break;
				
				case 3:
					rotate (Module.ML,90);
				break;
				
				case 4:
					// The call below does not work because MR does not receive messages from F3 (F3 does receive from MR (STRANGE?))
					// connect (Module.MR,Module.F3);
					// But connecting by specifying the connection number does work
					// However, we want the protocol to figure out this number itself.
					if (getId() == Module.MR) {
						connectionOnConn ((byte)6,true);
					}
				break;
				
				case 5:
					disconnect (Module.ML,Module.F0);
				break;
					
				case 6:
					rotate (Module.MR,180);
				break;
					
				case 7:
					rotate(Module.ML,-90);
				break;
				
				case 8:
					// connect (Module.ML,Module.F6);
					if (getId() == Module.ML) {
						connectionOnConn ((byte)2,true);
						connectionOnConn ((byte)4,true);
						connectionOnConn ((byte)6,true);
						connectionOnConn ((byte)0,true);
					}
				break;
				
				case 9:
					disconnect (Module.MR,Module.F3);
				break;
				
				case 10:
					rotate (Module.MR,90);
				break;
			}
//
//			bus.execAt(Module.F0,1).con.disconnect(Module.MR).next();
//			bus.execAt(Module.F0,2).con.disconnect(Module.F2).next();
//			bus.execAt(Module.F0,3).rotateDegrees(90).next();
//			bus.execAt(Module.ML,4).rotateDegrees(90).next();
//			bus.execAt(Module.MR,5).con.connect(Module.F3).next();
//			bus.execAt(Module.ML,6).con.disconnect(Module.F0).next();
//			
			colorize();
			yield();
			rotateToDegreeInDegrees(angle);
		}
		
	}
	
	public void nextState () {
		state++;
		neighbors.clear();
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
		if (!neighbors.containsKey(dest)) {
			broadcast(new Packet(getId(), dest));
			while (!neighbors.containsKey(dest)) {
				waiting();
	//			delay(100);
				if (time() - 10 > t) {
					broadcast(new Packet(getId(), dest));
					t = time();
					
				}
			}
		}
		info.addNotification("GOT nr " + dest + ": " + neighbors.get(dest)[0] + "," + neighbors.get(dest)[1]);
	}
	
	public void delay(int ms) {
    	float stopTime = module.getSimulation().getTime()+ms/1000f;
    	while(stopTime>module.getSimulation().getTime()) {
    		
    		yield();
    	}
	}


	public Module getId() {
		return Module.valueOf(getName());
	}
	
	public void setId(Module id) {
		info.addNotification(getId() + " renamed to " + id);
		getModule().setProperty("name",id.name());
	}
	
	public void colorize() {
		
		if (getId() == Module.MC) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#00FFFF"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#FFFF00"));
		}
		else if (getId() == Module.ML) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#008888"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#888800"));
		}
		else if (getId() == Module.MR) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#003333"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#333300"));
		}
		else {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#0000FF"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#FF0000"));
		}
		colorizeConnectors();
	}

	public void handleMessage(byte[] message, int messageLength, int connector) {
		
		Packet p = new Packet(message);
		neighbors.put(p.getSource(), new Byte[]{(byte)connector,p.getSourceConnector()});
		info.addNotification("rm from "+p.getSource()+"!");
		
		if (p.getSource() == getId()) {
			System.err.println("Source cannot be myself!");
			System.exit(0);
			return;
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
				neighbors.put(p.getSource(), new Byte[]{(byte)connector,p.getSourceConnector()});
			}
			
			info.addNotification(".handleMessage = " + p.toString() + " over " + connector);
			
			if (p.getType() == Type.DISCOVER && p.getDir() == Dir.REQ) {
				System.out.println(p.getAck());
				send(p.getAck().getBytes(),connector);
				
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
		byte c = neighbors.get(p.getDest())[0];
		send(p.setSourceConnector(c).getBytes(), c);
	}
	
	
	
	public void send(byte[] bs, int connector) {
		Packet p = new Packet(bs);
		p.setSourceConnector((byte)connector);
		p.setSource(getId());
		
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


	public HashMap <Module,Byte[]>  getNeighbors() {
		return neighbors;
	}
	
	public String getNeighborsString() {
		String r = "  \n";
		
		for (Map.Entry<Module, Byte[]> e : neighbors.entrySet()) {
			r += e.getKey() + "[" + e.getValue()[0] + "," + e.getValue()[1] + "], "; 
		}
		return r.substring(0, r.length()-2);
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

	private void assignRoles(boolean firstTime) {
		
		Map <Module,Module> trans = new HashMap<Module,Module>();
		trans.put(Module.F0, Module.MC);
		trans.put(Module.F1, Module.MR);
		trans.put(Module.F2, Module.ML);
		trans.put(Module.F3, Module.F0);
		trans.put(Module.F4, Module.F1);
		trans.put(Module.F5, Module.F2);
		trans.put(Module.F6, Module.F3);
	
		
		if (firstTime) {
			trans.put(Module.F7, Module.F4);
			trans.put(Module.F8, Module.F5);
			trans.put(Module.F9, Module.F6);
		}
		else {
			trans.put(Module.F1, Module.F6);
			trans.put(Module.F2, Module.F5);
			trans.put(Module.F3, Module.F4);
			
		}
		
		for (Map.Entry<Module, Module> entry : trans.entrySet()) {
		   if (getId() == entry.getKey()) {
			   setId(entry.getValue());
			
			   return;
		   }
		}

		colorize();
		
	} 

}
