package ussr.samples.atron.simulations.metaforma.lib;

public interface IVar {
	public byte index();
	
	public IVar fromByte(byte b);

	public boolean isLocal();
	public boolean isMeta();
	public boolean isMetaRegion();
	public boolean isLocalState();

}

