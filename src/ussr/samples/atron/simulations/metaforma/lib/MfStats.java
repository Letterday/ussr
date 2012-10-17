package ussr.samples.atron.simulations.metaforma.lib;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

class StatEntry {
	
	private float time;
	public byte metaID;
	public String msg;
	public IStateOperation stateOperation;
	
	public StatEntry(byte meta, IStateOperation s, String m,float t) {
		metaID = meta;
		time = t;
		stateOperation = s;
		msg = m;
	}
	
	public String toString () {
		return msg + ": " + metaID + " " +  stateOperation + " " + MfApi.round(time,1);
	}
	
	
}

public class MfStats {

	private static MfStats inst;
	
//	sequence --> time --> (metaId)
	
	private ArrayList<StatEntry> stat = new ArrayList<StatEntry>();

	private ConcurrentHashMap<IStateOperation,Float> incoming = new ConcurrentHashMap<IStateOperation,Float>();
	

	public static MfStats getInst () {
		if (inst == null) {
			inst = new MfStats();
		}
		return inst;
	}
	
	public MfStats() {
		
	}
	
	

	public void addStart(IStateOperation op, byte metaID, float time) {
		// If the previous start of a sequence is not ended succesfully
		if (!incoming.containsKey(op) || time - incoming.get(op) > 10) {
			stat.add(new StatEntry(metaID,op,"Start",time));
		}
		incoming.put(op,time);
	}
	
	
	public void addEnd(IStateOperation op, byte metaID, float time) {
		if (incoming.containsKey(op)){
			incoming.remove(op);
			stat.add(new StatEntry(metaID,op,"End",time));
		}
	}
	
	public String toString () {
		return stat.toString().replace(",", "\n") + "\n" + incoming + "\n";
	}
	

}
