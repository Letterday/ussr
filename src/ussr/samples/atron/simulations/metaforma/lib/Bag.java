package ussr.samples.atron.simulations.metaforma.lib;

import java.lang.reflect.Field;

public class Bag implements IBag {
	protected MfController ctrl;

	public IBag setVar(String name, Object value) {
		try {
			Field field = this.getClass().getField(name);
			field.setAccessible(true);
//			if (!value.equals(getVar(name))) {
				field.set(this, value);
				
//			}
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
		catch (NoSuchFieldException e) {
			System.out.println(e.getMessage());
		}
		catch (Exception e) {
			ctrl.visual.error("Error at " + this.getClass());
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
				if (!isOwnField(f) && !f.getName().contains("Left") && !f.getName().contains("Right") && !f.getName().contains("Top") && !f.getName().contains("Bottom")) {
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
