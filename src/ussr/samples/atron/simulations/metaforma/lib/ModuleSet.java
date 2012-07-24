package ussr.samples.atron.simulations.metaforma.lib;


import java.util.HashSet;
import ussr.samples.atron.simulations.metaforma.gen.Grouping;
import ussr.samples.atron.simulations.metaforma.gen.Module;



public class ModuleSet implements IModuleHolder {
	private HashSet<Module> modules = new HashSet<Module>();
	
	
	
	public ModuleSet add (Module m) { 
		modules.add(m);
		return this;
	}
	
	
	public boolean isEmpty() {
		return size() == 0;
	}

	public int size () {
		return modules.size();
	}
	
	public String toString() {
		return modules.toString();
	}
	

	public ModuleSet onGroup (Grouping g) {
		ModuleSet ret = new ModuleSet();
		for (Module m: modules) {
			if (m.getGrouping().equals(g)) {
				ret.add(m);
			}
		}
		return ret;
	}
	
	public HashSet<Module> modules () {
		return modules;
	}


	@Override
	public boolean contains(Module m) {
		return modules.contains(m);
	}
	
}