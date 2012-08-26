package ussr.samples.atron.simulations.metaforma.gen;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import sun.security.util.BigInt;
import ussr.samples.atron.simulations.metaforma.lib.IModule;
import ussr.samples.atron.simulations.metaforma.lib.IModuleHolder;


public class Module implements IModuleHolder,IModule {

	public Mod mod;
	public byte number;
	
	@Override
	public int hashCode () {
		if (number == 0) {
			return mod.hashCode();
		}
		else {
			return mod.hashCode() ^ number;
		}
	}
	
	@Override
	public boolean contains(IModule m) {
		return equals(m);
	}	
	
	public boolean equals (Object m) {
		return mod.equals(((IModule)m).getModule()) && number == ((IModule)m).getNr();		
	}

	public Module () {
		mod = Mod.NONE;
	}
	
	public Module (Mod m) {
		this(m,0);
	}

	public Module (Mod m, int nr) {
		mod = m;
		number = (byte) nr;
	}
	
	public byte ord() {
		return (byte) (mod.ord() + number);
	}
		 
	public static Module value(String name) {
		String parts[] = name.split("_");
		if (parts.length != 2) {
			throw new IllegalArgumentException(name);
		}
		for(Mod m : Mod.values()) {
			if (m.name().equalsIgnoreCase(name)) {
				return new Module(m);
			}
			if (m.name().equalsIgnoreCase(parts[0])) {
				return new Module(m,Integer.parseInt(parts[1]));
			}
		}
		
		return new Module();
	}
	
	public static Module value(int index) {
		byte s = 0;
		for (Mod m:Mod.values()) {
			if (s + m.count <= index) {
				s += m.count;
			}
			else {
				return new Module(m,index-s);
			}
		}
		return new Module();
	}
	
	public Set<IModule> modules() {
		Set<IModule> m = new HashSet<IModule>();
		m.add(this);
		return m;
	}

	public static Set<Module> fromBits(BigInteger consensus) {
		Set<Module> ret = new HashSet<Module>();
		
		
		for (int i=0; i<consensus.bitLength(); i++) {
			if (consensus.testBit(i)) {
				ret.add(Module.value(i));
			}
		}
		return ret;
	}
	
	public String toString () {
		return mod.toString() + ((mod.getCount()!=1) ? "_" + number : "");
	}
	
	public Module swapGrouping (Grouping to) {
		return new Module(Mod.valueOf(to + "_" + mod.name().split("_")[1]));
		
	}
	
	public Grouping getGrouping () {
		return Grouping.valueOf(mod.name().split("_")[0]);
	}

	public static Module modAll() {
		return new Module(Mod.ALL);
	}

	@Override
	public byte getNr() {
		return number;
	}

	@Override
	public Mod getModule() {
		return mod;
	}
	
}

