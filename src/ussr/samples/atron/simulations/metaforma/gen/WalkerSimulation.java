package ussr.samples.atron.simulations.metaforma.gen;
import java.awt.Color;
import ussr.description.Robot;
import ussr.model.Controller;
import ussr.model.debugging.ControllerInformationProvider;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.simulations.metaforma.lib.*;


class WalkerSimulation extends MetaformaSimulation {

	public static void main( String[] args ) {
		MetaformaSimulation.initSimulator();
        new WalkerSimulation().main();
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



class LocalWalkerController extends MetaformaRuntime implements ControllerInformationProvider {

	
	private static final byte HORIZONTAL = 0;
	private static final byte VERTICAL = 1;
	private static final byte HORIZONTAL_BACKWARD = 2;
	private static final byte VERTICAL_BACKWARD = 3;
	private static final int GETUP = 0;
	private static final int WALK = 1;
	private static final int WALK2 = 2;
	private static final int GETDOWN = 4;
	private static final int REPAIR = 5;
	private static final int GETDOWN_CHECK = 6;
	private float syncGradient;

	
	
	
	public String getOpStateName () {
		switch (getStateOperation()) {
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
				
			case GETDOWN_CHECK:
				return "GETDOWN_CHECK";
			
		}
		return null;
	}
	
	
	public void handleSyncs () {
		if (syncGradient == 0 || time() - syncGradient > 8) {
			syncGradient = time();
			discoverNeighbors();
			
			if (nbs(MALE&WEST).exists() && !nbs(EAST).exists()) {
				gradientCreate(HORIZONTAL);
			}
			
			if (nbs(FEMALE&WEST).exists() && !nbs(EAST).exists()) {
				gradientCreate(VERTICAL);
			}
			
		}
//		if (time() - stateLastBroadcast > 10) {
//			stateDisseminate();
//		}
	}
	
	
	// Event handlers
	public void handleEvents () {
		
		if (getId() == Module.Floor_Uplifter) {
			
			if (!nbs().connected().onGroup(Grouping.Walker).exists()) {
				notification("Uplifter event fires!");
				rotateTo(0);
				discoverNeighbors();
				
				for (Module m2 : nbs().disconnected().maleAlignedWithFemale().onGroup(Grouping.Floor).modules()) {
    				connect(m2);
    			}
				if (nbs().onGroup(Grouping.Floor).connected().size() == 2) {
					renameRestore();
				}
			}
		}
		
		/////////////////////////////////////////////////////////////
		
//		if (getId() == Module.Floor_UplifterLeft) {
//			
//			if (nbs().connected().onGroup(Grouping.Floor).size() == 2) {
//				notification("UplifterLeft event fires!");
//				renameRestore();
//			}
//		}
		
		/////////////////////////////////////////////////////////////
		
		if (getId() == Module.Floor_Downlifter) {

			if (!nbs().onGroup(Grouping.Walker).exists()) {
				notification("Downlifter event fires!");
				renameRestore();
			}
		}
		
		/////////////////////////////////////////////////////////////
		
//		if (getId() == Module.Floor_DownlifterLeft) {
//			if (!nbs().contains(Module.Floor_Downlifter)) {
//				notification("DownlifterLeft event fires!");
//				renameRestore();
//			}
//		}
	}
	
	
	
	public void handleStates () {
		
		
		if (stateOperation(GETUP)) {
			if (stateInstructionSimple(0)) {
				if (!stateIsFinished()) {
					if (gradient(HORIZONTAL) == 0 && gradient(VERTICAL) == 1){
						renameTo(Module.Walker_Head);
						stateFinish();
					}
					
					if (nbs(SOUTH&FEMALE&WEST).contains(Module.Walker_Head)) {
						renameTo(Module.Walker_Left);
						stateFinish();
					}
					if (nbs(SOUTH&FEMALE&EAST).contains(Module.Walker_Head)) {
						renameTo(Module.Walker_Right);
						stateFinish();
					}
					if (nbs(EAST).contains(Module.Walker_Left) && nbs(EAST).contains(Module.Walker_Right)) {
						renameTo(Module.Floor_Uplifter);
						stateFinish();
						stateInstrBroadcastNext();
					}
					
					waitAndDiscover();
					//notification(neighbors.toString());
				}
			}
			
			

    		if (stateInstruction(1)) {
    			// disconnect(Floor, Walker_Right);
    	
    			if (statePending(PENDING1 + PENDING2)) {
					stateInstrBroadcastNext(2);
				}
    			
    			if (!statePending(PENDING1) && getId() == Module.Walker_Right) {
    				while (nbs().connected().onGroup(Grouping.Floor).exists()) {//&& timeSpentInState() < 10
	    				for (Module m2 : nbs().onGroup(Grouping.Floor).maleAlignedWithFemale().modules()) {
	        				disconnect(m2);
	        			}
	    				waitAndDiscover();
	    				notification("time spent: " + timeSpentInState());
	    			}
    				statePendingBroadcast(PENDING1);
	    		}
    			if (!statePending(PENDING2) && getId() == Module.Floor_Uplifter) {
    				while (!nbs().connected().north().onGroup(Grouping.Floor).isEmpty()) {
    					for (Module m: nbs().connected().north().onGroup(Grouping.Floor).modules()) {
    						disconnect(m);
    					}
    					waitAndDiscover();
    				}
    				statePendingBroadcast(PENDING2);
    			}
    			
    			if (getGrouping() == Grouping.Floor && nbs().connected().male().contains(Module.Walker_Right))  {
        			disconnect(Module.Walker_Right);
    				waitAndDiscover();
	    			
    			}
    			

	    	}
	        	
	     
    		if (stateInstructionSimple(2)) {
    			if (getId() == Module.Walker_Left) {
    				rotate(90);
    				stateInstrBroadcastNext();
    				
    			}   			
	    	}

    		if (stateInstructionSimple(3)) {
    			if (getId() == Module.Floor_Uplifter) {
    				rotate(90);
    				stateOperationBroadcast(WALK);
    			}
	    	}
		}    		    
		
		if (stateOperation(GETDOWN_CHECK)) {
			if (stateInstructionSimple(0)) {
				if (getId() == Module.Walker_Left) {
					rotate (-90);
					stateInstrBroadcastNext();
				}
			}
			if (stateInstructionSimple(1)) {
				if (getId() == Module.Walker_Right) {
					
					if (nbs().onGroup(Grouping.Floor).size() < 4) {
						rotate (180);
						stateOperationBroadcast(GETDOWN);
					}
					else {
						rotate (180);
						discoverNeighbors();
						if (nbs().onGroup(Grouping.Floor).size() < 4) {
							
							stateOperationBroadcast(GETDOWN);
						}
						else {
							rotate (90);
							stateOperationBroadcast(WALK);
						}
						
					}
				
				}
			}
		}
		
		
		if (stateOperation(WALK) || stateOperation(WALK2)) {
			if (stateInstruction(0)) {
	    		if (getId() == Module.Walker_Right) {
	    			if (nbs().onGroup(Grouping.Floor).exists()) { 
	    				if (!nbs().onGroup(Grouping.Floor).maleAlignedWithFemale().disconnected().exists() && nbs().onGroup(Grouping.Floor).genderDismatch().exists()) {
	    					rotate(90);
	    					discoverNeighbors();
	    				}
		    			while (!nbs().connected().onGroup(Grouping.Floor).maleAlignedWithFemale().exists()) {
		    				for (Module nb: nbs().disconnected().onGroup(Grouping.Floor).maleAlignedWithFemale().modules()) {
		    					connect(nb);
		    				}
//		    				notification(nbs().disconnected().onGroup(Grouping.Floor).maleAlignedWithFemale().modules().toString());
		    				waitAndDiscover();
		    			}
		    			stateInstrBroadcastNext();
	    			}
	    			else {
	    				stateOperationBroadcast(GETDOWN_CHECK);
	    			}
	    		}
			}
	    	
			if (stateInstruction(1)) {
	    		if (getId() == Module.Walker_Left) {

	    			while (nbs().connected().onGroup(Grouping.Floor).exists()) {
	    				for (Module nb: nbs().connected().onGroup(Grouping.Floor).modules()) {
	    					disconnect(nb);
	    				}
	    				waitAndDiscover();
	    			}
	    			stateInstrBroadcastNext();
	    		}
	    		
	    		if (getGrouping() == Grouping.Floor) {
	    			discoverNeighbors();
	    			while (nbs().connected().contains(Module.Walker_Left)) {
	    				disconnect(Module.Walker_Left);
	    				waitAndDiscover();
	    			}
	    		}
	    		
			}
	    	
			if (stateInstructionSimple(2)) {
	    		if (getId() == Module.Walker_Head) {
	    			rotate(45);
	    			stateInstrBroadcastNext();
	    		}
			}
			
			if (stateInstructionSimple(3)) {
	    		if (getId() == Module.Walker_Right) {
	    			rotate(180);
	    			stateInstrBroadcastNext();
	    		}
			}
			
			if (stateInstructionSimple(4)) {
	    		if (getId() == Module.Walker_Head) {
	    			rotate(-45);
	    			stateInstrBroadcastNext();
	    		}
			}
		    	
			if (stateInstructionSimple(5)) {
		    	renameSwitch(Module.Walker_Left,Module.Walker_Right);
			}
		    	
			if (stateInstructionSimple(6)) {
	    		if (getId() == Module.Walker_Head) {
	    			if (stateOperation(WALK))
	    				stateOperationBroadcast(WALK2);
	    			else
	    				stateOperationBroadcast(WALK);
	    		}
			}		
		}
	
		
		
		if (stateOperation(GETDOWN)) {
			
			if (stateInstructionSimple(0) && !stateIsFinished()) {
				if (getGrouping() == Grouping.Floor && nbs().female().connected().contains(Module.Walker_Left)) {
					renameTo(Module.Floor_Downlifter);
					stateInstrBroadcastNext();
				}
				
				
			}
			
			if (stateInstruction(1)) {
				if (getId() == Module.Floor_Downlifter) {
					while (!nbs().connected().onGroup(Grouping.Floor).north().east().isEmpty()) {
						for (Module m: nbs().connected().onGroup(Grouping.Floor).north().east().modules()) {
							disconnect(m);
						}
    					waitAndDiscover();
    				}
					stateInstrBroadcastNext();
				
				}

			}
			if (stateInstructionSimple(2)) {
				if (!statePending(PENDING1) && getId() == Module.Floor_Downlifter) {
					rotate(90);
					statePendingBroadcast(PENDING1);
				}
				if (!statePending(PENDING2) && getId() == Module.Walker_Right) {
					rotateTo(0);
					statePendingBroadcast(PENDING2);
				}

				if (statePending(PENDING1 + PENDING2)) {
					stateInstrBroadcastNext(3);
				}

			}
			if (stateInstruction(3)) {
				if (!statePending(PENDING1)) {
					discoverNeighbors();
					if (getId() == Module.Walker_Right) {
						while (nbs().disconnected().onGroup(Grouping.Floor).exists()) {
	    					for (Module nb: nbs().disconnected().onGroup(Grouping.Floor).maleAlignedWithFemale().modules()) {
		    					connect(nb);
		    				}
	    					waitAndDiscover();
	    				}
						stateInstrBroadcastNext();
					}
					
				}
				if (getGrouping() == Grouping.Floor) {
	    			if (nbs().male().disconnected().contains(Module.Walker_Right)) {
	    				connect(Module.Walker_Right);
	    				waitAndDiscover();
	    			}
				}
				
			}
			if (stateInstruction(4)) {
				if (getId() == Module.Walker_Left) {
					while (nbs().connected().onGroup(Grouping.Floor).exists()) {
						for (Module nb: nbs().connected().onGroup(Grouping.Floor).maleAlignedWithFemale().modules()) {
	    					disconnect(nb);
	    				}
    					waitAndDiscover();
	    			}
					stateInstrBroadcastNext();
				}
			}
			
			if (stateInstructionSimple(5)) {
				if (!statePending(PENDING1) && getId() == Module.Walker_Left) {
//					rotate(90);
					rotateTo(0);
					statePendingBroadcast(1);
				}
				if (!statePending(PENDING2) && getId() == Module.Floor_Downlifter) {
					rotateTo(0);
					statePendingBroadcast(2);
					notification("Im floor_downlifter!");
				}
				if (statePending(PENDING1 + PENDING2)) {
					stateInstrBroadcastNext(6);
				}
			}
			
			if (stateInstruction(6)) {
				if (getId() == Module.Floor_Downlifter) {
	    			while (nbs().disconnected().maleAlignedWithFemale().exists()) {
	    				for (Module nb: nbs().disconnected().maleAlignedWithFemale().modules()) {
	    					connect(nb);
	    				}
	    				waitAndDiscover();
	    			}
	    			stateInstrBroadcastNext();
					
				}
			} 
			
			if (stateInstruction(7)) {
				if (getGrouping() == Grouping.Floor) {
					delay(2000);
					if (stateInstructionSimple(7)) { // TODO: Critical section!
						if (nbs().west().exists() && !nbs().east().exists()) {
							send(new Packet(getId()).setType(Type.FIX_SYMMETRY).getBytes(),NORTH_MALE_WEST);
							send(new Packet(getId()).setType(Type.FIX_SYMMETRY).getBytes(),SOUTH_MALE_WEST);
							stateFinish();
						}
					}
				}
				if (getId() == Module.Walker_Head) {
					delay(5000);
					stateInstrBroadcastNext();
				}
			}
			
			

			
			if (stateInstruction(8) && !stateIsFinished()) {
				
				
				if (getGrouping() == Grouping.Walker) {
					gradientResetAll();
					if (getId() == Module.Walker_Head) {
						renameRestore();
						stateOperationBroadcast(GETUP);
					}
					else {
						renameRestore();
					}
					
					
				}
				
				stateFinish();
			}
			
		}
		
	
	    
	}
  

	


	public void init () {
		setModuleColors (Module.Walker_Head,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		setModuleColors (Module.Walker_Left,new Color[]{Color.decode("#009999"),Color.decode("#999900")}); 
		setModuleColors (Module.Walker_Right,new Color[]{Color.decode("#003333"),Color.decode("#333300")}); 
		
		setModuleColors (Module.Floor_Uplifter,new Color[]{Color.decode("#FF9900"),Color.decode("#9900FF")});
//		setModuleColors (Module.Floor_UplifterLeft,new Color[]{Color.decode("#FF0099"),Color.decode("#0099FF")});
		
		setModuleColors (Module.Floor_Downlifter,new Color[]{Color.decode("#FF33CC"),Color.decode("#33CCFF")});
//		setModuleColors (Module.Floor_DownlifterLeft,new Color[]{Color.decode("#FF0099"),Color.decode("#9900FF")});
		
		
		setDefaultColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		setMessageFilter(Type.FIX_SYMMETRY.bit() | Type.STATE_INSTR_UPDATE.bit() | Type.STATE_OPERATION_NEW.bit() | Type.GRADIENT.bit());
		//setCommFailureRisk(0.25f,0.25f,0.98f,0.125f);
	}



	@Override
	protected void receiveMessage(Packet p, int connector) {
		if (p.getType() == Type.FIX_SYMMETRY) {
			if (!stateIsFinished()) {
				
				if (Connector.isFEMALE(connector)) {
					if (!Connector.isSOUTH(connector)) {
						switchNorthSouth();
					}
					if (Connector.isNORTH(p.getSourceConnector()) && !Connector.isWEST(connector) || Connector.isSOUTH(p.getSourceConnector()) && !Connector.isEAST(connector)) {
						switchEastWest();
					}
					
					send(p.getBytes(),NORTH_FEMALE_WEST);
					send(p.getBytes(),NORTH_FEMALE_EAST);
				}
				else if (Connector.isMALE(connector)) {
					if (!Connector.isEAST(connector)) {
						switchEastWest();
					}
					if (Connector.isEAST(p.getSourceConnector()) && !Connector.isSOUTH(connector) || Connector.isWEST(p.getSourceConnector()) && !Connector.isNORTH(connector)) {
						switchNorthSouth();
					}
					
					send(p.getBytes(),(NORTH_MALE_WEST));
					send(p.getBytes(),(SOUTH_MALE_WEST));
				}
				
				stateFinish();
			}
		}
			
		if (p.getType() == Type.GRADIENT && gradient(p.getData()[0]) > p.getData()[1]) {
			gradientTransit(p.getData()[0],p.getData()[1]);
		}
		
		if (p.getType() == Type.GRADIENT_RESET && p.getData()[0] != Byte.MAX_VALUE) {
			gradients.put(p.getData()[0], Byte.MAX_VALUE);
		}
		
		
		if (p.getType() == Type.GLOBAL_VAR && getGlobal(p.getData()[0]) != p.getData()[1]) {
			varsGlobal.put(p.getData()[0],p.getData()[1]);
			broadcast(new Packet(p),connector);
		}
		
	}




}
