package ussr.samples.atron.simulations.metaforma.lib;


import ussr.samples.atron.simulations.metaforma.gen.Module;

public abstract class MetaformaRuntime extends MetaformaController {

	public final static byte NORTH = (byte) (pow2(0) + pow2(1) + pow2(2) + pow2(3));
	public final static byte SOUTH = (byte) (pow2(4) + pow2(5) + pow2(6) + pow2(7));
	public final static byte WEST = (byte) (pow2(0) + pow2(1) + pow2(4) + pow2(5));
	public final static byte EAST = (byte) (pow2(2) + pow2(3) + pow2(6) + pow2(7));
	public final static byte MALE = (byte) (pow2(0) + pow2(2) + pow2(4) + pow2(6));
	public final static byte FEMALE = (byte) (pow2(1) + pow2(3) + pow2(5) + pow2(7));
	
	protected final static boolean REQ = false;
	protected final static boolean ACK = true;
	
	public MetaformaRuntime() {
		super();
	}
	
	public void rotate(int degrees) {
		visual.print("## rotate " + degrees + ", current = " + angle + "; new = " + (degrees + angle) + "");
		// TODO: There is still a strange issue: when rotating 180 degrees, it is undefined whether it goes CW or CCW (randomly).
		if (Math.abs(degrees) < 180) {
			angle = angle + degrees % 360;
			doRotate(angle);
		}
		else {
			// Therefore we rotate in 2 steps, such that the direction is no longer undefined, but can be chosen by a pos/neg degree
			angle = angle + (degrees /2) % 360;
			doRotate(angle);
			angle = angle + (degrees /2) % 360;
			doRotate(angle);
		}
		
	}
	
	public void rotateTo(int degrees) {
		visual.print("## rotateTo " + degrees + ", current = " + angle);
		angle = degrees;
		doRotate(angle);
	}
	
	private void doRotate (int degrees) {
		rotateToDegreeInDegrees(degrees);
		while (isRotating()) {
			yield();
		}
	}
	
	
	public void rotate(IModuleHolder g, int degrees) {
		if (g.contains(getId()) ) {
			rotate(degrees);
			commit();
		}
	}


	public void connect(IModuleHolder g1, IModuleHolder g2) {
		discoverNeighbors();
		connection(g1, g2, true);
		connection(g2, g1, true);
	}
	
	public void disconnect(IModuleHolder g1, IModuleHolder g2) {
		connection(g1, g2, false);
		connection(g2, g1, false);
		
	}
	
	private void connection(IModuleHolder g1, IModuleHolder g2, boolean connect) {
		if (g1.contains(getId())) {
			for (Module m: nbs(MALE).in(g2).isConnected(!connect).modules()) {
				connection (getId(),m,connect);
			}
		}
		
	}
	
	private void connection(Module dest, boolean makeConnection) {
		byte conToNb = nbs().getConnectorNrTo(dest);
		byte conFromNb = nbs().getConnectorNrFrom(dest);
		String action = makeConnection ? " connect to " : " disconnect from ";
		visual.print("# " + getId() + action + dest);
		if (conToNb % 2 == 0 && conFromNb % 2 == 1) {
			if (makeConnection)
				connect(conToNb);
			else
				disconnect(conToNb);
		}
	}
	

	private void connection(Module m1, Module m2, boolean c) {
		String action = c ? "connect" : "disconnect";
		if (getId() == m1) {
			if (nbs(MALE).contains(m2)) {
				while (!nbs(MALE).isConnected(c).contains(m2)) {
					connection(m2,c);
				}
				commit();
				visual.print("# "  + action + " from " + m1 + " to " + m2);
			}
		}
		
	}
	
	protected void connectionPart (IModuleHolder g, int part, boolean connect) {
		visual.print("# " + g + " disconnectPart " + part);
			for (int i=0; i<8; i++) {
				if ((part&pow2(i))==pow2(i)) {
					if (g.contains(getId())) {
						visual.print(i + " matches");
						if (context.isConnConnected(i) == !connect && isMale(i)) {
							//notification(i + " is male and connected");
							connection(i,connect);
							//stateTrans.commit();
						}
					}
					for (Module m: nbs(MALE).in(g).isConnected(!connect).usingConnector(i).modules()) {
						visual.print("# " + getId() + " has connector " + i + " matches");
						connection(m,connect);
						//stateTrans.commit();
						// this should be done by the module in question
					}

				}
			}
			if (g.contains(getId()) && nbs(part).isConnected(!connect).isEmpty()) {
				commit(); 
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
		
		broadcast(Type.GRADIENT,REQ,new byte[]{v.index(),min(varGet(v)+1,MAX_BYTE)});
		
	}


	protected void symmetryFix(boolean isReq, byte conSource, byte conDest, byte[] data) {

		if (isFEMALE(conDest)) {
			if (!stateGetBoolean("fixedYet")) {
				if (isReq == isNORTH(conDest)) context.switchNorthSouth();
							
				context.switchEastWestHemisphere(isNORTH(conSource) == isWEST(conDest), isSOUTH(conDest));
			}				
			
			if (isReq) {  
				unicast(FEMALE&NORTH, Type.SYMMETRY, REQ);
			}
			unicast(FEMALE&SOUTH,Type.SYMMETRY, ACK);
		}
		else if (isMALE(conDest)) {
			if (!stateGetBoolean("fixedYet")) {
				context.switchEastWestHemisphere(isSOUTH(conSource) == isWEST(conDest), isSOUTH(conDest));

				if (isWEST(conSource) == isSOUTH(conDest)) {
					context.switchNorthSouth();
				}
			}
			
			if (isReq) {  
				unicast(MALE&WEST,Type.SYMMETRY, REQ);
			} 
			unicast( MALE&EAST, Type.SYMMETRY, ACK);
		}
		stateSetVar("fixedYet", true);
		commit();
	}



	

}