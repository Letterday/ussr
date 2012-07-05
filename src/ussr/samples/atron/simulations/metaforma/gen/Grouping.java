package ussr.samples.atron.simulations.metaforma.gen;

import java.util.HashSet;
import java.util.Set;

import ussr.samples.atron.simulations.metaforma.lib.IModuleHolder;

public enum Grouping implements IModuleHolder{Floor,Walker,ALL, Clover, Left, Right, Struct, None;
	

	@Override
	public boolean contains(Module m) {
		return equals(m.getGrouping());
	}

	@Override
	public Set<Module> modules() {
		Set<Module> mods = new HashSet<Module>();
		for (Module m: Module.values()) {
			if (m.toString().startsWith(toString() + "_")) {
				mods.add(m);
			}
		}
		return mods;
		
	}
	
	public int length() {
		int j = 0;
		while (!Module.values()[j].toString().startsWith(toString() + "_")) {
			j++;
		}
		return j;
	}
}
