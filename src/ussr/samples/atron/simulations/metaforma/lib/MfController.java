package ussr.samples.atron.simulations.metaforma.lib;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


import ussr.model.debugging.ControllerInformationProvider;
import ussr.model.debugging.DebugInformationProvider;
import ussr.samples.atron.simulations.metaforma.lib.Packet;
import ussr.samples.atron.simulations.metaforma.lib.NeighborSet;



public abstract class MfController extends MfApi implements ControllerInformationProvider{

	/*
	 25..49 VarMeta
	 50..74 VarMetaCore
	 75..99 VarMetaGroup
	 100..125 VarMetaGroupCore 
	 
	 */
	
	public enum VarLocalStateCore implements IVar {
		NONE, fixedYet;

		public byte index() {
			return (byte) (ordinal());
		}

		public VarLocalStateCore fromByte(byte b) {
			return values()[b];
		}
		
		public boolean isLocal() {return false;	}
		public boolean isLocalState() {return true;	}
		public boolean isMeta() {return false;	}
		public boolean isMetaRegion() {return false;	}
	}
	
	public enum VarMetaCore implements IVar {
		NONE, Completed,BossId;

		public byte index() {
			return (byte) (ordinal()+50);
		}

		public VarMetaCore fromByte(byte b) {
			return values()[b-50];
		}
		
		public boolean isLocal() {return false;	}
		public boolean isLocalState() {return false;	}
		public boolean isMeta() {return true;	}
		public boolean isMetaRegion() {return false;	}
	}
	
	public enum VarMetaGroupCore implements IVar {
		NONE, GroupSize;

		public byte index() {
			return (byte) (ordinal()+100);
		}

		public VarMetaGroupCore fromByte(byte b) {
			return values()[b-100];
		}
		
		public boolean isLocal() {return false;	}
		public boolean isLocalState() {return false;	}
		public boolean isMeta() {return false;	}
		public boolean isMetaRegion() {return true;	}
	}
	
	protected DebugInformationProvider info;
	protected MfContext context = new MfContext(this);
	protected MfVisualizer visual = new MfVisualizer(this);
	protected MfScheduler scheduler = new MfScheduler(this);	
	protected MfStateManager stateMngr = new MfStateManager(this);
	private State stateRec = new State();
	private BigInteger consensusRec = BigInteger.ZERO;
	
	private HashMap<IVar,Byte> vars = new HashMap<IVar,Byte>();
	private HashMap<IVar,Byte> varsSequenceNrs = new HashMap<IVar,Byte>();

	
	private IModule previousName;
		
	private HashMap<String,Float> doRepeat = new HashMap<String,Float>();

	
	private byte metaId = 0;
	private IRole moduleRole = null;
	
	
	
	
	public byte varGet(IVar name) {	
		if (vars.containsKey(name)) {
			return vars.get(name);
		}
		else {
			return 0;
		}
	}
	
	public Set<IVar> varGetAll() {	
		return vars.keySet();
	}
	
	protected boolean varSet(IVar id, int val) {
		byte value = min(Byte.MAX_VALUE,val);

		boolean changed = (varGet(id) != value);
		if (changed) {
			visual.print(".varSet " + id + ", " + val);
			vars.put(id, value);
		}
				
		if (!id.isLocal()) {
			if (changed) {
				varsSequenceNrs.put(id,(byte)(varGetSequenceNr(id)+1));
				broadcastMetaVar(id);
			}
		}
		return changed;
	}
	
	protected byte varGetSequenceNr(IVar id) {
		if (!varsSequenceNrs.containsKey(id)) {
			varsSequenceNrs.put(id, (byte)0); 
		}
		return varsSequenceNrs.get(id);
	}
		
		
	public boolean freqLimit (String key,float interval) {
		if (!doRepeat.containsKey(key) || time() - doRepeat.get(key)  > interval/1000) {
			doRepeat.put(key, time()); 
			return true;
		}
		else {
			return false;
		}
	}
	
	 
	 protected void connection(int c, boolean connect) {
		 if (connect && !context.isConnConnected(c)) {
			 super.connect(context.abs2rel(c)); 
		 }
		 else if (!connect && context.isConnConnected(c)) {
			 super.disconnect(context.abs2rel(c));
		 }
	}

	
	public void waitAndDiscover () {
		scheduler.invokeIfScheduled ("broadcastDiscover");
		
		delay(500);
		yield();
	}
		

