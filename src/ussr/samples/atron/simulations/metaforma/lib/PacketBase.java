package ussr.samples.atron.simulations.metaforma.lib;

public abstract class PacketBase {
	
	protected byte[] data = new byte[]{};
	protected static MetaformaController ctrl;
	
	public abstract byte[] getBytes ();
	
	public static void setController (MetaformaController c) {
		ctrl = c;
	}
	
	public static PacketBase fromBytes(byte[] msg) {
		if (((msg[0]&255)>>7)%2 == 0) {
			return new Packet(msg);
		}
		else {
			return new MetaPacket(msg);
		}
	}
}
