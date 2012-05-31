//package ussr.samples.atron.simulations.metaforma.gen;
//import java.awt.Color;
//import java.util.ArrayList;
//
//import ussr.description.Robot;
//import ussr.description.geometry.VectorDescription;
//import ussr.description.setup.ModulePosition;
//import ussr.model.Controller;
//import ussr.model.debugging.ControllerInformationProvider;
//
//import ussr.samples.atron.ATRON;
//import ussr.samples.atron.ATRONBuilder;
//
//import ussr.samples.atron.simulations.metaforma.lib.*;
//
//
//class WalkerBigSimulation extends MetaformaSimulation {
//
//	public static void main( String[] args ) {
//		MetaformaSimulation.initSimulator();
//        new WalkerBigSimulation().main();
//    }
//	
//	@Override
//	protected ArrayList<ModulePosition> buildRobot() {
//		return new ATRONBuilder().buildRectangle(7,7,new VectorDescription(0,-5*ATRON.UNIT,0),"Floor_",false);
//	}
//	
//	protected Robot getRobot() {
//        ATRON a = new ATRON() {
//            public Controller createController() {
//            	return new WalkerBigController();
//            }
//        };
//        return a;
//    }
//
//	
//}
//
//
//class WalkerBigController extends MetaformaController implements ControllerInformationProvider {
//
//	public void handleStates () {
//    switch (state) {
//    	case 0: 
//    	renameFromTo(Module.Floor_0, Module.Walker_Left);
//    	renameFromTo(Module.Floor_3, Module.Walker_Head);
//    	renameFromTo(Module.Floor_7, Module.Walker_Right);
//    	increaseState();
////    		renameFromTo(Module.Floor_10, Module.Walker_Left2);
////    		renameFromTo(Module.Floor_14, Module.Walker_Left);
////        	renameFromTo(Module.Floor_17, Module.Walker_Head);
////        	renameFromTo(Module.Floor_21, Module.Walker_Right);
////    		disconnect(Module.Walker_Left2,Grouping.Floor);
//    		
//    	break;
//    	
//    	case 1:
////    		state = 17;
//    		disconnect (Module.Walker_Right, Grouping.Floor);
//    		break;
//    	
//    	case 2: 
//    		disconnect (Module.Floor_4, Module.Floor_1);
//    		break;
//    	
//    	case 3: 
//    		rotate (Module.Floor_4, 90);
//    		break;
//    
//    	case 4: 
//    		rotate (Module.Walker_Left, 90);
//    		break;
//    	
//    	case 5: 
//    		connect (Module.Walker_Right, Grouping.Floor);
//    		break;
//    	
//    	case 6: 
//    		disconnect (Module.Walker_Left, Grouping.Floor);
//    		break;
//    	
//    	case 7: 
//    		rotate (Module.Walker_Right, 180);
//    		break;
//    		
//    	case 8: 
//    		rotate (Module.Walker_Left, 90);
//    		break;
//    		
//    	case 9:
//    		connect (Module.Walker_Left, Module.Floor_6);
//    		break;
//    	
//    	case 10: 
//    		disconnect (Module.Walker_Right, Grouping.Floor);
//    		break;
//    		
//    	case 11: 
//    		rotate (Module.Walker_Left, 90);
//    		break;
//    		
//    	case 12: 
//    		disconnect (Module.Floor_6, Module.Floor_2);
//    		break;
//    	
//    	case 13: 
//    		rotate (Module.Floor_6, 90);
//    		break;
//    		
//    	case 14:
//    		connect (Module.Walker_Right, Grouping.Floor);
//    		break;
//    	
//    	case 15:
//    		renameFromTo(Module.Walker_Left, Module.Floor_0);
//        	renameFromTo(Module.Walker_Head, Module.Floor_3);
//        	renameFromTo(Module.Walker_Right, Module.Floor_7);
//        	increaseState();
//        	break;
//    	case 16:
//    		renameFromTo(Module.Floor_10, Module.Walker_Left2);
//    		renameFromTo(Module.Floor_14, Module.Walker_Left);
//        	renameFromTo(Module.Floor_17, Module.Walker_Head);
//        	renameFromTo(Module.Floor_21, Module.Walker_Right);
//        	increaseState();
//        	break;
//    	case 17: 
//    		disconnect (Module.Walker_Left, Grouping.Floor);
//    		break;
//    	case 18: 
//    		disconnect (Module.Floor_22, Module.Floor_18);
//    		break;
//    	case 19: 
//    		rotate (Module.Floor_18, -90);
//    		break;
//    	case 20: 
//    		rotate (Module.Walker_Right, -90);
//    		break;
//    	case 21:
//    		connect (Module.Walker_Left, Grouping.Floor);
//    		break;
//    	case 22: 
//    		disconnect (Module.Walker_Right, Grouping.Floor);
//    		break;
//    	case 23: 
//    		rotate (Module.Walker_Left, 180);
//    		break;
//    	case 24: 
//    		rotate (Module.Walker_Right, 90);
//    		break;
//    	case 25:
//    		connect (Module.Walker_Right, Grouping.Floor);
//    		break;
//    	case 26: 
//    		disconnect (Module.Walker_Left, Grouping.Floor);
//    		break;
//    	case 27: 
//    		rotate (Module.Walker_Right, -90);
//    		break;
//    	case 28: 
//    		rotate (Module.Walker_Left, 90);
//    		break;
//    	case 29:
//    		connect (Module.Walker_Left, Module.Floor_13);
//    		break;
//    	case 30: 
//    		disconnect (Module.Walker_Right, Grouping.Floor);
//    		break;
//    	case 31: 
//    		disconnect (Module.Floor_13, Module.Floor_16);
//    		break;
//    	case 32: 
//    		rotate (Module.Floor_13, -90);
//    		break;
//    	case 33:
//    		connect (Module.Walker_Left, Grouping.Floor);
//    		break;
//    	case 34:
//    		connect (Module.Walker_Right, Grouping.Floor);
//    		break;
//    }
//   }
//  
//  public void setColors () {
//		setModuleColors (Module.Walker_Head,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
//		setModuleColors (Module.Walker_Left,new Color[]{Color.decode("#009999"),Color.decode("#999900")}); 
//		setModuleColors (Module.Walker_Right,new Color[]{Color.decode("#003333"),Color.decode("#333300")}); 
//		setModuleColors (Module.Walker_Left2,new Color[]{Color.decode("#003333"),Color.decode("#AA3333")});
//		
//		addStructureColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
//		
//	}
//
//public boolean connectConstraint(int i, int j) {
//	return false;
//}
//
//
//
//
//}
