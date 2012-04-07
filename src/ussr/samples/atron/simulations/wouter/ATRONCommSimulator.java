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

        return a;
    }


	/**
	 * Delegate to library of builder helpers
	 */
	protected ArrayList<ModulePosition> buildRobot() {
		return new ATRONBuilder().buildRectangle(7,3,new VectorDescription(0,-5*ATRON.UNIT,0));
        //return new ATRONBuilder().buildCar(4, new VectorDescription(3f,-0.25f,0f));
	}

}
