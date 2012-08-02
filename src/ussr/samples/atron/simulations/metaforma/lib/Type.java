package ussr.samples.atron.simulations.metaforma.lib;


public enum Type {
		STATE_OPERATION_NEW, 
		DISCOVER,
		GRADIENT,
		CONSENSUS,
		SYMMETRY, META_ID;
		
	public byte bit() {
		return (byte)Math.pow(2,(byte)ordinal());
	}
	
	public byte ord() {
		return (byte)ordinal();
	}

}