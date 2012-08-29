package ussr.samples.atron.simulations.metaforma.lib;



public enum PacketCoreType implements IPacketType {
		DISCOVER,
		GRADIENT,
		CONSENSUS,
		SYMMETRY,
		META_VAR_SYNC,
		META_ID_SET;
		
	public byte bit() {
		return (byte)Math.pow(2,ord());
	}
	
	public byte ord() {
		return (byte)ordinal();
	}

}