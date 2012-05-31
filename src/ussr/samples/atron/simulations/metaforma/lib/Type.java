package ussr.samples.atron.simulations.metaforma.lib;


public enum Type {STATE_INSTR_INCR,STATE_INSTR_RESET,STATE_OPERATION_NEW, STATE_PENDING_INCR,DISCOVER,CONNECTOR_NR;
	public byte ord() {
		return (byte)ordinal();
	}
	
	public boolean shouldBroadcast() {
		//return equals(STATE_INSTR_INCR) || equals(STATE_INSTR_RESET) || equals(STATE_OPERATION_NEW)  || equals(STATE_PENDING_INCR);
		return !equals(DISCOVER);
	}
}