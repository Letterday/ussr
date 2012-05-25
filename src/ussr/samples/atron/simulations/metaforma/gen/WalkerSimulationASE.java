/**
 * Unified Simulator for Self-Reconfigurable Robots (USSR)
 * (C) University of Southern Denmark 2008
 * This software is distributed under the BSD open-source license.
 * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
 */
package ussr.samples.atron.simulations.metaforma.gen;

import java.util.ArrayList;

import ussr.description.Robot;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.description.setup.WorldDescription;
import ussr.model.Controller;
import ussr.model.debugging.SimpleWindowedInformationProvider;
import ussr.physics.PhysicsFactory;
import ussr.physics.PhysicsParameters;
import ussr.physics.PhysicsSimulation;
import ussr.physics.PhysicsParameters.Material;
import ussr.physics.jme.DebugInformationPicker;
import ussr.samples.ObstacleGenerator;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONBuilder;
import ussr.samples.atron.ase.GenericASESimulation;
import ussr.samples.atron.simulations.metaforma.lib.MetaformaASEController;




public class WalkerSimulationASE extends GenericASESimulation {
	
    private ObstacleGenerator.ObstacleType obstacle = ObstacleGenerator.ObstacleType.LINE;
    
	public static void main( String[] args ) {		
		PhysicsParameters.get().setPlaneMaterial(Material.CONCRETE);
        PhysicsParameters.get().setPhysicsSimulationStepSize(0.01f);
 		PhysicsParameters.get().setRealisticCollision(true);
		PhysicsParameters.get().setWorldDampingLinearVelocity(0.5f);
		PhysicsParameters.get().setMaintainRotationalJointPositions(false);
		PhysicsFactory.setDebugProviderFactory(SimpleWindowedInformationProvider.getFactory(true));
        new WalkerSimulationASE().main();
	}

	@Override
	protected void simulationHook(PhysicsSimulation simulation) {
        DebugInformationPicker.install(simulation);
    }
    
	
	protected Robot getRobot() {
        ATRON robot = new ATRON() {
            public Controller createController() {
                return new MetaformaASEController();
            }
        };
        robot.setRealistic();
        robot.setRadio();
        return robot;
    }
	
	
	
	protected ArrayList<ModulePosition> buildRobot() {
		return new ATRONBuilder().buildRectangle(7,3,new VectorDescription(0,-5*ATRON.UNIT,0),"Floor_",true);
	}
    
    protected void changeWorldHook(WorldDescription world) {
        //new ObstacleGenerator().obstacalize(obstacle, world);
    	world.setPlaneTexture(WorldDescription.WHITE_GRID_TEXTURE);
		world.setHasBackgroundScenery(false);
		PhysicsFactory.getOptions().setStartPaused(false);
    }
}
