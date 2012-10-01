package ussr.samples.atron.simulations.metaforma.lib;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;


import ussr.samples.atron.simulations.metaforma.lib.Packet.*;

public abstract class BagMetaCore extends Bag implements IMetaBag {
	public byte completed;
	public byte regionID;
	private byte metaModulesInRegion = 1;
	public byte orientation = 0;
	
	private HashMap<String,Byte> seqNrs = new HashMap<String, Byte>();
	
	

	public byte getCountInRegion () {
		return metaModulesInRegion;
	}
	
	public void setCountInRegion(byte groupSize) {
		metaModulesInRegion = groupSize;
	}
	
	public IMetaBag setVar (String name, int val, int seqNr) {
		// Make sure no byte overflow occurs
		byte value = MfApi.min(Byte.MAX_VALUE,val);
		seqNrs.put(name,(byte)(MfApi.max(getVarSeqNr(name),seqNr)));
		
		if (getVar(name) != value) {
			super.setVar(name, value);
			ArrayList<String> l = new ArrayList<String>();
			l.add(name);
//			ctrl.visual.print(ctrl.hashCode() +" - "+ ctrl.meta().hashCode() + name + ":::" + ctrl.meta().getVarSeqNr(name));
			ctrl.broadcastMetaVars(l);
		}
		return this;
	}
	
	public IMetaBag setVar (String name, int val) {
		return setVar(name,val,getVarSeqNr(name)+1);
	}
	
	public String getVarByNr(byte nr) {
//		ctrl.visual.print("getVarByNr " + nr + " = " + this.getClass().getFields()[nr].getName());
		return this.getClass().getFields()[nr].getName();
	}
	
	public byte getVarSeqNr(String name) {
		if (!seqNrs.containsKey(name)) {
			seqNrs.put(name, (byte)0); 
		}
		return seqNrs.get(name);
	}
	
	public void enable () {
		ctrl.visual.print("ENABLE META!");
		setVar("completed",1);
	}
	
	public void disable () {
		ctrl.visual.print("DISABLE META!");
		setVar("completed",0);
		// TODO: WHAT TO DO HERE?
	}
		
	public void releaseRegion () {
		ctrl.visual.print(".releaseRegion ");
		MfStats.getInst().addEnd(ctrl.stateMngr.getState().getOperation(),regionID(),ctrl.stateMngr.timeSpentInSequence());
		setRegionID((byte)0);
		
		for (Field field:this.getClass().getFields()) {
			setVar(field.getName(),0);
		}
		
		
		setCountInRegion((byte) 1);
	}

	public void createRegion(byte[] metaIDs,byte orient) {
		ctrl.visual.print(".createRegion " + ctrl.module().metaID);
		
		regionID = ctrl.module().metaID;
		
		metaModulesInRegion = (byte) (metaIDs.length+1);
		
		for (byte metaID : metaIDs) {
			if (metaID == 0) {
				ctrl.visual.error("Creating region with meta ID 0!!");
			}
			// We need to include the group size in the message
			
		}
		
		
		PacketRegion p = new PacketRegion(ctrl);
		p.sizeMeta = metaModulesInRegion;
		p.orientation = orient;
		for (byte metaID : metaIDs) {
			if (ctrl.nbs().nbsWithMetaId(metaID).isEmpty()) {
				// One of the indirect neighbors can travel along with the packet
				p.indirectNb = metaID;
			}
		}
		for (byte metaID : metaIDs) {
			// Send to direct neighbors
			ctrl.unicast(p,ctrl.nbs().nbsWithMetaId(metaID).connectors());
		}
		
		
		
		
		
		
		ctrl.stateMngr.commit("createRegion");
	}

	public boolean isCompleted() {
		return completed == 1;
	}
	
	public void setRegionID(byte ID){
		setVar("regionID",ID);
	}
	
	
	
	
	public byte getVarNr(String name) {
		byte i=0;
		try {
			for (Field f :this.getClass().getFields()) {
				if(f.getName().equals(name)) {
					return i;
				}
				i++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		throw new Error("Var " + name + " not found!");
	}

	public ArrayList<String> getVars() {
		ArrayList<String> ret = new ArrayList<String>();
		try {
			for (Field f :this.getClass().getFields()) {
				if (!isOwnField(f)){
					ret.add(f.getName());
				}
			}		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public abstract void neighborHook (Packet p);
	
	public abstract void broadcastNeighbors();

	public String toString() {
		return super.toString() + "\n" + seqNrs;
	}

	@Override
	public byte regionID() {
		return regionID;
	}
	
	public byte completed() {
		return completed;
	}
	
}
