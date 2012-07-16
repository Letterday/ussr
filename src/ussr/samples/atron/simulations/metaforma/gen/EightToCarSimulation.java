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
import ussr.samples.atron.simulations.metaforma.gen.Clover2Controller.StateOperation;
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


//Module 0 disconnecting connector 0 to module 2
//Module 3 disconnecting connector 4 to module 4
//Module 4 connected to connector 6 to module 3
//Module 6 disconnecting connector 2 to module 5
//Module 0 connected to connector 0 to module 6
//
//Module 6 disconnecting connector 6 to module 4
//Module 5 connected to connector 4 to module 6
//Module 2 connected to connector 4 to module 6
//Module 1 connected to connector 4 to module 4
//Module 4 disconnecting connector 6 to module 3

//Module 3 disconnecting connector 6 to module 1
//Module 1 connected to connector 6 to module 3
//Module 3 disconnecting connector 0 to module 5
//Module 3 disconnecting connector 2 to module 2

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
		if (stateInstruction(0)) {	
    		disconnectPart (Module.Floor_0, NORTH, new RunSeq(this));
		}
		
		if (stateInstruction(1)) {	
    		disconnectPart (Module.Floor_3, SOUTH&MALE&WEST, new RunSeq(this));
		}
		
		if (stateInstruction(2)) {	
    		connectPart (Module.Floor_4, SOUTH&MALE&EAST, new RunSeq(this));
		}
		
		if (stateInstruction(3)) {	
    		disconnectPart (Module.Floor_6, NORTH&MALE&EAST, new RunSeq(this));
		}
		
		if (stateInstruction(4)) {	
    		connectPart (Module.Floor_0, NORTH, new RunSeq(this));
		}
		
		if (stateInstruction(5)) {	
    		disconnectPart (Module.Floor_6, SOUTH&MALE&EAST, new RunSeq(this));
		}
		
		if (stateInstruction(6)) {	
    		connectPart (Module.Floor_5, SOUTH&MALE&WEST, new RunSeq(this));
		}
		
		if (stateInstruction(7)) {	
    		connectPart (Module.Floor_2, SOUTH&MALE&WEST, new RunSeq(this));
		}
		
		if (stateInstruction(8)) {	
    		connectPart (Module.Floor_1, SOUTH&MALE&WEST, new RunSeq(this));
		}
		
		if (stateInstruction(9)) {	
    		disconnectPart (Module.Floor_4, SOUTH&MALE&EAST, new RunSeq(this));
		}
		
		if (stateInstruction(10)) {	
    		disconnectPart (Module.Floor_3, SOUTH&MALE&EAST, new RunSeq(this));
		}
		
		if (stateInstruction(11)) {	
    		connectPart (Module.Floor_6, SOUTH&MALE&WEST, new RunSeq(this));
		}
		
		if (stateInstruction(12)) {	
    		disconnectPart (Module.Floor_3, NORTH&MALE&WEST, new RunSeq(this));
		}
		
		if (stateInstruction(13)) {	
    		disconnectPart (Module.Floor_3, NORTH&MALE&EAST, new RunSeq(this));
		}
    
   }
  
 
	
	@Override
	public void handleEvents() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void handleSyncs() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void init() {
		stateOperationInit(StateOperation.DEFAULT);
		Packet.operationHolder = StateOperation.DEFAULT;
		Packet.varHolder = Var.DEFAULT;
		
		setModuleColors (Module.Floor_0,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		setModuleColors (Module.Floor_3,new Color[]{Color.decode("#009999"),Color.decode("#999900")}); 
		setModuleColors (Module.Floor_6,new Color[]{Color.decode("#003333"),Color.decode("#333300")}); 
		
		setDefaultColors(new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		
	}
	
	@Override
	protected void receiveMessage(Packet p, int connector) {
		// TODO Auto-generated method stub
		
	}




}