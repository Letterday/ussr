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
	private static final int PENDING1 = 1;
	private static final int PENDING2 = 2;
	private static final int PENDING3 = 4;
	private static final int PENDING4 = 8;
	private static final int PENDING5 = 16;
	private static final int PENDING6 = 32;
	private static final int PENDING7 = 64;
	private static final int PENDING8 = 128;
	 
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
	
	public void doInPending (int pendingState) {
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
			
			if (stateInstruction(0)  && !stateCurrentFinished) {
				if (isC(NORTH_MALE_R,SOUTH_MALE_R,true)) {
					gradientCreate(HORIZONTAL,0);
					stateCurrentFinished = true;
				}
				
				if (isC(NORTH_FEMALE_R,SOUTH_FEMALE_R,true)) {
					gradientCreate(VERTICAL,0);
					stateCurrentFinished = true;
				}
				
				delay(1000);
			
				stateInstructionBroadcastNext(1);
			}
			
			
			if (stateInstruction(1) && !stateCurrentFinished) {
				if (getGradient(HORIZONTAL) == 0 && getGradient(VERTICAL) == 1){
					renameTo(Module.Walker_Head);
					stateCurrentFinished = true;
				}
				
				if (getConnectorToNb(Module.Walker_Head,false) == NORTH_FEMALE_R) {
					renameTo(Module.Walker_Left);
					stateCurrentFinished = true;
				}
				if (getConnectorToNb(Module.Walker_Head,false) == NORTH_FEMALE_L) {
					renameTo(Module.Walker_Right);
					stateCurrentFinished = true;
				}
				if (hasNeighbor(Module.Walker_Left,true) && hasNeighbor(Module.Walker_Right,true) && getId() != Module.Walker_Head) {
					renameTo(Module.Floor_Uplifter);
					stateCurrentFinished = true;
				}
				if (getConnectorToNb(Module.Floor_Uplifter,false) == NORTH_FEMALE_R) {
					renameTo(Module.Floor_UplifterLeft);
					stateCurrentFinished = true;
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
    			delay(500); 
    			//TODO
    			if (!statePending(PENDING1) && getId() == Module.Walker_Right) {
    				
    				while (exists(connected(onGroup(nbs(),Grouping.Floor))) && timeSpentInState() < 10) {
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
    				waitForPendingState(PENDING1 + PENDING2);
    				stateInstrBroadcastNext();
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
	    	}
	        	
	     
    		if (stateInstruction(1)) 
	    	{
    			if (getId() == Module.Walker_Left) {
    				rotate(90);
    				stateInstrBroadcastNext();
    				
    			}   			
	    	}

    		if (stateInstruction(2)) 
	    	{
    			if (getId() == Module.Floor_Uplifter) {
    				rotate(90);
    				stateOperationBroadcast(WALK);
    			}
	    	}
		}    		    
		
		if (stateOperation(WALK) || stateOperation(WALK2)) {
			
			
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
		    			stateInstrBroadcastNext();
		    		}
		    		
		    		if (getGrouping() == Grouping.Floor) {
		    			waitAndDiscover();
		    			while (hasNeighbor(Module.Walker_Left, true)) {
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
		
			if (stateOperation(GETDOWN)) {
				
				if (stateInstruction(0) && !stateCurrentFinished) {
					waitAndDiscover();
					
					if (getGrouping() == Grouping.Floor && hasNeighbor(Module.Walker_Left, true)) {
						renameTo(Module.Floor_Downlifter);
						stateCurrentFinished = true;
					}
					
					if (getConnectorToNb(Module.Floor_Downlifter,false) == SOUTH_FEMALE_R) {
						renameTo(Module.Floor_DownlifterLeft);
						stateCurrentFinished = true;
						stateInstrBroadcastNext();
					}
					
				}
				
				if (stateInstruction(1)) {
					waitAndDiscover();
					if (getId() == Module.Floor_Downlifter) {
						while (hasNeighbor(Module.Floor_DownlifterLeft, true)) {
	    					waitAndDiscover();
	    					disconnect(Module.Floor_DownlifterLeft);
	    				}
						stateInstrBroadcastNext();
						
					}
				}
				
				if (stateInstruction(2)) {
					if (getId() == Module.Walker_Left) {
						rotate(-90);
						stateInstrBroadcastNext();
					}
				}
				if (stateInstruction(3)) {
					if (!statePending(PENDING1) && getId() == Module.Floor_Downlifter) {
						rotate(90);
						statePendingBroadcast(PENDING1);
					}
					if (!statePending(PENDING2) && getId() == Module.Walker_Right) {
						rotateTo(0);
						statePendingBroadcast(PENDING2);
						waitForPendingState(PENDING1 + PENDING2);
						stateInstrBroadcastNext();
					}
				}
				if (stateInstruction(4)) {
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
				if (stateInstruction(5)) {
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
				if (stateInstruction(6)) {
					if (!statePending(PENDING1) && getId() == Module.Walker_Left) {
						rotate(90);
						statePendingBroadcast(1);
					}
					if (!statePending(PENDING2) && getId() == Module.Floor_Downlifter) {
						rotate(90);
						statePendingBroadcast(2);
						waitForPendingState(PENDING1 + PENDING2);
						stateInstrBroadcastNext();
					}
				}
				if (stateInstruction(7)) {
					if (getId() == Module.Floor_Downlifter) {
						waitAndDiscover();
		    			while (disconnected(nbs()).containsKey(Module.Walker_Left) || disconnected(nbs()).containsKey(Module.Floor_DownlifterLeft)) {
		    				connect(Module.Walker_Left);
		    				connect(Module.Floor_DownlifterLeft);
		    				waitAndDiscover();
		    			}
		    			stateInstrBroadcastNext();
						
					}
				}
				
				if (stateInstruction(8) && !stateCurrentFinished) {
					
					
					if (getId() == Module.Walker_Left) {
						renameRestore();
						switchNorthSouth();
					}
					if (getId() == Module.Walker_Right) {
						renameRestore();
						switchNorthSouth();
					}
					if (getId() == Module.Walker_Head) {
						renameRestore();
						//switchNorthSouth();
						stateOperationBroadcast(REPAIR);
						
					}
					stateCurrentFinished = true;
				}
			}
			
			if (stateOperation(REPAIR)) {
				if (stateInstruction(0)) {
					gradients.put(HORIZONTAL, Byte.MAX_VALUE);
					gradients.put(VERTICAL, Byte.MAX_VALUE);
					waitAndDiscover();
					if (!statePending(PENDING1) && getId() == Module.Floor_Uplifter) {
						rotateTo(0);
						statePendingBroadcast(PENDING1);
					}
					
					if (!statePending(PENDING2) && getId() == Module.Floor_UplifterLeft) {
	    				renameRestore();
	    				statePendingBroadcast(PENDING2);
	    			}
					
					if (!statePending(PENDING3) && getId() == Module.Floor_Downlifter) {
						statePendingBroadcast(PENDING3);
						renameRestore();
					}
					if (!statePending(PENDING4) && getId() == Module.Floor_DownlifterLeft) {
						statePendingBroadcast(PENDING4);
						renameRestore();
						waitForPendingState(PENDING1+PENDING2+PENDING3+PENDING4);
						stateInstrBroadcastNext();
					}
				}
				
				if (stateInstruction(1)) {
					if (getId() == Module.Floor_Uplifter) {
						discoverNeighbors();
						while (exists(maleAligned(disconnected(onGroup(nbs(), Grouping.Floor))))) {
							for (Module nb: maleAligned(disconnected(onGroup(nbs(), Grouping.Floor))).keySet()) {
		    					connect(nb);
		    				}
		    				waitAndDiscover();
		    			}
						
						renameRestore();
						stateOperationBroadcast(ELECT);
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
