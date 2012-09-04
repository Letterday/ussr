package ussr.samples.atron.simulations.metaforma.lib;

public class SettingsBase {
	private int ladderLength = 5;
	private int ladderWidth = 1;
	private boolean ladderBegin = true;
	
	private float metaVarSyncTime = 7;
	private float discoverTime = 5;
	private int gradientInterval = 200;
	private float gradientTime = 6;

	public int getLadderLength() {
		return ladderLength;
	}
	
	public int getLadderWidth() {
		return ladderWidth;
	}
	
	public boolean getLadderBegin() {
		return ladderBegin;
	}
	

	public float getMetaVarSyncTime() {
		return metaVarSyncTime ;
	}
	
	public float getDiscoverTime() {
		return discoverTime ;
	}

	public int getGradientInterval() {
		return gradientInterval;
	}

	public float getGradientTime() {
		return gradientTime;
	}

}
