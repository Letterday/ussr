package ussr.samples.atron.simulations.metaforma.lib.Packet;

import ussr.samples.atron.simulations.metaforma.lib.MfController;

public class PacketSymmetry extends Packet {
	public static byte getTypeNr() {return 4;}
	
	public PacketSymmetry(MfController c) {
		super(c);
		setType(getTypeNr());
	}
}
