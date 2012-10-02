package ussr.samples.atron.simulations.metaforma.lib;




public class MfContext  {
	
	
	private boolean switchNorthSouth;
	private boolean switchEastWestN;
	private boolean switchEastWestS;
	
	private MfController ctrl;
	private NeighborSet neighbors;
	
	protected boolean[] femaleConnectorCache = new boolean[8];
	private boolean debug = false;
	
	public MfContext(MfController c) {
		ctrl = c;
		neighbors = new NeighborSet(ctrl);
	}
	
	public NeighborSet nbs() {
		return neighbors;
	}

	public void deleteUnconnectedNeighbors() {
		neighbors.deleteUnconnectedOnes();
	}
	
	public byte abs2rel (int nr) {
		byte ret = (byte) nr;
		
		if (isSwitchedEastWestN() && ret <= 3) {
			ret = (byte) ((ret + 2)%4);
		}
		if (isSwitchedEastWestS() && ret >= 4) {
			ret = (byte) ((ret + 2)%4 + 4);
		}
		
		if (isSwitchedNorthSouth()) {
			ret = (byte) ((ret + 4)%8);
		}
		if (nr != ret) {
			if (!debug)System.out.println("ABS2REL " + nr + " to " + ret + " " + getFlipString());
		}
		return ret;
	}
	
	public byte rel2abs (int nr) {
		byte ret = (byte) nr;
		
		if (isSwitchedNorthSouth()) {
			ret = (byte) ((ret + 4)%8);
		}
		if (isSwitchedEastWestN() && ret <= 3) {
			ret = (byte) ((ret + 2)%4);
		}
		if (isSwitchedEastWestS() && ret >= 4) {
			ret = (byte) ((ret + 2)%4 + 4);
		}
		
		if (nr != ret) {
			if (!debug)System.out.println("rel2abs " + nr + " to " + ret + " " + getFlipString());
		}
		
		return ret;
	}
	
	public String getFlipString() {
		String flipStr = "";
		if (isSwitchedNorthSouth()) flipStr += "NORTH-SOUTH ";
		if (isSwitchedEastWestN()) flipStr += "EAST-WEST-N ";
		if (isSwitchedEastWestS()) flipStr += "EAST-WEST-S ";
		
		if (flipStr.equals("")) {
			flipStr = "<none>";
		}
		return flipStr;
	}
	
	public void switchEastWestHemisphere (boolean isCorrect, boolean southSide) {
		String side = southSide ? " southside " : " northside ";
		String correct = isCorrect ? " correct " : " incorrect ";
		print("$$$ Switch East West " + side + correct);
		int angle = getAngle();
		if (southSide == !isSwitchedNorthSouth()) {
			if (!isCorrect) {
				switchEWS();
				if (angle == 0) {
					switchEWN();
				} 
			}
			else if (angle != 0) {
				switchEWN();
			}
			
		}
		else {
			if (!isCorrect) {
				switchEWN();
				if (angle == 0) {
					switchEWS();
				} 
			}
			else if (angle != 0) {
				switchEWS();
			}
		}
		
		if (!debug)ctrl.getVisual().colorize();
	}	
	
	public void switchEWN () {
		if (!debug)neighbors.updateSymmetryEW(false);
		switchEastWestN = !switchEastWestN;
		print("# switch EW N");
	}
	
	public void switchEWS () {
		if (!debug)neighbors.updateSymmetryEW(true);
		switchEastWestS = !switchEastWestS;
		print("# switch EW S");
	}
	
	public void switchEastWest () {
		switchEWN();
		switchEWS();
	}
	
	public void switchNorthSouth () {
		 switchNorthSouth =! switchNorthSouth;
		 print("# Switch North South");
		 if (!debug)neighbors.updateSymmetryNS();
	}

	private void print(String string) {
		if (!debug) {
			ctrl.getVisual().print(string);
		}
		
	}

	public void addNeighbor(IModule source, byte connDest, byte connSource,IRole r, byte metaId, byte regionId) {
		neighbors.add(source,connDest,connSource,r,metaId,regionId);
		
	}
	

	public int getAngle() {
    	return (ctrl.getAngle() + ((isSwitchedEastWestN() ^ isSwitchedEastWestS()) ? 180 : 0)) %360;
    }


	public boolean isSwitchedNorthSouth() {
		return switchNorthSouth;
	}

	
	public boolean isSwitchedEastWestN() {
		return switchEastWestN;
	}


	public boolean isSwitchedEastWestS() {
		return switchEastWestS;
	}
	
	public boolean isConnConnected (int c) {
		if (c == -1) return false;
		//TODO: Restore 
//		if (MfController.isFEMALE(c)){
//			return femaleConnectorCache[c];
//		}
//		else {
			return ctrl.isConnected(rel2abs(c));
//		}
	}

	public void setFemaleConnected(int connectorNr, boolean connectorConnected) {
		femaleConnectorCache[connectorNr] = connectorConnected;
	}
	
	public boolean isFemaleConnected (int c) {
		return femaleConnectorCache[c];
	}

	public String getFemaleConnsAsString() {
		return "(" + isFemaleConnected(1) + "," + isFemaleConnected(3) + "," + isFemaleConnected(5) + "," + isFemaleConnected(7) + ")";
	}
	
}
