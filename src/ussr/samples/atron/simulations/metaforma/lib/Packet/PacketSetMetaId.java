package ussr.samples.atron.simulations.metaforma.lib.Packet;

import ussr.samples.atron.simulations.metaforma.lib.MfController;

public class PacketSetMetaId extends Packet {
	public static byte getTypeNr() {return 3;}
	
	public byte newMetaID = 0;
	
	public PacketSetMetaId(MfController c) {
		super(c);
		setType(getTypeNr());
	}
	
	public PacketSetMetaId deserializePayload (byte[] b) {
		newMetaID = b[0];
		return this;
	}
	
	public String toStringPayload () {
		return "[" + newMetaID + "]";
	}
	
	public byte[] serializePayload () {
		return new byte[]{newMetaID};
	}
	
	
}
