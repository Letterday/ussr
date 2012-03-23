package ussr.samples.atron.simulations.wouter;

import java.util.ArrayList;

import ussr.description.Robot;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.description.setup.WorldDescription;
import ussr.model.Controller;
import ussr.samples.ObstacleGenerator;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONBuilder;
import ussr.samples.atron.GenericATRONSimulation;

public class ATRONObstacleAvoiderSimulation extends GenericATRONSimulation {

	protected int wheelCount = 2;
	
	public static void main( String[] args ) {
        new ATRONObstacleAvoiderSimulation().main();
    }
	
	/**
	 * Default robot
	 */
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
                return new ATRONObstacleAvoiderControllerTwoWheels();
            }
        };
        a.setRubberRing();
        a.setGentle();
        return a;
    }

	@Override
	protected ArrayList<ModulePosition> buildRobot() {
		return new ATRONBuilder().buildCar(wheelCount, new VectorDescription(2,0,0));
	}
	
	protected void changeWorldHook(WorldDescription world) {
        ObstacleGenerator generator = new ObstacleGenerator();
        generator.obstacalize(ObstacleGenerator.ObstacleType.LINE, world);
        world.setPlaneTexture(WorldDescription.GREY_GRID_TEXTURE);
    }
	
}
