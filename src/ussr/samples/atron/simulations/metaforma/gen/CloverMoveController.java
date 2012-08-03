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
import ussr.samples.atron.simulations.metaforma.gen.CloverFlipthroughController.Var;
import ussr.samples.atron.simulations.metaforma.lib.*;


class CloverMoveSimulation extends MetaformaSimulation {
	

	public static void main( String[] args ) {
		MetaformaSimulation.initSimulator();
        new CloverMoveSimulation().main();
    }
	
	
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
            	return new CloverMoveController();
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



public class CloverMoveController extends MetaformaRuntime implements ControllerInformationProvider {
	
	enum StateOperation implements IStateOperation {
		DEFAULT;

		public byte ord() {
			return (byte)ordinal();
		}

		public IStateOperation fromByte(byte b) {
			return values()[b];
		}
	}
	
	enum Var implements IVar {
		gradH,gradV, NONE;

		public byte index() {
			return (byte)ordinal();
		}

		public Var fromByte(byte b) {
			return values()[b];
		}
	}
	
	public void handleStates () {
		if (stateOperation(StateOperation.DEFAULT)) {
			
			if (doOnce(0))  {
				scheduler.setInterval("gradientCreate",200);
				if (stateStartNext == 0) {
					stateStartNext = time() + 5;
				}
				if (stateStartNext< time()) {
					stateInstrBroadcastNext();
				}
			}
	
			 
			if (doOnce(1) )  {
				stateInstrBroadcastNext();
				scheduler.setInterval("gradientCreate",10000);
			}
			
			if (doOnce(2,4)) {
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

			if (doOnce(3)) {
				disconnect (Module.Clover_West, Module.Clover_South);
			}
			
			if (doOnce(4)) {
				rotate (Module.Clover_East,180);
			}
			
			if (doOnce(5)) {
				rotate (Module.Clover_North,180);
			}
			
			if (doOnce(6)) {
				connect (Module.Clover_East,Grouping.Floor);
			}
			
			if (doOnce(7)) {
				connect (Module.Clover_South,Grouping.Floor);
			}
 
			if (doOnce(8,2)) {
				disconnect (Module.Clover_West,Grouping.Floor);
				disconnect (Module.Clover_North,Grouping.Floor);
			}
			
			if (doOnce(9,2)) {
				rotate (Module.Clover_North,180);
				rotate (Module.Clover_East,180);
			}
			
			if (doOnce(10)) {
				connect (Module.Clover_South,Module.Clover_West);
			}
			
			if (doOnce(11,4)) {
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
		Ivar = Var.NONE;
		Packet.setController(this);
		
		varSet(Var.gradH, MAX_BYTE);
		varSet(Var.gradV, MAX_BYTE);
		
	
	}


	protected void receiveMessage(Type type, IStateOperation stateOperation, byte stateInstruction, boolean isReq, byte sourceCon, byte destCon, byte metaId, byte[] data) {
			
		if (type == Type.GRADIENT) {
			if (varGet(Var.NONE.fromByte(data[0])) > data[1]) {
				varSet(Var.NONE.fromByte(data[0]),data[1]);
				scheduler.invokeNow("gradientCreate");
			}
		}
		
	}
	
	public void gradientCreate () {
		//discoverNeighbors();
		
		boolean sourceH = nbs(EAST&MALE).size() == 2 && nbs(WEST&SOUTH&MALE).isEmpty() || nbs(EAST&FEMALE).size() == 2 && nbs(WEST&NORTH&FEMALE).isEmpty();
		boolean sourceV = nbs(EAST&MALE).size() == 2 && nbs(WEST&NORTH&MALE).isEmpty() || nbs(WEST&FEMALE).size() == 2 && nbs(EAST&NORTH&FEMALE).isEmpty();
		gradientSend(Var.gradH,sourceH);
		gradientSend(Var.gradV,sourceV);			
	}


}
