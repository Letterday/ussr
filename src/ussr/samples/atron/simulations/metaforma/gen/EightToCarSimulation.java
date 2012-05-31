//package ussr.samples.atron.simulations.metaforma.gen;
//import java.awt.Color;
//import java.util.ArrayList;
//
//import ussr.description.Robot;
//import ussr.description.geometry.VectorDescription;
//import ussr.description.setup.ModulePosition;
//import ussr.model.Controller;
//import ussr.model.debugging.ControllerInformationProvider;
//import ussr.physics.PhysicsFactory;
//import ussr.physics.PhysicsSimulation;
//import ussr.physics.jme.DebugInformationPicker;
//import ussr.samples.atron.ATRON;
//import ussr.samples.atron.ATRONBuilder;
//import ussr.samples.atron.network.ATRONReflectionEventController;
//import ussr.samples.atron.simulations.metaforma.lib.*;
//
//
//class EightToCarSimulation extends MetaformaSimulation {
//
//	public static void main( String[] args ) {
//		MetaformaSimulation.initSimulator();
//        new EightToCarSimulation().main();
//    }
//	
//	
//	protected Robot getRobot() {
//        ATRON a = new ATRON() {
//            public Controller createController() {
//            	return new EightToCarController();
//            }
//        };
//        return a;
//    }
//	
//	protected ArrayList<ModulePosition> buildRobot() {
//		return new ATRONBuilder().buildEight2(new VectorDescription(0,-5*ATRON.UNIT,0));
//	}
//	
//}
//
//
//
//class EightToCarController extends MetaformaController implements ControllerInformationProvider {
//
//	public void handleStates () {
//    switch (state) {
//    	
//    	case 0: 
//    		disconnect (Module.Floor_0, Module.Floor_1);
//    		break;
//    	
//    	case 1:
//    		disconnect (Module.Floor_3, Module.Floor_5);
//    		break;
//    		
//    	case 2: 
//    		rotate (Module.Floor_3, -90);
//    		break;
//    	
//    	case 3: 
//    		rotate (Module.Floor_4, 90);
//    		break;
//    	
//    	case 4: 
//    		connect (Module.Floor_4, Module.Floor_6);
//    		break;	
//    		
//     	case 5: 
//     		rotate (Module.Floor_1, -90);
//    		break;	
//    		
//     	case 6: 
//     		rotate (Module.Floor_4, 180);
//    		break;	
//    			
//    		
//    	case 14: 
//        	connect (Module.Floor_4, Grouping.Floor);
//        	break;
//    		
//    	case 15: 
//    		disconnect (Module.Floor_4, Module.Floor_6);
//    		break;
//    	case 116: 
//    		rotate (Module.Floor_5, -90);
//    		break;
//    	
//    }
//   }
//  
//  public void setColors () {
//		setModuleColors (Module.Floor_0,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
//		setModuleColors (Module.Floor_3,new Color[]{Color.decode("#009999"),Color.decode("#999900")}); 
//		setModuleColors (Module.Floor_6,new Color[]{Color.decode("#003333"),Color.decode("#333300")}); 
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
