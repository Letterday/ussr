package ussr.samples.atron.simulations.wouter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import ussr.samples.atron.ATRONController;

public class ATRONBusConnector {
	ATRONBus bus;
	ATRONController ctrl;
	
	private Set<Byte> connecting = new HashSet<Byte>();
	private Set<Byte> disconnecting = new HashSet<Byte>();
	private Map<String, Byte> neighbors = new HashMap<String, Byte>();
	
	
	public ATRONBusConnector(ATRONBus b) {
		bus = b;
		ctrl = b.ctrl;
	}
	
	public ATRONBus connect (byte c, boolean rotateAllowed) {
		if (!bus.checkForActiveModule()) return bus;
		connectIt(c);
		return bus;
	}
	
	public int getConnectionCount() {
		return getConnectionCount(HemisphereType.NORTH) + getConnectionCount(HemisphereType.SOUTH);
	}
	
	public int getConnectionCount (HemisphereType type) {
		int connections = 0;
		if (type == HemisphereType.NORTH) {
			for (int i = 0;i<4; i++) {
				if (ctrl.getModule().getConnectors().get(i).isConnected()) {
					connections++;
					bus.print.limited(4054 + i+type.ordinal(),ATRONBusPrinter.ACTION , "isConnected = " + i);
				}
			}
		}
		else {
			for (int i = 4;i<8; i++) {
				if (ctrl.getModule().getConnectors().get(i).isConnected()) {
					connections++;
					bus.print.limited(4054 + i+type.ordinal(),ATRONBusPrinter.ACTION,"isConnected = " + i);
				}
			}
		}
		
		return connections;
	}
	
	
	
	public ATRONBus disconnect (byte c) {
		if (!bus.checkForActiveModule()) return bus;
		
		disconnectIt(c);
		
		return bus;
	}
	
	public ATRONBus disconnect (String module) {
		if (!bus.checkForActiveModule()) return bus;
		if (!neighbors.containsKey(module)) {
			findConnector(module);
		}
		while (!neighbors.containsKey(module)) {ctrl.yield();bus.print.state();bus.print.limited(9702, ATRONBusPrinter.WARNING, "waiting for " + module);}

		
		disconnectIt(neighbors.get(module));
		
		return bus;
	}
	
	public ATRONBus connect (String module) {
		if (!bus.checkForActiveModule()) return bus;
		if (!neighbors.containsKey(module)) {
			findConnector(module);
		}
		
		while (!neighbors.containsKey(module)) {
			ctrl.yield();
			bus.print.limited(9702, ATRONBusPrinter.WARNING, "waiting for " + module);
			} 
		
		connectIt(neighbors.get(module));
		 
		return bus;
	}
	
	public void disconnectIt(byte c) {
		bus.print.limited(3425,ATRONBusPrinter.ALL,"Disconnecting " + c);
		
		if (c%2==1) {
			bus.send (new byte[]{
					MessageType.MSG_DISCONNECT.ord(),
					bus.getID()
					},	c);
			bus.print.limited(34264,ATRONBusPrinter.ALL," DISCONNECTing " + c);
			while (isConnected(c)) {ctrl.yield();}
		}
			
		
		if (!isConnected(c)) {
			bus.print.limited(3426,ATRONBusPrinter.ALL," YET " + c);
			disconnecting.remove(c);
			bus.printTask(Task.DISCONNECT, c);
			neighbors.values().remove(c);
			
		}
		else {
			if (!disconnecting.contains(c)) {
				ctrl.getModule().getConnectors().get(c).disconnect();
				disconnecting.add(c);
				bus.print.print(ATRONBusPrinter.ALL,"NOT YET " + c);
			}
			
		}
		
	}
	
	public void connectIt(byte c) {
		if (isConnected(c)) {
			getConnecting().remove(c);
			bus.printTask(Task.CONNECT, c);
		}
		else
		{
			if (c%2==0) {	
				if (ctrl.canConnect(c)) {
					// Male connector, can connect immediately
					bus.print.limited(1,ATRONBusPrinter.ACTION,"Can connect immediately, no rotation needed");
					ctrl.connect(c);
					getConnecting().add(c);
				}
				else {
					// Male connector, first rotate, then connect
					
					if (bus.canRotateFree()) {
						bus.print.limited(123,ATRONBusPrinter.ACTION,"Can not connect, so rotating");
						bus.rotateDegrees(90);
						
						while (bus.isRotating()) {ctrl.yield();}
						if (ctrl.canConnect(c)) {
							ctrl.connect(c);
							getConnecting().add(c);
						}
						else {
							bus.print.limited(5,ATRONBusPrinter.WARNING,"No possibility to connect!");
						}
					}
					else {
						if (!getConnecting().contains(c)) {
							bus.print.limited(853,ATRONBusPrinter.ACTION,"Can connect but not rotate! Asking other to rotate!");
							for (int i=0;i<8;i++)
								//send(new byte[]{MessageType.MSG_REQ_CANROTATE.ord()}, (byte)1, (byte)4);
								//send(new byte[]{MessageType.MSG_REQ_CANROTATE.ord()}, (byte)1, (byte)5);
								bus.send(new byte[]{MessageType.MSG_REQ_CANROTATE.ord(),bus.getID()}, (byte)i);
					
							getConnecting().add(c);
						}
					}
					
				}
				
			}
			else {
				// Female connector
				bus.send(new byte[]{MessageType.MSG_CONNECT.ord(),bus.getID()}, (byte)c);
			}
			
		}
		
	}
	
	public void findConnector (String module) {
		for (byte c=0; c<8; c++) {
				bus.send (new byte[]{
						MessageType.MSG_REQ_WHICHMODULE.ord(),
						bus.getID()
						},	c);
		}
	}
	
	

	public void disconnect (String module, byte c) {
		if (!bus.checkForActiveModule()) return;
		if (module.startsWith("!")) {
			if (!bus.getName().contains(module.substring(1))) {
				disconnect(c);
			}
		}
		else
		{
			if (bus.getName().contains(module)) {
				disconnect(c);
			}
		}
	}
	
	public boolean isConnected (int c) {
		return ctrl.getModule().getConnectors().get(c).isConnected();
	}

	public void setNeighbors(Map<String, Byte> neighbors) {
		this.neighbors = neighbors;
	}

	public Map<String, Byte> getNeighbors() {
		return neighbors;
	}

	public void setDisconnecting(Set<Byte> disconnecting) {
		this.disconnecting = disconnecting;
	}

	public Set<Byte> getDisconnecting() {
		return disconnecting;
	}

	public void setConnecting(Set<Byte> connecting) {
		this.connecting = connecting;
	}

	public Set<Byte> getConnecting() {
		return connecting;
	}

	
}
