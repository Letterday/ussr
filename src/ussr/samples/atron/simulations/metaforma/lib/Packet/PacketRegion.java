package ussr.samples.atron.simulations.metaforma.lib.Packet;


import ussr.samples.atron.simulations.metaforma.lib.MfController;

public class PacketRegion extends Packet {
	public static byte getTypeNr() {return 2;}
	
	public byte indirectNb;
	public byte sizeMeta;
	public byte orientation;
	
	public PacketRegion(MfController c) {
		super(c);
		setType(getTypeNr());
	}
	
	public byte[] serializePayload () {
		return new byte[] {indirectNb,sizeMeta,orientation};
	}
	
	public PacketRegion deserializePayload (byte[] b) {
		indirectNb = b[0];
		sizeMeta = b[1];
		orientation = b[2];
		return this;
	}
	
	@Override
	public String toStringPayload() {
		return "[" + indirectNb + "," + sizeMeta + "," + orientation + "]";
	}

	
}
