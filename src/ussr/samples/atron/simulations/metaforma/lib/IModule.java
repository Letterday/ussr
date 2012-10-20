package ussr.samples.atron.simulations.metaforma.lib;



public interface IModule extends IModuleHolder {
	public String toString();
	public boolean equals(Object m);
	public byte getNr();
	public IModEnum getMod();
	public IGroupEnum getGroup();
	public int ord();
}