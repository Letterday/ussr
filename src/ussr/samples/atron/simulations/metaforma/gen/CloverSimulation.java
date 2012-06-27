package ussr.samples.atron.simulations.metaforma.gen;
import java.awt.Color;
import java.util.ArrayList;

import org.python.antlr.PythonParser.power_return;

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
		return new ATRONBuilder().buildGrid(4, 4, "Floor_",false);
	}
	
}



class CloverController extends MetaformaController implements ControllerInformationProvider {

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
	
	public void handleStates () {
		if (stateOperation(GETUP) || stateOperation(ELECT)) {

//			if (stateInstruction(0)  && !stateIsFinished()) {
//				discoverNeighbors();
//				delay(1000);
//				
//				if (nbs().female().east().exists() && !nbs().west().exists()) {
//					gradientCreate(0);
//					stateFinish();
//				}
////				if (nbs().male().east().exists() && !nbs().west().exists()) {
////					gradientCreate(1);
////					stateFinish();
////				}
//				delay(2000);
//				
//				stateInstructionBroadcastNext(1);
//			}
			
			if (stateInstruction(0)) {
				discoverNeighbors();
				if (nbs().female().east().exists() && !nbs().west().exists()){
					renameTo(Module.Clover_South);
					stateFinish();
				}
				if (nbs().east().contains(Module.Clover_South)){
					renameTo(Module.Clover_West);
					stateFinish();
				}
				if (nbs().west().contains(Module.Clover_South)){
					renameTo(Module.Clover_East);
					stateFinish();
				}
				if (nbs().contains(Module.Clover_East) && nbs().contains(Module.Clover_West)){
					renameTo(Module.Clover_North);
					stateFinish();
					stateInstrBroadcastNext(1);
				}
			}
		
			disconnect (Module.Clover_West, Module.Clover_South, new RunAt(this, 1));
				
			
			rotate (Module.Clover_North,90, new RunAt(this,2,1,2));
			rotate (Module.Clover_East,-90, new RunAt(this,2,2,2));
			
			
			rotate (Module.Clover_North,90, new RunAt(this,3,1,2));
			rotate (Module.Clover_East,-90, new RunAt(this,3,2,2));
			
				
			
			
			connect (Module.Clover_East,Grouping.Floor,new RunAt(this,4,1,2));
			connect (Module.Clover_South,Grouping.Floor,new RunAt(this,4,2,2));

			disconnect (Module.Clover_West,Grouping.Floor,new RunAt(this,5,1,2));
			disconnect (Module.Clover_North,Grouping.Floor,new RunAt(this,5,2,2));

			rotate (Module.Clover_North,180,new RunAt(this,6,1,2));
			rotate (Module.Clover_East,180,new RunAt(this,6,2,2));


			connect (Module.Clover_South,Module.Clover_West, new RunAt(this,7));
//			
//			if (stateInstruction(10)) {
//				if(getGrouping() == Grouping.Clover && !statePending(pow(2,getId().getNumber()))) {
//					renameRestore();
//					switchNorthSouth();
//					switchEastWest(); 
//					statePendingBroadcast(pow(2,getId().getNumber()));
//				}
//				if (statePending(PENDING1+PENDING2+PENDING3+PENDING4)) {
//					stateOperationBroadcast(GETUP);
//				}
//			}
		}
		
   }
	
	
	public void rotate (Module m, int degrees, RunAt stateTrans) {
		if (getId() == m && stateTrans.preCon() ) {
			rotate(degrees);
			stateTrans.commit();
		}
	}
	
	
	public void connect (Module m, Grouping g, RunAt stateTrans) {
		connection(m,g,true, stateTrans);
	}
	

	public void disconnect (Module m, Grouping g, RunAt stateTrans) {
		connection(m,g,false, stateTrans);
	}
	
	
	private void connect(Grouping g) {
		discoverNeighbors();
		for (Module m: nbs().onGroup(g).connected().male().modules()) {
			connect(m);
		}
	}
	
	private void disconnect(Grouping g) {
		discoverNeighbors();
		for (Module m: nbs().onGroup(g).connected().male().modules()) {
			disconnect(m);
		}
	}
	
	private void connection(Module m, Grouping g, boolean connect, RunAt stateTrans) {
		discoverNeighbors();
		if (getGrouping() == g && stateTrans.preCon()) {
			if (nbs().male().isConnected(!connect).contains(m)) {
				connection(m,connect);
			}
		}
		
		if (getId() == m && stateTrans.preCon()) {
			for (Module mg: nbs().onGroup(g).isConnected(!connect).male().modules()) {
				connection(mg,connect);
			}
			if (nbs().isConnected(!connect).onGroup(g).isEmpty()) {
				stateTrans.commit();
			}
		}
		
	}
	
	private void connect(Module m1, Module m2, RunAt stateTrans) {
		connect_switched(m1,m2, true, stateTrans);
		connect_switched(m2,m1, true, stateTrans);
	}
  
	private void disconnect(Module m1, Module m2, RunAt stateTrans) {
		connect_switched(m1,m2, false, stateTrans);
		connect_switched(m2,m1, false, stateTrans);
	}
	
	private void connect_switched(Module m1, Module m2, boolean c, RunAt stateTrans) {
		if (getId() == m1 && stateTrans.preCon()) {
			discoverNeighbors();
			if (nbs().male().contains(m2) && stateTrans.preCon()) {
				while (!nbs().male().isConnected(c).contains(m2)) {
					connection(m2,c);
				}
				stateTrans.commit();
			}
		}
		
	}

public void init () {
		setModuleColors (Module.Clover_North,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		setModuleColors (Module.Clover_South,new Color[]{Color.decode("#00AAAA"),Color.decode("#AAAA00")}); 
		setModuleColors (Module.Clover_West,new Color[]{Color.decode("#006666"),Color.decode("#666600")}); 
		setModuleColors (Module.Clover_East,new Color[]{Color.decode("#002222"),Color.decode("#222200")}); 
		
		addStructureColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		setMessageFilter(Type.STATE_INSTR_UPDATE.bit() |Type.STATE_OPERATION_NEW.bit());
	}

@Override
protected void receiveMessage(Packet p, int connector) {
	// TODO Auto-generated method stub
	
}


}
