package ussr.samples.atron.simulations.wouter;


import ussr.model.Sensor;
import ussr.samples.atron.ATRONController;



class State implements IState {
	static final int FORWARD = 0;
	static final int BACKWARD = 1;
	static final int TURNING_LEFT = 2;
	static final int TURNING_RIGHT = 3;
	static final int AROUND = 4;
	
	public static String toString(int s) {
//		switch (s) {
//			case FORWARD:
//				return "FORWARD";
//			case BACKWARD:
//				return "BACKWARD";
//			case TURNING_LEFT:
//				return "FORWARD";
//			case TURNING_RIGHT:
//				return "TURNING_RIGHT";
//			case AROUND:
//				return "AROUND";
//		
//		}
//		return "UNKNOWN";
		return Integer.toString(s);
		
	}
}

public class ATRONEscapeControllerFourWheels extends ATRONController {
	
	

	public static void main( String[] args ) {
        new ATRONEscapeSimulation(4).main();
    }
	
	private static double turnaroundTime = 0.2;
	
   
    ATRONBus bus;
    
   
    int dir = 1;

	private double backwardTime = 2.5;

	
	
	public void activate() {
		
		setup();
		bus = new ATRONBus(this);
		bus.state = State.FORWARD;
		
		if (bus.moduleName.contains("right"))
			dir = -dir;
		
		while (true) {
			bus.maintainConnection();
			handleStates();
			
			
			
			for(Sensor s: module.getSensors()) {
	             if(s.getName().startsWith("Proximity")) {
	                 float v = s.readValue();
	                 if (v> 0.1f)
	                	 bus.printLimited(bus.moduleName + ".v-" + s.getName() + " = " + v);
			
	             }
			}
			
			bus.printState();
			
			if (bus.moduleName.contains("axleone")) {
				
				for(Sensor s: module.getSensors()) {
		           
		         
					 if(s.getName().startsWith("Proximity")) {
		                 float v = s.readValue();
		                 
		                 if(v > 0.2) {
		                	 if (bus.state != State.TURNING_LEFT && bus.state != State.BACKWARD) {
			                	System.out.println("object found");
			                	bus.broadcastState(State.TURNING_LEFT);
			                 }
//		                	 else {
//			                	System.out.println("object found");
//				                broadcastState(State.FORWARD);
//			                 }
		                 	
		             }
		         }
				}
				
				if (bus.state == State.TURNING_LEFT && bus.waitedFor(turnaroundTime))
				{
					bus.broadcastState(State.BACKWARD);
					//turnaroundTime = 2 * Math.random();
				}
				
				if (bus.state == State.BACKWARD && bus.waitedFor(backwardTime))
				{
					bus.broadcastState(State.TURNING_RIGHT);
					//backwardTime = 10 * Math.random();
				
				}
				
				
				
				if (bus.state == State.TURNING_RIGHT && bus.waitedFor(turnaroundTime))
				{
					bus.broadcastState(State.FORWARD);
					//offset = time;
				}
				
//				if (state == State.TURNING_LEFT && time - offset >= turnaroundTime * 2) {
//					broadcastState(State.FORWARD);
//				}
			}
			
//			if (name.contains("axlestwo")) {
//				
//				for(Sensor s: module.getSensors()) {
//		             if(s.getName().startsWith("Proximity")) {
//		                 float v = s.readValue();
//		                 //System.out.println(name + "." + s.getName() + " = " + s.readValue());
//		                 if (v > 0.2 && state == State.BACKWARD) {
//		                	 broadcastState(State.FORWARD);
//		                	 System.exit(0);
//		                 }
//		                	 
//		                 	
//		             }
//		         }
//			}
			
			yield();
		}
	}
	
	
	private void handleStates () {
		if (bus.state == State.FORWARD || bus.state == State.AROUND)
			if( bus.moduleName.contains("wheel"))
				rotateContinuous(dir);
			else
				centerStop();
			
		if (bus.state == State.BACKWARD)
			if (bus.moduleName.contains("wheel"))
				rotateContinuous(-dir);
			else
				centerStop();
			
		if (bus.state == State.TURNING_LEFT)
		{
			if (bus.moduleName.contains("axleone") )
				rotateToDegreeInDegrees(10);
			else if (bus.moduleName.contains("axletwo") )
				rotateToDegreeInDegrees(-10);
			else if (bus.moduleName.contains("wheel1") || bus.moduleName.contains("wheel2") )
				rotateContinuous(-1);
			else if (bus.moduleName.contains("wheel3") || bus.moduleName.contains("wheel4") )
				rotateContinuous(1);
			else
				centerBrake();
		}
			
		if (bus.state == State.TURNING_RIGHT)
		{
			if (bus.moduleName.contains("axleone") )
				rotateToDegreeInDegrees(-10);
			else if (bus.moduleName.contains("axletwo") )
				rotateToDegreeInDegrees(10);
			else if (bus.moduleName.contains("wheel1") || bus.moduleName.contains("wheel2") )
				rotateContinuous(1);
			else if (bus.moduleName.contains("wheel3") || bus.moduleName.contains("wheel4") )
				rotateContinuous(-1);
			
			else
				centerBrake();
		}
		
		
	}
	
	public void handleMessage(byte[] message, int messageLength, int connector) {
		bus.handleMessage(message, messageLength, connector);
	}
	
	

}
