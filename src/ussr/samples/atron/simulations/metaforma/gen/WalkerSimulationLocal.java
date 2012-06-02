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
	        	
	      
//    		if (stateInstr == 1) {
//    			if (getId() == Module.Walker_Left) {
//	    			if (errInPreviousState == 1) {
//	    				while (hasNeighbor(Grouping.Floor, true) && timeSpentInState() < 10) {
//	        				for (Module m2 : getNeighbors(Grouping.Floor).keySet()) {
//	            				connection(m2, false);
//	            			}
//	        				waitAndDiscover();
//	    				}
//	    			}
//	    			nextInstrState();
//	    		}
//    			else if (getGrouping() == Grouping.Floor)  {
//    				if (errInPreviousState == 1) {
//	    				while (hasNeighbor(Module.Walker_Left,true)){// && timeSpentInState() < 10) {
//	    					info.addNotification("time spent so  far: " + timeSpentInState());
//		        			connection(Module.Walker_Left, false);
//		    				waitAndDiscover();
//		    			}
//    				}
//    			}
//	    	}
//        	
//	    	if (stateInstr == 2) 
//	    	{
//        		if (getId() == Module.Floor_Uplifter) {
//        			
//        			while (!(hasNeighbor(Module.Walker_Left,false) && hasNeighbor(Module.Walker_Right,false))) {
//	    				waitAndDiscover();
//	    			}
//        			
//        			
//        			if (hasNeighbor(Module.Walker_Left,true)) {
//        				disconnect(Module.Floor_UplifterLeft);
//        				while (hasNeighbor(Module.Floor_UplifterLeft, true)){
//        					yield();
//        				}
//        				info.addNotification("##Left connected");
//        			}
//        			else if (hasNeighbor(Module.Walker_Right,true)) {
//        				disconnect(Module.Floor_UplifterRight);
//        				while (hasNeighbor(Module.Floor_UplifterRight, true)){
//        					yield();
//        				}
//        				info.addNotification("##Right connected");
//        			}
//        			
//        			nextInstrState();
//        		}
//	    	}
    		if (stateInstr == 1) 
	    	{
    			if (!isPendingState(1) && getId() == Module.Floor_Uplifter) {
    				rotate(90);
    				renameTo(Module.Floor_4);
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
	    	}
		}    		    
		
		if (stateOperation == WALK || stateOperation == WALK2) {
		    switch (stateInstr) {
			    case 0:
		    		if (getId() == Module.Walker_Right) {
		    			waitAndDiscover();
		    			if (exists(getNeighbors(Grouping.Floor))) { 
		    				if (!exists(maleAligned(disconnected(getNeighbors(Grouping.Floor))))){
		    					rotate(90);
		    					waitAndDiscover();
		    				}
			    			while (exists(maleAligned(disconnected(getNeighbors(Grouping.Floor))))) {
			    				for (Module nb: maleAligned(disconnected(getNeighbors(Grouping.Floor))).keySet()) {
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
		    			while (exists(connected(getNeighbors(Grouping.Floor)))) {
		    				for (Module nb: maleAligned(connected(getNeighbors(Grouping.Floor))).keySet()) {
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
					renameTo(Module.Floor_Uplifter);
					stateCurrentFinished = true;
				}
				
				if (getConnectorToNb(Module.Floor_Uplifter,false) == 7) {
					renameTo(Module.Floor_UplifterLeft);
					stateCurrentFinished = true;
					nextInstrState();
				}
				
			}
			
			if (stateInstr == 1) {
				waitAndDiscover();
				if (getId() == Module.Floor_Uplifter) {
					while (hasNeighbor(Module.Floor_UplifterLeft, true)) {
    					waitAndDiscover();
    					disconnect(Module.Floor_UplifterLeft);
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
				if (getId() == Module.Floor_Uplifter) {
					rotate(90);
					nextInstrState();
				}
			}
			if (stateInstr == 4) {
				if (!isPendingState(1)) {
					if (getId() == Module.Walker_Right) {
						while (exists(disconnected(getNeighbors(Grouping.Floor)))) {
	    					waitAndDiscover();
	    					for (Module nb: maleAligned(connected(getNeighbors(Grouping.Floor))).keySet()) {
		    					connect(nb);
		    				}
	    					
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
				if (!isPendingState(2)) {
					if (getId() == Module.Floor_UplifterLeft) {
						if (contains(nbs(),Module.Floor_Uplifter)) { 
		    				if (!contains(maleAligned(nbs()),Module.Floor_Uplifter)){
		    					rotate(90);
		    					waitAndDiscover();
		    				}
						}
						while (contains(disconnected(nbs()),Module.Floor_Uplifter)) {
		    				connect(Module.Floor_Uplifter);
		    				waitAndDiscover();
		    			}
						nextPendingState(2);
						waitForPendingState(3);
						nextInstrState();
					}
				}
			}
			if (stateInstr == 5 && !stateCurrentFinished) {
				gradients.put(HORIZONTAL, Byte.MAX_VALUE);
				gradients.put(VERTICAL, Byte.MAX_VALUE);
				
				if (getId() == Module.Walker_Left) {
					renameTo(Module.Floor_0);
				}
				if (getId() == Module.Walker_Right) {
					renameTo(Module.Floor_1);
				}
				if (getId() == Module.Walker_Head) {
					renameTo(Module.Floor_2);
					//broadcastNewOperationState(ELECT);
				}
				stateCurrentFinished = true;
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
		setModuleColors (Module.Floor_UplifterLeft,new Color[]{Color.decode("#FF0099"),Color.decode("#9900FF")});
		setModuleColors (Module.Floor_UplifterRight,new Color[]{Color.decode("#AA0033"),Color.decode("#3300AA")});
		
		addStructureColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		
	}




}
