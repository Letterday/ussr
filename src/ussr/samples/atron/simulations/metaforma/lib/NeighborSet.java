package ussr.samples.atron.simulations.metaforma.lib;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import ussr.samples.atron.simulations.metaforma.gen.Grouping;
import ussr.samples.atron.simulations.metaforma.gen.Module;


public class NeighborSet  {
	private ConcurrentHashMap<IModule, Byte[]> connectors;// = new HashMap<Module, Byte[]>();
	private ConcurrentHashMap<Byte, IModule> modules;// = new HashMap<Byte, Module>();
	
	private MetaformaRuntime ctrl;

	
	public NeighborSet (MetaformaRuntime c) {
		ctrl = c;
		connectors = new ConcurrentHashMap<IModule, Byte[]>();
		modules = new ConcurrentHashMap<Byte, IModule>();
	}
	
	public NeighborSet (NeighborSet nbs,MetaformaRuntime c) {
		connectors = new ConcurrentHashMap<IModule, Byte[]>(nbs.getConnectors());
		modules = new ConcurrentHashMap<Byte, IModule>(nbs.getModules());
//		connectors = nbs.getConnectors();
//		modules = nbs.getModules();
		ctrl = c;
	}
	
	public ConcurrentHashMap<Byte, IModule>  getModules() {
		return modules;
	}

	public ConcurrentHashMap<IModule, Byte[]>  getConnectors() {
		return connectors;
	}

	public NeighborSet nbs() {
		return new NeighborSet(this, ctrl); // TODO:Is this correct?
	}
	
	public void assoc (Map.Entry<IModule, Byte[]> e) {
		assoc(e.getKey(),e.getValue()[0],e.getValue()[1],e.getValue()[2],e.getValue()[3],e.getValue()[4]);
	}
	
	public void add (IModule nb, int conToNb, int conFromNb, IRole moduleRole, int metaId, int metaBossId) { 
		if (getConnectorNrTo(nb) != conToNb || getConnectorNrFrom(nb) != conFromNb || getModuleRole(nb) != moduleRole.index() || getMetaId(nb) != metaId || getMetaBossId(nb) != metaBossId) {
//			ctrl.getVisual().print(".addNeighbor " + nb + " [" + conToNb + "," + conFromNb + "," + moduleRole + "," + metaId + "," + metaBossId + "] (" + nb + "=" + conToNb + "!= " +getConnectorNrTo(nb)+")");
			assoc (nb,conToNb,conFromNb, moduleRole.index(),metaId,metaBossId);
		}
	}
	
	private void assoc (IModule nb, int conToNb, int conFromNb, int moduleRole, int metaId, int metaBossId) {
		if (!getModuleByConnector(conToNb).equals(nb)) {
			delete(nb);
			delete(conToNb);
		}
		connectors.put(nb, new Byte[]{(byte)conToNb,(byte)conFromNb, (byte)moduleRole, (byte)metaId, (byte)metaBossId});
		modules.put((byte)conToNb, nb);
	}
	
	private Byte[] getModuleInfo (IModule mod) {
		if (connectors.containsKey(mod)) {
			return connectors.get(mod);
		}
		else {
			return new Byte[]{-1,-1,-1,-1,-1};
		}		
	}
	
	public byte getConnectorNrTo(IModule mod) {
		return getModuleInfo(mod)[0];
	}
	
	public byte getConnectorNrFrom (IModule mod) {
		return getModuleInfo(mod)[1];
	}
	
	public byte getModuleRole (IModule mod) {
		return getModuleInfo(mod)[2];
	}
	
	public byte getMetaId (IModule mod) {
		return getModuleInfo(mod)[3];
	}
	
	public byte getMetaBossId (IModule mod) {
		return getModuleInfo(mod)[4];
	}
	
	
	public IModule getModuleByConnector (int con) {
		if (modules.containsKey((byte)con)) {
			return modules.get((byte)con);
		}
		else {
			return new Module();
		}		
	}
	
	public byte getMetaIdByConnector (int con) {
		return getMetaId(getModuleByConnector(con));		
	}
	
	public byte getMetaBossIdByConnector (int con) {
		return getMetaBossId(getModuleByConnector(con));		
	}
	
	
	public boolean exists () {
		return !modules.isEmpty();
	}
	

	public Set<Map.Entry<IModule,Byte[]>> entrySet() {
		return connectors.entrySet();
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}

	public int size () {
		if (modules.size() != connectors.size()) {
			System.err.println ("modules.size(" + modules.size() + ") != connectors.size(" + connectors.size() + ") --> this should not happen! -- " + connectors);
		}
		return modules.size();
	}
	
