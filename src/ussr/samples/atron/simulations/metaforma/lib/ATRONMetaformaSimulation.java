package ussr.samples.atron.simulations.metaforma.lib;

import java.util.ArrayList;

import ussr.description.Robot;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.description.setup.WorldDescription;
import ussr.model.Controller;
import ussr.model.debugging.ConsoleInformationProvider;
import ussr.model.debugging.SimpleWindowedInformationProvider;
import ussr.physics.PhysicsFactory;
import ussr.physics.PhysicsParameters;
import ussr.physics.PhysicsSimulation;
import ussr.physics.jme.DebugInformationPicker;
import ussr.samples.ObstacleGenerator;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONBuilder;
import ussr.samples.atron.GenericATRONSimulation;

public abstract class ATRONMetaformaSimulation extends GenericATRONSimulation {


	public static void initSimulator () {
		PhysicsParameters.get().setRealisticCollision(true);
		ussr.physics.jme.robots.JMEATRONFactory.setConnectorMaxAlignmentForce(110f);
		ussr.physics.jme.robots.JMEATRONFactory.setConnectorMaxAlignmentDistance(0.002f);
		ussr.physics.jme.robots.JMEATRONFactory.setConnectorEpsilonAlignmentDistance(0.01f);

		PhysicsFactory.getOptions().setLowLevelCommunicationDebugOutput(true);
		PhysicsFactory.setDebugProviderFactory(SimpleWindowedInformationProvider.getFactory(true));
	}

	@Override
	protected void simulationHook(PhysicsSimulation simulation) {
        DebugInformationPicker.install(simulation);
    }
		
	
	
	
	
	protected abstract Robot getRobot();

	@Override
	protected ArrayList<ModulePosition> buildRobot() {
		return new ATRONBuilder().buildRectangle(7,3,new VectorDescription(0,-5*ATRON.UNIT,0),"Floor_",false);
	}
	
	protected void changeWorldHook(WorldDescription world) {
        ObstacleGenerator generator = new ObstacleGenerator();
        generator.setCircleObstacleRadius(0.6f);
        generator.setNumberOfCircleObstacles(15);
        generator.setObstacleSize(0.07f);
        //generator.activateCircleGap(); 
        //generator.obstacalize(ObstacleGenerator.ObstacleType.CIRCLE, world);
        world.setPlaneTexture(WorldDescription.GRASS_TEXTURE);
    }
}