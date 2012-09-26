package ussr.samples.atron.simulations.metaforma.lib.Packet;

import java.lang.reflect.Field;

import ussr.samples.atron.simulations.metaforma.lib.MfController;

public abstract class PacketBase {
	
	protected MfController ctrl;
	public byte type;
	
	
	public PacketBase  (MfController c) {
		ctrl = c;
//		return this;
	}
	
	public byte getType () {
		return type;
	}
	
	
	
		
	public PacketBase setVar(String name, Object value) {
		try {
			Field field = this.getClass().getField(name);
			field.setAccessible(true);
			field.set(this, value);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public Object getVar(String name) {
		try {
			Field field = this.getClass().getField(name);
			field.setAccessible(true);
			return field.get(this);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}
