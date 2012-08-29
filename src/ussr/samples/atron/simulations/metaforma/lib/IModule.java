package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.gen.Grouping;
import ussr.samples.atron.simulations.metaforma.gen.Mod;



public interface IModule extends IModuleHolder {
	public String toString();
	public boolean equals(Object m);
	public byte getNr();
	public Mod getModule();
	public Grouping getGrouping();
	public byte ord();
}
