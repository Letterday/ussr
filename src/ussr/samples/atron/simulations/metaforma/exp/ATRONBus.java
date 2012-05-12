package ussr.samples.atron.simulations.metaforma.exp;
//package ussr.samples.atron.simulations.metaforma.experimental;
//import ussr.samples.atron.simulations.metaforma.generated.*;
//
//import java.awt.Color;
//import java.util.HashMap;
//
//import com.jmex.model.collada.schema.common_color_or_texture_type;
//
//import ussr.physics.PhysicsSimulation;
//
//enum MessageType {MSG_STATECHANGE, MSG_REQ_CANROTATE, MSG_ACK_CANROTATE, MSG_ACK_CANNOTROTATE, MSG_CONNECT, MSG_ACK_WHICHMODULE, MSG_REQ_WHICHMODULE, MSG_DISCONNECT;
//	public byte ord (){return (byte)this.ordinal();}}
//enum HemisphereType {NORTH, SOUTH}
//enum TransmissionState {NEUTRAL, WAITING_CONNECT }
//enum Task {ROTATE, CONNECT, DISCONNECT}
//
//
//
//
//
//public class ATRONBus {
//	ATRONBusPrinter print;
//	ATRONMetaModuleController ctrl;
//	public ATRONBusConnector con;
//	
//	int state;
//	
//	
//	private int execAtState;
//	private Module execAtId;
//	private int angle = 0;
//	
//	private int destination;
//	TransmissionState transmissionState = TransmissionState.WAITING_CONNECT;
//	HashMap <Module,Msg> buffer = new HashMap <Module,Msg>(); 
//	private Task currentTask;
//	
//	float baseTime;
//	
//	public ATRONBus (ATRONMetaModuleController c) {
//		ctrl = c;
//		print = new ATRONBusPrinter(this,255);
//		con = new ATRONBusConnector(this);
//	}
//
//	
//	public void maintainPosition () {
//		ctrl.rotateToDegreeInDegrees(angle);
//	}
//	
//	public float time () {
//		return ctrl.getModule().getSimulation().getTime();
//	}
//	public boolean canRotateFree () {
//		return con.getConnectionCount (HemisphereType.NORTH) == 0 || con.getConnectionCount (HemisphereType.SOUTH) == 0;
//	}
//	
//	
//	
//	public void next () {
//		if (!checkForActiveModule()) return;
//		if (!con.isDisconnecting() && !con.isConnecting() && !isRotating()) {
//			nextState();
//		}
//		else {
//			
//			
//		}
//	}
//	
//	public boolean isRotating() {
//		return ctrl.getModule().getActuators().get(0).isActive();
//	}
//	
//
//	public void setId(Module id) {
//		ctrl.info.addNotification(getId() + " renamed to " + id);
//		ctrl.getModule().setProperty("name",id.name());
//	}
//	
//	
//	
//	public void nextState () {
//		System.out.println(getName() + ".nextState(curr="+state+")");
//		state++;
//		broadcast (new Msg(state).setType(Type.STATE));
//	}
//	
//	
//	
//	
//	private void send(byte[] bs, byte connector) {
//		
//		
////		if (!ctrl.isObjectNearby(connector) && MessageType.MSG_STATECHANGE.ord() != bs[0]) {
////			System.err.println("no neighbor at " + connector);
////		}
//		ctrl.sendMessage(bs, (byte)bs.length, connector);
//	}
//	
//	
//	
//	
//	public int getState () {
//		return state;
//	}
//	
//	
//	
//
//	public ATRONBus rotateDegrees(int d) {
//		// setMaintainRotationalJointPositions
//		if (!checkForActiveModule() ) return this;
//		if (con.getConnectionCount() > 2) {
//			print.limited(3341, ATRONBusPrinter.WARNING, "More than 2 connected while rotating!");
//		}
//		if (!isRotating() && d != destination) {
//			ctrl.rotateToDegreeInDegrees(d + angle);
//			angle = d + angle;
//			destination = d;
//			ctrl.info.addNotification("Rotating to " + d);
//		}
//		
//		return this;
//	}
//	
//	
//	
//	
//
//	public void rotateToDegreeInDegrees(int d) {
//		if (!checkForActiveModule()) return;
//		ctrl.rotateToDegreeInDegrees(d);
//		
//		
//	}
//	
//	public Module getId() {
//		return Module.valueOf(getName());
//	}
//	
//
//	public boolean checkForActiveModule() {
//		return getId() == execAtId && state == execAtState;
//	}
//
//	public ATRONBus markModule() {
//		if (!checkForActiveModule()) return this;
//		ctrl.getModule().getComponent(0).setModuleComponentColor(Color.decode("#6666FF"));
//		ctrl.getModule().getComponent(1).setModuleComponentColor(Color.decode("#FF6666"));
//		return this;
//	}
//
//	public ATRONBus execAt(Module id, int state) {
//		this.execAtId = id;
//		this.execAtState = state;
//		return this;
//	}
//
//	public String getName() {
//		return ctrl.getModule().getProperty("name").toUpperCase();
//	}
//
//	public PhysicsSimulation getSimulation() {
//		return ctrl.getModule().getSimulation();
//	}
//
//	public ATRONBusPrinter getPrint() {
//		return print;
//	}
//
//	public void initBaseTime() {
//		baseTime = time();
//		
//	}
//	
//	/*
//	public void handleMessage(byte[] message, int messageLength, byte connector) {
//		
//		if (message[0] == MessageType.MSG_STATECHANGE.ord()) {
//			print.print(ATRONBusPrinter.STATE_MESSAGE, ".received " + MessageType.values()[message[0]].toString() + " along " + connector + " (from " + Module.values()[message[1]] + ")");
//		} else {
//			ctrl.info().addNotification(".received " + MessageType.values()[message[0]].toString() + " along " + connector + " (" + Module.values()[message[1]] + ")");
//			con.addNeighbor(Module.values()[message[1]], connector);
//		}
//		
//        switch (MessageType.values()[message[0]]) {
//        	case MSG_STATECHANGE:
//        		
//        		if (state == message[3])
//                {
//        			// Neighborhood is likely to change at next state!
//        			con.clearNeighbors();
//                	// printStateSwitch(message[1]);
//                	
//                	if (state != message[2]) {
//                		state = message[2];
//                		//print.print(ATRONBusPrinter.STATE_UPDATE, ".state=" + state);
//        	        	for(byte c=0; c<8; c++) 
//        	        	{
//        	        		//System.out.println("c="+connector);
//        	        		if (c != connector)
//        	        			send(message, c);
//                		}
//                	}
//                }
////                else
////                	System.err.println("State mismatch in " + ctrl.getName() + "! " + State.toString(state) + " != " + State.toString(message[1]));
////                
//               
//            break;
//        		
//        	case MSG_REQ_WHICHMODULE:
//        		send(new byte[]{MessageType.MSG_ACK_WHICHMODULE.ord(),getId().ord()},connector);
//        		 
//        	break;  
//        	
//        	case MSG_ACK_WHICHMODULE:
//        		con.getNeighbors().put(Module.values()[message[1]], connector);
//        	break;  
//        		
//        	case MSG_REQ_CANROTATE:
//        		if (canRotateFree()) {
//        			ctrl.info.addNotification("Request for rotating - GRANTED");
//    				rotateDegrees(90);
//    				while (isRotating()) {ctrl.yield();}
//    				
//    				if (connector%2 == 0) {
//    					ctrl.connect(connector);
//    				}
//    				else {
//    					send(new byte[]{MessageType.MSG_ACK_CANROTATE.ord(),getId().ord()},connector);
//    				}
//    			}
//    			else {
//    				ctrl.info.addNotification("Request for rotating - DENIED");
//    				send(new byte[]{MessageType.MSG_ACK_CANNOTROTATE.ord(),getId().ord()},connector);
//    			}
//        		
//        	break;
//        		
//        	case MSG_ACK_CANROTATE:	
//        		//con.connect(connector);
//        	break;
//        	
//        	case MSG_CONNECT:	
//        		con.connectConnector(connector);
//        		con.connecting.remove(connector);
//        		nextState(); 
//        		
//        	break;
//        	
//        	case MSG_DISCONNECT:	
//        		con.disconnectConnector(connector);
//        		con.disconnecting.remove(connector);
//        		nextState(); 
//        	break;
//        	
//        	// Request to rotate
//        	
//        }
//		
//    }
//*/
//
//	public void receive(Msg m, byte connector) {
//		if (m.type == ussr.samples.atron.simulations.metaforma.generated.library.DISCOVER && m.dir == Msg.Dir.REQ) {
//			send (new Msg(0).setDir(Msg.Dir.ACK).setType(ussr.samples.atron.simulations.metaforma.generated.library.DISCOVER),m.getSource());
//		}
//		
//		if (m.type == ussr.samples.atron.simulations.metaforma.generated.library.STATE && state < m.content[0]) {
//			// state update, so broadcast
//			System.out.println(getName() + ".receive state " + m.content[0]);
//			state = m.content[0];
//			for (byte i=0; i<8; i++) {
//				if (i != connector) {
//					send (m.setSource(getName()).getBytes(),i);
//					
//				}
//			}
//		}
//		
//	}
//	
//	public Msg send (Msg m, Module dest) {
//		m.setDest(dest);
//		m.setSource(getName());
//
//		if (con.getNeighbors().containsKey(dest)) {
//			send (m.getBytes(),con.getNeighbors().get(dest));
//		}
//		else {
//			broadcast(m.getBytes());
//		}
//		
//		
//		if(m.dir == Msg.Dir.REQ) {
//			while (!buffer.containsKey(dest)) {
//				ctrl.yield();
//			}
//
//			Msg msg = buffer.get(dest);
//		
//			return msg;
//		}
//		return null;
//	}
//	
//	
//	
//	public void broadcast(Msg m) {
//		for (byte c=0; c<8; c++) {
//			send (m.setSource(getName()).setDest(Module.ALL).getBytes(),c);
//		}
//	}
//	
//	public void broadcast (byte[] bs) {
//		ctrl.info().addNotification(".broadcast(" + bs[0] + ") ");
//		for (byte c=0; c<8; c++) {
//			send (bs,c);
//		}
//	}
//
//
//}
