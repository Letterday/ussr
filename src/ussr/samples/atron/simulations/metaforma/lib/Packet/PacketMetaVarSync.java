package ussr.samples.atron.simulations.metaforma.lib.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import ussr.samples.atron.simulations.metaforma.lib.MfController;
import ussr.util.Pair;



public class PacketMetaVarSync extends Packet {
	public static byte getTypeNr() {return 1;}
	
		
	public HashMap<String,Pair<Byte,Byte>> vars = new HashMap<String,Pair<Byte,Byte>>();
	
	public PacketMetaVarSync(MfController c) {
		super(c);
		setType(getTypeNr());
	}
	 
	public byte[] serializePayload () {
		byte[] ret = new byte[vars.size() * 3];
		int i = 0;
		for (Entry<String,Pair<Byte,Byte>> e:vars.entrySet()) {
			ret[i] = ctrl.meta().getVarNr(e.getKey());
			ret[i+1] = e.getValue().fst();
			ret[i+2] = e.getValue().snd();
			i=i+3;
		}
		
		return ret;
	}
	
	public PacketMetaVarSync deserializePayload (byte[] p) {
		for (int i = 0; i< p.length; i=i+3) {
			vars.put(ctrl.meta().getVarByNr(p[i+0]), new Pair<Byte,Byte>(p[i+1],p[i+2]));
		}
		return this;
	}
	
	public String toStringPayload () {
		String ret = "";
		for (Entry<String,Pair<Byte,Byte>> e:vars.entrySet()) {
			ret+= e.getKey() + ": " + e.getValue().fst() + " (" + e.getValue().snd() + "), ";
		}
		return ret;
	}
	            

	public void setVarList(ArrayList<String> names) {
//		ctrl.getVisual().print("setVarList");
//		System.err.println(ctrl.getId() + "setVarList");
		for (String name: names) {
			vars.put(name, new Pair<Byte,Byte>(ctrl.meta().getVar(name),ctrl.meta().getVarSeqNr(name)));
		}
	}

	
	
}
