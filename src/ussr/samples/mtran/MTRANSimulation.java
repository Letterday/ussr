/**
 * Uniform Simulator for Self-reconfigurable (modular) Robots
 * 
 * (C) 2006 University of Southern Denmark
 */
package ussr.samples.mtran;

import java.util.ArrayList;

import ussr.model.Controller;
import ussr.physics.PhysicsFactory;
import ussr.physics.PhysicsLogger;
import ussr.physics.PhysicsObserver;
import ussr.physics.PhysicsParameters;
import ussr.physics.PhysicsSimulation;
import ussr.robotbuildingblocks.ModuleConnection;
import ussr.robotbuildingblocks.ModulePosition;
import ussr.robotbuildingblocks.Robot;
import ussr.robotbuildingblocks.RotationDescription;
import ussr.robotbuildingblocks.VectorDescription;
import ussr.robotbuildingblocks.WorldDescription;
import ussr.samples.GenericSimulation;

/**
 * Simple MTRAN simulation
 * 
 * @author david
 *
 */
public abstract class MTRANSimulation extends GenericSimulation implements PhysicsObserver {
	private static float unit = 0.065f*2+0.01f;
	private static float pi = (float)Math.PI;
    public static PhysicsSimulation simulation;
    
    private static ArrayList<ModulePosition> modulePos = new ArrayList<ModulePosition>();
    private static int constructIndex=0;
    private static boolean printContrutionProgram = true;
    
	public void runSimulation(WorldDescription world, boolean startPaused) {
        PhysicsLogger.setDefaultLoggingLevel();
        simulation = PhysicsFactory.createSimulator();
        simulation.setRobot(new MTRAN(){
        	public Controller createController() {
        		return getController("MTRAN");
        	}},"MTRAN");
        if(world==null) world = createWorld();
        this.changeWorldHook(world);
        simulation.setWorld(world);
        simulation.setPause(startPaused);
        simulation.subscribePhysicsTimestep(this);
        simulation.start();
    }
	protected void changeWorldHook(WorldDescription world) {
		world.setPlaneTexture(WorldDescription.GRID_TEXTURE);
		world.setHasBackgroundScenery(false);
		PhysicsParameters.get().setPhysicsSimulationStepSize(0.005f);
		PhysicsParameters.get().setWorldDampingLinearVelocity(0.9f);
    }
	/**
     * Create a world description for our simulation
     * @return the world description
     */
    private static WorldDescription createWorld() {
    	WorldDescription world = new WorldDescription();
        world.setPlaneSize(100);
        constructRobot();
		ArrayList<ModuleConnection> connections = allConnections(modulePos);
		System.out.println("#connection found = "+connections.size());
		world.setModuleConnections(connections);
		System.out.println("#Module Placed = "+modulePos.size());
		world.setModulePositions(modulePos);
		System.out.println("#Total         = "+modulePos.size());
		return world;
    }
    public static final RotationDescription ORI1 = new RotationDescription(pi,0,0);
    public static final RotationDescription ORI2 = new RotationDescription(-pi/2,0,0);
    public static final RotationDescription ORI3 = new RotationDescription(pi,0,pi/2);
	private static void constructRobot() {
		addModule(0,0,0,ORI2);
		addModule(2,0,0,ORI2);
		addModule(4,0,0,ORI2);
		addModule(6,0,0,ORI2);
		addModule(8,0,0,ORI2);
		addModule(10,0,0,ORI2);
		addModule(12,0,0,ORI2);
		addModule(14,0,0,ORI2);
		
		//addModule(-4,0,0,ORI3);

		
    }
    private static void addModule(int x, int y, int z, RotationDescription ori) {
    	VectorDescription pos = new VectorDescription(x*unit/2,y*unit/2-0.43f,z*unit/2);
    	//RotationDescription rot = rotFromBalls(ballPos.get(i),ballPos.get(j));
    	modulePos.add(new ModulePosition(Integer.toString(constructIndex),"MTRAN", pos, ori));
    	if(printContrutionProgram) System.out.println("addBall("+x+", "+y+", "+z+");");
    	constructIndex++;
	}
    private static ArrayList<ModuleConnection> allConnections(ArrayList<ModulePosition> modulePos) {
    	ArrayList<ModuleConnection> connections = new ArrayList<ModuleConnection>();
    	//System.out.println("modulePos.size()"+modulePos.size());
    	for(int i=0;i<modulePos.size();i++) {
    		for(int j=i+1;j<modulePos.size();j++) {
    			if(isConnectable(modulePos.get(i), modulePos.get(j))) {
    				System.out.println("Found connection from module "+modulePos.get(i).getName()+" to "+modulePos.get(j).getName());
    				connections.add(new ModuleConnection(modulePos.get(i).getName(),modulePos.get(j).getName()));
    			}
    		}
    	}
		return connections;
	}
	public static boolean isConnectable(ModulePosition m1, ModulePosition m2) {
    	float dist = m1.getPosition().distance(m2.getPosition());
    	return Math.abs(dist-unit)<0.01f;
    }

    public static void printConnectorPos() {
    	for(int x=-2;x<2;x++) {
        	for(int y=-2;y<2;y++) {
        		for(int z=-2;z<2;z++) {
        			if((x+y+z)%2==0&&(x*x+y*y+z*z)<3&&!(x==0&&y==0&&z==0)) {
        				System.out.println("new VectorDescription("+x+"*unit, "+y+"*unit, "+z+"*unit),");
        			}
        		}
        	}
        }
    }
	protected Robot getRobot() {
		// TODO Auto-generated method stub
		return null;
	}
	public abstract Controller getController(String type);
}
