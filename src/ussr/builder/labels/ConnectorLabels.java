package ussr.builder.labels;

import ussr.model.Connector;

/**
 * The main responsibility of this class is to provide support for handling labels
 * previously assigned to the connector;
 * @author Konstantinas
 */
public class ConnectorLabels extends Label {
	
	/**
	 * The connector as an entity in simulation.
	 */
	private Connector connector;	
	
	/**
	 * Handles the labels assigned to the connector.	
	 * @param connector, the connector
	 */
	public ConnectorLabels(Connector connector){
	this.connector = connector;	
	}
	
	/**
	 * Checks if the connector was assigned the label passed as a string.
	 * NOTE: IT IS SIMPLE CONTAINS CHECK FOR NOW.
	 * It is the method following STRATEGY pattern.
	 * @param label, the label name to check;
	 * @return true, if passed label was assigned to the connector, false - if not. 
	 */
	@Override
	public boolean has(String label) {		
		if (getLabels(connector).contains(label)){
			return true;
		}
		return false;		
	}		
	
}
