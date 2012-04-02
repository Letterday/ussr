package ussr.samples.atron.simulations.wouter;

import java.util.ArrayList;

import ussr.description.Robot;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.model.Controller;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONBuilder;
import ussr.samples.atron.GenericATRONSimulation;

public class ATRONCommSimulator extends GenericATRONSimulation {

	public static void main( String[] args ) {
        new ATRONCommSimulator().main();
    }
	
	/**
	 * Default robot
	 */
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
                return new ATRONCommController();
            }
        };
        a.setRubberRing();
        a.setGentle();
        return a;
    }


	/**
	 * Delegate to library of builder helpers
	 */
	protected ArrayList<ModulePosition> buildRobot() {
		ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>();
		 mPos.add(new ModulePosition("driver", new VectorDescription(2*0*ATRON.UNIT,0*ATRON.UNIT,0*ATRON.UNIT), ATRON.ROTATION_EW));
         mPos.add(new ModulePosition("axle1", new VectorDescription(1*ATRON.UNIT,-1*ATRON.UNIT,0*ATRON.UNIT), ATRON.ROTATION_UD));
         mPos.add(new ModulePosition("axle2", new VectorDescription(-1*ATRON.UNIT,-1*ATRON.UNIT,0*ATRON.UNIT), ATRON.ROTATION_UD));
         mPos.add(new ModulePosition("wheel1", new VectorDescription(-1*ATRON.UNIT,-2*ATRON.UNIT,1*ATRON.UNIT), ATRON.ROTATION_SN));
         mPos.add(new ModulePosition("wheel2", new VectorDescription(-1*ATRON.UNIT,-2*ATRON.UNIT,-1*ATRON.UNIT), ATRON.ROTATION_NS));
         mPos.add(new ModulePosition("wheel3", new VectorDescription(1*ATRON.UNIT,-2*ATRON.UNIT,1*ATRON.UNIT), ATRON.ROTATION_SN));
         mPos.add(new ModulePosition("wheel4", new VectorDescription(1*ATRON.UNIT,-2*ATRON.UNIT,-1*ATRON.UNIT), ATRON.ROTATION_NS));
		return mPos;
        //return new ATRONBuilder().buildCar(4, new VectorDescription(3f,-0.25f,0f));
	}

}
