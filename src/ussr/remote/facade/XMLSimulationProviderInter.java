package ussr.remote.facade;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import ussr.builder.saveLoadXML.SaveLoadXMLFileTemplateInter;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;

public interface XMLSimulationProviderInter extends Remote {

	
	public String getRobotMorphologyLocation()throws RemoteException;
	
	public Map<Integer,ModulePosition> getRobotModules()throws RemoteException;

	
	public String getIDsModules()throws RemoteException;
}