	public String toString() {
		String r = "neighbors:  \n";
		for (Map.Entry<IModule, Byte[]> e : connectors.entrySet()) {
			String m = (ctrl.getContext().isConnConnected(e.getValue()[0])?e.getKey().toString().toUpperCase(): e.getKey()) + " ("+ String.format("%7s",ctrl.moduleRoleGet().fromByte(e.getValue()[2])) + ", "+ String.format("%2d",e.getValue()[3]) + ","+ String.format("%2d",e.getValue()[4]) + ") [" + e.getValue()[0] + ", "+ e.getValue()[1] + "], ";
			r += m + "\n";
		}
		r =  r.substring(0, r.length() - 2) + "\n";
		return r;
	}
	
	public void delete (IModule nb) {
		modules.remove(getConnectorNrTo(nb));
		connectors.remove(nb);
	}
	
	public void delete (int connector) {
		//ctrl.notification("remove " + getModuleByConnector(connector));
		connectors.remove(getModuleByConnector(connector));
		modules.remove((byte)connector);
	}
	
	public boolean contains(IModule m) {
		return connectors.containsKey(m);
	}
	
	
	public NeighborSet nbsConnected() {
		return nbsIsConnected(true);
	}

	public NeighborSet nbsDisconnected() {
		return nbsIsConnected(false);
	}

	public NeighborSet nbsIsConnected(boolean connected) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			if (ctrl.getContext().isConnConnected(e.getValue()[0]) == connected) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	public NeighborSet nbsIsModRole(IRole p) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			if (e.getValue()[2] == p.index()) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	public NeighborSet nbsInMetaGoup() {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			if (e.getValue()[4] == ctrl.metaBossIdGet()) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
//	public NeighborSet maleAlignedWithFemale () {
//		NeighborSet ret = new NeighborSet(this.ctrl);
//		for (Map.Entry<IModule, Byte []> e : entrySet()) {
//			if (e.getValue()[0]%2 == 0 && e.getValue()[1]%2 == 1) {
//				ret.assoc(e);
//			}
//		}
//		return ret;
//	}
	
//	public NeighborSet genderDismatch () {
//		NeighborSet ret = new NeighborSet(this.ctrl);
//		for (Map.Entry<IModule, Byte []> e : entrySet()) {
//			if (e.getValue()[0]%2 == e.getValue()[1]%2) {
//				ret.assoc(e);
//			}
//		}
//		return ret;
//	} 
	

	public NeighborSet nbsOnGroup (Grouping g) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			if (e.getKey().getGrouping().equals(g)) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	
	public Set<IModule> modules() {
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
		for (Map.Entry<IModule, Byte[]> entry : nbs().entrySet()) {
			assoc(entry.getKey(), (entry.getValue()[0] + 4) % 8, entry.getValue()[1],entry.getValue()[2],entry.getValue()[3],entry.getValue()[4]);
		}
	}
	
	
	public void updateSymmetryEW (boolean south) {
		for (Map.Entry<IModule, Byte[]> entry : nbs().entrySet()) {
			if (entry.getValue()[0] < 4 && !south) {
				assoc(entry.getKey(), (entry.getValue()[0] + 2) % 4, entry.getValue()[1],entry.getValue()[2],entry.getValue()[3],entry.getValue()[4]);
			}
			else if (entry.getValue()[0] >= 4 && south) { 
				assoc(entry.getKey(), ((entry.getValue()[0] + 2) % 4) + 4, entry.getValue()[1],entry.getValue()[2],entry.getValue()[3],entry.getValue()[4]);
			}
		}
	}

	public NeighborSet nbsIn(IModuleHolder g) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			if (g.contains(e.getKey())) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	public NeighborSet nbsUsingConnector(int connector) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			if (e.getValue()[1] == connector) {
				ret.assoc(e);
			}
		}
		return ret;
	}

	public NeighborSet nbsFilterConn(int part) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			for (int i=0; i<8; i++) {
				if ((part&ctrl.pow2(i))==ctrl.pow2(i) && e.getValue()[0] == i) {
					ret.assoc(e);
				}
			}
		}
		return ret;
	}
	
	public NeighborSet nbsFilterConnDest(int part) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			for (int i=0; i<8; i++) {
				if ((part&ctrl.pow2(i))==ctrl.pow2(i) && e.getValue()[1] == i) {
					ret.assoc(e);
				}
			}
		}
		return ret;
	}

	
}

