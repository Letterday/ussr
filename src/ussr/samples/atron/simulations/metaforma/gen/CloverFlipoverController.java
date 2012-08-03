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




class CloverFlipoverSimulation extends MetaformaSimulation {
	

	public static void main( String[] args ) {
		MetaformaSimulation.initSimulator();
        new CloverFlipoverSimulation().main();
    }
	
	
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
            	return new CloverFlipoverController();
            }
        };
        return a;
    }
	
	protected ArrayList<ModulePosition> buildRobot() {
		return new ATRONBuilder().buildBlocks("Floor_",false);
	}
	
}

 

public class CloverFlipoverController extends MetaformaRuntime implements ControllerInformationProvider {
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
		NONE, GRADIENT_H,GRADIENT_V;

		public byte index() {
			return (byte)ordinal();
		}
	 
		public IVar fromByte(byte b) {
			return values()[b];
		}
	}

	
	public void handleStates () {
		if (stateOperation(StateOperation.DEFAULT)) {
			waitAndDiscover();
			
			if (doOnce(0,true)) {
				if (getId() == Module.Floor_0) {
					commit();
				}
			}
			
			if (doOnce(1)) {
				setModuleColors(getId(), new Color[]{Color.CYAN,Color.CYAN});
				colorize();
			}
			
//			if (doOnce(0))  {
//				scheduler.setInterval("gradientCreate",200);
//				if (stateStartNext == 0) {
//					stateStartNext = time() + 5;
//				}
//				if (stateStartNext< time()) {
//					stateInstrBroadcastNext();
//				}
//			}
//	
//			  
//			if (doOnce(1) )  {
//				stateInstrBroadcastNext();
//				scheduler.setInterval("gradientCreate",10000);
//			}
//			
//			if (doOnce(2,10))  {
//				renameStore();
//				if (varGet(Var.GRADIENT_H) == 0 && varGet(Var.GRADIENT_V) < 3 || varGet(Var.GRADIENT_V) == 0 && varGet(Var.GRADIENT_H) < 3) {
//					renameGroup(Grouping.Left);
//					commit();
//				}
//				if (varGet(Var.GRADIENT_H) > 0 && varGet(Var.GRADIENT_V) > 0) {
//					renameGroup(Grouping.Right);
//					commit();
//				}
//			}
//			
//			if (doOnce(3,4)) {
//				disconnect(Grouping.Left, Grouping.Right);
//			}
//			
//
//			
//			if (doOnce(4,4,true)) {
//				if (varGet(Var.GRADIENT_H) == 3 && varGet(Var.GRADIENT_V) == 1) {
//					renameTo(Module.Right_Bottom);
//					commit();
//				}
//				if (varGet(Var.GRADIENT_H) == 1 && varGet(Var.GRADIENT_V) == 3) {
//					renameTo(Module.Right_Top);
//					commit();
//				}
//				if (varGet(Var.GRADIENT_H) == 2 && varGet(Var.GRADIENT_V) == 0) {
//					renameTo(Module.Left_Bottom);
//					commit();
//				}
//				if (varGet(Var.GRADIENT_H) == 0 && varGet(Var.GRADIENT_V) == 2) {
//					renameTo(Module.Left_Top);
//					commit();
//				}
//			}	
//			
//			
//			if (doOnce(5,2,false)) {
//				setModuleColors(getId(), new Color[]{Color.CYAN,Color.CYAN});
//				colorize();
//			
////				rotate(Module.Right_Bottom,-180);
////				rotate(Module.Right_Top,180);
//			}		
//			
//			if (doOnce(6,2)) {
//				rotate(Module.Left_Bottom,-180);
//				rotate(Module.Left_Top,180);
//			}
//			
//			if (doOnce(7,4)) {
//				connect(Grouping.Left,Grouping.Right);
//			}
//					
//			if (doRepeat(8,1000,2,12)) {
//				if (nbs(WEST).onGroup(Grouping.Left).size() == 1 && nbs(WEST).onGroup(Grouping.Right).size() == 1 && nbs().size() == 2) {
//					unicast(new Packet(getId()).setType(Type.SYMMETRY),MALE&WEST);
//					commit();
//				}
//			}
//			
//			if (doOnce(9,10)) {
//				if (getGrouping() != Grouping.Floor) {
//					renameRestore();
//				}
//			}
			
			
		}
		
		
		
//		
//		if (stateOperation(StateOperation.MOVE)) {
//	
//			if (stateInstruction(0)) {
//				for (int i=0; i<3; i++) {
//					discoverNeighbors();
//				}
//				stateInstrBroadcastNext();
//			}
//			
//			if (stateInstruction(1)) {
//				if (nbs(MALE&EAST).size() == 2 && nbs().size() == 2){
//					renameTo(Module.Clover_West);
//					commit(true);
//				}
//				if (nbs(EAST).contains(Module.Clover_West)){
//					renameTo(Module.Clover_South);
//					commit(true);
//				}
//				if (nbs(WEST).contains(Module.Clover_West)){
//					renameTo(Module.Clover_North);
//					commit(true);
//				}
//				if (nbs().contains(Module.Clover_North) && nbs().contains(Module.Clover_South)){
//					renameTo(Module.Clover_East);
//					commit(true);
//				}
//				consensusIfCompletedNextState(4); 
//			}
//			
//			if (stateInstruction(2)) {
//				disconnect (Module.Clover_West, Module.Clover_North, new RunSeq(this));
//			}
//			
//			if (stateInstruction(3)) {
//				rotate (Module.Clover_East,90, new RunSeq(this));
//			}
//				
//			if (stateInstruction(4)) {
//				rotate (Module.Clover_South,90, new RunSeq(this));
//			}
//			
//			if (stateInstruction(5)) {
//				rotate (Module.Clover_South,90, new RunSeq(this));
//			}
//			
//			if (stateInstruction(6)) {
//				rotate (Module.Clover_East,90, new RunSeq(this));
//			}
//			
//			if (stateInstruction(7)) {
//				connect (Module.Clover_South,Grouping.Floor, new RunSeq(this));
//			}
//			
//			if (stateInstruction(8)) {
//				connect (Module.Clover_West,Grouping.Floor, new RunSeq(this));
//			}
//			
//			if (stateInstruction(9)) {
//				disconnect (Module.Clover_North,Grouping.Floor, new RunPar(this));
//				disconnect (Module.Clover_East,Grouping.Floor, new RunPar(this));
//				consensusIfCompletedNextState(2);
//			}
//
//			if (stateInstruction(10)) {
//				rotate (Module.Clover_South,90, new RunSeq(this));
//			}
//			
//
//			if (stateInstruction(11)) {
//				rotate (Module.Clover_East,180, new RunSeq(this));
//			}
//			
//
//			if (stateInstruction(12)) {
//				rotate (Module.Clover_South,90, new RunSeq(this));
//			}
//			
//			
//		}
		
   }
	


	private boolean countdown(int i) {
		return true;
	}



	public void init () {
		setModuleColors (Module.Clover_North,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		setModuleColors (Module.Clover_South,new Color[]{Color.decode("#00AAAA"),Color.decode("#AAAA00")}); 
		setModuleColors (Module.Clover_West,new Color[]{Color.decode("#006666"),Color.decode("#666600")}); 
		setModuleColors (Module.Clover_East,new Color[]{Color.decode("#002222"),Color.decode("#222200")}); 
		
		setGroupingColors (Grouping.Left,new Color[]{Color.BLACK,Color.GRAY});
		setGroupingColors (Grouping.Right,new Color[]{Color.DARK_GRAY,Color.WHITE});
		
		setMessageFilter(Type.DISCOVER.bit() );
		 
		Packet.setController(this);
		IstateOperation = StateOperation.DEFAULT;
		Ivar = Var.NONE;
		
		varSet(Var.GRADIENT_H, MAX_BYTE);
		varSet(Var.GRADIENT_V, MAX_BYTE);
		
		stateOperationInit(StateOperation.DEFAULT);
	}
	
	
	public void gradientCreate() {
		broadcastDiscover();
		
		boolean isSourceH = (nbs(EAST&MALE).size() == 2 && nbs(WEST&SOUTH&MALE).isEmpty() || nbs(EAST&FEMALE).size() == 2 && nbs(WEST&NORTH&FEMALE).isEmpty());
		boolean isSourceV = (nbs(WEST&MALE).size() == 2 && nbs(EAST&SOUTH&MALE).isEmpty() || nbs(EAST&FEMALE).size() == 2 && nbs(WEST&SOUTH&FEMALE).isEmpty());

		gradientSend(Var.GRADIENT_H,isSourceH);
		gradientSend(Var.GRADIENT_V,isSourceV);
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

	
	


}
