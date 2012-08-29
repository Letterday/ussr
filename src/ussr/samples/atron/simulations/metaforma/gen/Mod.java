package ussr.samples.atron.simulations.metaforma.gen;

import java.util.HashSet;
import java.util.Set;

import ussr.samples.atron.simulations.metaforma.lib.IModule;
import ussr.samples.atron.simulations.metaforma.lib.IModuleHolder;


public enum Mod  implements IModuleHolder,IModule{
	ALL,
	NONE,xx,
	Floor(100),
	Walker_Head, Walker_Left, Walker_Right,
	Clover_North, Clover_South, Clover_West, Clover_East, 
	Left(5),
	Right(5), 
	Uplifter_Left, Uplifter_Right, Uplifter_Top, Uplifter_Bottom;

	byte count;
	
	private Mod () {
		count = 1;
	}
	
	private Mod (int c) {
		count = (byte) c;
	}
	
	public byte getCount() {
		return count;
	}

	public boolean contains(IModule m) {
		return m.equals(this);
	}	
	
	public Set<IModule> modules() {
		Set<IModule> m = new HashSet<IModule>();
		m.add(this);
		return m;
	}

	public boolean equals(IModule m) {
		return this.ordinal() == m.getModule().ordinal();
	}

	@Override
	public Mod getModule() {
		return this;
	}

	@Override
	public byte getNr() {
		return 0;
	}

	@Override
	public Grouping getGrouping () {
		return Grouping.valueOf(name().split("_")[0]);
	}

	@Override
	public byte ord() {
		byte ret = 0;
		for (Mod m:values()) {
			if (m.ordinal() != ordinal()) {
				ret+=m.count;
			}
			else {
				return ret;
			}
		}
		throw new Error ("Enum not found!");
	}

	
}