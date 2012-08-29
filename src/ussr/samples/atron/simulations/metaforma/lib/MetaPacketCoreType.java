package ussr.samples.atron.simulations.metaforma.lib;

public enum MetaPacketCoreType implements IPacketType {
	SET_BOSS,
	ADD_NEIGHBOR;
	
	public byte bit() {
		return (byte)Math.pow(2,ord());
	}
	
	public byte ord() {
		return (byte)ordinal();
	}

}