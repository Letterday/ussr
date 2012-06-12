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
	private static final int WALK2 = 2;
	private static final int GETUP = 3;
	private static final int GETDOWN = 4;
	private static final int REPAIR = 5;

	
	
	
	public String getOpStateName () {
		switch (getStateOperation()) {
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
	
	public void doInPending22f (int pendingState) {
		if (!statePending(pendingState) && getId() == Module.Walker_Right) {
			while (exists(connected(onGroup(nbs(),Grouping.Floor))) && timeSpentInState() < 10) {
				info.addNotification("time spent so  far: " + timeSpentInState());
				for (Module m2 : maleAligned(onGroup(nbs(),Grouping.Floor)).keySet()) {
    				disconnect(m2);
    			}
				waitAndDiscover();
			}
			
			statePendingBroadcast(pendingState);

		}
	}
	
	
	public void handleStates () {
		if (stateOperation(ELECT)) {
			
			if (stateInstruction(0)  && !stateIsFinished()) {
				discoverNeighbors();
			
				if (exists(male(west(nbs()))) && !exists(east(nbs()))) {
					gradientCreate(HORIZONTAL,0);
					stateFinish();
				}
				
			
				if (exists(female(west(nbs()))) && !exists(east(nbs()))) {
					gradientCreate(VERTICAL,0);
					stateFinish();
				}
				
				delay(2000);
			
				stateInstructionBroadcastNext(1);
			}
			
			
			if (stateInstruction(1) && !stateIsFinished()) {
				if (getGradient(HORIZONTAL) == 0 && getGradient(VERTICAL) == 1){
					renameTo(Module.Walker_Head);
					stateFinish();
				}
				
				if (getConnectorToNb(Module.Walker_Head,false) == (SOUTH_FEMALE_WEST)) {
					renameTo(Module.Walker_Left);
					stateFinish();
				}
				if (getConnectorToNb(Module.Walker_Head,false) == (SOUTH_FEMALE_EAST)) {
					renameTo(Module.Walker_Right);
					stateFinish();
				}
				if (hasNeighbor(Module.Walker_Left,true) && hasNeighbor(Module.Walker_Right,true) && getId() != Module.Walker_Head) {
					renameTo(Module.Floor_Uplifter);
					stateFinish();
				}
				if (getConnectorToNb(Module.Floor_Uplifter,false) == (SOUTH_FEMALE_WEST)) {
					renameTo(Module.Floor_UplifterLeft);
					stateFinish();
					stateOperationBroadcast(GETUP);
				}
				waitAndDiscover();
				
			}
			

	   }
		
		//disconnect (@Floor, Walker@Right);

	    if (stateOperation(GETUP)) {
    		if (stateInstruction(0)) {
    			// disconnect(Floor, Walker_Right);
    			discoverNeighbors();
    	
    			if (!statePending(PENDING1) && getId() == Module.Walker_Right) {
    				while (exists(connected(onGroup(nbs(),Grouping.Floor))) ) {//&& timeSpentInState() < 10
    					info.addNotification("time spent so  far: " + timeSpentInState());
	    				for (Module m2 : maleAligned(onGroup(nbs(),Grouping.Floor)).keySet()) {
	        				disconnect(m2);
	        			}
	    				waitAndDiscover();
	    			}
    				statePendingBroadcast(PENDING1);
	    		}
    			if (!statePending(PENDING2) && getId() == Module.Floor_Uplifter) {
    				
    				while (hasNeighbor(Module.Floor_UplifterLeft, true)) {
    					disconnect(Module.Floor_UplifterLeft);
    					waitAndDiscover();
    				}
    				statePendingBroadcast(PENDING2);
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
    			//if (getId() == Module.Floor_Uplifter) {
	    			if (statePending(PENDING1 + PENDING2)) {
	    				notification("OK!!");
						stateInstrBroadcastNext();
					}
    			//}
	    	}
	        	
	     
    		if (stateInstruction(1)) {
    			if (getId() == Module.Walker_Left) {
    				rotate(90);
    				stateInstrBroadcastNext();
    				
    			}   			
	    	}

    		if (stateInstruction(2)) {
    			if (getId() == Module.Floor_Uplifter) {
    				rotate(90);
    				stateOperationBroadcast(WALK);
    			}
	    	}
		}    		    
		
		if (stateOperation(WALK) || stateOperation(WALK2)) {
			
			// Event handlers
			// REPAIR is allowed in any instruction state
			if (getId() == Module.Floor_Uplifter) {
				if (!exists(connected(onGroup(nbs(),Grouping.Walker)))) {
					notification("Event handler fires!");
					rotateTo(0);
					discoverNeighbors();
					for (Module m2 : disconnected(maleAligned(onGroup(nbs(),Grouping.Floor))).keySet()) {
        				connect(m2);
        			}
					renameRestore();
				}
			}
			
			if (getId() == Module.Floor_UplifterLeft) {
				if (exists(west(south(onGroup(nbs(),Grouping.Floor))))) {
					notification("Event handler fires!");
					renameRestore();
				}
			}
				
			
			
			if (stateInstruction(0)) {
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
		    			stateInstrBroadcastNext();
	    			}
	    			else {
	    				stateOperationBroadcast(GETDOWN);
	    			}
	    		}
			}
	    	
			if (stateInstruction(1)) {
	    		if (getId() == Module.Walker_Left) {
	    			waitAndDiscover();
	    			while (exists(connected(onGroup(nbs(),Grouping.Floor)))) {
	    				for (Module nb: maleAligned(connected(onGroup(nbs(),Grouping.Floor))).keySet()) {
	    					disconnect(nb);
	    				}
	    				waitAndDiscover();
	    			}
	    			info.addNotification(connected(onGroup(nbs(),Grouping.Floor)).toString());
	    			stateInstrBroadcastNext();
	    		}
	    		
	    		if (getGrouping() == Grouping.Floor) {
	    			waitAndDiscover();
	    			while (connected(nbs()).containsKey(Module.Walker_Left)) {
	    				disconnect(Module.Walker_Left);
	    				waitAndDiscover();
	    			}
	    		}
	    		
			}
	    	
			if (stateInstruction(2)) {
	    		if (getId() == Module.Walker_Right) {
	    			rotate(180);
	    			stateInstrBroadcastNext();
	    		}
			}
		    	
			if (stateInstruction(3)) {
		    	renameSwitch(Module.Walker_Left,Module.Walker_Right);
			}
		    	
			if (stateInstruction(4)) {
	    		if (getId() == Module.Walker_Head) {
	    			if (stateOperation(WALK))
	    				stateOperationBroadcast(WALK2);
	    			else
	    				stateOperationBroadcast(WALK);
	    		}
			}		
		}
	
		
		if (getId() == Module.Floor_Downlifter) {
			if (!exists(onGroup(nbs(),Grouping.Walker))) {
				renameRestore();
			}
		}
		
		
		// Event handlers
		if (getId() == Module.Floor_DownlifterLeft) {
			if (!hasNeighbor(Module.Floor_Downlifter, false)) {
				renameRestore();
			}
		}
		
		if (stateOperation(GETDOWN)) {
			
			if (stateInstruction(0) && !stateIsFinished()) {
				waitAndDiscover();
				
				if (getGrouping() == Grouping.Floor && hasNeighbor(Module.Walker_Left, true)) {
					renameTo(Module.Floor_Downlifter);
					stateFinish();
				}
				
				if (getConnectorToNb(Module.Floor_Downlifter,false) == (NORTH_FEMALE_WEST)) {
					renameTo(Module.Floor_DownlifterLeft);
					stateFinish();
					stateInstrBroadcastNext();
				}
				
			}
			
			if (stateInstruction(1)) {
				if (!statePending(PENDING1)) {
					waitAndDiscover();
					if (getId() == Module.Floor_Downlifter) {
						while (hasNeighbor(Module.Floor_DownlifterLeft, true)) {
	    					waitAndDiscover();
	    					disconnect(Module.Floor_DownlifterLeft);
	    				}
						statePendingBroadcast(PENDING1);
					}
				}
				if (!statePending(PENDING2)) {
					if (getId() == Module.Walker_Left) {
						rotate(-90);
						statePendingBroadcast(PENDING2);
					}
				}
				if (getId() == Module.Walker_Left) {
					if (statePending(PENDING1 + PENDING2)) {
						stateInstrBroadcastNext();
					}
				}
			}
			if (stateInstruction(2)) {
				if (!statePending(PENDING1) && getId() == Module.Floor_Downlifter) {
					rotate(90);
					statePendingBroadcast(PENDING1);
				}
				if (!statePending(PENDING2) && getId() == Module.Walker_Right) {
					rotateTo(0);
					statePendingBroadcast(PENDING2);
				}
				//if (getId() == Module.Walker_Right) {
					if (statePending(PENDING1 + PENDING2)) {
						stateInstrBroadcastNext();
					}
				//}
			}
			if (stateInstruction(3)) {
				if (!statePending(PENDING1)) {
					waitAndDiscover();
					if (getId() == Module.Walker_Right) {
						while (exists(disconnected(onGroup(nbs(),Grouping.Floor)))) {
	    					for (Module nb: maleAligned(disconnected(onGroup(nbs(),Grouping.Floor))).keySet()) {
		    					connect(nb);
		    				}
	    					waitAndDiscover();
	    				}
						stateInstrBroadcastNext();
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
			if (stateInstruction(4)) {
				if (getId() == Module.Walker_Left) {
					discoverNeighbors();
					while (exists(connected(onGroup(nbs(),Grouping.Floor)))) {
						for (Module nb: maleAligned(connected(onGroup(nbs(),Grouping.Floor))).keySet()) {
	    					disconnect(nb);
	    				}
    					waitAndDiscover();
	    			}
					stateInstrBroadcastNext();
				}
			}
			if (stateInstruction(5)) {
				if (!statePending(PENDING1) && getId() == Module.Walker_Left) {
					rotate(90);
					statePendingBroadcast(1);
				}
				if (!statePending(PENDING2) && getId() == Module.Floor_Downlifter) {
					rotateTo(0);
					statePendingBroadcast(2);
					
				}
				if (getId() == Module.Walker_Left && statePending(PENDING1 + PENDING2)) {
					stateInstrBroadcastNext();
				}
			}
			if (stateInstruction(6)) {
				if (getId() == Module.Floor_Downlifter) {
					waitAndDiscover();
	    			while (exists(disconnected(maleAligned(nbs())))) {
	    				for (Module nb: maleAligned(disconnected(nbs())).keySet()) {
	    					connect(nb);
	    				}
	    				waitAndDiscover();
	    			}
	    			stateInstrBroadcastNext();
					
				}
			} 
			
			if (stateInstruction(7)) {
				if (getGrouping() == Grouping.Floor) {
					discoverNeighbors();
					delay(2000);
					if (stateInstruction(7)) { // TODO: Critical section!
						if (exists(west(nbs())) && !exists(east(nbs())) && !stateIsFinished()) {
							send(new Packet(getId()).setType(Type.FIX_DIRECTION).getBytes(),NORTH_MALE_WEST);
							send(new Packet(getId()).setType(Type.FIX_DIRECTION).getBytes(),SOUTH_MALE_WEST);
							notification("Im only allowed in state 7!!!!" + getStateOperation() + " - " + getStateInstruction());
							stateFinish();
						}
					}
				}
				if (getId() == Module.Walker_Head) {
					delay(4000);
					stateInstrBroadcastNext();
				}
			}
			
			if (stateInstruction(8) && !stateIsFinished()) {
				
				
				if (getId() == Module.Walker_Left) {
					renameRestore();
				}
				if (getId() == Module.Walker_Right) {
					renameRestore();
				}
				if (getId() == Module.Walker_Head) {
					renameRestore();
					stateInstrBroadcastNext();
					
				}
				stateFinish();
			}
			
			if (stateInstruction(9)) {
				gradients.put(HORIZONTAL, Byte.MAX_VALUE);
				gradients.put(VERTICAL, Byte.MAX_VALUE);
				waitAndDiscover();
								
			
				stateOperationBroadcast(ELECT);
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
