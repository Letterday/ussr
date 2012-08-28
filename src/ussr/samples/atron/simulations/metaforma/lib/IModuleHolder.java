package ussr.samples.atron.simulations.metaforma.lib;

import java.util.Set;


public interface IModuleHolder  {
	public String toString();
	public Set<IModule> modules ();
	public boolean contains(IModule m);
}
