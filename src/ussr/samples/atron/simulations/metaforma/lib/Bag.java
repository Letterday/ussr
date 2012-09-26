package ussr.samples.atron.simulations.metaforma.lib;

import java.lang.reflect.Field;

public class Bag implements IBag {
	protected MfController ctrl;

	public IBag setVar(String name, Object value) {
		try {
			Field field = this.getClass().getField(name);
			field.setAccessible(true);
			if (!value.equals(getVar(name))) {
				field.set(this, value);
				ctrl.visual.print("setVar(" + name + "," + value + ")");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public final byte getVar(String name) {
		try {			
			Field field = this.getClass().getField(name);
			field.setAccessible(true);
			byte ret = ((Byte)field.getByte(this)).byteValue();
//			System.out.println(ctrl.getId() + ": " +ctrl.meta().getClass().getSimpleName() + ".getVar(" + name + ") = " + ret);
			return ret;
		}
		catch (Exception e) {
			System.err.println("Error at " + this.getClass());
			e.printStackTrace();
		}
		return 0;
	}
	
	public void setController (MfController c) {
		ctrl = c;
	}
	
	public boolean isOwnField (Field f) {
		return f.getName().startsWith("_");
	}
	
	public String toString () {
		String ret = "";
		try {
			for (Field f:this.getClass().getFields()) {
				if (!isOwnField(f)) {
					f.setAccessible(true);
					ret += f.getName() + ":" + f.get(this) + "  ";
				}
			}
		}
		catch (Exception e) {
			System.err.println("Error at " + this.getClass());
			e.printStackTrace();
		}
		return ret;
	}
	
}
