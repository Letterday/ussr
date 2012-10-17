package ussr.samples.atron.simulations.metaforma.lib;

public interface IModSeparator {
	public int ord();

	public IModEnum[] getValues();
	public IModEnum valueFrom(String string);
}
