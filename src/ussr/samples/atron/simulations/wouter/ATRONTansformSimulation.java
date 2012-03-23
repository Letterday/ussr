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

public class ATRONTansformSimulation extends GenericATRONSimulation {

	public static void main( String[] args ) {
        new ATRONTansformSimulation().main();
    }
	
	/**
	 * Default robot
	 */
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
                return new ATRONTransformController();
            }
        };
        a.setRubberRing();
        a.setGentle();
        return a;
    }

	@Override
	protected ArrayList<ModulePosition> buildRobot() {
		return new ATRONBuilder().buildEight(new VectorDescription(0,0,0));
	}
	
	protected void changeWorldHook(WorldDescription world) {
        ObstacleGenerator generator = new ObstacleGenerator();
        generator.obstacalize(ObstacleGenerator.ObstacleType.LINE, world);
        world.setPlaneTexture(WorldDescription.GRASS_TEXTURE);
    }
	
}
