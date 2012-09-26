package ussr.samples.atron.simulations.metaforma.lib.Packet;

import ussr.samples.atron.simulations.metaforma.lib.MfController;

public class PacketDiscover extends Packet {
	public static byte getTypeNr() {return 0;}
		
	public PacketDiscover(MfController c) {
		super(c);
		setType(getTypeNr());
	}
}
