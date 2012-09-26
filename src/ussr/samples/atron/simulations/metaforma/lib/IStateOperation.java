package ussr.samples.atron.simulations.metaforma.lib;

public interface IStateOperation extends IState{
	public byte ord();

	public IStateOperation fromByte(byte b);
}
