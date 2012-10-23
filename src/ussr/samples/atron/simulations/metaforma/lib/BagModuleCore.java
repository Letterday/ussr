package ussr.samples.atron.simulations.metaforma.lib;

import java.math.BigInteger;

import ussr.model.Sensor;
import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketConsensus;
import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketDiscover;
import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketSymmetry;

public abstract class BagModuleCore extends Bag {
	public IMetaPart part;
	public byte metaID;
	public byte continueWalk;
	private Module idPrevious;
	
	public Module getID () {
		return ctrl.getID();
	}

	public void setPart(IMetaPart p) {
		ctrl.visual.print(".setPart " + p);
		part = p;
	}
	
	public IMetaPart getPart() {
		return part;
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
		ctrl.visual.print("$$$ store name " + getID());
		idPrevious = getID();
	}
	
	public void restoreID () {
		if (idPrevious != null) {
			ctrl.visual.print("$$$ restore name " + getID() + " to " + idPrevious);
			setID(idPrevious);
		}
		else {
			ctrl.visual.print("$$$ no name to restore");
		}
	}
	
	public void discover () {
		ctrl.broadcast(new PacketDiscover(ctrl).setSource(getID()));
	}

	public void setMetaID(int i) {
		ctrl.visual.print("setMetaID " + i);
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
	
	public boolean at(BorderLine d){return false;}
	
	public void fixSymmetry(byte connSource, byte connDest) {		
		ctrl.visual.print("orig: " + connSource + " " + MfController.isNORTH(connSource) + " == " + MfController.isWEST(connDest) + " " + connDest);
		if (MfController.isFEMALE(connDest)) {
			if (!ctrl.stateMngr.committed()) {
				if (MfController.isWEST(connSource) != MfController.isSOUTH(connDest)) {
					ctrl.context.switchNorthSouth();
					connDest = (byte) ((connDest + 4) % 8);
				}
				ctrl.visual.print(connSource + " " + MfController.isNORTH(connSource) + " == " + MfController.isWEST(connDest) + " " + connDest);
				ctrl.context.switchEastWestHemisphere(MfController.isNORTH(connSource) == MfController.isWEST(connDest), MfController.isSOUTH(connDest));
					
				
			}				
		}
		else if (MfController.isMALE(connDest)) {
			if (!ctrl.stateMngr.committed()) {
				if (MfController.isWEST(connSource) != MfController.isNORTH(connDest)) {
					ctrl.context.switchNorthSouth();
					connDest = (byte) ((connDest + 4) % 8);
				}
			
				// sure!!
				ctrl.context.switchEastWestHemisphere(MfController.isNORTH(connSource) == MfController.isEAST(connDest), MfController.isSOUTH(connDest));
				
				
			}
		}
		if (ctrl.freqLimit("SYMM passthrough", ctrl.settings.getPropagationRate())) {
			discover();
			ctrl.broadcast(new PacketSymmetry(ctrl));
		}
		ctrl.stateMngr.commit("Symmetry fix done");
		
	}
	
	
	
}