/**
 * Unified Simulator for Self-Reconfigurable Robots (USSR)
 * (C) University of Southern Denmark 2008
 * This software is distributed under the BSD open-source license.
 * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
 */
package atower;

import java.util.ArrayList;
/* Essentially a stripped down car */

import ussr.description.Robot;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.description.setup.WorldDescription;
import ussr.model.Controller;
import ussr.samples.ObstacleGenerator;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONBuilder;
import ussr.samples.atron.GenericATRONSimulation;

/**
 * A simulation for a two-wheeler ATRON robot.
 * 
 * @author Modular Robots @ MMMI
 *
 */
public class ATRONSMetaVehicleSimulation extends GenericATRONSimulation {
	
    public static final float lateral_misalignment = 0.0f; // current limit between 0.1 and 0.2
    
	public static void main( String[] args ) {
        new ATRONSMetaVehicleSimulation().main();
    }
	
	/**
	 * Default robot
	 */
	protected Robot getRobot() {
        ATRON wheel = new ATRON() {
            public Controller createController() {
                return new ATRONSimpleVehicleController();
            }
        };
        wheel.setRubberRing();
        wheel.setGentle();
        /*ATRON normal = new ATRON() {
            public Controller createController() {
                return new ATRONMetaVehicleController1();
            }
        };
        super.setRobot(wheel,"wheel");
        super.setRobot(normal,"normal");
        return Robot.NO_DEFAULT;*/
        return wheel;
    }

	/**
	 * Delegate to library of builder helpers
	 */
	protected ArrayList<ModulePosition> buildRobot() {
	    ATRONBuilder builder = new ATRONBuilder("normal");
	    builder.setWheelModuleName("wheel");
	    builder.buildAsLattice(20, 3, 1, 5);
	    builder.buildCar(2, new VectorDescription(-0.4f,-0.25f,0.135f));
		return builder.getPositions();
	}

	/**
	 * Add obstacle
	 */
	protected void changeWorldHook(WorldDescription world) {
        ObstacleGenerator generator = new ObstacleGenerator();
        generator.obstacalize(ObstacleGenerator.ObstacleType.LINE, world);
    }
}
