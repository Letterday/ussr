package ussr.samples.atron.simulations.metaforma.exp;

import ussr.model.Controller;
import ussr.model.Module;
import ussr.model.Sensor;
import ussr.samples.atron.ATRONController;
// maintainoriginalposition
public class ATRONTransformController extends ATRONController {

	public static void main( String[] args ) {
        new ATRONTansformSimulation().main();
    }
	
	private static double turnaroundTime = 2.5;
	
    private static enum State {FORWARD,BACKWARD,TURNING_LEFT,TURNING_RIGHT,AROUND}

    
    State state = State.FORWARD;
    String name;
    int ID;
    int dir = 1;
    double timePrev = 0.0;
    
	
	public void activate() {
		
		setup();
		
		
		name = module.getProperty("name");
		ID = getModuleID();
		
		
		timePrev = module.getSimulation().getTime();

		
		 
//		while (true) {
//			handleStates();
//			//if ((int)(module.getSimulation().getTime()/4) == module.getSimulation().getTime())
//			//	System.out.println(name + ".state = " + state);
//			
//			if (module.getSimulation().getTime() == 2) {
//				for (int i=0;i<8;i++){
//					if (ID == i) {
//						System.out.print("\nModule "+i + ":  ");
//						for (int j=0;j<7;j++){
//							if (module.getConnectors().get(j).isConnected()) {
//								
//								System.out.print(j + "  ");
//							}
//						}
//					}	
//				}
//			}
//			
//			if (module.getSimulation().getTime() == 10) {
//				if (ID == 3) {
//					module.getConnectors().get(2).disconnect();
//				}
//				
//			}
//			
//			
//			if (module.getSimulation().getTime() == 13) {
//				if (ID == 4) {
//					rotate(1);
//				}
//				
//			}l
//
//			yield();
//		}
	}
	
	private void handleStates () {
		
		
		
	}
	
	public void broadcastState (State stateNew) {
		
		for(byte c=0; c<8; c++) this.sendMessage(
				new byte[]{
						(byte)stateNew.ordinal(),
						(byte)state.ordinal()
						}, (byte)2,	c);
		state = stateNew;
		timePrev = module.getSimulation().getTime();
	}
	
	public void handleMessage(byte[] message, int messageLength, int connector) {
        if (state == State.values()[message[1]])
        {
        	printStateSwitch(State.values()[message[0]].toString());
        	state = State.values()[message[0]];
        	
        }
        else
        	System.err.println("State mismatch in " + name + "! " + state.toString() + " != " + State.values()[message[1]]);
        
        
        timePrev = module.getSimulation().getTime();
    }
	
	public void printStateSwitch () {
		printStateSwitch("");
	}
	
	public void printStateSwitch (String newstate) {
		System.out.println(module.getProperty("name") + ".state = " + state + " -> " + newstate);
	}
	
	
	

}
