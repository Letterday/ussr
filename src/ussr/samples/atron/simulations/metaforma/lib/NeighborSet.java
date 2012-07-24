package ussr.samples.atron.simulations.metaforma.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import ussr.samples.atron.simulations.metaforma.gen.Grouping;
import ussr.samples.atron.simulations.metaforma.gen.Module;

//class Neighbor {
//	byte sourceCon;
//	byte destCon;
//	Module m;
//}

public class NeighborSet implements IModuleHolder {
	private ConcurrentHashMap<Module, Byte[]> connectors;// = new HashMap<Module, Byte[]>();
	private ConcurrentHashMap<Byte, Module> modules;// = new HashMap<Byte, Module>();
	private MetaformaRuntime ctrl;
	
	public NeighborSet (MetaformaRuntime c) {
		ctrl = c;
		connectors = new ConcurrentHashMap<Module, Byte[]>();
		modules = new ConcurrentHashMap<Byte, Module>();
	}
	
	public NeighborSet (NeighborSet nbs,MetaformaRuntime c) {
		connectors = new ConcurrentHashMap<Module, Byte[]>(nbs.getConnectors());
		modules = new ConcurrentHashMap<Byte, Module>(nbs.getModules());
		connectors = nbs.getConnectors();
		modules = nbs.getModules();
		ctrl = c;
	}
	
	public ConcurrentHashMap<Byte, Module>  getModules() {
		return modules;
	}

	public ConcurrentHashMap<Module, Byte[]>  getConnectors() {
		return connectors;
	}

	public NeighborSet nbs() {
		return new NeighborSet(this, ctrl); // TODO:Is this correct?
	}
	
	public void assoc (Map.Entry<Module, Byte[]> e) {
		assoc(e.getKey(),e.getValue()[0],e.getValue()[1]);
	}
	
	public void add (Module nb, int conToNb, int conFromNb) { 
		if (getConnectorNrTo(nb) != conToNb || getConnectorNrFrom(nb) != conFromNb) {
			ctrl.notification(".addNeighbor " + nb + " [" + conToNb + "," + conFromNb + "] (" + nb + "=" + conToNb + "!= " +getConnectorNrTo(nb)+")");
			assoc (nb,conToNb,conFromNb);
		}
	}
	
	private void assoc (Module nb, int conToNb, int conFromNb) {
		if (!getModuleByConnector(conToNb).equals(nb)) {
//			ctrl.notification("before: " + toString());
			delete(nb);
			delete(conToNb);
//			ctrl.notification("delete " + conToNb);
//			ctrl.notification("after  : " + toString());
		}
		connectors.put(nb, new Byte[]{(byte)conToNb,(byte)conFromNb});
		modules.put((byte)conToNb, nb);
	}
	
	public byte getConnectorNrTo (Module mod) {
		//ctrl.notification(connectors.toString());
		if (connectors.containsKey(mod)) {
			return connectors.get(mod)[0];
		}
		else {
			return -1;
		}		
	}
	
	public byte getConnectorNrFrom (Module mod) {
		if (connectors.containsKey(mod)) {
			return connectors.get(mod)[1];
		}
		else {
			return -1;
		}		
	}
	
	public Module getModuleByConnector (int con) {
		if (modules.containsKey((byte)con)) {
			return modules.get((byte)con);
		}
		else {
			return Module.None;
		}		
	}
	
	public boolean exists () {
		return !modules.isEmpty();
	}
	
//	public NeighborBag filter (NeighborBag neighbors, ArrayList<Filter> filters) {
//		NeighborBag ret = new NeighborBag(ctrl);
//		
//		for (Map.Entry<Module, Byte[]>entry : neighbors.entrySet()) {
//			boolean match = true;
//			for (Filter f : filters) {
//				if (!f.apply(entry)) {
//					match = false;
//				}
//			}
//			if (match) {
//				ret.assoc(entry.getKey(),entry.getValue()[0],entry.getValue()[1]);
//			}
//		}
//		return ret;
//	}

	public Set<Map.Entry<Module,Byte[]>> entrySet() {
		return connectors.entrySet();
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}

	public int size () {
		if (modules.size() != connectors.size()) {
			System.err.println ("modules.size(" + modules.size() + ") != connectors.size(" + connectors.size() + ") --> this should not happen!");
		}
		return modules.size();
	}
	
	public String toString() {
		String r = "";
		for (Map.Entry<Module, Byte[]> e : connectors.entrySet()) {
			String m = e.getKey() + " [" + e.getValue()[0] + ", "+ e.getValue()[1] + "], ";
			if (ctrl.isConnected(e.getValue()[0])) {
					m = m.toUpperCase();
				}
				r += m;
			}
		//r =  r.substring(0, r.length() - 2) + "\n" + modules.toString() + "\n";
		return r;
	}
	
