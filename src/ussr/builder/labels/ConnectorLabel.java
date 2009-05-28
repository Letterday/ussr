package ussr.builder.labels;

import ussr.builder.BuilderHelper;
import ussr.model.Connector;
import ussr.model.Module;

public class ConnectorLabel extends Label {
	
	private Connector connector;
	
	public ConnectorLabel(Connector connector){
	this.connector = connector;	
	}

	
	
	
	public String getLabels(){
		String labels = connector.getProperty(BuilderHelper.getLabelsKey());
		if (labels ==null){
			return "none";
		}
		return labels;
	}




	@Override
	public boolean has(String label) {		
		if (getLabels().contains(label)){
			return true;
		}
		return false;		
	}	
	
}