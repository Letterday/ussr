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
	
	public static void main( String[] args ) {
        new ATRONSMetaVehicleSimulation().main();
    }
	
	/**
	 * Default robot
	 */
	protected Robot getRobot() {
        ATRON wheel = new ATRON() {
            public Controller createController() {
                return new ATRONMetaVehicleController1();
            }
        };
        wheel.setRubberRing();
        wheel.setGentle();
        ATRON normal = new ATRON() {
            public Controller createController() {
                return new ATRONMetaVehicleController1();
            }
        };
        super.setRobot(wheel,"wheel");
        super.setRobot(normal,"normal");
        return Robot.NO_DEFAULT;
    }

	/**
	 * Delegate to library of builder helpers
	 */
	protected ArrayList<ModulePosition> buildRobot() {
        ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>();
        makeMetaVehicle(mPos, new VectorDescription(3f,-0.25f,0f));
        makeMetaVehicle(mPos, new VectorDescription(2f,-0.25f,0f));
		return mPos;
	}

	private int meta_id = 0;
    private void makeMetaVehicle(ArrayList<ModulePosition> mPos, VectorDescription position) {
        float Xoffset = position.getX();
        float Yoffset = position.getY();
        float Zoffset = position.getZ();
        String id = "id"+(meta_id++)+".";
        mPos.add(new ModulePosition("centerForward_"+id, "normal", new VectorDescription(-2*ATRON.UNIT+Xoffset,-2*ATRON.UNIT+Yoffset,0*ATRON.UNIT+Zoffset), ATRON.ROTATION_EW));
        mPos.add(new ModulePosition("left_center_wheel_"+id, "wheel", new VectorDescription(-1*ATRON.UNIT+Xoffset,-2*ATRON.UNIT+Yoffset,1*ATRON.UNIT+Zoffset), ATRON.ROTATION_SN));
        mPos.add(new ModulePosition("right_forward_wheel_"+id, "wheel", new VectorDescription(-3*ATRON.UNIT+Xoffset,-2*ATRON.UNIT+Yoffset,-1*ATRON.UNIT+Zoffset), ATRON.ROTATION_NS));
        mPos.add(new ModulePosition("centerBackward_"+id, "normal", new VectorDescription(0*ATRON.UNIT+Xoffset,-2*ATRON.UNIT+Yoffset,0*ATRON.UNIT+Zoffset), ATRON.ROTATION_EW));
        mPos.add(new ModulePosition("left_backward_wheel_"+id, "wheel", new VectorDescription(1*ATRON.UNIT+Xoffset,-2*ATRON.UNIT+Yoffset,1*ATRON.UNIT+Zoffset), ATRON.ROTATION_SN));
        mPos.add(new ModulePosition("right_center_wheel_"+id, "wheel", new VectorDescription(-1*ATRON.UNIT+Xoffset,-2*ATRON.UNIT+Yoffset,-1*ATRON.UNIT+Zoffset), ATRON.ROTATION_NS));
    }
    
	/**
	 * Add obstacle
	 */
	protected void changeWorldHook(WorldDescription world) {
        ObstacleGenerator generator = new ObstacleGenerator();
        generator.obstacalize(ObstacleGenerator.ObstacleType.LINE, world);
    }
}
