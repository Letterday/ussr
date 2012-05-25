package ussr.samples.atron.simulations.metaforma.lib;


public enum Type {ROTATE,CONNECT,DISCONNECT,STATE,DISCOVER,BROADCAST, DETOUR;
	public byte ord() {
		return (byte)ordinal();
	}
}