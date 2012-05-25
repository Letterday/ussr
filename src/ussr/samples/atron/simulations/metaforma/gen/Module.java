package ussr.samples.atron.simulations.metaforma.gen;

public enum Module {ALL,Walker_Head,Walker_Left,Walker_Right,Floor_0,Floor_1,Floor_2,Floor_3,Floor_4,Floor_5,Floor_6,Floor_7,Floor_8,Floor_9,Floor_10,Floor_11,Floor_12,Floor_13,Floor_14,Floor_15,Floor_16,Floor_17,Floor_18,Floor_19,Floor_20,Floor_21,Floor_22,Floor_23,Floor_24,Floor_25, Walker_Left2,Clover_North,Clover_South,Clover_West,Clover_East;

	public Grouping getGrouping () {
		return Grouping.valueOf(name().split("_")[0]);
	}
	
	public static Module getOnNumber (int g, int i) {
		return getOnNumber (Grouping.values()[g],i);
	}
	
	public static Module getOnNumber (Grouping s, int i) {
		String group = s.toString();
		int j = 0;
		while (!values()[j].toString().startsWith(group + "_")) {
			j++;
		}
		//System.out.println(".getOnNumber " + s.toString() + " " + i + " = " + j);
		return values()[i + j];
	}
	
	public byte ord() {
		return (byte)ordinal();
	}
	
	public boolean belongsTo (Grouping s) {
		return name().startsWith(s.name());
	}

	public int getNumber() {
		int j = 0;
		while (!values()[j].toString().startsWith(getGrouping().toString() + "_")) {
			j++;
		}
		return ordinal() - j;
	}
}