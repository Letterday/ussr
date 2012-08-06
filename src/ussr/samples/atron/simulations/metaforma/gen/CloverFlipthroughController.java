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




class CloverFlipthroughSimulation extends MetaformaSimulation {
	

	public static void main( String[] args ) {
		MetaformaSimulation.initSimulator();
        new CloverFlipthroughSimulation().main();
    }
	
	
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
            	return new CloverFlipthroughController();
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



public class CloverFlipthroughController extends MetaformaRuntime implements ControllerInformationProvider {
	
	enum StateOperation implements IStateOperation {
		DEFAULT, MOVE;

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
				assign (1,1,Module.Uplifter_Left);
				assign (0,2,Module.Uplifter_Right);
				assign (0,0,Module.Uplifter_Top);
				assign (1,3,Module.Uplifter_Bottom);
			}

			if (doOnce(3,2)) {
				disconnectPart (Module.Uplifter_Left, NORTH&MALE&EAST|SOUTH&MALE&WEST);
				disconnectPart (Module.Uplifter_Right, NORTH&MALE&EAST);
			}
			
			if (doOnce(4,2)) {
				rotate (new ModuleSet().add(Module.Uplifter_Left).add(Module.Uplifter_Right),-90);
			}
 
			if (doOnce(5)) {
				disconnectPart (Module.Uplifter_Right,NORTH);
			}
			
			if (doOnce(6)) {
				rotate (Module.Uplifter_Top,-180);
			}

			
			if (doOnce(7,2)) {
				rotate (new ModuleSet().add(Module.Uplifter_Left).add(Module.Uplifter_Right),-90);
			}

			if (doOnce(8,2)) {
				rotate (Module.Uplifter_Bottom,-180);
				rotate (Module.Uplifter_Right,180);
			}
						
			if (doOnce(9,4)) {
				connect (Grouping.Floor,Grouping.Uplifter);
			}
			
			if (doOnce(10,4)) {
				if (getGrouping() == Grouping.Uplifter) {
					renameRestore();
					commit();
				}
			}
			
			if (doRepeat(11,1000,0,12)) {
				discoverNeighbors();
				if (nbs(WEST&MALE).size() == 2 && nbs().size() == 2) {
					unicast(new Packet(getId()).setType(Type.SYMMETRY),MALE&WEST);
					commit();
				}
			}
		}
		
   }
	
	
	private void assign(int h, int v, Module m) {
		if (varGet(Var.gradH) == h && varGet(Var.gradV) == v){
			renameStore();
			renameTo(m);
			commit();
		}
		
	}


	public void init () {
		visual.setModuleColors (Module.Uplifter_Left,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		visual.setModuleColors (Module.Uplifter_Right,new Color[]{Color.decode("#00AAAA"),Color.decode("#AAAA00")}); 
		visual.setModuleColors (Module.Uplifter_Top,new Color[]{Color.decode("#006666"),Color.decode("#666600")}); 
		visual.setModuleColors (Module.Uplifter_Bottom,new Color[]{Color.decode("#002222"),Color.decode("#222200")}); 
		
		visual.setDefaultColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		visual.setMessageFilter(Type.SYMMETRY.bit() |Type.DISCOVER.bit());
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
		
		if (type == Type.SYMMETRY) {
			if (checkState(stateOperation,stateInstruction)) {
				if (freqLimit(500,1)) {
					symmetryFix (isReq,sourceCon, destCon, data);
				}
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


	public void metaNeighborHook(int connectorNr) {
		
	}


	@Override
	public void metaNeighborHook(int connectorNr, byte metaId) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void receiveMetaMessage(MetaType type, byte source, byte dest,
			byte[] data) {
		// TODO Auto-generated method stub
		
	}
	

}
