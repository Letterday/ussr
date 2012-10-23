package ussr.samples.atron.simulations.metaforma.lib;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class NeighborSet  {
	private ConcurrentHashMap<IModule, Byte[]> connectors;// = new HashMap<Module, Byte[]>();
	private ConcurrentHashMap<Byte, IModule> modules;// = new HashMap<Byte, Module>();
	
	private MfController ctrl;
	private final byte CON_SRC = 0;
	private final byte CON_DEST = 1;
	private final byte NR_META_PART = 2;
	private final byte ID_META = 3;
	private final byte ID_REGION = 4;
	
	public NeighborSet (MfController c) {
		ctrl = c;
		connectors = new ConcurrentHashMap<IModule, Byte[]>();
		modules = new ConcurrentHashMap<Byte, IModule>();
	}
	
	public NeighborSet (NeighborSet nbs,MfController c) {
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
		assoc(e.getKey(),e.getValue()[CON_SRC],e.getValue()[CON_DEST],e.getValue()[NR_META_PART],e.getValue()[ID_META],e.getValue()[ID_REGION]);
	}
	
	public void add (IModule nb, int conToNb, int conFromNb, IMetaPart metaPart, int metaID, int regionID) { 
		if (getConnectorNrTo(nb) != conToNb || getConnectorNrFrom(nb) != conFromNb || getModuleMetaPart(nb) != metaPart.index() || getMetaId(nb) != metaID || getRegionId(nb) != regionID) {
			ctrl.getVisual().print(".addNeighbor " + nb + " [" + conToNb + "," + conFromNb + "," + metaPart + "," + metaID + "," + regionID + "] (" + nb + "=" + conToNb + "!= " +getConnectorNrTo(nb)+")");
			assoc (nb,conToNb,conFromNb, metaPart.index(),metaID,regionID);
		}
	}
	
	private void assoc (IModule nb, int conToNb, int conFromNb, int metaPart, int metaId, int regionId) {
		if (!getModuleByConnector(conToNb).equals(nb)) {
			delete(nb);
			delete(conToNb);
		}
		connectors.put(nb, new Byte[]{(byte)conToNb,(byte)conFromNb, (byte)metaPart, (byte)metaId, (byte)regionId});
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
		return getModuleInfo(mod)[CON_SRC];
	}
	
	public byte getConnectorNrFrom (IModule mod) {
		return getModuleInfo(mod)[CON_DEST];
	}
	
	public byte getModuleMetaPart (IModule mod) {
		return getModuleInfo(mod)[NR_META_PART];
	}
	
	public byte getMetaId (IModule mod) {
		return getModuleInfo(mod)[ID_META];
	}
	
	public byte getRegionId (IModule mod) {
		return getModuleInfo(mod)[ID_REGION];
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
	
	public byte getRegionIdByConnector (int con) {
		return getRegionId(getModuleByConnector(con));		
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
	
	public boolean sizeEquals (int count,boolean inRegion) {
		return nbsInRegion(inRegion).size() == ctrl.nbs().nbsInRegion(inRegion).size() && nbsInRegion(inRegion).size() == count;
	}
	
	public String toString() {
		String r = "neighbors:  \n";
		for (Map.Entry<IModule, Byte[]> e : connectors.entrySet()) {
			String m = String.format("%7s (%5s,%2d,%2d) [%2d,%2d]", (ctrl.getContext().isConnConnected(e.getValue()[CON_SRC])?e.getKey().toString().toUpperCase(): e.getKey().toString().toLowerCase()), ctrl.getMetaPart().fromByte(e.getValue()[NR_META_PART]), e.getValue()[ID_META], e.getValue()[ID_REGION], e.getValue()[CON_SRC] ,e.getValue()[CON_DEST]);
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
	
	
	public Set<IModule> modules() {
		return connectors.keySet();
	}
	
	public byte connectors() {
		byte ret = 0;
		for (Byte b:modules.keySet()) {
			ret += MfController.pow2(b);
		}
		return ret;
	}

	public void deleteUnconnectedOnes() {
		for (byte c : modules.keySet()) {
			if (!ctrl.getContext().isConnConnected(c)) {				
				delete(c);
			}
		}
	}
	
	public void deleteFemaleOnes() {
		for (byte c : modules.keySet()) {
			if (MfApi.isFEMALE(c)) {				
				delete(c);
			}
		}
	}
	
	public void updateSymmetryNS () {
		for (Map.Entry<IModule, Byte[]> entry : nbs().entrySet()) {
			assoc(entry.getKey(), (entry.getValue()[CON_SRC] + 4) % 8, entry.getValue()[CON_DEST],entry.getValue()[NR_META_PART],entry.getValue()[ID_META],entry.getValue()[ID_REGION]);
		}
	}
	

	public void updateSymmetryEW (boolean south) {
		for (Map.Entry<IModule, Byte[]> entry : nbs().entrySet()) {
			if (entry.getValue()[CON_SRC] < 4 && !south) {
				assoc(entry.getKey(), (entry.getValue()[CON_SRC] + 2) % 4, entry.getValue()[CON_DEST],entry.getValue()[NR_META_PART],entry.getValue()[ID_META],entry.getValue()[ID_REGION]);
			}
			else if (entry.getValue()[CON_SRC] >= 4 && south) { 
				assoc(entry.getKey(), ((entry.getValue()[CON_SRC] + 2) % 4) + 4, entry.getValue()[CON_DEST],entry.getValue()[NR_META_PART],entry.getValue()[ID_META],entry.getValue()[ID_REGION]);
			}
		}
	}
	
	
	

	
	public NeighborSet nbsIsConnected(boolean connected) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			if (ctrl.getContext().isConnConnected(e.getValue()[CON_SRC]) == connected) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	public NeighborSet nbsIsMetaPart(IMetaPart p) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			if (e.getValue()[NR_META_PART] == p.index()) {
				ret.assoc(e);
			}
		}
		return ret;
	}
	
	public NeighborSet nbsInRegion(boolean inRegion) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			if (!inRegion || e.getValue()[ID_REGION] == ctrl.meta().regionID()) {
				ret.assoc(e);
			}
		}
		return ret;
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
	
	public NeighborSet nbsFilterConnSource(int part) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			for (int i=0; i<8; i++) {
				if ((part&MfController.pow2(i))==MfController.pow2(i) && e.getValue()[CON_SRC] == i) {
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
				if ((part&MfController.pow2(i))==MfController.pow2(i) && e.getValue()[CON_DEST] == i) {
					ret.assoc(e);
				}
			}
		}
		return ret;
	}

	public NeighborSet nbsWithMetaId(byte metaID) {
		NeighborSet ret = new NeighborSet(this.ctrl);
		for (Map.Entry<IModule, Byte []> e : entrySet()) {
			for (int i=0; i<8; i++) {
				if (e.getValue()[ID_META] == metaID) {
					ret.assoc(e);
				}
			}
		}
		return ret;
	}

	
}

