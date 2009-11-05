package ussr.remote.facade;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.jme.scene.state.LightState;
import com.jme.scene.state.WireframeState;

import ussr.description.Robot;
import ussr.description.setup.WorldDescription;
import ussr.physics.PhysicsSimulation;

/**
 * Wrapper for a standard PhysicsSimulation allowing it to be used as a remote object.
 * (Wrapper for the simulation, used on the simulation side.)
 * 
 * Note to developers: additional methods for controlling a remote simulation can be added
 * here; RMI beginners should consider whether objects implement Serializable (and hence are
 * copied when passed as an argument to or returned from methods in this class) or should be
 * proxy objects like this one extending UnicastRemoteObject (and hence remain in the frontend
 * process when passed as argument or remain the simulator process when returned)
 * 
 * @author ups
 *
 */
public class RemotePhysicsSimulationImpl extends UnicastRemoteObject implements RemotePhysicsSimulation  {
    private PhysicsSimulation simulation;
    
    public RemotePhysicsSimulationImpl(PhysicsSimulation simulation) throws RemoteException {
        this.simulation = simulation;
    }

    public float getTime() throws RemoteException {
       return simulation.getTime();
    }

    public boolean isPaused() throws RemoteException {
        return simulation.isPaused();
    }
    
    public void setPause(boolean paused) throws RemoteException {
     simulation.setPause(paused);
    }

    public boolean isStopped() throws RemoteException {
        return simulation.isStopped();
    }

    public void setRobot(Robot bot) throws RemoteException {
        simulation.setRobot(bot);
    }

    public void setRobot(Robot bot, String type) throws RemoteException {
        simulation.setRobot(bot, type);
    }

    public void setWorld(WorldDescription world) throws RemoteException {
        simulation.setWorld(world);
    }

    public void start() throws RemoteException {
        simulation.start();
    }

    public void stop() throws RemoteException {
        simulation.stop();
    }
    
    public void setRealtime(boolean realtime)throws RemoteException{
    	simulation.setRealtime(realtime);
    }

	@Override
	public void setSingleStep(boolean singleStep) throws RemoteException {
		simulation.setSingleStep(singleStep);
	}

	@Override
	public void setShowPhysics(boolean showPhysics) throws RemoteException {
		simulation.setShowPhysics(showPhysics);
		
	}

	@Override
	public boolean isShowPhysics() throws RemoteException {
		return simulation.isShowingPhysics();
	}

	@Override
	public WireframeState getWireFrame() throws RemoteException {
		return simulation.getWireFrame();
	}

	@Override
	public boolean isWireFrameEnabled() throws RemoteException {
		return simulation.isWireFrameEnabled();
	}

	@Override
	public void setWireFrameEnabled(boolean enabled) throws RemoteException {
		 simulation.setWireFrameEnabled(enabled);
	}

	@Override
	public boolean isShowingBounds() throws RemoteException {
		return simulation.isShowingBounds();
	}

	@Override
	public void setShowBounds(boolean showBounds) throws RemoteException {
		simulation.setShowBounds(showBounds);		
	}

	@Override
	public boolean isShowingNormals() throws RemoteException {
		return simulation.isShowingNormals();
	}

	@Override
	public void setShowNormals(boolean showNormals) throws RemoteException {
		simulation.setShowNormals(showNormals);		
	}

	@Override
	public LightState getLightState() throws RemoteException {
		return simulation.getLightState();
	}

	@Override
	public boolean isLightStateShowing() throws RemoteException {		
		return simulation.isLightStateShowing();
	}

	@Override
	public void setLightState(LightState lightState) throws RemoteException {
		simulation.setLightState(lightState);		
	}

	@Override
	public void setLightStateShowing(boolean showLights) throws RemoteException {
		simulation.getLightState().setEnabled(showLights);		
	}

	@Override
	public boolean isShowingDepth() throws RemoteException {
		return simulation.isShowingDepth();
	}

	@Override
	public void setShowDepth(boolean showDepth) throws RemoteException {
		simulation.setShowDepth(showDepth);		
	};
	
	
	
	
	


}
