package ussr.samples.atron.simulations.metaforma.lib;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import ussr.samples.atron.simulations.metaforma.gen.Grouping;
import ussr.samples.atron.simulations.metaforma.gen.Module;


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
		assoc(e.getKey(),e.getValue()[0],e.getValue()[1],e.getValue()[2],e.getValue()[3]);
	}
	
	public void add (Module nb, int conToNb, int conFromNb, IRole moduleRole, int metaId) { 
		if (getConnectorNrTo(nb) != conToNb || getConnectorNrFrom(nb) != conFromNb || getModuleRole(nb) != moduleRole.index() || getMetaId(nb) != metaId) {
			ctrl.getVisual().print(".addNeighbor " + nb + " [" + conToNb + "," + conFromNb + "," + moduleRole + "," + metaId + "] (" + nb + "=" + conToNb + "!= " +getConnectorNrTo(nb)+")");
			assoc (nb,conToNb,conFromNb, moduleRole.index(),metaId);
		}
	}
	
	private void assoc (Module nb, int conToNb, int conFromNb, int moduleRole, int metaId) {
		if (!getModuleByConnector(conToNb).equals(nb)) {
			delete(nb);
			delete(conToNb);
		}
		connectors.put(nb, new Byte[]{(byte)conToNb,(byte)conFromNb, (byte)moduleRole, (byte)metaId});
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
	
	public byte getModuleRole (Module mod) {
		if (connectors.containsKey(mod)) {
			return connectors.get(mod)[2];
		}
		else {
			return -1;
		}		
	}
	
	public byte getMetaId (Module mod) {
		if (connectors.containsKey(mod)) {
			return connectors.get(mod)[3];
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
		String r = "  ";
		for (Map.Entry<Module, Byte[]> e : connectors.entrySet()) {
			String m = e.getKey() + " ("+ ctrl.moduleRoleGet().fromByte(e.getValue()[2]) + ", "+ e.getValue()[3] + ") [" + e.getValue()[0] + ", "+ e.getValue()[1] + "], ";
			if (ctrl.getContext().isConnConnected(e.getValue()[0])) {
					m = m.toUpperCase();
				}
				r += m;
			}
		r =  r.substring(0, r.length() - 2) + "\n" + modules.toString() + "\n";
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
			if (ctrl.getContext().isConnConnected(e.getValue()[0]) == connected) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	public NeighborSet isModRole(IRole p) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<Module, Byte []> e : entrySet()) {
			if (e.getValue()[2] == p.index()) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	public NeighborSet inMetaGoup() {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<Module, Byte []> e : entrySet()) {
			if (e.getValue()[3] == ctrl.metaBossIdGet()) {
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
	
	
	public Set<Module> modules() {
		return connectors.keySet();
	}

	public void deleteUnconnectedOnes() {
		for (byte c : modules.keySet()) {
			if (!ctrl.getContext().isConnConnected(c)) {				
				delete(c);
			}
		}
		
	}
	
	public void updateSymmetryNS () {
		for (Map.Entry<Module, Byte[]> entry : nbs().entrySet()) {
			assoc(entry.getKey(), (entry.getValue()[0] + 4) % 8, entry.getValue()[1],entry.getValue()[2],entry.getValue()[3]);
		}
	}
	
	
	public void updateSymmetryEW (boolean south) {
		for (Map.Entry<Module, Byte[]> entry : nbs().entrySet()) {
			if (entry.getValue()[0] < 4 && !south) {
				assoc(entry.getKey(), (entry.getValue()[0] + 2) % 4, entry.getValue()[1],entry.getValue()[2],entry.getValue()[3]);
			}
			else if (entry.getValue()[0] >= 4 && south) { 
				assoc(entry.getKey(), ((entry.getValue()[0] + 2) % 4) + 4, entry.getValue()[1],entry.getValue()[2],entry.getValue()[3]);
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

