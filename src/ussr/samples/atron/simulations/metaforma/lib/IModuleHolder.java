package ussr.samples.atron.simulations.metaforma.lib;

import java.util.Set;

import ussr.samples.atron.simulations.metaforma.gen.Module;

public interface IModuleHolder {
	public String toString();
	public Set<Module> modules ();
	public boolean contains(Module m);
}
