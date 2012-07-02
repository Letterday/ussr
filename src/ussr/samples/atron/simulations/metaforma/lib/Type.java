package ussr.samples.atron.simulations.metaforma.lib;


public enum Type {
		STATE_INSTR_UPDATE,
		STATE_OPERATION_NEW, 
		DISCOVER,
		GRADIENT,
		GLOBAL_VAR, 
		GRADIENT_RESET,
		CONSENSUS,
		FIX_SYMMETRY;
		
	public byte bit() {
		return (byte)Math.pow(2,(byte)ordinal());
	}
	
	public byte ord() {
		return (byte)ordinal();
	}
	
	public boolean shouldBroadcast() {
		//return equals(STATE_INSTR_INCR) || equals(STATE_INSTR_RESET) || equals(STATE_OPERATION_NEW)  || equals(STATE_PENDING_INCR);
		return !equals(DISCOVER);
	}
}