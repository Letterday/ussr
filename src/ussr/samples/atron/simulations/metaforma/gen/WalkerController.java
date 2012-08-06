//package ussr.samples.atron.simulations.metaforma.gen;
//import java.awt.Color;
//import ussr.description.Robot;
//import ussr.model.Controller;
//import ussr.model.debugging.ControllerInformationProvider;
//import ussr.samples.atron.ATRON;
//import ussr.samples.atron.simulations.metaforma.gen.CloverFlipoverController.StateOperation;
//import ussr.samples.atron.simulations.metaforma.gen.CloverFlipoverController.Var;
//import ussr.samples.atron.simulations.metaforma.lib.*;
//
//
//class WalkerSimulation extends MetaformaSimulation {
//
//	public static void main( String[] args ) {
//		MetaformaSimulation.initSimulator();
//        new WalkerSimulation().main();
//    }
//	
//	
//	
//	protected Robot getRobot() {
//        ATRON a = new ATRON() {
//            public Controller createController() {
//            	return new WalkerController();
//            }
//        };
//        return a;
//    }
//
//	
//}
//
//
//
//public class WalkerController extends MetaformaRuntime implements ControllerInformationProvider {	
//	
//	enum StateOperation implements IStateOperation {
//		DEFAULT, GETUP, GETDOWN, GETDOWN_CHECK, WALK;
//
//		public byte ord() {
//			return (byte)ordinal();
//		}
//
//		public IStateOperation fromByte(byte b) {
//			return values()[b];
//		}
//	}
//	
//	enum Var implements IVar {
//		gradH,gradV,NONE;
//
//		public byte index() {
//			return (byte)ordinal();
//		}
//
//		public Var fromByte(byte b) {
//			return values()[b];
//		}
//	}
//	
//
//	
//	
//	
//	
//	// Event handlers
//	public void handleEvents () {
//		if (getId() == Module.Floor_Uplifter) {
//			
//			if (!nbs().connected().onGroup(Grouping.Walker).exists()) {
//				visual.print("Uplifter event fires!");
//				rotateTo(0);
//				discoverNeighbors();
//				
//				for (Module m2 : nbs().disconnected().maleAlignedWithFemale().onGroup(Grouping.Floor).modules()) {
//    			//TODO:	connection(m2,true);
//    			}
//				if (nbs().onGroup(Grouping.Floor).connected().size() == 2) {
//					renameRestore();
//				}
//			}
//		}
//		
//		/////////////////////////////////////////////////////////////
//		
////		if (getId() == Module.Floor_UplifterLeft) {
////			
////			if (nbs().connected().onGroup(Grouping.Floor).size() == 2) {
////				notification("UplifterLeft event fires!");
////				renameRestore();
////			}
////		}
//		
//		/////////////////////////////////////////////////////////////
//		
//		if (getId() == Module.Floor_Downlifter) {
//
//			if (!nbs().onGroup(Grouping.Walker).exists()) {
//				visual.print("Downlifter event fires!");
//				renameRestore();
//			}
//		}
//		
//		/////////////////////////////////////////////////////////////
//		
////		if (getId() == Module.Floor_DownlifterLeft) {
////			if (!nbs().contains(Module.Floor_Downlifter)) {
////				notification("DownlifterLeft event fires!");
////				renameRestore();
////			}
////		}
//	}
//	
//	
//	
//	public void handleStates () {
//		
//		if (stateOperation(StateOperation.GETUP)) {
//			
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
//			if (doOnce(2,4)) {
//				if (varGet(Var.gradH) == 0 && varGet(Var.gradV) == 1){
//					renameTo(Module.Walker_Head);
//					commit();
//				}
//				
//				if (nbs(SOUTH&FEMALE&WEST).contains(Module.Walker_Head)) {
//					renameTo(Module.Walker_Left);
//					commit();
//				}
//				if (nbs(SOUTH&FEMALE&EAST).contains(Module.Walker_Head)) {
//					renameTo(Module.Walker_Right);
//					commit();
//				}
//				if (nbs(EAST).contains(Module.Walker_Left) && nbs(EAST).contains(Module.Walker_Right)) {
//					renameTo(Module.Floor_Uplifter);
//					commit();
//				}
//			}
//
//    		if (doOnce(3)) {
//    			disconnect(Grouping.Floor, Module.Walker_Right);
//    		}
//    		
//    		if (doOnce(4)) {
//    			disconnectDiagonal(Module.Floor_Uplifter);
//    		}
//    		
//    		if (doOnce(5)) {
//    			rotate(Module.Walker_Left,90);
//    		}
//    		
//    		if (doOnce(6)) {
//    			rotate(Module.Floor_Uplifter,90);
//    		}
//    			
//    		if (doOnce(7)) {
//    			stateOperationBroadcast(StateOperation.WALK);
//    		}
//		}
//		
//		if (stateOperation(StateOperation.WALK)) {
//			
//			if (doOnce (0)){
//				connect(Module.Walker_Right, Grouping.Floor);
//			}
//			
//			if (doOnce (1)){
//				disconnect(Module.Walker_Left, Grouping.Floor);
//			}
//			
//			if (doOnce (2)){
//				rotate(Module.Walker_Head, 45);
//			}
//			
//			if (doOnce (3)){
//				rotate(Module.Walker_Right, 180);
//			}
//			
//			if (doOnce (4)){
//				rotate(Module.Walker_Head, -45);
//			}
//		}
//		
//		
//	
//	    
//	}
//  
//
//	
//
//
//	private void disconnectDiagonal(Module m) {
//		if (getId() == m) {
//			if (nbs((NORTH&EAST)|(SOUTH&WEST)).connected().size() == 1) {
//				disconnectPart(m,(NORTH&EAST)|(SOUTH&WEST));
//			}
//			if (nbs((NORTH&WEST|SOUTH&EAST)).connected().size() == 1) {
//				disconnectPart(m,(NORTH&WEST)|(SOUTH&EAST));
//			}
//			commit();
//		}
//		
//	}
//
//
//
//	public void init () {
//		visual.setModuleColors (Module.Walker_Head,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
//		visual.setModuleColors (Module.Walker_Left,new Color[]{Color.decode("#009999"),Color.decode("#999900")}); 
//		visual.setModuleColors (Module.Walker_Right,new Color[]{Color.decode("#003333"),Color.decode("#333300")}); 
//		
//		visual.setModuleColors (Module.Floor_Uplifter,new Color[]{Color.decode("#FF9900"),Color.decode("#9900FF")});		
//		visual.setModuleColors (Module.Floor_Downlifter,new Color[]{Color.decode("#FF33CC"),Color.decode("#33CCFF")});
//
//		visual.setDefaultColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
//		visual.setMessageFilter(Type.SYMMETRY.bit() | Type.STATE_OPERATION_NEW.bit() | Type.GRADIENT.bit());
//
//		
//		Packet.setController(this);
//		IstateOperation = StateOperation.DEFAULT;
//		Ivar = Var.NONE;
//		
//		varSet(Var.gradV, MAX_BYTE);
//		varSet(Var.gradH, MAX_BYTE);
//		
//		stateOperationInit(StateOperation.GETUP);
//	}
//
//	public void gradientCreate() {
//		broadcastDiscover();
//		
//		boolean isSourceH = nbs(WEST&MALE).size() == 2 && nbs().size() == 2;
//		boolean isSourceV = nbs(WEST&FEMALE).size() == 2 && nbs().size() == 2;
//
//		gradientSend(Var.gradH,isSourceH);
//		gradientSend(Var.gradV,isSourceV);
//	}
//
//
//
//
//	
//
//	protected void receiveMessage(Type type, IStateOperation stateOperation, byte stateInstruction, boolean isReq, byte sourceCon, byte destCon, byte metaId, byte[] data) {
//		if (type == Type.GRADIENT) {
//			if (varGet(Var.NONE.fromByte(data[0])) > data[1]) {
//				varSet(Var.NONE.fromByte(data[0]),data[1]);
//				scheduler.invokeNow("gradientCreate");
//				
//			}
//		}
//		
//		if (type == Type.SYMMETRY) {
//			if (checkState(stateOperation,stateInstruction)) {
//				if (freqLimit(500,1)) {
//					symmetryFix (isReq,sourceCon, destCon, data);
//				}
//			}
//		}
//		
//	}
//
//
//
//
//
//}
