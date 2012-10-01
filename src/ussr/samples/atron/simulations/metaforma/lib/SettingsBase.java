package ussr.samples.atron.simulations.metaforma.lib;

public class SettingsBase {
	private int ladderLength = 1;
	private int ladderWidth = 2;
	private boolean ladderBegin = true;
	
	private float metaVarSyncTime = 7;
	private float discoverTime = 5;
	private float gradientInterval = 0.2f;
	private float gradientTime = 6;
	private byte metaTTL = 2;
	private float metaIndirectDiscoverInterval = 0.5f;
	private float metaDirectDiscoverInterval = 0.2f;

	public int getLadderLength() {
		return ladderLength;
	}
	
	public int getLadderWidth() {
		return ladderWidth;
	}
	
	public boolean getLadderBegin() {
		return ladderBegin;
	}
	

	public float getMetaIndirectDiscoverTime() {
		return metaVarSyncTime ;
	}
	
	public float getMetaDirectDiscoverTime() {
		return discoverTime ;
	}

	public float getGradientInterval() {
		return gradientInterval;
	}

	public float getGradientTime() {
		return gradientTime;
	}

	public byte getMetaTTL() {
		return metaTTL ;
	}

	public float getMetaIndirectDiscoverInterval() {
		return metaIndirectDiscoverInterval;
	}
	
	public float getMetaDirectDiscoverInterval() {
		return metaDirectDiscoverInterval;
	}

}
