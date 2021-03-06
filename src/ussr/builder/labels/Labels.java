package ussr.builder.labels;

import ussr.comm.Packet;

/**
 * Provides support for manipulating labels during implementation of controllers, which are
 * utilizing labels for identification of modules in the morphology of a modular robot.
 * @author Konstantinas
 */
public interface Labels {	
	
	/**
	 * Checks if the entity was assigned the label passed as a string.
	 * NOTE: IT IS SIMPLE CONTAINS CHECK FOR NOW.
	 * @param label, the label name to check;
	 * @return true, if passed label was assigned, false - if not. 
	 */
	public boolean has(String label);
	
	public String getLabels();
	
	public void sendMessage(int connectorNr, Packet packet);
	
	public String receiveMessage(int connectorNr);
}
