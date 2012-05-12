package ussr.samples.atron.simulations.metaforma.exp;

import java.util.ArrayList;

import ussr.description.Robot;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.model.Controller;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONBuilder;
import ussr.samples.atron.GenericATRONSimulation;

public class ATRONLookAroundSimulator extends GenericATRONSimulation {

	public static void main( String[] args ) {
        new ATRONLookAroundSimulator().main();
    }
	
	/**
	 * Default robot
	 */
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
                return new ATRONLookAroundController();
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
		mPos.add(new ModulePosition("driver0", new VectorDescription(0,0,0), ATRON.ROTATION_EW));
        mPos.add(new ModulePosition("axleOne5", new VectorDescription(1*ATRON.UNIT,-1*ATRON.UNIT,0*ATRON.UNIT), ATRON.ROTATION_UD));
        mPos.add(new ModulePosition("axleTwo6", new VectorDescription(-1*ATRON.UNIT,-1*ATRON.UNIT,0*ATRON.UNIT), ATRON.ROTATION_UD));
        mPos.add(new ModulePosition("wheel1left", new VectorDescription(-1*ATRON.UNIT,-2*ATRON.UNIT,1*ATRON.UNIT), ATRON.ROTATION_SN));
        mPos.add(new ModulePosition("wheel2right", new VectorDescription(-1*ATRON.UNIT,-2*ATRON.UNIT,-1*ATRON.UNIT), ATRON.ROTATION_NS));
        mPos.add(new ModulePosition("wheel3left", new VectorDescription(1*ATRON.UNIT,-2*ATRON.UNIT,1*ATRON.UNIT), ATRON.ROTATION_SN));
        mPos.add(new ModulePosition("wheel4right", new VectorDescription(1*ATRON.UNIT,-2*ATRON.UNIT,-1*ATRON.UNIT), ATRON.ROTATION_NS));
        mPos.add(new ModulePosition("arm1", new VectorDescription(1*ATRON.UNIT,1*ATRON.UNIT,0*ATRON.UNIT),ATRON.ROTATION_UD));
        mPos.add(new ModulePosition("arm2", new VectorDescription(2*ATRON.UNIT,2*ATRON.UNIT,0*ATRON.UNIT),ATRON.ROTATION_EW));
   
		return mPos;
        //return new ATRONBuilder().buildCar(4, new VectorDescription(3f,-0.25f,0f));
	}

}
