package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketDiscover;

public class BagModuleCore extends Bag {
	public IRole role;
//	public byte ID;
	public byte metaID;
	private IModule id; 
	
	
	public IModule getId () {
		return id;
	}

	public void setRole(IRole r) {
		role = r;
	}
	
	public IRole getRole() {
		return role;
	}
	
	public void setId(IModule idNew) {
		ctrl.visual.print("$$$ rename " + id + " to " + idNew);
		ctrl.getModule().setProperty("name", idNew.toString());
		ctrl.visual.colorize();
		id = idNew;
	}
	
	public void discover () {
		ctrl.broadcast(new PacketDiscover(ctrl).setSource(getId()));
	}
}
