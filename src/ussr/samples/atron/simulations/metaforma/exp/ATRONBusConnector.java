package ussr.samples.atron.simulations.metaforma.exp;
//package ussr.samples.atron.simulations.metaforma.experimental;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
//
//
//public class ATRONBusConnector {
//	ATRONBus bus;
//	ATRONMetaModuleController ctrl;
//	
//	Set<Byte> connecting = new HashSet<Byte>();
//	Set<Byte> disconnecting = new HashSet<Byte>();
//	private Map<Module, Byte> neighbors = new HashMap<Module, Byte>();
//	
//	
//	public ATRONBusConnector(ATRONBus b) {
//		bus = b;
//		ctrl = b.ctrl;
//	}
//	
//	public ATRONBus connect (byte c, boolean rotateAllowed) {
//		if (!bus.checkForActiveModule()) return bus;
//		connectConnector(c);
//		return bus;
//	}
//	
//	public int getConnectionCount() {
//		return getConnectionCount(HemisphereType.NORTH) + getConnectionCount(HemisphereType.SOUTH);
//	}
//	
//	public int getConnectionCount (HemisphereType type) {
//		int connections = 0;
//		if (type == HemisphereType.NORTH) {
//			for (int i = 0;i<4; i++) {
//				if (ctrl.getModule().getConnectors().get(i).isConnected()) {
//					connections++;
//				}
//			}
//		}
//		else {
//			for (int i = 4;i<8; i++) {
//				if (ctrl.getModule().getConnectors().get(i).isConnected()) {
//					connections++;
//				}
//			}
//		}
//		
//		return connections;
//	}
//	
//	
//	
//	public ATRONBus disconnect (byte c) {
//		if (!bus.checkForActiveModule()) return bus;
//		
//		disconnectConnector(c);
//		
//		return bus;
//	}
//	
//	public ATRONBus disconnect (Module id) {
//		if (!bus.checkForActiveModule()) return bus;
//		
//		if (!neighbors.containsKey(id)) {
//			findConnector(id);
//		}
//		
//		
//		float t = bus.time();
//		while (!neighbors.containsKey(id)) {
//			ctrl.yield();
//			if (bus.time() - 3 > t) {
//				findConnector(id);
//				t = bus.time();
//			}
//		}
//		
//		byte c = neighbors.get(id);
//		
//		if (!disconnecting.contains(c)) {
//			ctrl.info.addNotification("#DISCONNECT " + id);
//			disconnecting.add(c);
//			if (c%2 == 0) {
//				disconnect(c);
//			} 
//			else {
//				bus.send(new Msg(1).setType(ussr.samples.atron.simulations.metaforma.generated.library.DISCONNECT),id);
//			}	
//		}
//		//disconnectConnector(c);
//		
//		
//		return bus;
//	}
//	
//	public ATRONBus connect (Module id) {
//		if (!bus.checkForActiveModule()) return bus;
//		ctrl.info.addNotification("#CONNECT " + id);
//		if (!neighbors.containsKey(id)) {
//			findConnector(id);
//		}
//		int i=0;
//		float t = bus.time();
//		while (!neighbors.containsKey(id)) {
//			ctrl.yield();
//			if (bus.time() - 8 > t) {
//				findConnector(id);
//				t = bus.time();
//				i++;
//			}
//		}
//		
//		ctrl.info.addNotification("have connector, connect " + id);
//		
//		connectConnector(neighbors.get(id));
//		 
//		return bus;
//	}
//	
//	private void disconnectConnector(byte c) {
//		ctrl.info.addNotification(".disconnectConnector " + c);
//		
//		if (c%2==1) {
//			//bus.send(new Msg(1).setType(Msg.Type.DISCONNECT), neigh);
//			
////			bus.send (new byte[]{
////					MessageType.MSG_DISCONNECT.ord(),
////					bus.getId().ord()
////					},	c);
//			ctrl.info.addNotification("Disconnecting odd " + c);
//			while (isConnected(c)) {ctrl.yield();}
//		}
//			
//		
//		if (!isConnected(c)) {
//			ctrl.info.addNotification(" disconnecting.remove " + c);
//			disconnecting.remove(c);
//			neighbors.values().remove(c);
//			
//		}
//		else {
//			if (!disconnecting.contains(c)) {
//				ctrl.info.addNotification(" disconnecting.add " + c);
//				ctrl.getModule().getConnectors().get(c).disconnect();
//				disconnecting.add(c);
//			}
//			
//		}
//		
//	}
//	
//	private void connectConnector (byte c) {
//		if (isConnected(c)) {
//			connecting.remove(c);
//			//bus.printTask(Task.CONNECT, c);
//		}
//		else
//		{
//			if (c%2==0) {	
//				if (ctrl.canConnect(c)) {
//					// Male connector, can connect immediately
//					ctrl.info.addNotification("Can connect immediately, no rotation needed");
//					ctrl.connect(c);
//					connecting.add(c);
//				}
//				else {
//					// Male connector, first rotate, then connect
//					
//					if (bus.canRotateFree()) {
//						ctrl.info.addNotification("Can not connect, so rotating");
//						bus.rotateDegrees(90);
//						
//						while (bus.isRotating()) {ctrl.yield();}
//						if (ctrl.canConnect(c)) {
//							ctrl.connect(c);
//							connecting.add(c);
//						}
//						else {
//							ctrl.info.addNotification("No possibility to connect!!!");
//						}
//					}
//					else {
//						if (!connecting.contains(c)) {
//							ctrl.info.addNotification("Can connect but not rotate! Asking other to rotate!");
//							for (int i=0;i<8;i++)
//								//send(new byte[]{MessageType.MSG_REQ_CANROTATE.ord()}, (byte)1, (byte)4);
//								//send(new byte[]{MessageType.MSG_REQ_CANROTATE.ord()}, (byte)1, (byte)5);
////								bus.send(new byte[]{MessageType.MSG_REQ_CANROTATE.ord(),(byte)bus.getId().ordinal()}, (byte)i);
//					
//							connecting.add(c);
//						}
//					}
//					
//				}
//				
//			}
//			else {
//				// Female connector
////				bus.send(new byte[]{MessageType.MSG_CONNECT.ord(),(byte)bus.getId().ordinal()}, (byte)c);
//			}
//			
//		}
//		
//	}
//	
//	public void findConnector (Module id) {
//		bus.broadcast(new Msg(1).setType(ussr.samples.atron.simulations.metaforma.generated.library.DISCOVER));
//	}
//	
//	
//	public boolean isConnected (int c) {
//		return ctrl.getModule().getConnectors().get(c).isConnected();
//	}
//
//	public void setNeighbors(Map<Module, Byte> neighbors) {
//		this.neighbors = neighbors;
//	}
//
//	public Map<Module, Byte> getNeighbors() {
//		return neighbors;
//	}
//
//	
//
//	public boolean isDisconnecting() {
//		return !disconnecting.isEmpty();
//	}
//
//	public boolean isConnecting() {
//		return !connecting.isEmpty();
//	}
//	
//	public Set<Byte> getConnecting () {
//		return connecting;
//	}
//	
//	public Set<Byte> getDisconnecting () {
//		return disconnecting;
//	}
//
//	public void addNeighbor (Module id, byte c) {
//		ctrl.info.addNotification("add nb " + id + " on connector " + c);
//		neighbors.put(id, c);
//	}
//
//	public void clearNeighbors() {
//		neighbors.clear();
//		
//	}
//
//	public Module getNeighbor(byte connector) {
////		for (Entry<Module, Byte> entry : neighbors.entrySet()) {
////            if (entry.getValue().equals(connector)) {
////                return entry.getKey();
////            }
////        }
//		return null;
//	}
//	
//	
//}
