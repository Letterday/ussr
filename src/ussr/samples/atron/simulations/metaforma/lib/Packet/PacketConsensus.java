package ussr.samples.atron.simulations.metaforma.lib.Packet;

import java.math.BigInteger;

import ussr.samples.atron.simulations.metaforma.lib.MfController;
import ussr.samples.atron.simulations.metaforma.lib.Module;

public class PacketConsensus extends Packet {
	public static byte getTypeNr() {return 5;}
	
	public BigInteger consensus = BigInteger.ZERO;
	
	public PacketConsensus(MfController c) {
		super(c);
		setType(getTypeNr());
	}
	 
	public byte[] serializePayload () {
		return consensus.toByteArray();
	}
	
	public PacketConsensus deserializePayload (byte[] b) {
		consensus = new BigInteger(b);
		return this;
	}
	
	public String toStringPayload () {
		return consensus.bitCount() + " - " + Module.fromBits(consensus);
	}
	
	
}
