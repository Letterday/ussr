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
		if (g.contains(ctrl.getId()) ) {
			rotate(degrees);
		}
	}
	
	public void disconnect(IModuleHolder g1, IModuleHolder g2) {
		disconnect(g1, g2, true);
	}
	
	public void connect(IModuleHolder g1, IModuleHolder g2) {
		connect(g1, g2, true);
	}


	public void connect(IModuleHolder g1, IModuleHolder g2, boolean insideRegionOnly) {
		ctrl.module().discover();
		ctrl.visual.print("##connect " + g1 + "," + g2);
		connection(g1, g2, true, insideRegionOnly);
		connection(g2, g1, true, insideRegionOnly);
	}
	
	
	public void disconnect(IModuleHolder g1, IModuleHolder g2, boolean insideRegionOnly) {
		ctrl.module().discover();
		ctrl.visual.print("## disconnect " + g1 + "," + g2);
		connection(g1, g2, false, insideRegionOnly);
		connection(g2, g1, false, insideRegionOnly);
		
	}
	
	
	private void connection(IModuleHolder g1, IModuleHolder g2, boolean connect, boolean insideRegionOnly) {
		ctrl.stateMngr.commitNotAutomatic(g1,g2);
	
		if (g1.contains(ctrl.getId())) {
			for (IModule nb: ctrl.nbs(MfController.MALE).nbsInRegion(insideRegionOnly).nbsIn(g2).nbsIsConnected(!connect).modules()) {
				ctrl.visual.print("##connection " + g1 + "," + g2 + "  " + connect + " " + insideRegionOnly);
				connection(nb,connect);
			}
			
			if (ctrl.nbs().nbsInRegion(insideRegionOnly).nbsIn(g2).nbsIsConnected(!connect).isEmpty()) {
				// Commit 
				ctrl.stateMngr.commit("Member of " + g1 + " and did my action to " + g2);
			}
			if (ctrl.nbs().nbsInRegion(insideRegionOnly).nbsIn(g2).isEmpty()) {
				// We need to to this because the whole grouping is excluded from automatic commit, also non-nb's!
				ctrl.stateMngr.commit("Member of " + g1 + " but not connected to " + g2);
			}
		}
		
	}
	
	private void connection(IModule dest, boolean makeConnection) {
		if (!ctrl.stateMngr.committed()) {
			byte conToNb = ctrl.nbs().getConnectorNrTo(dest);
			byte conFromNb = ctrl.nbs().getConnectorNrFrom(dest);
			String action = makeConnection ? " connect to " : " disconnect from ";
			ctrl.visual.print("# " + ctrl.getId() + action + dest);
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
			
//		for (int i=0; i<8; i++) {
//			if ((part&pow2(i))==pow2(i)) {
//				if (g.contains(ctrl.getId())) {
//					if (context.isConnConnected(i) == !connect && isMfController.MALE(i)) {
//						connection(i,connect);
//					}
//				}
//				for (IModule m: ctrl.nbs(MfController.MALE).nbsIn(g).nbsIsConnected(!connect).nbsUsingConnector(i).modules()) {
//					ctrl.visual.print("# " + ctrl.getId() + " has connector " + i + " matches");
//					connection(m,connect);
//				}
////				ctrl.visual.print(ctrl.nbs(FEMfController.MALE).toString());
////				ctrl.visual.print(ctrl.nbs(FEMfController.MALE).nbsIn(g).toString());
////				ctrl.visual.print(ctrl.nbs(FEMfController.MALE).nbsIn(g).nbsIsConnected(connect).toString());
////				ctrl.visual.print(ctrl.nbs(FEMfController.MALE).nbsIn(g).nbsIsConnected(connect).nbsUsingConnector(i).toString());
////				if (!ctrl.nbs(FEMfController.MALE).nbsIn(g).nbsIsConnected(connect).nbsUsingConnector(i).isEmpty()) {
////					commit();
////				}
//
//			}
//		}
		if (g.contains(ctrl.getId())) {
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
	
	
}