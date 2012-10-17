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
		// Because connected cache from females might be out of date!
		neighbors.deleteFemaleOnes();
		
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
		
		
		return ret;
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
		swapFemaleCache(0,2);
		swapFemaleCache(1,3);		
		print("# switch EW N");
	}
	
	private void swapFemaleCache(int i, int j) {
		boolean temp;
		temp = femaleConnectorCache[i];
		femaleConnectorCache[i] = femaleConnectorCache[j];
		femaleConnectorCache[j] = temp;
		
	}

	public void switchEWS () {
		if (!debug)neighbors.updateSymmetryEW(true);
		switchEastWestS = !switchEastWestS;
		swapFemaleCache(4,6);
		swapFemaleCache(5,7);
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
		 swapFemaleCache(0,4);
		 swapFemaleCache(1,5);
		 swapFemaleCache(2,6);
		 swapFemaleCache(3,7);
	}

	private void print(String string) {
		if (!debug) {
			ctrl.getVisual().print(string);
		}
		
	}

	public void addNeighbor(IModule source, byte connDest, byte connSource,IMetaPart r, byte metaId, byte regionId) {
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
		 
		if (MfController.isFEMALE(c)){
			return femaleConnectorCache[c];
		}
		else {
			return ctrl.isConnected(rel2abs(c));
		}
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
