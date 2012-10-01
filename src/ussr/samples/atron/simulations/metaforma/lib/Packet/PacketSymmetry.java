package ussr.samples.atron.simulations.metaforma.lib.Packet;

import ussr.samples.atron.simulations.metaforma.lib.MfController;

public class PacketSymmetry extends Packet {
	public static byte getTypeNr() {return 4;}
	
	public PacketSymmetry(MfController c) {
		super(c);
		setType(getTypeNr());
	}
	
	@Override
	public String toStringPayload() {
		return "";
	}

	@Override
	public Packet deserializePayload(byte[] payload) {
		return this;
	}

	@Override
	public byte[] serializePayload() {
		return new byte[0];
	}
}
