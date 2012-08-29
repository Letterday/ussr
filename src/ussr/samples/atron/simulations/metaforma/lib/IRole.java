package ussr.samples.atron.simulations.metaforma.lib;


public interface IRole {
	public byte index();
	
	public IRole fromByte(byte b);
	
	public byte size();
}