	public void activate() {
		setup();
		 info = info();
		 setCommFailureRisk(0.25f,0.25f,0.98f,0.125f);
		PacketBase.setController(this);
		State.setController(this);
		
		init();
		
		delay(500);
		 // All threads and controllers need to be ready before proceeding!
		 
		scheduler.setInterval("broadcastDiscover",1000);
		scheduler.setInterval("broadcastConsensus",4000);
		scheduler.setInterval("broadcastMetaVars",5000);
		
//		scheduler.setInterval("broadcastSymmetry",2000);
			
		
		
		for (int i=0; i<5; i++) {
			discoverNeighbors();
		}
		
		while (true) {
			// To make sure it will remain at correct pos
			rotateToDegreeInDegrees(angle);
			
			scheduler.sync();
			
			handleStates();
			
			yield();
			
			stateMngr.merge(stateRec,consensusRec);

			
			if (freqLimit("colorize",500)) {		
				visual.colorize();
			}
			
		}
	}
	
	public void handleMessage(byte[] message, int messageLength, int connectorNr) {
		// Translate absolute connector to relative connector (symmetry feature)
		byte connector = context.abs2rel(connectorNr);
		
		PacketBase base = PacketBase.fromBytes(message); 
		
		
		try {
			if (base instanceof MetaPacket) {
				MetaPacket p = (MetaPacket)base;
				if (p.getDest() == metaId) {
					visual.print(p,".RECEIVE " + p);
				}
				
				if (p.getSource() == 0 || p.getDest() == 0) {
					visual.error("Meta packet with source=0 or dest=0!");
				}
				
				if (!p.isDying() ) {
					broadcast(p);
				}
				
				
				
				if (p.getDest() == metaIdGet()) {
					if (p.getType() == MetaPacketCoreType.SET_BOSS&& (!metaBossIdExists() || metaBossIdGet() == p.getSource())) {// && !stateMngr.committed() 
						// the origin will become the boss
						varSet(VarMetaCore.BossId, p.getSource());
						varSet(VarMetaGroupCore.GroupSize, p.getData()[0]);
//						stateOperationCntrRec = 0;
//						stateOperationCounter = 0;
						
						stateMngr.commit("BOSS ID received");
					}
					else {
						receiveMetaMessage (p.getType(),p.getSource(),p.getDest(),p.getData());
					}
					 
				}
			}
			else {
				Packet p = (Packet) base;
				
				if (isFEMALE(connector)) {
					context.setFemaleConnected(connector,p.getConnectorConnected());
				}
				context.addNeighbor(p.getSource(), connector, p.getSourceConnector(),p.getModRole(),p.getMetaSourceId(),p.getMetaBossId());
				
				if (p.getMetaSourceId() != metaIdGet() && metaIdExists() && p.getMetaSourceId() != 0) {
					// During a sequence we should not add meta neighbors as the connector numbers can be swapped! So only do this at DEFAULT operation state
					if (metaNeighborHookAllow()) {
						byte metaSourceId = p.getMetaSourceId() == 0 ? -1 : p.getMetaSourceId();
						
						if (metaNeighborHook (connector,metaSourceId)) {
							visual.print(".metaNeighborHook " + connector + "," + p.getMetaSourceId() );
						}
					}
				}
				
				// TODO: Is this correct??
				if (!(metaIdExists() && (metaIdGet() == p.getMetaSourceId() || (metaBossIdExists() && metaBossIdGet() == p.getMetaBossId()))) && p.getType() != PacketCoreType.META_ID_SET) {
					
					if (p.getType() != PacketCoreType.DISCOVER) {
						visual.print("!!! Packet dropped from outside region (" + metaIdGet() + "," + metaBossIdGet() + ")  : " +p);
					}
					return;
				}
	
				if (stateRec.isNewer(p.getState())){
					stateRec.merge(p.getState());
					consensusRec = BigInteger.ZERO;
				}
				
				if (!(stateMngr.at(p.getState())) && p.getType() != PacketCoreType.META_VAR_SYNC && p.getType() != PacketCoreType.GRADIENT && p.getType() != PacketCoreType.DISCOVER) {
					visual.print("!!! Packet dropped due to state mismatch: " + p.toString());
					return;
				}
				
				if (p.getSource().equals(getId())) {
					try {
						throw new Error("Source cannot be myself (" + getId() + ")!");
					}
					catch (Error e) {
						e.printStackTrace();
					}
					
				}
			
				visual.print(p,".receive on " + connector + ": " + p);

				
				if (p.getType() == PacketCoreType.META_VAR_SYNC) {
					if (p.getData().length % 3 != 0 || p.getData().length < 3) {
						visual.error("Groups of 3 bytes required and at least 1 group! " + p.getData().length);
					}
					
					for (int i=0; i<p.getData().length; i=i+3) {
						IVar v = varInitFromBytes(p.getData()[i+0]);
						 if (v.isMeta() && p.getMetaSourceId() == metaIdGet() || v.isMetaRegion() ) {
							if (varGetSequenceNr(v) < p.getData()[i+2] || varGetSequenceNr(v) == p.getData()[i+2] && varGet(v) == 0 && p.getData()[1] != 0) {
								varSet(v,p.getData()[i+1]);
								if (v.equals(VarMetaCore.BossId)){ //TODO: Why??? || p.getData()[i+0] == 0) {
									stateMngr.commit("BOSS ID received through meta sync");
								}
							}
						 }
					}					
				}
				else if (p.getType() == PacketCoreType.CONSENSUS) {				
					if (!(p.getDataAsInteger().or(consensusRec)).equals(consensusRec)) {
						if (stateRec.equals(p.getState())) {
							consensusRec = consensusRec.or(p.getDataAsInteger());
						}
						else {
							consensusRec = p.getDataAsInteger();
							stateRec = p.getState();
						}
						
					}
				}
				else {
					// Custom message 
					receiveMessage (p.getType(),p.getState(),p.getDir() == Dir.REQ,p.getSourceConnector(),connector,p.getMetaBossId(),p.getData());
				}

			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(base);
		}
	}
	
	
	public float time() {
		return getModule().getSimulation().getTime();
	}


	
	public void discoverNeighbors () {
		scheduler.invokeNow("broadcastDiscover");
		delay(300);
	}

	

	public NeighborSet nbs(int connectors) {
		return context.nbs().nbsFilterConn(connectors);
	}
	
	public NeighborSet nbs(IGroupEnum g) {
		return context.nbs().nbsOnGroup(g);
	}
	
	public NeighborSet nbs(int connectors, IRole r) {
		return context.nbs().nbsFilterConn(connectors).nbsIsModRole(r);
	}
	
	public NeighborSet nbs(int connectors, IGroupEnum g) {
		return context.nbs().nbsFilterConn(connectors).nbsOnGroup(g);
	}
	
	public NeighborSet nbs() {
		return context.nbs();
	}
	

	public void delay(int ms) {
		float stopTime = module.getSimulation().getTime() + ms / 1000f;
		
		while (stopTime > module.getSimulation().getTime()) {
			yield();
		}

	}

	public Module getId() {
		return Module.value(getName());
	}
	
	public IGroupEnum getGrouping() {
		return getId().getGrouping();
	}


	public void broadcastConsensus() {
		if (!stateMngr.getConsensus().equals(BigInteger.ZERO) && metaIdExists()) {
			debugForceMetaId();
			broadcast(new Packet(getId()).setType(PacketCoreType.CONSENSUS).setData(stateMngr.getConsensus()));
		}
	}

	public void debugForceMetaId() {
		if (!metaIdExists()) {
			try {
				throw new Error("Consensus may only be used when having meta ID!");
			}
			catch (Error e) {
				e.printStackTrace();
			}
		}
	}

	public void broadcastDiscover () {
		broadcast(new Packet(getId()));
	}
	
	
	
	public void broadcastMetaVars () {
		ArrayList<IVar> ids = new ArrayList<IVar>();
		for (IVar v:vars.keySet()) {
			if (!v.isLocal() && metaIdExists() && v.isMeta() || metaBossIdExists() && v.isMetaRegion())  {
				ids.add(v);
			}
		}
		if (ids.size() > 0) {
			broadcastMetaVar(ids);
		}
	}
	
	public void broadcastMetaVar (IVar id) {
		ArrayList<IVar> l = new ArrayList<IVar>();
		l.add(id);
		broadcastMetaVar(l);
	}
	
	public void broadcastMetaVar (ArrayList<IVar> ids) {
		byte[] data = new byte[ids.size() *3];
		
		for (int i=0; i<ids.size(); i++) {	
			data[i*3+0] = ids.get(i).index();
			data[i*3+1] = varGet(ids.get(i));
			data[i*3+2] = varGetSequenceNr(ids.get(i));
		}
		visual.print("META VAR SYNC " + ids);

		broadcast(new Packet(getId()).setType(PacketCoreType.META_VAR_SYNC).setData(data));
	}
	

	protected abstract void receiveMessage (IPacketType type, State state, boolean isReq, byte sourceCon, byte destCon, byte sourceMetaId, byte[] data);
	protected abstract void receiveMetaMessage (IPacketType type, byte source, byte dest, byte[] data); 
	protected abstract boolean metaNeighborHookAllow();
	public abstract void handleStates();
	public abstract void init();

	public DebugInformationProvider info() {
		if (info == null) {
			info = this.getModule().getDebugInformationProvider();
		}
		return info;
	}
	
	
	
	public void renameTo(IModule to) {
//		previousName = getId();
		setId(to);
		scheduler.invokeNow("broadcastDiscover");
	}
	
	public void renameGroup(IGroupEnum to) {
		visual.print("rename group");
		setId(getId().swapGrouping(to));
		scheduler.invokeNow("broadcastDiscover");
	}
	
	private void setId(IModule id) {
		visual.print("$$$ rename " + getId() + " to " + id);
		getModule().setProperty("name", id.toString());
		visual.colorize();
	}
	
	public void renameStore () {
		previousName = getId();
	}
	
	public void renameRestore () {
		visual.print("$$$ restore name " + getId() + " to " + previousName);
		getModule().setProperty("name", previousName.toString());
		visual.colorize();
		scheduler.invokeNow("broadcastDiscover");
	}

	protected void moduleRoleSet(IRole r) {
		visual.print(".setModuleRole " + r);
		moduleRole = r;
		
	}
	

	public boolean isLocked() {
		return varGet(VarMetaCore.BossId) != 0;
	}

	
	protected void metaIdSet (int v) {
		visual.print(".metaIdSet " + v);
		metaId = (byte)v;
		
	}
	 
	
	protected boolean metaIdExists () {
		return metaId != 0;
	}
	
	protected boolean moduleRoleExists () {
		return moduleRole.index() > 0;
		// The first module role is NONE with index 0
	}
	
	public byte metaIdGet() {
		return metaId;
	}
	
	
	public void metaBossIdReset () {
		varSet(VarMetaCore.BossId, 0);
	}
	
	public byte metaBossIdGet() {
		return varGet(VarMetaCore.BossId);
	}
	
	public boolean metaBossIdExists() {
		return varGet(VarMetaCore.BossId) != 0;
	}
	
	
	public boolean metaBossMyself() {
		return metaIdGet() == metaBossIdGet();
	}

	
	public IRole moduleRoleGet () {
		return moduleRole;
	}
	
	public void metaRegionRelease(boolean metaAlso) {
		visual.print(".metaBossIdUnlock ");

		if (metaAlso) {
			metaIdSet(0);
		}
		
		metaBossIdReset();
		
		for (IVar v:vars.keySet()) {

			if (v.isMeta() && !v.equals(VarMetaCore.Completed))
				varSet(v,0);
			
			if (v.isMetaRegion())
				varSet(v,0);
		}
	}
	


	public void metaBossIdSetTo(byte[] metaIds) {
		visual.print(".setMetaBossId " + metaId);
		
		varSet(VarMetaCore.BossId, metaIdGet());
		varSet(VarMetaGroupCore.GroupSize, moduleRoleGet().size() * (metaIds.length+1));
		
		for (byte metaId : metaIds) {
			if (metaId == 0) {
				visual.error("Creating region with meta ID 0!!");
			}
			// We need to include the group size in the message
			send(MetaPacketCoreType.SET_BOSS,metaId, new byte[]{varGet(VarMetaGroupCore.GroupSize)});
		}
	}
	
	
	public IVar varInitFromBytes (byte index) {
		IVar v = null;
		if (index >= 25 && index < 50) {
			v = varFromByteMeta(index);
		}
		else if (index < 75) {
			v = VarMetaCore.NONE.fromByte(index);
		}
		else if (index < 100) {
			v = varFromByteMetaGroup(index);
		}
		else if (index < 125) {
			v = VarMetaGroupCore.NONE.fromByte(index);
		}
		else {
			throw new Error("Wrong var index!");
		}
		return v;
		
	}
	
	public abstract boolean metaNeighborHook(int connectorNr, byte metaId);
	public abstract void addNeighborhood(StringBuffer o);
	public abstract IPacketType getMetaPacketType (int index);	

	private void broadcast (MetaPacket p) {

		p.decreaseTTL();

		p.setLastHop(metaIdGet());
		
		if (!p.isDying()) {
			visual.print(p,".BROADCAST " + p);
			for (byte c = 0; c < 8; c++) {
				if (nbs().getMetaIdByConnector(c) == p.getDest() || nbs().getMetaBossIdByConnector(c) == p.getSource()) {
					visual.print(p,".SEND (BC) to "  + nbs().getModuleByConnector(c) + " " + p);
					sendMessage(p.getBytes(), (byte) p.getBytes().length, context.abs2rel(c));
				}
			}
		}
		else {
//			visual.print("DIE " + p);
		}
	}
	

	protected void send(IPacketType type, byte dest, byte[] data) {
		broadcast(new MetaPacket(metaIdGet(),dest).setType(type).setData(data));
	}
	
	public void broadcast (PacketCoreType t, boolean isAck, byte[] data) {
		Dir d = isAck ? Dir.ACK : Dir.REQ;
		broadcast (new Packet(getId()).setType(t).setDir(d).setData(data));
	}
	
	public void unicast (int connectors, PacketCoreType t, boolean isAck) {
		unicast(connectors, t, isAck,new byte[]{});
	}
	
	public void broadcast (Packet p) {	
		broadcast (p,-1);
	}
	
	
		
	
	
	public void unicast (int connectors, PacketCoreType t, boolean isAck, byte[] data) {
		Dir d = isAck ? Dir.ACK : Dir.REQ;
		unicast (new Packet(getId()).setType(t).setDir(d).setData(data),connectors);
	}
	
	private void addToPacket (Packet p) {
		p.addState(stateMngr.getState());
		p.setMetaBossId(metaBossIdGet());
		if (varGet(VarMetaCore.Completed) == 1) {
			p.setMetaSourceId(metaIdGet());
		}
//		else {
//			// -1 means that a meta module is present but not fully initialised with a meta id yet
//			p.setMetaSourceId(-1);
//		}
		p.setSource(getId());
		p.setModRole(moduleRoleGet());
	}
	
	private void broadcast(Packet p,int exceptConnector) {
		addToPacket(p);
		
		visual.print(p,".broadcast (" + p.toString() + ") ");

		for (byte c = 0; c < 8; c++) {
			if (c != exceptConnector) {
				send(p, c);
			}
		}
	}
	
	private void unicast (Packet p, int connectors) {
		addToPacket(p);
		visual.print(p,".unicast packet " + "(" + p.toString() + ") ");
		for (byte i=0; i<8; i++) {
			if ((connectors&pow2(i))==pow2(i)) {
				p.setSourceConnector(i);
				send(p,i);
				visual.print(".unicast to " + i);
			}
		}
	}
	
	private void send(Packet p, int connector) {
		p.setSourceConnector((byte) connector);
		addToPacket(p);
		
		if (connector < 0 || connector > 7) {
			System.err.println("Connector has invalid nr " + connector);
		}
		if (isMALE(connector)) {
			p.setConnectorConnected(context.isConnConnected(connector));
		}		
		
		sendMessage(p.getBytes(), (byte) p.getBytes().length, context.abs2rel(connector));
	}
	
	public void metaSetCompleted () {
		varSet(VarMetaCore.Completed, 1);
	}
	
	public boolean metaGetCompleted () {
		return varGet(VarMetaCore.Completed) == 1;
	}

	public MfContext getContext() {
		return context;
	}

	public MfVisualizer getVisual() {
		return visual;
	}
	
	public MfStateManager getStateMngr () {
		return stateMngr;
	}
	
	public State getStateReceived () {
		return stateRec;
	}

	public MfScheduler getScheduler() {
		return scheduler;
	}

	public MfVisualizer getVisualizer() {
		return visual;
	}

	public HashMap<String,Float> getDoRepeat() {
		return doRepeat;
	}

	public HashMap<IVar,Byte> getVars() {
		return vars;
	}

	public HashMap<IVar,Byte> getVarSequenceNrs() {
		return varsSequenceNrs;
	}
	
	public String getModuleInformation () {
		return visual.getModuleInformation();
	}
	
	public String getTitle() {
		return visual.getTitle();
	}
	
	public abstract IVar varFromByteLocal (byte index);
	
	public abstract IVar varFromByteMeta (byte index);
	
	public abstract IVar varFromByteMetaGroup (byte index);
	
	
	public void pause() {
		getModule().getSimulation().setPause(true);
	}

	public void prepareNextState() {
		// Instead of throwing all NB's away, only remove the unconnected ones
		context.deleteUnconnectedNeighbors();
		doRepeat.clear();
		visual.colorize();
	}
}
