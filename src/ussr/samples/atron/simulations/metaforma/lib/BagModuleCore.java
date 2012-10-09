package ussr.samples.atron.simulations.metaforma.lib;

import java.math.BigInteger;

import ussr.model.Sensor;
import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketConsensus;
import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketDiscover;

public abstract class BagModuleCore extends Bag {
	public IRole role;
	public byte metaID;
	private Module idPrevious;
	
	public Module getID () {
		return ctrl.getID();
	}

	public void setRole(IRole r) {
		role = r;
	}
	
	public IRole getRole() {
		return role;
	}
	
	
	public IGroupEnum getGroup() {
//		ctrl.visual.print(".getGroup() = " + ctrl.getID().getGroup());
		return ctrl.getID().getGroup();
	}
	
	public void setID(IModule idNew) {
		if (!ctrl.getID().equals(idNew)) {
			ctrl.visual.print("$$$ rename " + ctrl.getID() + " to " + idNew);
			ctrl.getModule().setProperty("name", idNew.toString());
			ctrl.visual.colorize();
			discover ();
		}
	}
	
	public void swapGroup(IGroupEnum to) {
		ctrl.visual.print("swap group from " + getGroup() + " to " + to + " result:" + getID().swapGroup(to));
		setID(getID().swapGroup(to));
	}
	
	public void storeID () {
		idPrevious = getID();
	}
	
	public void restoreID () {
		ctrl.visual.print("$$$ restore name " + getID() + " to " + idPrevious);
		setID(idPrevious);
	}
	
	public void discover () {
		ctrl.broadcast(new PacketDiscover(ctrl).setSource(getID()));
	}

	public void setMetaID(int i) {
		setVar("metaID", (byte)i);
	}
	
	public byte getMetaID() {
		return getVar("metaID");
	}
	
	public void broadcastConsensus() {
//		visual.print(".broadcastConsensus()");
		if (!ctrl.stateMngr.getConsensus().equals(BigInteger.ZERO) && metaID != 0) {
			ctrl.debugForceMetaId();
			ctrl.broadcast((PacketConsensus)new PacketConsensus(ctrl).setVar("consensus", ctrl.stateMngr.getConsensus()));
		}
	}
	
	public float proximitySensor() {
		float ret = 0;
		for(Sensor s: ctrl.getModule().getSensors()) {
            if(s.getName().startsWith("Proximity")) {
                float v = s.readValue();
                ret = Math.max(v,ret);
            }
		}
		return ret;
	}
	
	public boolean atT(){return false;}
	public boolean atB(){return false;}
	public boolean atL(){return false;}
	public boolean atR(){return false;}

	
	
}