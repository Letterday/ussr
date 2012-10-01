package ussr.samples.atron.simulations.metaforma.lib;



public class MfActuation  {
	
	private MfController ctrl;



	public MfActuation(MfController c) {
		ctrl = c;
	}

	public void rotate(int degrees) {
		ctrl.visual.print("## rotate " + degrees + ", current = " + ctrl.getAngle() + "; new = " + (degrees + ctrl.getAngle()) + "");
		// TODO: There is still a strange issue: when rotating 180 degrees, it is undefined whether it goes CW or CCW (randomly).
		if (Math.abs(degrees) < 180) {
			ctrl.setAngle((360 + ctrl.getAngle() + degrees) % 360);
			rotateAbs(ctrl.getAngle());
		}
		else {
			// Therefore we rotate in 2 steps, such that the direction is no longer undefined, but can be chosen by a pos/neg degree
			ctrl.setAngle (360 + ctrl.getAngle() + (degrees /2) %360);
			rotateAbs(ctrl.getAngle());
			ctrl.setAngle (360 + ctrl.getAngle() + (degrees /2)%360) ;
			rotateAbs(ctrl.getAngle());
		}
		ctrl.stateMngr.commit("Rotating done to " + degrees);
	}

	
	private void rotateAbs (int degrees) {
		ctrl.rotateToDegreeInDegrees(degrees);
		while (ctrl.isRotating()) {
			ctrl.yield();
		}
	}
	
	
	public void rotate(IModuleHolder g, int degrees) {
		ctrl.stateMngr.commitNotAutomatic(g);
		if (g.contains(ctrl.module().getID()) ) {
			rotate(degrees);
		}
	}
	
	public void disconnect(IModuleHolder g1) {
		connection(g1,false,true);
	}
	
	public void connect(IModuleHolder g1) {
		connection(g1,true,true);
	}
	
	public void disconnect(IModuleHolder g1, IModuleHolder g2) {
		disconnect(g1, g2, true);
	}
	
	public void connect(IModuleHolder g1, IModuleHolder g2) {
		connect(g1, g2, true);
	}


	public void connect(IModuleHolder g1, IModuleHolder g2, boolean insideRegionOnly) {
		ctrl.module().discover();
		ctrl.visual.print("## connect " + g1 + "," + g2);
		ctrl.stateMngr.commitNotAutomatic(g1,g2);
		if (g1.contains(ctrl.getID())) {
			connection(g2, true, insideRegionOnly);
		}
		if (g2.contains(ctrl.getID())) {
			connection(g1, true, insideRegionOnly);
		}
	}
	
	
	public void disconnect(IModuleHolder g1, IModuleHolder g2, boolean insideRegionOnly) {
		ctrl.module().discover();
		ctrl.visual.print("## disconnect " + g1 + "," + g2);
		ctrl.stateMngr.commitNotAutomatic(g1,g2);
		if (g1.contains(ctrl.getID())) {
			connection(g2, false, insideRegionOnly);
		}
		if (g2.contains(ctrl.getID())) {
			connection(g1, false, insideRegionOnly);
		}
		
	}
	
	
	private void connection(IModuleHolder g2, boolean connect, boolean insideRegionOnly) {
		
		for (IModule nb: ctrl.nbs(MfController.MALE).nbsInRegion(insideRegionOnly).nbsIn(g2).nbsIsConnected(!connect).modules()) {
			ctrl.visual.print("##connection ME," + g2 + "  " + connect + " " + insideRegionOnly);
			connection(nb,connect);
		}
		
		if (ctrl.nbs().nbsInRegion(insideRegionOnly).nbsIn(g2).nbsIsConnected(!connect).isEmpty()) {
			// Commit 
			ctrl.stateMngr.commit("Member of ME and did my action to " + g2);
		}
		if (ctrl.nbs().nbsInRegion(insideRegionOnly).nbsIn(g2).isEmpty()) {
			// We need to to this because the whole grouping is excluded from automatic commit, also non-nb's!
			ctrl.stateMngr.commit("Member of ME but not connected to " + g2);
		}
	
		
	}
	
	private void connection(IModule dest, boolean makeConnection) {
		if (!ctrl.stateMngr.committed()) {
			byte conToNb = ctrl.nbs().getConnectorNrTo(dest);
			byte conFromNb = ctrl.nbs().getConnectorNrFrom(dest);
			String action = makeConnection ? " connect to " : " disconnect from ";
			ctrl.visual.print("# " + ctrl.module().getID() + action + dest);
			if (conToNb % 2 == 0 && conFromNb % 2 == 1) {
				connection(conToNb,makeConnection);
			}
			else {
				throw new Error("Wrong invocation, " + dest + " is connected with feMfController.MALE connector so cant do anything: " + conToNb);
			}
			
		}
	}
	
	
	 protected void connection(int c, boolean connect) {
		 if (connect && !ctrl.context.isConnConnected(c)) {
			 ctrl.connect(ctrl.context.abs2rel(c)); 
		 }
		 else if (!connect && ctrl.context.isConnConnected(c)) {
			 ctrl.disconnect(ctrl.context.abs2rel(c));
		 }
	}
	
	protected void connectionPart (IModuleHolder g, int part, boolean connect) {
		ctrl.visual.print("# " + g + " disconnectPart " + connectorsToString(part));
		ctrl.stateMngr.commitNotAutomatic(g);
			
		if (g.contains(ctrl.module().getID())) {
			for (IModule m:ctrl.nbs(MfController.MALE).nbsIsConnected(!connect).nbsFilterConn(part).modules()){
				connection(m,connect);
			}
			if (ctrl.nbs().nbsIsConnected(!connect).nbsFilterConn(part).isEmpty()){
				ctrl.stateMngr.commit("My part " + part + " is processed");
			}
		}
		else {
			for (IModule m:ctrl.nbs(MfController.MALE).nbsIsConnected(!connect).nbsIn(g).nbsFilterConnDest(part).modules()){
				connection(m,connect);
			}
				
		}
	}
	

	private String connectorsToString(int part) {
		String ret = " ";
		for (int i=0; i<8; i++) {
			if ((MfController.pow2(i)&part) != 0) {
				ret+= i + ",";	
			}
		}
		return ret.substring(0, ret.length()-1);
	}

	protected void connectPart (IModuleHolder g, int part) {
		connectionPart(g, part, true);
	}
	
	public void disconnectPart (IModuleHolder g, int part) {
		connectionPart(g, part, false);
	}

//	THIS DOES NOT WORK FOR FEMALE CONNECTORS!
//	private void disconnectDiag (IModuleHolder g,int c1,int c2, int c3, int c4) {
//		if (ctrl.getContext().isConnConnected(c1) && ctrl.getContext().isConnConnected(c2)) {
//			disconnectPart(g, MfApi.pow2(c3) | MfApi.pow2(c4));
//		}
//	}
//	
//	public void disconnectDiagonal (IModuleHolder g) {
//		disconnectDiag(g, 0, 6, 2, 4);
//		disconnectDiag(g, 1, 7, 3, 5);
//		disconnectDiag(g, 2, 4, 0, 6);
//		disconnectDiag(g, 3, 5, 1, 7);
//	}
	
	
	
	
}