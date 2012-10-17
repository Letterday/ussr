package ussr.samples.atron.simulations.metaforma.lib;


public interface IMetaPart {
	public byte index();
	
	public IMetaPart fromByte(byte b);
	
	public byte size();
}
