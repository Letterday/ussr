package ussr.samples.atron.simulations.wouter;


import java.awt.Color;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.util.Set;

import ussr.physics.PhysicsSimulation;
import ussr.samples.atron.ATRONController;

enum MessageType {MSG_STATECHANGE, MSG_REQ_CANROTATE, MSG_ACK_CANROTATE, MSG_ACK_CANNOTROTATE, MSG_CONNECT, MSG_ACK_WHICHMODULE, MSG_REQ_WHICHMODULE, MSG_DISCONNECT;
	public byte ord (){return (byte)this.ordinal();}}
enum HemisphereType {NORTH, SOUTH}
enum TransmissionState {NEUTRAL, WAITING_CONNECT }
enum Task {ROTATE, CONNECT, DISCONNECT}





public class ATRONBus {
	ATRONBusPrinter print;
	ATRONController ctrl;
	ATRONBusConnector con;
	
	int state;
	
	
	private int stateCheck;
	private int angle = 0;
	
	private int destination;
	TransmissionState transmissionState = TransmissionState.WAITING_CONNECT;
	
	private Task currentTask;
	private String moduleNameCheck;
	private float baseTime;
	
	public ATRONBus (ATRONController c, int visibleMessages) {
		ctrl = c;
		print = new ATRONBusPrinter(this,visibleMessages);
		con = new ATRONBusConnector(this);
	}
	
	public void setName(String name) {
		ctrl.getModule().setProperty("name",name);
	}
	
	public void maintainConnection () {
		ctrl.rotateToDegreeInDegrees(angle);
	}
	
	public float time () {
		return ctrl.getModule().getSimulation().getTime();
	}
	public boolean canRotateFree () {
		return con.getConnectionCount (HemisphereType.NORTH) == 0 || con.getConnectionCount (HemisphereType.SOUTH) == 0;
	}
	
	
	
	public void next () {
		if (!checkForActiveModule()) return;
		if (con.getDisconnecting().isEmpty() && con.getConnecting().isEmpty() && !isRotating()) {
			broadcastNextState();
		}
		else {
			print.limited(345,ATRONBusPrinter.STATE_INFO,"Waiting for:" + con.getConnecting().size() + " - " + con.getDisconnecting().size() + " - " + isRotating());
			
		}
	}
	
	public boolean isRotating() {
		return ctrl.getModule().getActuators().get(0).isActive();
	}
	
	
	
	
	
	public void broadcastState (int stateNew) {
		print.stateBroadcast(stateNew);
		if (state != stateNew) {
			for(byte c=0; c<8; c++) send(
					new byte[]{
							MessageType.MSG_STATECHANGE.ord(),
							getID(),
							(byte)stateNew,
							(byte)state
							},	c);
			state = stateNew;
		}
		
	}
	
	public void broadcastNextState () {
		destination = Integer.MAX_VALUE;
		broadcastState (state + 1);
		
	}
	
	

	
	
	
	public byte getID (){
		byte id = (byte)Integer.parseInt(getName().substring(1));
		
		if (id >= 192) {
			System.err.println("ID number too big!");
		}
		else if (getName().startsWith("m")) {
			id = (byte) (0-id);
		}
		else if (getName().startsWith("f")) {
			id += 0;
		}
		else {
			System.err.println("Unknown name!");
		}
		
		return id;
		
	}
	
	private String idToName (byte id) {
		if (id < 0) {
			return "m" + (-id);
		}
		else {
			return "f" + id;
		}
	}
	
	
	public void send(byte[] bs, byte connector) {
		if (bs[0] == MessageType.MSG_STATECHANGE.ord()) {
			print.print (ATRONBusPrinter.STATE_MESSAGE, ".send(" + MessageType.values()[bs[0]].toString() + ") over " + connector);
		}
		else {
			print.print (ATRONBusPrinter.MESSAGE, ".send(" + MessageType.values()[bs[0]].toString() + ") over " + connector);
		}
		
//		if (!ctrl.isObjectNearby(connector) && MessageType.MSG_STATECHANGE.ord() != bs[0]) {
//			System.err.println("no neighbor at " + connector);
//		}
		ctrl.sendMessage(bs, (byte)bs.length, connector);
	}
	
	
	
	
	public int getState () {
		return state;
	}
	
	
	public boolean moduleMatcher (String module) {
		return getName().contains(module);
	}

	public ATRONBus rotateDegrees(int d) {
		// setMaintainRotationalJointPositions
		if (!checkForActiveModule() ) return this;
		if (con.getConnectionCount() > 2) {
			print.limited(3341, ATRONBusPrinter.WARNING, "More than 2 connected while rotating!");
		}
		if (!isRotating() && d != destination) {
			ctrl.rotateToDegreeInDegrees(d + angle);
			angle = d + angle;
			destination = d;
			printTask(Task.ROTATE,d);
		}
		
		return this;
	}
	
	
	
