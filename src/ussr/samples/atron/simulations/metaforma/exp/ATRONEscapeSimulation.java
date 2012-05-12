package ussr.samples.atron.simulations.metaforma.exp;

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
/*
public class ATRONEscapeSimulation extends GenericATRONSimulation {

	protected int wheelCount;
	
	public static void main( String[] args ) {
		PhysicsParameters.get().setRealisticCollision(true);
        new ATRONEscapeSimulation(2).main();
    }
	
	public ATRONEscapeSimulation (int wheels) {
		wheelCount = wheels;
	}
	
	
	
	
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
            	if (wheelCount == 2) {
            		return new ATRONEscapeControllerTwoWheels();
            	}
            	else {
            		return new ATRONEscapeControllerFourWheels();
            	}
            }
        };
        a.setGentle();
        a.setRubberRing();
        return a;
    }

	@Override
	protected ArrayList<ModulePosition> buildRobot() {
		return new ATRONBuilder().buildCar(wheelCount, new VectorDescription(0,0,0));
	}
	
	protected void changeWorldHook(WorldDescription world) {
        ObstacleGenerator generator = new ObstacleGenerator();
        generator.setCircleObstacleRadius(0.6f);
        generator.setNumberOfCircleObstacles(15);
        generator.setObstacleSize(0.07f);
        generator.activateCircleGap(); 
        generator.obstacalize(ObstacleGenerator.ObstacleType.CIRCLE, world);
        world.setPlaneTexture(WorldDescription.GRASS_TEXTURE);
    }
	
} */
