package ussr.samples.atron.simulations.metaforma.lib.Packet;


import ussr.samples.atron.simulations.metaforma.lib.MfController;

public class PacketRegion extends Packet {
	public static byte getTypeNr() {return 2;}
	
	public byte[] metaIDs;
	
	public PacketRegion(MfController c) {
		super(c);
		setType(getTypeNr());
	}
	
	public byte[] serializePayload () {
		return metaIDs;
	}
	
	public PacketRegion deserializePayload (byte[] b) {
		metaIDs = b;
		return this;
	}
}
