package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketDiscover;

public abstract class BagModuleCore extends Bag {
	public IRole role;
	public byte metaID;
	private Module id; 
	private Module idPrevious;
	
	public Module getID () {
		return id;
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
		ctrl.visual.print("$$$ rename " + id + " to " + idNew);
		ctrl.getModule().setProperty("name", idNew.toString());
		id = new Module(idNew);
		ctrl.visual.colorize();
		discover ();
	}
	
	public void renameGroup(IGroupEnum to) {
		ctrl.visual.print("rename group");
		setID(getID().swapGrouping(to));
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
	
	public abstract boolean atT();
	public abstract boolean atB();
	public abstract boolean atL();
	public abstract boolean atR();
	
}
