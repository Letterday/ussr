package ussr.samples.atron.simulations.wouter;

import java.util.ArrayList;

import ussr.description.Robot;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.description.setup.WorldDescription;
import ussr.model.Controller;
import ussr.physics.PhysicsParameters;
import ussr.samples.ObstacleGenerator;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONBuilder;
import ussr.samples.atron.GenericATRONSimulation;

public class ATRONMetaModuleSimulation extends GenericATRONSimulation {

	
	public static void main( String[] args ) {
		PhysicsParameters.get().setRealisticCollision(true);
		ussr.physics.jme.robots.JMEATRONFactory.maxAlignmentForce = 10f;
		ussr.physics.jme.robots.JMEATRONFactory.maxAlignmentDistance = 2f;
		ussr.physics.jme.robots.JMEATRONFactory.epsilonAlignmentDistance = 0.01f;
		//PhysicsParameters.get().setGravity(0);
        new ATRONMetaModuleSimulation().main();
    }
	
	/**
	 * Default robot
	 */
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
            		return new ATRONMetaModuleController();
            }
        };
        //a.setRubberRing();
        
        return a;
    }

	@Override
	protected ArrayList<ModulePosition> buildRobot() {
		return new ATRONBuilder().buildRectangle(7,3,new VectorDescription(0,-5*ATRON.UNIT,0));
	}
	
	protected void changeWorldHook(WorldDescription world) {
        ObstacleGenerator generator = new ObstacleGenerator();
        generator.setCircleObstacleRadius(0.6f);
        generator.setNumberOfCircleObstacles(15);
        generator.setObstacleSize(0.07f);
        generator.activateCircleGap(); 
        //generator.obstacalize(ObstacleGenerator.ObstacleType.CIRCLE, world);
        world.setPlaneTexture(WorldDescription.GRASS_TEXTURE);
    }
	
}
