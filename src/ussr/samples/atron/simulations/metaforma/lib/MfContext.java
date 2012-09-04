package ussr.samples.atron.simulations.metaforma.lib;




public class MfContext  {
	
	
	private boolean switchNorthSouth;
	private boolean switchEastWestN;
	private boolean switchEastWestS;
	
	private MfController ctrl;
	private NeighborSet neighbors;
	
	protected boolean[] femaleConnectorCache = new boolean[8];
	
	public MfContext(MfController c) {
		ctrl = c;
		neighbors = new NeighborSet((MfRuntime) ctrl);
	}
	
	public NeighborSet nbs() {
		return neighbors;
	}

	public void deleteUnconnectedNeighbors() {
		neighbors.deleteUnconnectedOnes();
	}
	
	public byte abs2rel (int nr) {
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
		ctrl.getVisual().print("$$$ Switch East West " + side + correct);
		if (southSide) {
			if (!isCorrect) {
				switchEWS();
				if (ctrl.getAngle() == 0) {
					switchEWN();
				} 
			}
			else { 
				if (ctrl.getAngle() != 0) {
					switchEWN();
				}
			}
			
		}
		else {
			if (!isCorrect) {
				switchEWN();
				if (ctrl.getAngle() == 0) {
					switchEWS();
				} 
			}
			else if (ctrl.getAngle() != 0) {
				switchEWS();
			}
		}
		
		
		
		ctrl.getVisual().colorize();
	}	
	
	private void switchEWN () {
		neighbors.updateSymmetryEW(false);
		switchEastWestN = !switchEastWestN;
		ctrl.getVisual().print("# switch EW N");
	}
	
	private void switchEWS () {
		neighbors.updateSymmetryEW(true);
		switchEastWestS = !switchEastWestS;
		ctrl.getVisual().print("# switch EW S");
	}
	
	public void switchEastWest () {
		switchEWN();
		switchEWS();
	}
	
	public void switchNorthSouth () {
		 switchNorthSouth =! switchNorthSouth;
		 ctrl.getVisual().print("# Switch North South");
		 neighbors.updateSymmetryNS();
	}

	public void addNeighbor(IModule source, byte connector, byte sourceConnector,IRole r, byte metaId, byte metaBossId) {
		neighbors.add(source,connector,sourceConnector,r,metaId,metaBossId);
		
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
			return ctrl.isConnected(abs2rel(c));
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
