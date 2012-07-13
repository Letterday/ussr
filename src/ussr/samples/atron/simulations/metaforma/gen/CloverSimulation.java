package ussr.samples.atron.simulations.metaforma.gen;
import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;

import org.python.antlr.PythonParser.power_return;

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
import ussr.samples.atron.simulations.metaforma.gen.CloverFlipoverController.StateOperation;
import ussr.samples.atron.simulations.metaforma.lib.*;




class CloverSimulation extends MetaformaSimulation {
	

	public static void main( String[] args ) {
		MetaformaSimulation.initSimulator();
        new CloverSimulation().main();
    }
	
	
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
            	return new CloverController();
            }
        };
        return a;
    }
	
	protected ArrayList<ModulePosition> buildRobot() {
		BitSet b = new BitSet();
		b.set(0,3);
		return new ATRONBuilder().buildGrid(b, "Floor_",false);
	}
	
}



class CloverController extends MetaformaRuntime implements ControllerInformationProvider {
	
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
		if (stateOperation(StateOperation.DEFAULT)) {
			
			if (stateInstruction(0)) {
				discoverNeighbors();
				if (!stateIsFinished()) {
					if (nbs(FEMALE&EAST).exists() && !nbs(WEST).exists()){
						renameTo(Module.Clover_South);
						commit(true);
					}
					if (nbs(EAST).contains(Module.Clover_South)){
						renameTo(Module.Clover_West);
						commit(true);
					}
					if (nbs(WEST).contains(Module.Clover_South)){
						renameTo(Module.Clover_East);
						commit(true);
					}
					if (nbs().contains(Module.Clover_East) && nbs().contains(Module.Clover_West)){
						renameTo(Module.Clover_North);
						commit(true);
					}
				}
				consensusIfCompletedNextState(4);
			}

			if (stateInstruction(12)) {
				disconnect (Module.Clover_West, Module.Clover_South, new RunSeq(this));
			}
			
			if (stateInstruction(2)) {
				rotate (Module.Clover_East,180, new RunSeq(this));
			}
			
			if (stateInstruction(3)) {
				rotate (Module.Clover_North,180, new RunSeq(this));
			}
			
			if (stateInstruction(4)) {
				connect (Module.Clover_East,Grouping.Floor,new RunSeq(this));
			}
			
			if (stateInstruction(5)) {
				connect (Module.Clover_South,Grouping.Floor,new RunSeq(this));
			}
 
			if (stateInstruction(6)) {
				disconnect (Module.Clover_West,Grouping.Floor,new RunPar(this));
				disconnect (Module.Clover_North,Grouping.Floor,new RunPar(this));
				consensusIfCompletedNextState(2);
			}
			
			if (stateInstruction(7)) {
				rotate (Module.Clover_North,180,new RunPar(this));
				rotate (Module.Clover_East,180,new RunPar(this));
				consensusIfCompletedNextState(2);
			}

			
			if (stateInstruction(8)) {
				connect (Module.Clover_South,Module.Clover_West, new RunSeq(this));
			}

			
			
			if (stateInstruction(9)) {
				if(getGrouping() == Grouping.Clover) {
					renameRestore();
					switchNorthSouth();
					switchEastWest(); 
					commit(true);
				}
				consensusIfCompletedNextState(4);
			}
		}
		
   }
	
	
	public void init () {
		setModuleColors (Module.Clover_North,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		setModuleColors (Module.Clover_South,new Color[]{Color.decode("#00AAAA"),Color.decode("#AAAA00")}); 
		setModuleColors (Module.Clover_West,new Color[]{Color.decode("#006666"),Color.decode("#666600")}); 
		setModuleColors (Module.Clover_East,new Color[]{Color.decode("#002222"),Color.decode("#222200")}); 
		
		setDefaultColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		setMessageFilter(Type.DISCOVER.bit() |Type.STATE_OPERATION_NEW.bit());
		stateOperationInit(StateOperation.DEFAULT);
		Packet.operationHolder = StateOperation.DEFAULT;
		Packet.varHolder = Var.DEFAULT;
	}
	
	@Override
	protected void receiveMessage(Packet p, int connector) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void handleEvents() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void handleSyncs() {
		// TODO Auto-generated method stub
		
	}


}
