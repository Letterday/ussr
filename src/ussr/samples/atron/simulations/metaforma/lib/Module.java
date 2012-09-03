package ussr.samples.atron.simulations.metaforma.lib;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class Module implements IModuleHolder,IModule {

	public static IModEnum Mod;
	public static IGroupEnum Group;
	public IModEnum mod;
	public byte number;
	
	
	@Override
	public int hashCode () {
		if (number == 0) {
			// To make it equal to ModEnum
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
//		System.out.println(number + "==" + ((IModule)m).getNr());
		return mod.equals(((IModule)m).getMod()) && number == ((IModule)m).getNr();		
	}
	
	public Module () {
		mod = Mod.getNone();
	}
	
	public Module (IModEnum m) {
		this(m,0);
	}

	public Module (IModEnum m, int nr) {
		mod = m;
		number = (byte) nr;
	}
	
	public byte ord() {
		return (byte) (mod.ord() + number);
	}
		 
	
	
	public Set<IModule> modules() {
		Set<IModule> m = new HashSet<IModule>();
		m.add(this);
		return m;
	}

	
	public static Module value(String name) {
		String parts[] = name.split("_");
		if (parts.length != 2) {
			throw new IllegalArgumentException(name);
		}
		for (IModEnum m : Mod.getValues()) {
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
		for (IModEnum m:Mod.getValues()) {
			if (s + m.getCount() <= index) {
				s += m.getCount();
			}
			else {
				return new Module(m,index-s);
			}
		}
		return new Module();
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
	
	public Module swapGrouping (IGroupEnum to) {
		return new Module(Mod.valueFrom(to.name() + "_" + mod.name().split("_")[1]));
		
	}
	
	public IGroupEnum getGrouping () {
		return Group.valueFrom(mod.name().split("_")[0]);
	}


	@Override
	public byte getNr() {
		return number;
	}

	@Override
	public IModEnum getMod() {
		return mod;
	}
	
}

