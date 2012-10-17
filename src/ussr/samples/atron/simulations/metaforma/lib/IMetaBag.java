package ussr.samples.atron.simulations.metaforma.lib;

import java.util.ArrayList;

public interface IMetaBag extends IBag {

	public IMetaBag setVar(String var, int val, int seqNr);

	public byte getVarNr(String name);
	public byte getVarSeqNr(String name);
	public String getVarByNr(byte b);
	
	

	public byte regionID();

	public byte completed();

	public ArrayList<String> getVars();

	

	public byte getCountInRegion();

	public void setRegionID(byte ID);

	public void setCountInRegion(byte c);

	public void releaseRegion();
	public void resetVars();
	public void disable();
	
}
