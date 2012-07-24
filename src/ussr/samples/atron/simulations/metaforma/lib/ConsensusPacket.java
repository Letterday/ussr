//package ussr.samples.atron.simulations.metaforma.lib;
//
//import java.util.BitSet;
//
//
//public class ConsensusPacket extends Packet {
//	
//	public ConsensusPacket(Packet p) {
//		super(p);
//	}
//
//	private static final long serialVersionUID = 1L;
//	
//	Type type = Type.CONSENSUS;
//	
//		
//	public BitSet getBits() {
//		BitSet bits = new BitSet();
//	    for (int i=0; i<data.length*8; i++) {
//	        if ((data[data.length-i/8-1]&(1<<(i%8))) > 0) {
//	            bits.set(i);
//	        }
//	    }
//	    return bits;
//	}
//	
//	public ConsensusPacket setBits(BitSet bs) {
//		for (int i=0; i<bs.length(); i++) {
//	        if (bs.get(i)) {
//	            data[data.length-i/8-1] |= 1<<(i%8);
//	        }
//	    }
//		return this;
//	}
//
//	
//
//	
//	
//}
