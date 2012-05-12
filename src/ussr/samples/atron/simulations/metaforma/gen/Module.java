package ussr.samples.atron.simulations.metaforma.gen;

public enum Module {ALL,Walker_Head,Walker_Left,Walker_Right,Floor_0,Floor_1,Floor_2,Floor_3,Floor_4,Floor_5,Floor_6,Floor_7,Floor_8,Floor_9;
public Grouping getGrouping () {
		return Grouping.valueOf(name().split("_")[0]);
	}
	
	public static Module getOnNumber (Grouping s, int i) {
		return valueOf(s.toString() + "_" + i);
	}
	
	public byte ord() {
		return (byte)ordinal();
	}
	
	public boolean belongsTo (Grouping s) {
		return name().startsWith(s.name());
	}
}