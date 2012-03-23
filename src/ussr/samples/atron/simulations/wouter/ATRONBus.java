package ussr.samples.atron.simulations.wouter;


import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import ussr.samples.atron.ATRONController;

public class ATRONBus {
	int state;
	ATRONController ctrl;
	float baseTime;
	public float time = 0;
	float printDelay = 0;
	String moduleName;
	private boolean isRotating;
	private String moduleNameCheck;
	private int stateCheck;
	private int angle = 0;
	private Set<Integer> connecting = new HashSet<Integer>();
	private Set<Integer> disconnecting = new HashSet<Integer>();
	
	public ATRONBus (ATRONController c) {
		ctrl = c;
		moduleName = ctrl.getModule().getProperty("name").toLowerCase();
		//System.out.println("BUS created for " + moduleName );
	
	}
	
	public void setName(String name) {
		ctrl.getModule().setProperty("name",name);
		moduleName = name;
	}
	
	public void maintainConnection () {
		time = ctrl.getModule().getSimulation().getTime();
		
	}
	
	public ATRONBus connect (String module, int c, boolean rotateAllowed) {
		if (!checkMod()) return this;
		if (moduleName.contains(module)) {
			connect (c,rotateAllowed);
		}
		return this;
	}
	
	public ATRONBus connect (int c, boolean rotateAllowed) {
		if (!checkMod()) return this;
		if (isConnected(c))
			connecting.remove(c);
		else
		{
//			if (rotateAllowed) {
//				this.rotateDegrees(90);
//			}
//			else {
				ctrl.getModule().getConnectors().get(c).connect();
				connecting.add(c);
			//}
		}
		return this;
	}
	
	public ATRONBus disconnect (int c) {
		if (!checkMod()) return this;
		if (!isConnected(c))
			disconnecting.remove(c);
		else {
			ctrl.getModule().getConnectors().get(c).disconnect();
			disconnecting.add(c);
		}
		 
		return this;
	}
	
	public void bcNextState () {
		if (!checkMod()) return;
		if (disconnecting.isEmpty() && connecting.isEmpty()) {
			broadcastNextState();
		}
	}
	
	public void disconnect (String module, int c) {
		if (!checkMod()) return;
		if (module.startsWith("!")) {
			if (!moduleName.contains(module.substring(1))) {
				ctrl.getModule().getConnectors().get(c).disconnect();
			}
		}
		else
		{
			if (moduleName.contains(module)) {
				disconnect(c);
			}
		}
	}
	
	public boolean isConnected (int c) {
		return ctrl.getModule().getConnectors().get(c).isConnected();
	}
	
	public void initBaseTime () {
		baseTime = ctrl.getModule().getSimulation().getTime();
	}
	
	public void broadcastState (int stateNew) {
		broadcastState(moduleName,stateNew);
	}
	
	public void broadcastNextState () {
		System.out.println(".broadcast " + state + "->" + (state + 1));
		broadcastState (state + 1);
		
	}
	
	public void broadcastState (String source, int stateNew) {
		if (ctrl.getName().contains(source)) {
			System.out.println("BROADCAST " + stateNew + "(" + state + ")");
			if (state != stateNew) {
				for(byte c=0; c<8; c++) ctrl.sendMessage(
						new byte[]{
								(byte)stateNew,
								(byte)state
								}, (byte)2,	c);
				state = stateNew;
			}
			initBaseTime();
		}
		
	}
	
	public void handleMessage(byte[] message, int messageLength, int connector) {
        if (state == message[1])
        {
        	printStateSwitch(message[0]);
        	
        	if (state != message[0]) {
        		state = message[0];
	        	for(byte c=0; c<8; c++) 
	        	{
	        		//System.out.println("c="+connector);
	        		if ((int)c != connector)
	        			ctrl.sendMessage(message, (byte)2,	c);
        		}
        	}
        }
        else
        	System.err.println("State mismatch in " + ctrl.getName() + "! " + State.toString(state) + " != " + State.toString(message[1]));
        
        
        baseTime = ctrl.getModule().getSimulation().getTime();
    }
	
	public void printStateSwitch () {
		printStateSwitch(-1);
	}
	
	public void printStateSwitch (int newstate) {
		System.out.println(ctrl.getModule().getProperty("name") + ".state = " + State.toString(state) + " -> " + State.toString(newstate));
	}
	
	public String getState () {
		return State.toString(state);
	}
	
	public void printLimited (String s){
		if (time - printDelay > 5) {
			System.out.println("." + ctrl.getName() + ": " + s);
			printDelay = ctrl.getModule().getSimulation().getTime();
		}
	}
	
	public void printState () {
		//if (moduleName.contains("m")) {
			printLimited(moduleName + ".state = " + getState());
		//}
	}
	
	public boolean waitedFor (double w) {
		return baseTime + w < time;
	}
	
	public boolean containsModule (String module) {
		return moduleName.contains(module);
	}

	public void rotateDegrees(int d) {
		// setMaintainRotationalJointPositions
		if (!checkMod()) return;
		if (!isRotating) {
			ctrl.rotateToDegreeInDegrees(d + angle);
			angle = d + angle;
			isRotating = true;
		}
		if (!ctrl.getModule().getActuators().get(0).isActive())
		{
			isRotating = false;
			broadcastNextState();
		}	
	}
	
	public void rotateToDegreeInDegrees(int d) {
		if (!checkMod()) return;
		ctrl.rotateToDegreeInDegrees(d);
		if (!ctrl.getModule().getActuators().get(0).isActive())
			broadcastNextState();
		
	}
	
	

	private boolean checkMod() {
		return moduleName == moduleNameCheck && state == stateCheck;
	}

	public void markModule() {
		if (!checkMod()) return;
		ctrl.getModule().getComponent(0).setModuleComponentColor(Color.WHITE);
		
	}

	public ATRONBus execAt(String mod, int state) {
		this.moduleNameCheck = mod;
		this.stateCheck = state;
		return this;
	}
	
}