	public void printTask(Task t, int d) {
		if (t != currentTask) {
			currentTask = t;
			if (t == Task.ROTATE) {
				print.print(ATRONBusPrinter.ACTION, "ROTATing " + d + "deg");
			} 
			else if (t == Task.DISCONNECT) {
				print.print(ATRONBusPrinter.ACTION, "DISCONNECTING CONNECTOR " + d);
			}
			else if (t == Task.CONNECT) {
				print.print(ATRONBusPrinter.ACTION, "CONNECTING CONNECTOR " + d);
			}
		} 
		
	}

	public void rotateToDegreeInDegrees(int d) {
		if (!checkForActiveModule()) return;
		ctrl.rotateToDegreeInDegrees(d);
		
		
	}
	
	

	public boolean checkForActiveModule() {
		return getName() == moduleNameCheck && state == stateCheck;
	}

	public ATRONBus markModule() {
		if (!checkForActiveModule()) return this;
		ctrl.getModule().getComponent(0).setModuleComponentColor(Color.decode("#6666FF"));
		ctrl.getModule().getComponent(1).setModuleComponentColor(Color.decode("#FF6666"));
		return this;
	}

	public ATRONBus execAt(String mod, int state) {
		this.moduleNameCheck = mod;
		this.stateCheck = state;
		return this;
	}

	public String getName() {
		return ctrl.getModule().getProperty("name").toLowerCase();
	}

	public PhysicsSimulation getSimulation() {
		return ctrl.getModule().getSimulation();
	}

	public ATRONBusPrinter getPrint() {
		return print;
	}

	public void initBaseTime() {
		baseTime = time();
		
	}
	
	
	public void handleMessage(byte[] message, int messageLength, byte connector) {
		con.getNeighbors().put(idToName(message[1]), connector);
		if (message[0] == MessageType.MSG_STATECHANGE.ord()) {
			print.print(ATRONBusPrinter.STATE_MESSAGE, ".received " + MessageType.values()[message[0]].toString() + " along " + connector + " (" + idToName(message[1]) + ")");
		} else {
			print.print(ATRONBusPrinter.MESSAGE, ".received " + MessageType.values()[message[0]].toString() + " along " + connector + " (" + idToName(message[1]) + ")");
		}
		
        switch (MessageType.values()[message[0]]) {
        	case MSG_STATECHANGE:
        		
        		if (state == message[3])
                {
                	// printStateSwitch(message[1]);
                	
                	if (state != message[2]) {
                		state = message[2];
                		print.print(ATRONBusPrinter.STATE_UPDATE, ".state=" + state);
        	        	for(byte c=0; c<8; c++) 
        	        	{
        	        		//System.out.println("c="+connector);
        	        		if (c != connector)
        	        			send(message, c);
                		}
                	}
                }
//                else
//                	System.err.println("State mismatch in " + ctrl.getName() + "! " + State.toString(state) + " != " + State.toString(message[1]));
//                
               
            break;
        		
        	case MSG_REQ_WHICHMODULE:
        		send(new byte[]{MessageType.MSG_ACK_WHICHMODULE.ord(),getID()},connector);
        		 
        	break;  
        	
        	case MSG_ACK_WHICHMODULE:
        		con.getNeighbors().put(idToName(message[1]), connector);
        	break;  
        		
        	case MSG_REQ_CANROTATE:
        		if (canRotateFree()) {
        			print.limited(1820,ATRONBusPrinter.ACTION,"Request for rotating - GRANTED");
    				rotateDegrees(90);
    				while (isRotating()) {ctrl.yield();}
    				
    				if (connector%2 == 0) {
    					ctrl.connect(connector);
    				}
    				else {
    					send(new byte[]{MessageType.MSG_ACK_CANROTATE.ord(),getID()},connector);
    				}
    			}
    			else {
    				print.limited(1821,ATRONBusPrinter.ACTION,"Request for rotating - DENIED");
    				send(new byte[]{MessageType.MSG_ACK_CANNOTROTATE.ord(),getID()},connector);
    			}
        		
        	break;
        		
        	case MSG_ACK_CANROTATE:	
        		//con.connect(connector);
        	break;
        	
        	case MSG_CONNECT:	
        		con.connectIt(connector);
        		broadcastNextState(); 
        		
        	break;
        	
        	case MSG_DISCONNECT:	
        		con.disconnectIt(connector);
        		broadcastNextState(); 
        	break;
        	
        	// Request to rotate
        	
        }
		
    }
	
}
