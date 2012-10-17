//package ussr.samples.atron.simulations.metaforma.lib;
//
//import java.util.Map;
//
//import ussr.samples.atron.simulations.metaforma.gen.BrandtController;
//import ussr.samples.atron.simulations.metaforma.lib.Packet.Packet;
//import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketMetaVarSync;
//import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketSetMetaId;
//import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketSymmetry;
//import ussr.util.Pair;
//
//public abstract class MfMetaController extends MfController {
//
//	public void activate () {
//		super.activate();
//		scheduler.enable("meta.broadcastNeighbors");
//		scheduler.enable("meta.broadcastVars");	
//	}
//	
//public abstract boolean receivePacket (PacketSetMetaId p);
//	
//	public boolean receivePacket (PacketMetaVarSync p) {
//		for (Map.Entry<String,Pair<Byte,Byte>> e:p.vars.entrySet()) {
//			String var = e.getKey();
//
//			meta().setVar(var, e.getValue().fst(),e.getValue().snd());
//			
//			
//			//TODO: BrandtController.StateOperation.CHOOSE needs to be converted to independent state
//			if (var.equals("regionID")) {
//				// The new region ID is not allowed to be zero!
//				if (stateMngr.at(BrandtController.StateOperation.CHOOSE) && e.getValue().fst() != 0) {
//					getStateMngr().commit("BOSS ID received through meta sync");
////					visual.print("COMMMMIIITT");
//				}
//				else {
////					visual.print("no consensus commit because in faulty state....");
//				}
//			}
//			
//		}
//		
//		return true;
//	}
//	
//
//	public void debugForceMetaId() {
//		if (module().metaID == 0) {
//			try {
//				throw new Error("Consensus may only be used when having meta ID!");
//			}
//			catch (Error e) {
//				e.printStackTrace();
//			}
//		}
//	}
//	
//
//}
