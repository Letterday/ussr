package ussr.samples.atron.simulations.metaforma.exp;

import ussr.model.Controller;
import ussr.model.Module;
import ussr.model.Sensor;
import ussr.samples.atron.ATRONController;

public class ATRONObstacleAvoiderControllerTwoWheels extends ATRONController {

	public static void main( String[] args ) {
        new ATRONObstacleAvoiderSimulation().main();
    }
	
	private static double turnaroundTime = 2.5;
	
    private static enum State {FORWARD,BACKWARD,TURNING_LEFT,TURNING_RIGHT,AROUND}

    
    State state = State.FORWARD;
    String name;
    int dir = 1;
    float offset = 0.0f;
    
	
	public void activate() {
		
		setup();
		
		
		name = module.getProperty("name");
		
		if (name.contains("RightWheel"))
			dir = -dir;
		
		offset = module.getSimulation().getTime();

		
		 
		while (true) {
			handleStates();
			if ((int)(module.getSimulation().getTime()/4) == module.getSimulation().getTime())
				System.out.println(name + ".state = " + state);
			
//			System.out.println(name);
			if (name.contains("driver")) {
				
				for(Sensor s: module.getSensors()) {
		             if(s.getName().startsWith("Proximity")) {
		                 float v = s.readValue();
		                 
		                 if(state == State.FORWARD && v > 0.4f)
		                 {
		                
		                	 System.out.println("object found");
		                	broadcastState(State.BACKWARD);
		                 }
		                 	
		             }
		         }
				
				
				if (state == State.BACKWARD && module.getSimulation().getTime() - offset >= 5)
				{
					broadcastState(State.TURNING_RIGHT);
				
				}
				
				if (state == State.TURNING_RIGHT && module.getSimulation().getTime() - offset >= turnaroundTime)
				{
					broadcastState(State.AROUND);
				}
				
				if (state == State.AROUND && module.getSimulation().getTime() - offset >= 6)
				{
					broadcastState(State.TURNING_LEFT);
				}
				
				if (state == State.TURNING_LEFT && module.getSimulation().getTime() - offset >= turnaroundTime * 1.3)
				{
					broadcastState(State.FORWARD);
				}
			}
			yield();
		}
	}
	
	private void handleStates () {
		if (state == State.FORWARD || state == State.AROUND)
			if( name.contains("Wheel"))
				rotateContinuous(dir);
			else
				centerStop();
			
		if (state == State.BACKWARD)
			if (name.contains("Wheel"))
				rotateContinuous(-dir);
			else
				centerStop();
			
		if (state == State.TURNING_LEFT)
		{
			if (name.contains("Left") || name.contains("driver"))
				rotateContinuous(-dir);
			else if(name.contains("Right"))
				rotateContinuous(dir);
		}
			
		if (state == State.TURNING_RIGHT)
		{
			if (name.contains("Left") || name.contains("driver"))
				rotateContinuous(dir);
			else if(name.contains("Right"))
				rotateContinuous(-dir);
		}
		
		
	}
	
	public void broadcastState (State stateNew) {
		
		for(byte c=0; c<8; c++) this.sendMessage(
				new byte[]{
						(byte)stateNew.ordinal(),
						(byte)state.ordinal()
						}, (byte)2,	c);
		state = stateNew;
		offset = module.getSimulation().getTime();
	}
	
	public void handleMessage(byte[] message, int messageLength, int connector) {
        if (state == State.values()[message[1]])
        {
        	printStateSwitch(State.values()[message[0]].toString());
        	state = State.values()[message[0]];
        	
        }
        else
        	System.err.println("State mismatch in " + name + "! " + state.toString() + " != " + State.values()[message[1]]);
        
        
    	offset = module.getSimulation().getTime();
    }
	
	public void printStateSwitch () {
		printStateSwitch("");
	}
	
	public void printStateSwitch (String newstate) {
		System.out.println(module.getProperty("name") + ".state = " + state + " -> " + newstate);
	}
	
	
	

}
