package ussr.samples.atron.simulations.metaforma.lib;


public enum Type {
		STATE_INSTR_INCR,
		STATE_OPERATION_NEW, 
		STATE_PENDING_INCR,
		DISCOVER,
		CONNECTOR_NR,
		GRADIENT,
		GLOBAL_VAR, 
		GRADIENT_RESET;
		
	public byte ord() {
		return (byte)ordinal();
	}
	
	public boolean shouldBroadcast() {
		//return equals(STATE_INSTR_INCR) || equals(STATE_INSTR_RESET) || equals(STATE_OPERATION_NEW)  || equals(STATE_PENDING_INCR);
		return !equals(DISCOVER);
	}
}