package ussr.samples.atron.simulations.wouter;
import java.awt.Color;
import ussr.description.Robot;
import ussr.model.Controller;
import ussr.samples.atron.ATRON;

class WalkerSimulator extends ATRONSimulation {

	public static void main( String[] args ) {
//		ussr.physics.jme.robots.JMEATRONFactory.setConnectorMaxAlignmentForce(100f);
//		ussr.physics.jme.robots.JMEATRONFactory.setConnectorMaxAlignmentDistance(0.2f);
//		ussr.physics.jme.robots.JMEATRONFactory.setConnectorEpsilonAlignmentDistance(0.02f);

		ATRONSimulation.initSimulator();
		
        new WalkerSimulator().main();
    }
	
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
            	return new WalkerController();
            }
        };
        return a;
    }

	
}

class WalkerController extends ATRONPacketController{

	public void handleStates () {
    switch (state) {
    	case 0: 
    	renameFromTo(Module.Floor_0, Module.Walker_Head);
    	renameFromTo(Module.Floor_1, Module.Walker_Left);
    	renameFromTo(Module.Floor_2, Module.Walker_Right);
    	int i;
    	for(i=3; i<10; i++) {
    		renameFromTo (Module.getOnNumber(Grouping.Floor,i),Module.getOnNumber(Grouping.Floor,i-3));
    		}
    		break;
    	
    	case 1: 
    	disconnect (Module.Floor_0, Module.Floor_1);
    		break;
    	
    	case 2: 
    	disconnect (Module.Walker_Right, Grouping.Floor);
    		break;
    	
    	case 3: 
    
    	rotate (Module.Floor_0, -90);
    		break;
    	
    	case 4: 
    	rotate (Module.Walker_Left, -90);
    		break;
    	
    	case 5: 
    	connect (Module.Walker_Right, Module.Floor_3);
//    		if (getId() == Module.Walker_Right) {
//				connectionOnConn ((byte)6,true);
//			}
    		break;
    	
    	case 6: 
    	disconnect (Module.Walker_Left, Grouping.Floor);
    		break;
    	
    	case 7: 
    	rotate (Module.Walker_Right, 180);
    		break;
    	
    	case 8: 
    	connect (Module.Walker_Left, Module.Floor_6);
    		break;
    	
    	case 9: 
    	disconnect (Module.Walker_Right, Grouping.Floor);
    		break;
    	
    	case 10: 
    	rotate (Module.Walker_Right, 90);
    		break;
    	
    	case 11: 
    	disconnect (Module.Floor_4, Module.Floor_6);
    		break;
    	
    	case 12: 
    	rotate (Module.Floor_4, 90);
    		break;
    	
    	case 13: 
    	rotate (Module.Walker_Right, 90);
    		break;
    	
    	case 14: 
    	renameFromTo(Module.Walker_Head, Module.Floor_9);
    	renameFromTo(Module.Walker_Left, Module.Floor_7);
    	renameFromTo(Module.Walker_Right, Module.Floor_8);
    		break;
    	
    	
    }
   }
  
  public void setColors () {
		setModuleColors (Module.Walker_Head,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		setModuleColors (Module.Walker_Left,new Color[]{Color.decode("#009999"),Color.decode("#999900")}); 
		setModuleColors (Module.Walker_Right,new Color[]{Color.decode("#003333"),Color.decode("#333300")}); 
		
		addStructureColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		
	}
}

enum Grouping {Walker,Floor,}

enum Module {ALL,Walker_Head,Walker_Left,Walker_Right,Floor_0,Floor_1,Floor_2,Floor_3,Floor_4,Floor_5,Floor_6,Floor_7,Floor_8,Floor_9;
public Grouping getGrouping () {
		return Grouping.valueOf(name().split("_")[0]);
	}
	
	public static Module getOnNumber (Grouping s, int i) {
		return valueOf(s.toString() + "_" + i);
	}
	
	public byte ord() {
		return (byte)ordinal();
	}
	
	public boolean belongsTo (Grouping s) {
		return name().startsWith(s.name());
	}
}
