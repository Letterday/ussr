package ussr.samples.atron.simulations.metaforma.gen;

import java.util.HashSet;
import java.util.Set;

import ussr.samples.atron.simulations.metaforma.lib.IModule;
import ussr.samples.atron.simulations.metaforma.lib.IModuleHolder;

public enum Grouping implements IModuleHolder{ALL, NONE, Floor,Walker, Clover, Left, Right, Uplifter;
	

	public boolean contains(IModule m) {
		return equals(m.getGrouping());
	}


	public Set<IModule> modules() {
		Set<IModule> mods = new HashSet<IModule>();
		for (IModule m: Mod.values()) {
			if (m.toString().startsWith(toString() + "_")) {
				mods.add(m);
			}
		}
		return mods;
		
	}
	
}
