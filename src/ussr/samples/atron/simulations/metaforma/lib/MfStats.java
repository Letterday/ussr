package ussr.samples.atron.simulations.metaforma.lib;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.print.attribute.standard.Finishings;

class StatEntry {
	
	private float startTime;
	private float endTime;
	public byte metaID;
	public IStateOperation stateOperation;
	public Orientation orient;
	private boolean finished;
	
	public StatEntry(byte meta, IStateOperation s, Orientation o, float t) {
		metaID = meta;
		startTime = t;
		stateOperation = s;
		orient = o;
		finished = false;
	}
	
	public float getStartTime() {
		return startTime;
	}
	
	public void setFinished (float time) {
		finished = true;
		endTime = time;
	}
	
	public String toString () {
		return (finished? "finished": "working") + ": " + metaID + " " +  stateOperation + " - " + orient + " start: " + MfApi.round(startTime,1) + " end: " + MfApi.round(endTime,1);
	}
	
	
}

public class MfStats {

	private static MfStats inst;
	
//	sequence --> time --> (metaId)
	
	private ArrayList<StatEntry> stat = new ArrayList<StatEntry>();

	private ConcurrentHashMap<Byte,StatEntry> incoming = new ConcurrentHashMap<Byte,StatEntry>();
	

	public static MfStats getInst () {
		if (inst == null) {
			inst = new MfStats();
		}
		return inst;
	}
	
	public MfStats() {
		
	}
	
	

	public void addStart(IStateOperation op, Orientation or, byte metaID, float time, MfController c) {
		// If the previous start of a sequence is not ended successfully
		
		StatEntry e = new StatEntry(metaID,op,or,time);
//		if (!incoming.containsKey(metaID) || time - incoming.get(metaID).getStartTime() > 10) {
//			
//		}
		incoming.put(metaID,e);
	}
	
	
	public void addEnd(IStateOperation op, Orientation or, byte metaID, float time) {
		if (incoming.containsKey(metaID)){
			incoming.get(metaID).setFinished(time);
			stat.add(incoming.get(metaID));
			incoming.remove(metaID);
		}
	}
	
	public String toString () {
		return stat.toString().replace(",", "\n") + "\n" + incoming + "\n";
	}
	

}
