package ussr.samples.atron.simulations.metaforma.gen;
import java.awt.Color;
import java.util.ArrayList;

import ussr.description.Robot;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.model.Controller;
import ussr.model.debugging.ControllerInformationProvider;
import ussr.physics.PhysicsFactory;
import ussr.physics.PhysicsSimulation;
import ussr.physics.jme.DebugInformationPicker;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONBuilder;
import ussr.samples.atron.network.ATRONReflectionEventController;
import ussr.samples.atron.simulations.metaforma.gen.CloverFlipthroughController.StateOperation;
import ussr.samples.atron.simulations.metaforma.lib.*;


class EightToCarSimulation extends MetaformaSimulation {

	public static void main( String[] args ) {
		MetaformaSimulation.initSimulator();
        new EightToCarSimulation().main();
    }
	
	
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
            	return new EightToCarController();
            }
        };
        return a;
    }
	
	protected ArrayList<ModulePosition> buildRobot() {
		return new ATRONBuilder().buildEight2(new VectorDescription(0,-5*ATRON.UNIT,0));
	}
	
}

//
//Module 0 disconnecting connector 0 to module 2
//Module 3 disconnecting connector 4 to module 4
//Module 3 rotate -1
//Module 4 rotate -1
//Module 4 connected to connector 6 to module 3
//Module 1 rotate -1
//Module 6 disconnecting connector 2 to module 5
//Module 4 rotate 1
//Module 6 rotate 1
//Module 0 connected to connector 0 to module 6
//Module 6 disconnecting connector 6 to module 4
//Module 0 rotate -1
//Module 1 rotate 1
//Module 0 rotate 1
//Module 5 connected to connector 4 to module 6
//Module 2 connected to connector 4 to module 6
//Module 1 connected to connector 4 to module 4
//Module 4 disconnecting connector 6 to module 3
//Module 3 disconnecting connector 6 to module 1
//Module 1 rotate 1
//Module 3 rotate 1
//Module 1 connected to connector 6 to module 3
//Module 3 disconnecting connector 0 to module 5
//Module 3 disconnecting connector 2 to module 2
//Module 1 rotate 1

class EightToCarController extends MetaformaRuntime implements ControllerInformationProvider {

	enum StateOperation implements IStateOperation {
		DEFAULT, MOVE;

		public byte ord() {
			return (byte)ordinal();
		}

		public IStateOperation fromByte(byte b) {
			return values()[b];
		}
	}
	
	public void handleStates () {
		if (doOnce(0)) {
			for (int i=0; i<3; i++) {
				discoverNeighbors();
			}
			
			stateInstrBroadcastNext();
		}
		
		if (doOnce(1,7)) {
			if (nbs().size() == 4) {
				renameTo(Module.Floor_3);
				commit();
			}
			if (nbs(SOUTH&EAST).contains(Module.Floor_3)) {
				renameTo(Module.Floor_2);
				commit();
			}
			if (nbs(SOUTH&WEST).contains(Module.Floor_3)) {
				renameTo(Module.Floor_1);
				commit();
			}
			if (nbs(NORTH&EAST).contains(Module.Floor_3)) {
				renameTo(Module.Floor_5);
				commit();
			}
			if (nbs(NORTH&WEST).contains(Module.Floor_3)) {
				renameTo(Module.Floor_4);
				commit();
			}
			if (nbs(WEST&MALE).size() == 2  && nbs().size()==2) {
				renameTo(Module.Floor_0);
				commit();
			}
			if (nbs(EAST&MALE).size() == 2 && nbs().size()==2) {
				renameTo(Module.Floor_6);
				commit();
			}
		}
		
		if (doOnce(2,2)) {	
	    	disconnectPart (Module.Floor_0, NORTH&MALE&WEST);
	    	disconnectPart (Module.Floor_3, SOUTH&MALE&WEST);
		
		}
		
		if (doOnce(3)) {	
    		rotate(Module.Floor_3,-90);
		}
		
		if (doOnce(4)) {	
    		rotate(Module.Floor_4,90);
		}
				
		if (doOnce(5)) {	
    		connectPart (Module.Floor_4, SOUTH&MALE&EAST);
		}
		
//		if (stateInstruction(3)) {
//			rotate(Module.Floor_1,90,new RunSeq(this));
//		}
		
		if (doOnce(14)) {	
    		disconnectPart (Module.Floor_6, NORTH&MALE&EAST);
		}
		
		if (doOnce(15)) {	
    		connectPart (Module.Floor_0, NORTH);
		}
		
		if (doOnce(51)) {	
    		disconnectPart (Module.Floor_6, SOUTH&MALE&EAST);
		}
		
		if (doOnce(61)) {	
    		connectPart (Module.Floor_5, SOUTH&MALE&WEST);
		}
		
		if (doOnce(71)) {	
    		connectPart (Module.Floor_2, SOUTH&MALE&WEST);
		}
		
		if (doOnce(8)) {	
    		connectPart (Module.Floor_1, SOUTH&MALE&WEST);
		}
		
		if (doOnce(9)) {	
    		disconnectPart (Module.Floor_4, SOUTH&MALE&EAST);
		}
		
		if (doOnce(10)) {	
    		disconnectPart (Module.Floor_3, SOUTH&MALE&EAST);
		}
		
		if (doOnce(11)) {	
    		connectPart (Module.Floor_6, SOUTH&MALE&WEST);
		}
		
		if (doOnce(12)) {	
    		disconnectPart (Module.Floor_3, NORTH&MALE&WEST);
		}
		
		if (doOnce(13)) {	
    		disconnectPart (Module.Floor_3, NORTH&MALE&EAST);
		}
    
   }
  
 
	
	
	@Override
	public void init() {
		stateOperationInit(StateOperation.DEFAULT);
//		Packet.operationHolder = StateOperation.DEFAULT;
//		Packet.varHolder = Var.DEFAULT;
		
		setModuleColors (Module.Floor_0,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		setModuleColors (Module.Floor_3,new Color[]{Color.decode("#009999"),Color.decode("#999900")}); 
		setModuleColors (Module.Floor_6,new Color[]{Color.decode("#003333"),Color.decode("#333300")}); 
		
		setDefaultColors(new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		
	}




	@Override
	protected void receiveMessage(Type type, IStateOperation stateOp,
			byte stateInstr, boolean isReq, byte sourceCon, byte destCon,byte metaId, 
			byte[] data) {
		// TODO Auto-generated method stub
		
	}
	





}