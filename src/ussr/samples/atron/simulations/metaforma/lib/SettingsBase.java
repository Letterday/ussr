package ussr.samples.atron.simulations.metaforma.lib;

import java.util.HashMap;

import ussr.util.Pair;


public class SettingsBase {
	public HashMap<String,Pair<Float,Float>> intervals = new HashMap<String, Pair<Float,Float>>();
	public HashMap<String,Float> settings = new HashMap<String,Float>();
	private float propagationRate;
	private float withdrawTime;

	
	
	public SettingsBase () {
		intervals.put("module.discover", new Pair<Float,Float>(2f,3f));
		intervals.put("module.gradientPropagate", new Pair<Float,Float>(0.3f,5f));
		intervals.put("module.broadcastConsensus", new Pair<Float,Float>(6f,5f));
		intervals.put("meta.broadcastVars", new Pair<Float,Float>(4f,0f));
		intervals.put("meta.broadcastNeighbors", new Pair<Float,Float>(4f,15f));
		
		settings.put("turnAroundTime", 2f);
		settings.put("backwardTime", 10f);
		settings.put("assignTime", 4f);
		settings.put("proximity", 0.20f);
		
		propagationRate = 0.3f;
		withdrawTime = 20f;
	}
	
	public float getPropagationRate() {
		return propagationRate;	
	}
	
	public float getWithdrawTime() {
		return withdrawTime;	
	}
	
	public float getInterval (String name) {
		return intervals.get(name).fst();
	}
	
	public float getDuration (String name) {
		return intervals.get(name).snd();
	}
	
	private int ladderLength = 3;
	private int ladderWidth = 1;
	private boolean ladderBegin = false;
//	
//	private float metaVarSyncTime = 7;
//	private float discoverTime = 5;
//	private float gradientInterval = 0.2f;
//	private float gradientTime = 6;
//	private byte metaTTL = 2;
//	private float metaIndirectDiscoverInterval = 0.5f;
//	private float metaDirectDiscoverInterval = 0.2f;

	public int getLadderLength() {
		return ladderLength;
	}
	
	public int getLadderWidth() {
		return ladderWidth;
	}
	
	public boolean getLadderBegin() {
		return ladderBegin;
	}
//	
//
//	public float getMetaIndirectDiscoverTime() {
//		return metaVarSyncTime ;
//	}
//	
//	public float getMetaDirectDiscoverTime() {
//		return discoverTime ;
//	}
//
//	public float getGradientInterval() {
//		return gradientInterval;
//	}
//
//	public float getGradientTime() {
//		return gradientTime;
//	}
//
//	public byte getMetaTTL() {
//		return metaTTL ;
//	}
//
//	public float getMetaIndirectDiscoverInterval() {
//		return metaIndirectDiscoverInterval;
//	}
//	
//	public float getMetaDirectDiscoverInterval() {
//		return metaDirectDiscoverInterval;
//	}

	public float get(String name) {
		return settings.get(name);
	}

}
