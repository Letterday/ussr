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
		return new ATRONBuilder().buildGrid(4, 1, "Floor", false);
	}
	
}



class CloverController extends MetaformaController implements ControllerInformationProvider {

	public void handleStates () {
    switch (state) {
    	
    	case 0: 
    		disconnect (Module.Floor_0, Module.Floor_1);
    		break;
    	
    	case 1:
    		disconnect (Module.Floor_3, Module.Floor_5);
    		break;
    		
    	case 2: 
    		rotate (Module.Floor_3, -90);
    		break;
    	
    	case 3: 
    		rotate (Module.Floor_2, -90);
    		break;
    	
    	
        	
    	case 4: 
    		rotate (Module.Floor_5, 90);
    		break;
    		
    	case 14: 
        	connect (Module.Floor_4, Grouping.Floor);
        	break;
    		
    	case 5: 
    		disconnect (Module.Floor_4, Module.Floor_6);
    		break;
    	case 6: 
    		rotate (Module.Floor_5, -90);
    		break;
    	
    }
   }
  
  public void setColors () {
		setModuleColors (Module.Clover_North,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		setModuleColors (Module.Clover_South,new Color[]{Color.decode("#00AAAA"),Color.decode("#AAAA00")}); 
		setModuleColors (Module.Clover_West,new Color[]{Color.decode("#006666"),Color.decode("#666600")}); 
		setModuleColors (Module.Clover_East,new Color[]{Color.decode("#002222"),Color.decode("#222200")}); 
		
		addStructureColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		
	}




}
