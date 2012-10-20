package ussr.samples.atron.simulations.metaforma.lib;


public interface IModEnum {
	int ordinal();
	IModEnum getNone();
	int ord();
	
	String name();
	
	IModEnum valueFrom(String string);
	
	byte getCount();
	
	IModEnum[] getValues();


}