package ussr.samples.atron.simulations.metaforma.lib;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

class Pair {
	public byte v1;
	public byte v2;
	
	public Pair (byte v_1,byte v_2) {
		v1 = v_1;
		v2 = v_2;
	}
	
	@Override
	public String toString () {
		return "\n(" + v1 + "," + v2 + ")";
	}
	
	@Override 
	public int hashCode () {
		return v1*255+v2;
	}
	
	@Override 
	public boolean equals (Object p) {
		return v1 == ((Pair)p).v1 && v2 == ((Pair)p).v2;
	}
}

class StatEntry {
	
	private float startTime;
	private float endTime;
	public byte metaID;
	public IStateOperation stateOperation;
	public Orientation orient;
	private Finish finished = Finish.RUNNING;
	
	public StatEntry(byte meta, IStateOperation s, Orientation o, float t) {
		metaID = meta;
		startTime = t;
		stateOperation = s;
		orient = o;
	}
	
	public float getStartTime() {
		return startTime;
	}
	
	public void setFinished (float time, Finish f) {
		finished = f;
		endTime = time;
	}
	
	public String toString () {
//		return String.format("%.2f, %.2f, %d, %s, %s", startTime,endTime,metaID,stateOperation,finished);
		return String.format("		%.2f		& 	%.2f		&		%.2f		&	%d		&	%s	&	%s		\\\\", startTime,endTime,endTime-startTime,metaID,stateOperation,finished);
	}
	
	public String toLatex () {
		return String.format("		%.2f		& 	%.2f		&		%.2f		&	%d		&	%s	&	%s		\\\\", startTime,endTime,endTime-startTime,metaID,stateOperation,finished);
	}
		
}

public class MfStats {

	private static MfStats inst;
	
//	sequence --> time --> (metaId)
	
	private ArrayList<StatEntry> stat = new ArrayList<StatEntry>();

	private ConcurrentHashMap<Pair,StatEntry> incoming = new ConcurrentHashMap<Pair,StatEntry>();
	

	public static MfStats getInst () {
		if (inst == null) {
			inst = new MfStats();
		}
		return inst;
	}
	
	public MfStats() {
		
	}
	
	

	public synchronized void addStart(State s, MfController c) {
		// If the previous start of a sequence is not ended successfully
		
		StatEntry e = new StatEntry(c.module().metaID,s.getOperation(),s.getOrientation(),c.time());

		incoming.put(new Pair(c.module().getMetaID(),s.getOperationCounter()),e);
	}
	
	
	public synchronized void addEnd(State s,byte metaID, float time, Finish f) {
		
		Pair key = new Pair(metaID,s.getOperationCounter());
		if (incoming.containsKey(key)){
			System.err.println(key + " does exist for state " + s + "with meta " + metaID);
			incoming.get(key).setFinished(time,f);
			stat.add(incoming.get(key));
			incoming.remove(key);
		}
		else {
			System.err.println(key + " does not exist for state " + s + "with meta " + metaID);
//			throw new Error("Incoming " + metaID + " was not found! ");
		}
	}
	
	public String toString () {
		return stat.toString().replace(",", "\n") + "\n" + incoming + "\n";
	}

	

}
