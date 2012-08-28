package ussr.samples.atron.simulations.metaforma.lib;

import java.util.HashSet;
import java.util.Set;





public abstract class MetaformaRuntime extends MetaformaController {

	public final static byte NORTH = (byte) (pow2(0) + pow2(1) + pow2(2) + pow2(3));
	public final static byte SOUTH = (byte) (pow2(4) + pow2(5) + pow2(6) + pow2(7));
	public final static byte WEST = (byte) (pow2(0) + pow2(1) + pow2(4) + pow2(5));
	public final static byte EAST = (byte) (pow2(2) + pow2(3) + pow2(6) + pow2(7));
	public final static byte MALE = (byte) (pow2(0) + pow2(2) + pow2(4) + pow2(6));
	public final static byte FEMALE = (byte) (pow2(1) + pow2(3) + pow2(5) + pow2(7));
	
	protected final static boolean REQ = false;
	protected final static boolean ACK = true;
	protected boolean commitAutoAfterState = true;
	
	public MetaformaRuntime() {
		super();
	}
	
	public void commitMyselfIfNotUsed () {
		if (commitAutoAfterState) {
			commit("AUTO commit at the end!");
		}
	}
	
	public void commitNotAutomatic (IModuleHolder g) {
		if (g.contains(getId()) && commitAutoAfterState) {
			visual.print("Disable auto commit");
			commitAutoAfterState = false;
		}
	}
	
	public void commitNotAutomatic (IModuleHolder g,IModuleHolder g2) {
		if (g.contains(getId()) && !nbs().nbsIn(g2).isEmpty() || g2.contains(getId()) && !nbs().nbsIn(g).isEmpty()) {
			visual.print("Disable auto commit");
			commitAutoAfterState = false;
		}
	}
	
	@Override
	public void stateInstrInitNew () {
		visual.print("Clean autocommit");
		commitAutoAfterState = true;
		super.stateInstrInitNew();
	}
	
	public void rotate(int degrees) {
		visual.print("## rotate " + degrees + ", current = " + angle + "; new = " + (degrees + angle) + "");
		// TODO: There is still a strange issue: when rotating 180 degrees, it is undefined whether it goes CW or CCW (randomly).
		if (Math.abs(degrees) < 180) {
			angle = (360 + angle + degrees) % 360;
			rotateAbs(angle);
		}
		else {
			// Therefore we rotate in 2 steps, such that the direction is no longer undefined, but can be chosen by a pos/neg degree
			angle = (360 + angle + (degrees /2)) %360;
			rotateAbs(angle);
			angle = (360 + angle + (degrees /2)) %360;
			rotateAbs(angle);
		}
		commit("Rotating done to " + degrees);
	}

	
	private void rotateAbs (int degrees) {
		rotateToDegreeInDegrees(degrees);
		while (isRotating()) {
			yield();
		}
	}
	
	
	public void rotate(IModuleHolder g, int degrees) {
		commitNotAutomatic(g);
		if (g.contains(getId()) ) {
			rotate(degrees);
		}
	}


	public void connect(IModuleHolder g1, IModuleHolder g2) {
		discoverNeighbors();
		visual.print("##connect " + g1 + "," + g2);
		connection(g1, g2, true);
		connection(g2, g1, true);
	}
	
	public void disconnect(IModuleHolder g1, IModuleHolder g2) {
		discoverNeighbors();
		visual.print("## disconnect " + g1 + "," + g2);
		connection(g1, g2, false);
		connection(g2, g1, false);
		
	}
	 
	private void connection(IModuleHolder g1, IModuleHolder g2, boolean connect) {
		commitNotAutomatic(g1,g2);
	
		if (g1.contains(getId())) {
			for (IModule nb: nbs(MALE).nbsInMetaGoup().nbsIn(g2).nbsIsConnected(!connect).modules()) {
				visual.print("##connection " + g1 + "," + g2 + "  " + connect);
				connection(nb,connect);
			}
			
			if (nbs(MALE).nbsInMetaGoup().nbsIn(g2).nbsIsConnected(!connect).isEmpty()) {
				// Commit 
				commit("Member of " + g2 + " and did my action to " + g1);
			}
			if (nbs().nbsInMetaGoup().nbsIn(g2).isEmpty()) {
				// We need to to this because the whole grouping is excluded from automatic commit, also non-nb's!
				commit("Member of " + g2 + " but not connected to " + g1);
			}
		}
		
	}
	
