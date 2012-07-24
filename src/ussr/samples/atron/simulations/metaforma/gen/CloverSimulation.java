package ussr.samples.atron.simulations.metaforma.gen;
import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;


import ussr.description.Robot;
import ussr.description.setup.ModulePosition;
import ussr.model.Controller;
import ussr.model.debugging.ControllerInformationProvider;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONBuilder;
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
			
			if (doOnce(0,4)) {
				if (nbs(FEMALE&EAST).exists() && !nbs(WEST).exists()){
					renameStore();
					renameTo(Module.Clover_South);
					commit();
				}
				if (nbs(EAST).contains(Module.Clover_South)){
					renameStore();
					renameTo(Module.Clover_West);
					commit();
				}
				if (nbs(WEST).contains(Module.Clover_South)){
					renameStore();
					renameTo(Module.Clover_East);
					commit();
				}
				if (nbs().contains(Module.Clover_East) && nbs().contains(Module.Clover_West)){
					renameStore();
					renameTo(Module.Clover_North);
					commit();
				}

			}

			if (doOnce(1)) {
				disconnect (Module.Clover_West, Module.Clover_South);
			}
			
			if (doOnce(2)) {
				rotate (Module.Clover_East,180);
			}
			
			if (doOnce(3)) {
				rotate (Module.Clover_North,180);
			}
			
			if (doOnce(4)) {
				connect (Module.Clover_East,Grouping.Floor);
			}
			
			if (doOnce(5)) {
				connect (Module.Clover_South,Grouping.Floor);
			}
 
			if (doOnce(6,2)) {
				disconnect (Module.Clover_West,Grouping.Floor);
				disconnect (Module.Clover_North,Grouping.Floor);
			}
			
			if (doOnce(7,2)) {
				rotate (Module.Clover_North,180);
				rotate (Module.Clover_East,180);
			}
			
			if (doOnce(8)) {
				connect (Module.Clover_South,Module.Clover_West);
			}
			
			if (doOnce(9,4)) {
				if(getGrouping() == Grouping.Clover) {
					renameRestore();
					switchNorthSouth();
					switchEastWest(); 
					commit();
				}
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
		
		IstateOperation = StateOperation.DEFAULT;
		Packet.setController(this);
	
	}


	protected void receiveMessage(Type type, IStateOperation stateOperation, byte stateInstruction, boolean isReq, byte sourceCon, byte destCon, byte[] data) {
			
	
		
	}
	


}