	public void delete (Module nb) {
		modules.remove(getConnectorNrTo(nb));
		connectors.remove(nb);
	}
	
	public void delete (int connector) {
		//ctrl.notification("remove " + getModuleByConnector(connector));
		connectors.remove(getModuleByConnector(connector));
		modules.remove((byte)connector);
	}
	
	public boolean contains(Module m) {
		return connectors.containsKey(m);
	}
	
	
	public NeighborSet connected() {
		return isConnected(true);
	}

	public NeighborSet disconnected() {
		return isConnected(false);
	}

	public NeighborSet isConnected(boolean connected) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<Module, Byte []> e : entrySet()) {
			if (ctrl.isConnected(e.getValue()[0]) == connected) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	public NeighborSet maleAlignedWithFemale () {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<Module, Byte []> e : entrySet()) {
			if (e.getValue()[0]%2 == 0 && e.getValue()[1]%2 == 1) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	public NeighborSet genderDismatch () {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<Module, Byte []> e : entrySet()) {
			if (e.getValue()[0]%2 == e.getValue()[1]%2) {
				ret.assoc(e);
			}
		}
		return ret;
	} 
	

	public NeighborSet onGroup (Grouping g) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<Module, Byte []> e : entrySet()) {
			if (e.getKey().getGrouping().equals(g)) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	private NeighborSet matchConnectors(List<Integer> connectors) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<Module, Byte []> e : entrySet()) {
			if (connectors.contains((int)e.getValue()[0])) {
				ret.assoc(e);
			}
		}
		return ret;
	}

	public NeighborSet male() {
		return matchConnectors(Arrays.asList(0,2,4,6));
	}
	
	public NeighborSet female() {
		return matchConnectors(Arrays.asList(1,3,5,7));
	}
	
	public NeighborSet north() {
		return matchConnectors(Arrays.asList(0,1,2,3));
	}
	
	public NeighborSet south() {
		return matchConnectors(Arrays.asList(4,5,6,7));
	}
	
	public NeighborSet west() {
		return matchConnectors(Arrays.asList(0,1,4,5));
	}
	
	public NeighborSet east() {
		return matchConnectors(Arrays.asList(2,3,6,7));
	}

	public Set<Module> modules() {
		return connectors.keySet();
	}

	public void deleteUnconnectedOnes() {
		for (byte c : modules.keySet()) {
			if (!ctrl.isConnected(c)) {				
				delete(c);
			}
		}
		
	}
	
	public void updateSymmetryNS () {
		for (Map.Entry<Module, Byte[]> entry : nbs().entrySet()) {
			assoc(entry.getKey(), (entry.getValue()[0] + 4) % 8, entry.getValue()[1]);
		}
	}
	
	
	public void updateSymmetryEW (boolean south) {
		for (Map.Entry<Module, Byte[]> entry : nbs().entrySet()) {
			if (entry.getValue()[0] < 4 && !south) {
				assoc(entry.getKey(), (entry.getValue()[0] + 2) % 4, entry.getValue()[1]);
			}
			else if (entry.getValue()[0] >= 4 && south) { 
				assoc(entry.getKey(), ((entry.getValue()[0] + 2) % 4) + 4, entry.getValue()[1]);
			}
		}
	}

	public NeighborSet in(IModuleHolder g) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<Module, Byte []> e : entrySet()) {
			if (g.contains(e.getKey())) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	public NeighborSet usingConnector(int connector) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<Module, Byte []> e : entrySet()) {
			if (e.getValue()[1] == connector) {
				ret.assoc(e);
			}
		}
		return ret;
	}

	public NeighborSet filter(int part) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<Module, Byte []> e : entrySet()) {
			for (int i=0; i<8; i++) {
				if ((part&ctrl.pow2(i))==ctrl.pow2(i) && e.getValue()[0] == i) {
					ret.assoc(e);
				}
			}
		}
		return ret;
	}
	
}






//
//
//abstract class Filter {
//	public abstract boolean apply (Map.Entry<Module, Byte[]> entry);
//}
//
//class Male extends Filter{
//	public boolean apply (Map.Entry<Module, Byte[]> entry) {
//		return Connector.isMALE(entry.getValue()[0]);
//	}
//}
//
//class Female extends Filter{
//	public boolean apply (Map.Entry<Module, Byte[]> entry) {
//		return Connector.isFEMALE(entry.getValue()[0]);
//	}
//}
//
//class North extends Filter{
//	public boolean apply (Map.Entry<Module, Byte[]> entry) {
//		return Connector.isNORTH(entry.getValue()[0]);
//	}
//}
//
//class InGroup extends Filter {
//	private Grouping grouping;
//	
//	public InGroup (Grouping g) {
//		grouping = g;
//	}
//	
//	public boolean apply (Map.Entry<Module, Byte[]> entry) {
//		return entry.getKey().belongsTo(grouping);
//	}
//}

