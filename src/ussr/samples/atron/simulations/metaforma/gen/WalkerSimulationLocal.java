package ussr.samples.atron.simulations.metaforma.gen;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import ussr.description.Robot;
import ussr.model.Controller;
import ussr.model.debugging.ControllerInformationProvider;
import ussr.physics.PhysicsFactory;
import ussr.physics.PhysicsSimulation;
import ussr.physics.jme.DebugInformationPicker;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.network.ATRONReflectionEventController;
import ussr.samples.atron.simulations.metaforma.lib.*;


class WalkerSimulationLocal extends MetaformaSimulation {

	public static void main( String[] args ) {
		MetaformaSimulation.initSimulator();
        new WalkerSimulationLocal().main();
    }
	
	
	
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
            	return new LocalWalkerController();
            }
        };
        return a;
    }

	
}



class LocalWalkerController extends MetaformaController implements ControllerInformationProvider {

	
	private static final byte HORIZONTAL = 0;
	private static final byte VERTICAL = 1;
	private static final int ELECT = 0;
	private static final int WALK = 1;
	private static final int WALK2 = 5;
	private static final int GETUP = 2;
	private static final int GETDOWN = 3;
	private static final int GLOBAL_META_EXISTS = 25;
	private static final int REPAIR = 6;
	
	
	
	
	public String getOpStateName () {
		switch (stateOperation) {
			case ELECT:
				return "ELECT";
			
			case WALK:
				return "WALK";
			
			case GETUP:
				return "GETUP";
			
			case GETDOWN:
				return "GETDOWN";
				
			case WALK2:
				return "WALK2";
				
			case REPAIR:
				return "REPAIR";
			
		}
		return null;
	}
	
	
	
	
	public void handleStates () {
		if (stateOperation == ELECT) {
			
			if (stateInstr == 0) {
				if (isC(6,2,true)) {
					gradientCreate(HORIZONTAL,0);
				}
				
				if (isC(3,7,true)) {
					gradientCreate(VERTICAL,0);
				}
				
				nextInstrState();
			}
			
			
			if (stateInstr == 1 && !stateCurrentFinished) {
				if (getGradient(HORIZONTAL) == 0 && getGradient(VERTICAL) == 1){
					renameTo(Module.Walker_Head);
					stateCurrentFinished = true;
				}
				
				if (getConnectorToNb(Module.Walker_Head,false) == 3) {
					renameTo(Module.Walker_Left);
					stateCurrentFinished = true;
				}
				if (getConnectorToNb(Module.Walker_Head,false) == 1) {
					renameTo(Module.Walker_Right);
					stateCurrentFinished = true;
				}
				if (hasNeighbor(Module.Walker_Left,true) && hasNeighbor(Module.Walker_Right,true) && getId() != Module.Walker_Head) {
					renameTo(Module.Floor_Uplifter);
					stateCurrentFinished = true;
				}
				if (getConnectorToNb(Module.Floor_Uplifter,false) == 3) {
					renameTo(Module.Floor_UplifterLeft);
					stateCurrentFinished = true;
					broadcastNewOperationState(GETUP);
				}
				waitAndDiscover();
			}
			
//			
////			if (getGlobal(GLOBAL_META_EXISTS) == 1)
////				return;
//			
//			if (!currentOperationDone) {
//				//info.addNotification("xx"+getGlobal(GLOBAL_META_EXISTS));
//				waitAndDiscover();
//				if (isC(6,2,true) && getNeighborInfo(NORTH_MALE_R,NEIGHBOR_COUNT) == 2) {
//					renameTo(Module.Walker_Head);
//					discoverNeighbors();
//					currentOperationDone = true;
//					setGlobal(GLOBAL_META_EXISTS,1);
//				}
			
//			}
	   }

	    if (stateOperation == GETUP) {
    		if (stateInstr == 0) {
    			// disconnect(Floor, Walker_Right);
    			discoverNeighbors();
    			delay(500); 
    			
    			if (!isPendingState(1) && getId() == Module.Walker_Right) {
    				while (exists(connected(onGroup(nbs(),Grouping.Floor))) && timeSpentInState() < 10) {
    					info.addNotification("time spent so  far: " + timeSpentInState());
	    				for (Module m2 : maleAligned(onGroup(nbs(),Grouping.Floor)).keySet()) {
	        				disconnect(m2);
	        			}
	    				waitAndDiscover();
	    			}
    				
    				nextPendingState(1);

	    		}
    			if (getGrouping() == Grouping.Floor)  {
    				while (contains(connected(nbs()),Module.Walker_Right)){// && timeSpentInState() < 10) {
    					info.addNotification("time spent so  far: " + timeSpentInState());
	        			if (isMale(getConnectorToNb(Module.Walker_Right, false))) {
	        				disconnect(Module.Walker_Right);
	        			}
	    				waitAndDiscover();
	    			}
    			}
    			
    			
    			if (!isPendingState(2) && getId() == Module.Floor_Uplifter) {
    				
    				while (hasNeighbor(Module.Floor_UplifterLeft, true)) {
    					waitAndDiscover();
    					disconnect(Module.Floor_UplifterLeft);
    				}
    				nextPendingState(2);
    				waitForPendingState(1);
    				nextInstrState();
    			}
	    	}
	        	
	     
    		if (stateInstr == 1) 
	    	{
    			if (!isPendingState(1) && getId() == Module.Floor_Uplifter) {
    				rotate(90);
    				nextPendingState(1);
    			}
    			if (!isPendingState(2) && getId() == Module.Walker_Left) {
    				rotate(90);
    				nextPendingState(2);
    				waitForPendingState(7);
    				broadcastNewOperationState(WALK);
    			}
    			if (!isPendingState(4) && getId() == Module.Floor_UplifterLeft) {
    				renameTo(Module.Floor_6);
    				nextPendingState(4);
    			}
    			delay(1000);
    			
	    	}
		}    		    
		
		if (stateOperation == WALK || stateOperation == WALK2) {
		    switch (stateInstr) {
			    case 0:
		    		if (getId() == Module.Walker_Right) {
		    			waitAndDiscover();
		    			if (exists(onGroup(nbs(),Grouping.Floor))) { 
		    				if (!exists(maleAligned(disconnected(onGroup(nbs(),Grouping.Floor))))){
		    					rotate(90);
		    					waitAndDiscover();
		    				}
			    			while (exists(maleAligned(disconnected(onGroup(nbs(),Grouping.Floor))))) {
			    				for (Module nb: maleAligned(disconnected(onGroup(nbs(),Grouping.Floor))).keySet()) {
			    					connect(nb);
			    				}
			    				waitAndDiscover();
			    			}
			    			nextInstrState();
		    			}
		    			else {
		    				broadcastNewOperationState(GETDOWN);
		    			}
		    		}
		    		
		    	break;
		    	
		    	case 1:
		    		if (getId() == Module.Walker_Left) {
		    			waitAndDiscover();
		    			while (exists(connected(onGroup(nbs(),Grouping.Floor)))) {
		    				for (Module nb: maleAligned(connected(onGroup(nbs(),Grouping.Floor))).keySet()) {
		    					disconnect(nb);
		    				}
		    				waitAndDiscover();
		    			}
		    			nextInstrState();
		    		}
		    		
		    		if (getGrouping() == Grouping.Floor) {
		    			waitAndDiscover();
		    			while (hasNeighbor(Module.Walker_Left, true)) {
		    				disconnect(Module.Walker_Left);
		    				waitAndDiscover();
		    			}
		    		}
		    		
		    	break;
		    	
		    	case 2:
		    		if (getId() == Module.Walker_Right) {
		    			rotate(180);
		    			nextInstrState();
		    		}
		    	break;
		    	
		    	case 3:
		    		renameSwitch(Module.Walker_Left,Module.Walker_Right);
		    	break;
		    	
		    	case 4:
		    		if (getId() == Module.Walker_Head) {
		    			if (stateOperation == WALK)
		    				broadcastNewOperationState(WALK2);
		    			else
		    				broadcastNewOperationState(WALK);
		    		}
		    		break;
		    		
			    }
			}
		if (stateOperation == GETDOWN) {
			
			if (stateInstr == 0 && !stateCurrentFinished) {
				waitAndDiscover();
				
				if (getGrouping() == Grouping.Floor && hasNeighbor(Module.Walker_Left, true)) {
					renameTo(Module.Floor_Downlifter);
					stateCurrentFinished = true;
				}
				
				if (getConnectorToNb(Module.Floor_Downlifter,false) == 7) {
					renameTo(Module.Floor_DownlifterLeft);
					stateCurrentFinished = true;
					nextInstrState();
				}
				
			}
			
			if (stateInstr == 1) {
				waitAndDiscover();
				if (getId() == Module.Floor_Downlifter) {
					while (hasNeighbor(Module.Floor_DownlifterLeft, true)) {
    					waitAndDiscover();
    					disconnect(Module.Floor_DownlifterLeft);
    				}
					nextInstrState();
					
				}
			}
			
			if (stateInstr == 2) {
				if (getId() == Module.Walker_Left) {
					rotate(-90);
					nextInstrState();
				}
			}
			if (stateInstr == 3) {
				if (!isPendingState(1) && getId() == Module.Floor_Downlifter) {
					rotate(90);
					nextPendingState(1);
				}
				if (!isPendingState(2) && getId() == Module.Walker_Right) {
					rotateTo(0);
					nextPendingState(2);
					waitForPendingState(3);
					nextInstrState();
				}
			}
			if (stateInstr == 4) {
				if (!isPendingState(1)) {
					waitAndDiscover();
					if (getId() == Module.Walker_Right) {
						while (exists(disconnected(onGroup(nbs(),Grouping.Floor)))) {
	    					for (Module nb: maleAligned(disconnected(onGroup(nbs(),Grouping.Floor))).keySet()) {
		    					connect(nb);
		    				}
	    					waitAndDiscover();
	    				}
						nextInstrState();
					}
					
					if (getGrouping() == Grouping.Floor) {
						waitAndDiscover();
		    			while (disconnected(nbs()).containsKey(Module.Walker_Right)) {
		    				connect(Module.Walker_Right);
		    				waitAndDiscover();
		    			}
					}
				}
				
				
			}
			if (stateInstr == 5) {
				if (getId() == Module.Walker_Left) {
					discoverNeighbors();
					while (exists(connected(onGroup(nbs(),Grouping.Floor)))) {
						for (Module nb: maleAligned(connected(onGroup(nbs(),Grouping.Floor))).keySet()) {
	    					disconnect(nb);
	    				}
    					waitAndDiscover();
	    			}
					nextInstrState();
				}
			}
			if (stateInstr == 6) {
				if (!isPendingState(1) && getId() == Module.Walker_Left) {
					rotate(90);
					nextPendingState(1);
				}
				if (!isPendingState(2) && getId() == Module.Floor_Downlifter) {
					rotate(90);
					nextPendingState(2);
					waitForPendingState(3);
					nextInstrState();
				}
			}
			if (stateInstr == 7) {
				if (getId() == Module.Floor_Downlifter) {
					waitAndDiscover();
	    			while (disconnected(nbs()).containsKey(Module.Walker_Left) || disconnected(nbs()).containsKey(Module.Floor_DownlifterLeft)) {
	    				connect(Module.Walker_Left);
	    				connect(Module.Floor_DownlifterLeft);
	    				waitAndDiscover();
	    			}
	    			nextInstrState();
					
				}
			}
			
			if (stateInstr == 8 && !stateCurrentFinished) {
				
				
				if (getId() == Module.Walker_Left) {
					renameTo(Module.Floor_0);
				}
				if (getId() == Module.Walker_Right) {
					renameTo(Module.Floor_1);
				}
				if (getId() == Module.Walker_Head) {
					renameTo(Module.Floor_2);
					broadcastNewOperationState(REPAIR);
				}
				stateCurrentFinished = true;
			}
		}
		
		if (stateOperation == REPAIR) {
			if (stateInstr == 0) {
				gradients.put(HORIZONTAL, Byte.MAX_VALUE);
				gradients.put(VERTICAL, Byte.MAX_VALUE);
				if (!isPendingState(1) && getId() == Module.Floor_Uplifter) {
					discoverNeighbors();
					rotateTo(0);
					
					nextPendingState(1);
				}
				
				if (!isPendingState(2) && getId() == Module.Floor_Downlifter) {
					nextPendingState(2);
					renameTo(Module.Floor_22);
				}
				if (!isPendingState(4) && getId() == Module.Floor_DownlifterLeft) {
					nextPendingState(4);
					renameTo(Module.Floor_23);
					waitForPendingState(7);
					nextInstrState();
				}
			}
			
			if (stateInstr == 1) {
				if (getId() == Module.Floor_Uplifter) {
					discoverNeighbors();
					while (exists(maleAligned(disconnected(onGroup(nbs(), Grouping.Floor))))) {
						for (Module nb: maleAligned(disconnected(onGroup(nbs(), Grouping.Floor))).keySet()) {
	    					connect(nb);
	    				}
	    				waitAndDiscover();
	    			}
					
					renameTo(Module.Floor_4);
					broadcastNewOperationState(ELECT);
				}
			}
		}

	    
//	    if (masterState == repair) {
//		    if (getGrouping() == Grouping.Floor)  {
//		    	if (isC(0) || isC(2) || isC(4) || isC(6)) {
//		    		rotateTo(0);
//		    		if (isC(0)) connect(4);
//		    		if (isC(2)) connect(6);
//		    		if (isC(4)) connect(2);
//		    		if (isC(6)) connect(0);
//		    	}
//		    }
//	    }

	    
	  //rotate(Module.Walker_Head, -45);
//    	case 9: 
//    	rotate (Module.Walker_Left, -90);
//    		break;
//    	
//    	case 10: 
//    		rotate(Module.Walker_Head,45);
//    		break;
//    	
//    		
//    	case 11: 
//    		connect (Module.Walker_Right, Grouping.Floor);
//    		break;
//    	
//    	case 12: 
//    		disconnect (Module.Walker_Left, Grouping.Floor);
//    		break;
//    	
//    	case 13: 
//    		rotate(Module.Walker_Head, -45);
//    		break;
//    		
//    	case 14: 
//    		rotate (Module.Walker_Right, 180);
//    		break;
//    	
//    	case 15: 
//    		rotate(Module.Walker_Head, 45);
//    		break;
//    		
//    	case 16: 
//    		connect (Module.Walker_Left, Grouping.Floor);
//    		break;
//    	
//    	case 17: 
//    		disconnect (Module.Walker_Right, Grouping.Floor);
//    		break;
//    	
//    	case 18: 
//    		rotate(Module.Walker_Head, -45);
//    		break;
//    		
//    	case 19: 
//    		rotate (Module.Walker_Left, -90);
//    		break;
//    	
//    	case 20: 
//    		rotate(Module.Walker_Head, 45);
//    		break;	
//    	
//    	case 21: 
//    		disconnect (Module.Floor_4, Module.Floor_6);
//    		break;
//	    }
	    
	}
  

	
		


	


	public void setColors () {
		setModuleColors (Module.Walker_Head,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		setModuleColors (Module.Walker_Left,new Color[]{Color.decode("#009999"),Color.decode("#999900")}); 
		setModuleColors (Module.Walker_Right,new Color[]{Color.decode("#003333"),Color.decode("#333300")}); 
		
		setModuleColors (Module.Floor_Uplifter,new Color[]{Color.decode("#FF9900"),Color.decode("#9900FF")});
		setModuleColors (Module.Floor_UplifterLeft,new Color[]{Color.decode("#FF0099"),Color.decode("#0099FF")});
		
		setModuleColors (Module.Floor_Downlifter,new Color[]{Color.decode("#FF33CC"),Color.decode("#33CCFF")});
		setModuleColors (Module.Floor_DownlifterLeft,new Color[]{Color.decode("#FF0099"),Color.decode("#9900FF")});
		
		
		addStructureColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		
	}




}