	private void connection(IModule dest, boolean makeConnection) {
		if (!committed()) {
			byte conToNb = nbs().getConnectorNrTo(dest);
			byte conFromNb = nbs().getConnectorNrFrom(dest);
			String action = makeConnection ? " connect to " : " disconnect from ";
			visual.print("# " + getId() + action + dest);
			if (conToNb % 2 == 0 && conFromNb % 2 == 1) {
				connection(conToNb,makeConnection);
			}
			else {
				throw new Error("Wrong invocation, " + dest + " is connected with female connector so cant do anything: " + conToNb);
			}
			
		}
	}
	
	
	protected void connectionPart (IModuleHolder g, int part, boolean connect) {
		visual.print("# " + g + " disconnectPart " + part);
		commitNotAutomatic(g);
			
//		for (int i=0; i<8; i++) {
//			if ((part&pow2(i))==pow2(i)) {
//				if (g.contains(getId())) {
//					if (context.isConnConnected(i) == !connect && isMale(i)) {
//						connection(i,connect);
//					}
//				}
//				for (IModule m: nbs(MALE).nbsIn(g).nbsIsConnected(!connect).nbsUsingConnector(i).modules()) {
//					visual.print("# " + getId() + " has connector " + i + " matches");
//					connection(m,connect);
//				}
////				visual.print(nbs(FEMALE).toString());
////				visual.print(nbs(FEMALE).nbsIn(g).toString());
////				visual.print(nbs(FEMALE).nbsIn(g).nbsIsConnected(connect).toString());
////				visual.print(nbs(FEMALE).nbsIn(g).nbsIsConnected(connect).nbsUsingConnector(i).toString());
////				if (!nbs(FEMALE).nbsIn(g).nbsIsConnected(connect).nbsUsingConnector(i).isEmpty()) {
////					commit();
////				}
//
//			}
//		}
		if (g.contains(getId())) {
			for (IModule m:nbs(MALE).nbsIsConnected(!connect).nbsFilterConn(part).modules()){
				connection(m,connect);
			}
			if (nbs().nbsIsConnected(!connect).nbsFilterConn(part).isEmpty()){
				commit("My part " + part + " is processed");
			}
		}
		else {
			for (IModule m:nbs(MALE).nbsIsConnected(!connect).nbsIn(g).nbsFilterConnDest(part).modules()){
				connection(m,connect);
			}
				
		}
		
	
	}
	

	protected void connectPart (IModuleHolder g, int part) {
		connectionPart(g, part, true);
	}
	
	protected void disconnectPart (IModuleHolder g, int part) {
		connectionPart(g, part, false);
	}
	
	
	
	//////////////////////////////////////////////////////////////
	// Generated shared functions
	
	protected void gradientSend(IVar v, boolean isSource) {
		if (isSource) {
			varSet(v,0);
			visual.print("I am source for " + v);
		}
		
		broadcast(PacketCoreType.GRADIENT,REQ,new byte[]{v.index(),min(varGet(v)+1,MAX_BYTE)});
		
	}


	protected void symmetryFix(boolean isReq, byte conSource, byte conDest) {

		if (isFEMALE(conDest)) {
			if (!committed()) {
				if (isWEST(conSource) == isNORTH(conDest)) {
					context.switchNorthSouth();
				}
							
				context.switchEastWestHemisphere(isNORTH(conSource) == isWEST(conDest), isSOUTH(conDest));
			}				
			
			if (isReq) {  
				unicast(FEMALE&EAST, PacketCoreType.SYMMETRY, REQ);
			}
			unicast(FEMALE&WEST,PacketCoreType.SYMMETRY, ACK);
		}
		else if (isMALE(conDest)) {
			if (!committed()) {
				context.switchEastWestHemisphere(isSOUTH(conSource) == isWEST(conDest), isSOUTH(conDest));

				if (isWEST(conSource) == isSOUTH(conDest)) {
					context.switchNorthSouth();
				}
			}
			
			if (isReq) {  
				unicast(MALE&NORTH,PacketCoreType.SYMMETRY, REQ);
			} 
			unicast( MALE&SOUTH, PacketCoreType.SYMMETRY, ACK);
		}
		commit("Symmetry fix done");
	}



	

}