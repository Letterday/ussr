package ussr.aGui.tabs;

import ussr.physics.jme.JMESimulation;

/**
 * Defines visual appearance of the tab called "Assign Behaviours".  
 * @author Konstantinas
 */
public class AssignBehavioursTab extends Tabs {

	public AssignBehavioursTab(String tabTitle,JMESimulation jmeSimulation){
		this.tabTitle = tabTitle;
		this.jPanel1000 = new javax.swing.JPanel();
		this.jmeSimulation = jmeSimulation;
		initComponents();
	}
	
	/**
     * Initializes the visual appearance of all components in the panel.
     * Follows Strategy  pattern.
     */
	public void initComponents() {
		// TODO Auto-generated method stub
		
	}

}
