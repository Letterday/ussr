package ussr.samples.atron.simulations.metaforma.lib;


public enum Type {
		STATE_OPERATION_NEW, 
		DISCOVER,
		GRADIENT,
		CONSENSUS,
		SYMMETRY,
		META_VAR_SYNC,
		META_ID;
		
	public byte bit() {
		return (byte)Math.pow(2,ord());
	}
	
	public byte ord() {
		return (byte)ordinal();
	}

}