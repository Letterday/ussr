package ussr.samples.atron.simulations.metaforma.lib;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;


import ussr.samples.atron.simulations.metaforma.lib.Packet.*;

public abstract class BagMetaCore extends Bag implements IMetaBag {
	public byte completed;
	public byte size;
	public byte regionID;
	public byte metaModulesInRegion = 1;
	private float timeInitRegion;
	
	private HashMap<String,Byte> seqNrs = new HashMap<String, Byte>();
	
	public byte getCountInRegion () {
		return metaModulesInRegion;
	}
	
	public void setCountInRegion(byte groupSize) {
		setVar("metaModulesInRegion",groupSize);
	}
	
	public IMetaBag setVar (String name, int val, int seqNr) {
		
		if (getVarSeqNr(name) == seqNr) {
			if (val != getVar(name)) {
				ctrl.visual.print("Conflict in " + name + " between " + getVar(name) + " and " + val + ", take highest");
				val = MfApi.max(val,getVar(name));
			}
		}
		
		if (seqNr >= getVarSeqNr(name)) {
			seqNrs.put(name,(byte)seqNr);
			// Make sure no byte overflow occurs
			byte value = MfApi.min(Byte.MAX_VALUE,val);
			
			
			if (getVar(name) != value) {
				super.setVar(name, value);
				ctrl.visual.print("setVar(" + name + "," + value + "," + seqNr + ")");
				ArrayList<String> l = new ArrayList<String>();
				l.add(name);
	//			ctrl.visual.print(ctrl.hashCode() +" - "+ ctrl.meta().hashCode() + name + ":::" + ctrl.meta().getVarSeqNr(name));
				broadcastVars(l);

				if (name.equals("regionID")) {
					ctrl.getStateMngr().cleanConsensus();
				}
			}
			
		}
		return this;
	}
	
	public IMetaBag setVar (String name, int val) {
		if (getVar(name) != val) {
			return setVar(name,val,getVarSeqNr(name)+1);
		}
		return this;
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
		
		if (ctrl.module().getMetaID() == 0) {
			ctrl.visual.print("ENABLE (EMPTY) META!");
			ctrl.module().setMetaID(1);
		}
		else {
			ctrl.visual.print("ENABLE META!");
		}
		setVar("completed",1);
	}
	
	public void disable () {
		ctrl.visual.print("DISABLE META!");
		setVar("completed",0);
		ctrl.module().setMetaID(0);
		// TODO: WHAT TO DO HERE?
	}
		
	public void releaseRegion () {
		ctrl.visual.print(".releaseRegion ");
		MfStats.getInst().addEnd(ctrl.stateMngr.getState().getOperation(),regionID(),ctrl.stateMngr.timeSpentInSequence());
		setRegionID((byte)0);		
		
		setCountInRegion((byte) 1);
	}
	
	public void resetVars () {
		for (Field field:this.getClass().getFields()) {
			setVar(field.getName(),0);
		}
	}
	
	public boolean regionTakesTooLong() {
		if (regionID() != 0 && regionID() == ctrl.module().metaID && ctrl.time() - timeInitRegion > ctrl.settings.getWithdrawTime()) {
			ctrl.visual.print("Region takes too long!!");
			return true;
		}
		return false;
	}

	public void createRegion(byte[] metaIDs) {
		ctrl.visual.print(".createRegion " + ctrl.module().metaID);
		
		setRegionID(ctrl.module().metaID);	
		
		timeInitRegion = ctrl.time();
		metaModulesInRegion = (byte) (metaIDs.length+1);
		
		for (byte metaID : metaIDs) {
			if (metaID == 0) {
				ctrl.visual.error("Creating region with meta ID 0!!");
			}
			// We need to include the group size in the message
			
		}
		
		
		PacketRegion p = new PacketRegion(ctrl);
		p.sizeMeta = metaModulesInRegion;
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
		if (ID == 0) {
			setCountInRegion((byte) 1);
		}
	}
	
	
	
	
	public byte getVarNr(String name) {
		byte i=0;
		try {
			for (Field f : getClass().getFields()) {
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
	
	
	public void broadcastVars () {
		if (ctrl.module().metaID != 0) {
			broadcastVars(getVars());
		}
	}
	
	public void broadcastVars (ArrayList<String> names) {
//		visual.print(".....broadcastMetaVars " + names);
		PacketMetaVarSync p = new PacketMetaVarSync(ctrl);
		p.setVarList(names);
		ctrl.broadcast(p);
	}
	
//	public abstract void neighborHook (Packet p);	
//	
//	public abstract void broadcastNeighbors();
	
}
