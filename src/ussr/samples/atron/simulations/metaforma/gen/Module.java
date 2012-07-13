package ussr.samples.atron.simulations.metaforma.gen;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import ussr.samples.atron.simulations.metaforma.lib.IModuleHolder;

public enum Module implements IModuleHolder {ALL,Walker_Head,Walker_Left,Walker_Right,Floor_0,Floor_1,Floor_2,Floor_3,Floor_4,Floor_5,Floor_6,Floor_7,Floor_8,Floor_9,Floor_10,Floor_11,Floor_12,Floor_13,Floor_14,Floor_15,Floor_16,Floor_17,Floor_18,Floor_19,Floor_20,Floor_21,Floor_22,Floor_23,Floor_24,Floor_25, Floor_26,Floor_27,Floor_28,Floor_29,Floor_30,Floor_31,Floor_32, Floor_33,Floor_34,Floor_35,Floor_36,Walker_Left2,Clover_North,Clover_South,Clover_West,Clover_East, Floor_Uplifter,Struct_0,Struct_1,Struct_2,Struct_3,Struct_4,Struct_5,Struct_6,Struct_7,Struct_8,Struct_9,Struct_10,Struct_11,Struct_12,Struct_13,Struct_14,Struct_15,Struct_16,Struct_17,Struct_18,Struct_19,Struct_20,Struct_21,Struct_22,Struct_23,Struct_24,Struct_25, Floor_Downlifter, None, Floor_UplifterTop, Floor_Top, Floor_Bottom, Floor_UplifterBottom,Left_0,Left_1,Left_2,Left_3,Left_4,Left_5,Left_6,Left_7,Left_8,Left_9,Left_10,Left_11,Left_12,Left_13,Left_14,Left_15,Left_16,Left_17,Left_18
,Right_0,Right_1,Right_2,Right_3,Right_4,Right_5,Right_6,Right_7,Right_8,Right_9,Right_10,Right_11,Right_12,Right_13,Right_14,Right_15,Right_16,Right_17,Right_18, Left_Bottom, Right_Top, Left_Top, Right_Bottom,Uplifter_Left,Uplifter_Right;

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
	
	
	public Module swapGrouping (Grouping to) {
		return valueOf(to + "_" + name().split("_")[1]);
		
	}

	@Override
	public boolean contains(Module m) {
		return equals(m);
	}
	
	public Set<Module> modules() {
		Set<Module> m = new HashSet<Module>();
		m.add(this);
		return m;
	}

	public static Set<Module> fromBits(BigInteger consensus) {
		Set<Module> ret = new HashSet<Module>();
		for (int i=0; i<Module.values().length; i++) {
			if (consensus.testBit(i)) {
				ret.add(Module.values()[i]);
			}
		}
		return ret;
	}
}