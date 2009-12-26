package ussr.remote.facade;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import ussr.aGui.helpers.hintPanel.HintPanelTypes;
import ussr.aGui.tabs.constructionTabs.ConstructRobotTab;
import ussr.aGui.tabs.controllers.AssignBehaviorsTabController;
import ussr.aGui.tabs.controllers.ConstructRobotTabController;
import ussr.aGui.tabs.controllers.LabelingTabController;
import ussr.aGui.tabs.controllers.SimulationTabController;
import ussr.aGui.tabs.simulation.SimulationTab;
import ussr.builder.enumerations.ConstructionTools;
import ussr.builder.enumerations.SupportedModularRobots;
import ussr.builder.simulationLoader.SimulationSpecification;


/**
 * Wrapper for a GUI call backs allowing it to be used as a remote object.
 * (Used on the GUI side.)
 * @author Konstantinas
 */
public class GUICallbackControlImpl extends UnicastRemoteObject implements GUICallbackControl {

	/**
	 * Wrapper for a GUI call backs allowing it to be used as a remote object.
     * (Used on the GUI side.)
	 * @throws RemoteException
	 */
	public GUICallbackControlImpl() throws RemoteException {
	}

	/**
	 * Adapts Construct Robot tab to the tool chosen by user. To be more precise disables and enables relevant components of the tab. 
	 * @param chosenTool,the tool chosen by the user in Construct Robot tab.
	 */
	public void adaptConstructRobotTabToChosenTool(ConstructionTools chosenTool)throws RemoteException{
		ConstructRobotTabController.adaptConstructRobotTabToChosenTool(chosenTool);
	};
	
	/**
	 * Populates the table in Assign behavior tab with labels of entity(module, connector and so on) selected in simulation environment.
	 * @param labels, the string of labels separated by comma to populate the table with.
	 */
	public void updateTableWithLabels(String labels) throws RemoteException{
		LabelingTabController.updateTableLabels(labels);
	}
	
	/**
	 * Sets the global ID of the module selected in simulation environment.
	 * @param selectedModuleID, the global ID of the module selected in simulation environment.
	 */
	public void setSelectedModuleID(int selectedModuleID)throws RemoteException{
		ConstructRobotTabController.setSelectedModuleID(selectedModuleID);
	}
	
	/**
	 * Adapts construct robot to the module type selected in simulation environment.
	 * @param supportedModularRobot, the type of modular robot to adapt to.
	 */
	public void adaptConstructRobotTabToSelectedModuleType(SupportedModularRobots supportedModularRobot)throws RemoteException{
		ConstructRobotTabController.adaptTabToSelectedModule(supportedModularRobot);		
	}

	/**
	 * Updates the hint panel in the tab called Assign Behaviors by changing icon and text of it.
	 * @param hintPanelTypes, the type of hint panel.
	 * @param text, the text for hint panel to display.
	 */
	public void updateHintPanelAssignBehaviorsTab(HintPanelTypes hintPanelTypes, String text)throws RemoteException{
		AssignBehaviorsTabController.updateHintPanel(hintPanelTypes, text);
	}
	
	/**
	 * Calls back GUI in order to indicate new module addition in simulation environment.
	 */
	public void newModuleAdded()throws RemoteException{
		ConstructRobotTabController.newModuleAdded();		
	}
	
	public SimulationSpecification getSimulationSpecification() throws RemoteException{
		return SimulationTabController.getSimulationSpecification();
	}
	
	
	public void newRobotLoaded(SimulationSpecification simulationSpecification)throws RemoteException{
		SimulationTab.addRobotNode(simulationSpecification);
        SimulationTabController.setSimulationSpecification(simulationSpecification);
	}
	
	/*public void adaptTabToModuleInSimulation()throws RemoteException{
		ConstructRobotTabController.adaptTabToModuleInSimulation();
	};*/
	
	public String getDefaultConstructionModuleType()throws RemoteException{
		return ConstructRobotTab.getDefaultConstructionModuleType();
	}
	
	
	

}
