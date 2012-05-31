package ussr.samples.atron.simulations.metaforma.gen;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;

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

	
	private static final int ELECT = 0;
	private static final int WALK = 1;
	private static final int GETUP = 2;
	private static final int GETDOWN = 3;
	private boolean currentOperationDone;
	
	
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
			
		}
		return null;
	}
	
	public void handleStates () {
		if (!currentOperationDone){
			if (isC(6,2)) {
				renameTo(Module.Walker_Head);
				discoverNeighbors();
				currentOperationDone = true;
			}
			if (isC(3,7) && getNeighbors(Grouping.Walker).containsKey(Module.Walker_Head)) {
				renameTo(Module.Walker_Left);
				currentOperationDone = true;
			}
			if (isC(1,5) && getNeighbors(Grouping.Walker).containsKey(Module.Walker_Head)) {
				renameTo(Module.Walker_Right);
				currentOperationDone = true;
			}
			if (getNeighbors(Grouping.Walker).containsKey(Module.Walker_Left) && getNeighbors(Grouping.Walker).containsKey(Module.Walker_Right) && getId() != Module.Walker_Head) {
				renameTo(Module.Floor_Uplifter);
				currentOperationDone = true;
			}
			if (getNeighbors(Grouping.Floor).containsKey(Module.Floor_Uplifter) && isC(1,5)) {
				renameTo(Module.Floor_UplifterLeft);
				currentOperationDone = true;
			}
			if (getNeighbors(Grouping.Floor).containsKey(Module.Floor_Uplifter) && isC(3,7)) {
				renameTo(Module.Floor_UplifterRight);
				currentOperationDone = true;
			}
		}
			
		
	}
	
	
	public void handleStates2 () {
		if (stateOperation == ELECT) { 
		    
//////////////////////////////////////////////////////////////////////
    		
			
			while (stateInstr == 0 && !isC(6,2)) {
				waitAndDiscover();
			}
			if (stateInstr == 0 && isC(6,2)){
				renameTo(Module.Walker_Head);
				nextInstrState();
			}
	
//////////////////////////////////////////////////////////////////////
		    		
			while (stateInstr == 1 && !(
					isC(3,7) && getNeighbors(Grouping.Walker).containsKey(Module.Walker_Head)
					||
					isC(1,5) && getNeighbors(Grouping.Walker).containsKey(Module.Walker_Head)
				)) 
			{
				waitAndDiscover();
			}
			if (stateInstr == 1 && isPendingState(0) && isC(3,7) && getNeighbors(Grouping.Walker).containsKey(Module.Walker_Head)) {
				renameTo(Module.Walker_Left);
				nextPendingState(1);
			}
			if (stateInstr == 1 && isC(1,5) && getNeighbors(Grouping.Walker).containsKey(Module.Walker_Head)) {
				renameTo(Module.Walker_Right);
				waitForPendingState(1);
				nextInstrState();
			}
		    		
		    		
	   
	    	// walker is created at this point
//////////////////////////////////////////////////////////////////////		    	
	    	

    
    		while (stateInstr == 2 && !(getNeighbors(Grouping.Walker).containsKey(Module.Walker_Left) && getNeighbors(Grouping.Walker).containsKey(Module.Walker_Right) && getId() != Module.Walker_Head)) {
    			waitAndDiscover();
    		}
    		if (stateInstr == 2 && (getNeighbors(Grouping.Walker).containsKey(Module.Walker_Left) && getNeighbors(Grouping.Walker).containsKey(Module.Walker_Right) && getId() != Module.Walker_Head)) {
    			renameTo(Module.Floor_Uplifter);
    			nextInstrState();

    		}

    		
    
    	// floor 0 is created at this point
    	
//////////////////////////////////////////////////////////////////////		    	
    	
    		
    		while (stateInstr == 3 && !(
    				(getNeighbors(Grouping.Floor).containsKey(Module.Floor_Uplifter) && isC(1,5) && getId().getGrouping() == Grouping.Floor)
    				||
    				(getNeighbors(Grouping.Floor).containsKey(Module.Floor_Uplifter) && isC(3,7) && getId().getGrouping() == Grouping.Floor)
    		)) 
    		{
    			waitAndDiscover();
    		}
    		
    		if (isPendingState(0) && getNeighbors(Grouping.Floor).containsKey(Module.Floor_Uplifter) && isC(1,5) && getId().getGrouping() == Grouping.Floor) {
    			renameTo(Module.Floor_UplifterRight);
    			nextPendingState(1);
    		}
        	
    		if (getNeighbors(Grouping.Floor).containsKey(Module.Floor_Uplifter) && isC(3,7) && getId().getGrouping() == Grouping.Floor) {
    			renameTo(Module.Floor_UplifterLeft);
    			waitForPendingState(1);
    			setNewOperationState(GETUP);
    		}
        	
	        // floor uplifter left/right are created at this point
	   }
		    
	    if (stateOperation == GETUP) {
    		if (stateInstr == 0) {
    			// disconnect(Floor, Walker_Right);
    			discoverNeighbors();
    			delay(500);
    			
    			if (getId() == Module.Walker_Right) {
    				while (hasNeighbor(Grouping.Floor, true) && timeSpentInState() < 10) {
    					info.addNotification("time spent so  far: " + timeSpentInState());
	    				for (Module m2 : getNeighbors(Grouping.Floor).keySet()) {
	        				connection(m2, false);
	        			}
	    				waitAndDiscover();
	    			}
    				
//	    			if (hasNeighbor(Grouping.Floor, true)) {
//	    				for (Module m2 : getNeighbors(Grouping.Floor).keySet()) {
//	        				connection(m2, true);
//	        			}
//	    				delay(200);
//	    				errInCurrentState = 1;
//	    			}

    				nextInstrState();

	    		}
    			else if (getGrouping() == Grouping.Floor)  {
    				while (hasNeighbor(Module.Walker_Right,true)){// && timeSpentInState() < 10) {
    					info.addNotification("time spent so  far: " + timeSpentInState());
	        			connection(Module.Walker_Right, false);
	    				waitAndDiscover();
	    			}
    			}
	    	}
	        	
	      
    		if (stateInstr == 1) {
    			if (getId() == Module.Walker_Left) {
	    			if (errInPreviousState == 1) {
	    				while (hasNeighbor(Grouping.Floor, true) && timeSpentInState() < 10) {
	        				for (Module m2 : getNeighbors(Grouping.Floor).keySet()) {
	            				connection(m2, false);
	            			}
	        				waitAndDiscover();
	    				}
	    			}
	    			nextInstrState();
	    		}
    			else if (getGrouping() == Grouping.Floor)  {
    				if (errInPreviousState == 1) {
	    				while (hasNeighbor(Module.Walker_Left,true)){// && timeSpentInState() < 10) {
	    					info.addNotification("time spent so  far: " + timeSpentInState());
		        			connection(Module.Walker_Left, false);
		    				waitAndDiscover();
		    			}
    				}
    			}
	    	}
        	
	    	if (stateInstr == 2) 
	    	{
        		if (getId() == Module.Floor_Uplifter) {
        			
        			while (!(hasNeighbor(Module.Walker_Left,false) && hasNeighbor(Module.Walker_Right,false))) {
	    				waitAndDiscover();
	    			}
        			
        			
        			if (hasNeighbor(Module.Walker_Left,true)) {
        				disconnect(Module.Floor_UplifterLeft);
        				while (hasNeighbor(Module.Floor_UplifterLeft, true)){
        					yield();
        				}
        				info.addNotification("##Left connected");
        			}
        			else if (hasNeighbor(Module.Walker_Right,true)) {
        				disconnect(Module.Floor_UplifterRight);
        				while (hasNeighbor(Module.Floor_UplifterRight, true)){
        					yield();
        				}
        				info.addNotification("##Right connected");
        			}
        			
        			nextInstrState();
        		}
	    	}
    		if (stateInstr == 3) 
	    	{
    			if (!isPendingState(1) && getId() == Module.Floor_Uplifter) {
    				rotate(90);
    				nextPendingState(1);
    			}
    			if (getId() == Module.Walker_Left) {
    				rotate(90);
    				waitForPendingState(1);
    				broadcastNewOperationState(WALK);
    			}
	    	}
		}    		    
		
		if (stateOperation == WALK) {
		    switch (stateInstr) {
			    case 0:
		    		if (getId() == Module.Walker_Right) {
		    			discoverNeighbors();
		    			delay(1000);
		    			if (hasNeighbor(Grouping.Floor, false)) { 
			    			while (hasNeighborsConnected(Grouping.Floor,false)) {
			    				connect(Module.Walker_Right, Grouping.Floor);
			    			}
		    			}
		    			else {
		    				setNewOperationState(GETDOWN);
		    			}
		    		}
		    		
		    	break;
		    	
		    	case 1:
		    		if (getId() == Module.Walker_Left) {
		    			while (hasNeighborsConnected(Grouping.Floor,true)) {
		    				for (Module m: getNeighborsConnected(Grouping.Floor, true).keySet()) {
		    					disconnect(m);
		    				}
		    			}
		    			nextInstrState();
		    		}
		    		
		    	break;
		    	
		    	case 2:
		    		rotate(Module.Walker_Right,180);
		    	break;
		    	
		    	case 3:
		    		renameSwitch(Module.Walker_Left,Module.Walker_Right);
		    	break;
		    	
		    	case 4:
		    		if (getId() == Module.Walker_Head) {
		    			resetInstrState(0);
		    		}
		    		break;
		    		
		    	case 5:
		    		if (getId() == Module.Walker_Head) {
		    			resetInstrState(0);
		    		}
		    		break;
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
