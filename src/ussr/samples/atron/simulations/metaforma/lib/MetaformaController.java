package ussr.samples.atron.simulations.metaforma.lib;

import java.awt.Color;
import java.math.BigInteger;
import java.util.HashMap;

import ussr.model.debugging.ControllerInformationProvider;
import ussr.model.debugging.DebugInformationProvider;
import ussr.samples.atron.simulations.metaforma.gen.*;
import ussr.samples.atron.simulations.metaforma.lib.Packet;
import ussr.samples.atron.simulations.metaforma.lib.NeighborSet;



public abstract class MetaformaController extends MetaformaApi implements ControllerInformationProvider{

	/*
	 0..24 VarLocal
	 25..49 VarMeta
	 50..74 VarMetaCore
	 75..99 VarMetaGroup
	 100..125 VarMetaGroupCore 
	 
	 */
	
	public enum VarMetaCore implements IVar {
		NONE, Completed,BossId;

		public byte index() {
			return (byte) (ordinal()+50);
		}

		public VarMetaCore fromByte(byte b) {
			return values()[b-50];
		}
		
		public boolean isLocal() {return false;	}
		public boolean isMeta() {return true;	}
		public boolean isMetaGroup() {return false;	}
	}
	
	public enum VarMetaGroupCore implements IVar {
		NONE, GroupSize, StateOperation;

		public byte index() {
			return (byte) (ordinal()+100);
		}

		public VarMetaGroupCore fromByte(byte b) {
			return values()[b-100];
		}
		
		public boolean isLocal() {return false;	}
		public boolean isMeta() {return false;	}
		public boolean isMetaGroup() {return true;	}
	}
	
	protected DebugInformationProvider info;
	protected MetaformaContext context = new MetaformaContext(this);
	protected MetaformaVisualizer visual = new MetaformaVisualizer(this);
	protected MetaformaScheduler scheduler = new MetaformaScheduler(this);	
	
	private byte stateInstruction = 0;
	private byte stateInstructionReceived;


	private float stateStartTime;
	protected float stateStartNext; // Used to wait before entering new state
	
	private HashMap<IVar,Byte> vars = new HashMap<IVar,Byte>();
	private HashMap<IVar,Byte> varsSequenceNrs = new HashMap<IVar,Byte>();

	
	private BigInteger consensus = BigInteger.ZERO; 
	
	private Module previousName;
		
		
	private HashMap<Byte,Float> doRepeat = new HashMap<Byte,Float>();
	
	
	private HashMap<String,Boolean> stateBools = new HashMap<String,Boolean>();
	private HashMap<String,Byte> stateBytes = new HashMap<String,Byte>();
	
	
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
	
	protected boolean varSet(IVar id, int val) {
		visual.print(".varSet " + id + ", " + val);
		byte value = min(Byte.MAX_VALUE,val);

		boolean changed = (varGet(id) != value);
		vars.put(id, value);
		
		if (id.equals(VarMetaCore.Completed)) {
			stateStartTime = time();
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
	
	
	protected boolean checkState(IStateOperation stateOperation, byte stateInstruction) {
		return getStateInstruction() == stateInstruction && getStateOperation().equals(stateOperation);
	}
	
	public boolean stateGetBoolean(String name) {
		if (!stateBools.containsKey(name)) {
			stateBools.put(name, false);
		}
		return stateBools.get(name);
	}
	
	public void stateSetVar (String name, byte value) {
		stateBytes.put(name, value);
	}
	
	public void stateSetVar (String name, boolean value) {
		stateBools.put(name, value);
	}
	
	public byte stateGetByte(String name) {
		if (!stateBytes.containsKey(name)) {
			stateBytes.put(name, (byte)0);
		}
		return stateBytes.get(name);
	}
	
	
	public boolean freqLimit (float interval, int nr) {
		if (!doRepeat.containsKey((byte)nr) || time() - doRepeat.get((byte)nr)  > interval/1000) {
			doRepeat.put((byte)nr, time());
			return true;
		}
		else {
			return false;
		}
	}
	
	
	
	public boolean doRepeat(int state, float interval, int nr) {
		return doRepeat(state,interval,nr,varGet(VarMetaGroupCore.GroupSize));
	}

	
	public boolean doRepeat (int state, float interval, int nr, int consensusCount) {
		consensusIfCompletedNextState(consensusCount);
		return (stateInstruction == state && freqLimit(interval, nr));
	}
	
	public boolean stateOperation(IStateOperation state) {
		return varGet(VarMetaGroupCore.StateOperation) == state.ord();
	}
	
	public boolean doOnce(int state, int consensusCount) {
		if (getStateInstruction() == state) {
			consensusIfCompletedNextState(consensusCount);
		}
		if (stateInstructionSimple(state)) {
			discoverNeighbors();
			return true;
		}
		return false;
	}
		
	public boolean doOnce(int state) {
		if (getStateInstruction() == state) {
			consensusIfCompletedNextState();
		}
		if (stateInstructionSimple(state)) {
			discoverNeighbors();
			return true;
		}
		return false;
	}
	
	private boolean stateInstructionSimple (int state) {
		return stateInstruction == state && !consensusMyself();	
	}
	
	
	
	
	
	 public void disconnect(int c) {
		 connection(c, false);
	 }
	 
	 public void connect(int c) {
		connection(c, true);
	 }
	 
	 protected void connection(int c, boolean connect) {
		 if (connect && !context.isConnConnected(c)) {
			 super.connect(context.abs2rel(c)); 
		 }
		 else if (!connect && context.isConnConnected(c)) {
			 super.disconnect(context.abs2rel(c));
		 }
		 
		 while (c%2==0 && context.isConnConnected(c) != connect) {
			 waitAndDiscover();
		 }
	}

	private float round (float f, int decimals) {
		return (float) (Math.round(f * Math.pow(10,decimals)) / Math.pow(10,decimals));
	}
	
	protected static int pow (int base, int exp) {
		return (int)Math.pow(base,exp);
	}
	
	public static int pow2(int i) {
		return pow(2,i);
	}
	
	public float timeSpentInState() {
		return round((time() - stateStartTime),3);
	}
	
	
	
	public void waitAndDiscover () {
		scheduler.invokeIfScheduled ("broadcastDiscover");
		
		delay(500);
		yield();
	}
		

	public void refresh() {
		rotateToDegreeInDegrees(angle);
	}

	
	public abstract void handleStates();
	public abstract void init();

	public void activate() {
		setup();
		 info = info();
		
		scheduler.setInterval("broadcastDiscover",3000);
		scheduler.setInterval("broadcastConsensus",4000);
		scheduler.setInterval("broadcastMetaVars",5000);
		
//		scheduler.setInterval("broadcastSymmetry",2000);
			
		setCommFailureRisk(0.25f,0.25f,0.98f,0.125f);
		
		init();
		
		
		for (int i=0; i<5; i++) {
			discoverNeighbors();
		}
		
		while (true) {
			refresh();
			handleStates();
			scheduler.sync();
//			if (stateOperation.ord() == 0) {
//				// Default state
//				if (stateInstructionReceived <= 1 && stateInstructionReceived > stateInstruction) {
//					stateInstrIncr(stateInstructionReceived);
//				}
//				
//			}
//			else {
				if (stateInstructionReceived > stateInstruction) {
					// State instr 0 should not be spread, modules have to increment to 1 itself
					stateInstrIncr(stateInstructionReceived);
				}
//			}
			
			yield();
			if (freqLimit(500, 73)) {		
				visual.colorize();
			}
			
		}
	}
	
	
	
	
	

	protected void stateOperationInit (IStateOperation state) {
		varSet(VarMetaGroupCore.StateOperation,state.ord());
	}
	
	protected void stateInstrInit(int i) {
		stateInstruction = (byte) i;
		
	}
	
	protected void stateInstrInitNew() {
		// Instead of throwing all NB's away, only remove the unconnected ones
		context.deleteUnconnectedNeighbors(); 
		stateBools.clear();
		stateBytes.clear();
		doRepeat.clear();
		
		stateStartNext = 0;

		stateStartTime = time();
		consensus = BigInteger.ZERO;
		visual.colorize();
	}
	
	private void stateInstrIncr(int newStateInstruction) {
		if (newStateInstruction > stateInstruction + 1) {
			visual.print("!!! I might have missed a state, from " + stateInstruction + " to " + newStateInstruction);
		}
		stateInstruction = (byte) newStateInstruction;
		
		visual.printInstrState();
		stateInstrInitNew();
	}
	
		
	
	protected void increaseInstrState() {
		stateInstrInitNew();
		stateInstruction++;
		visual.printInstrState();
	}
	
	
	
//	protected void stateOperationBroadcast (IStateOperation newState) {
//		stateOperationNew(newState);
//		scheduler.invokeNow("broadcastDiscover");
//
//		broadcast(new Packet(getId(), Module.ALL).setType(Type.STATE_OPERATION_NEW));
//	}
	
	protected void stateOperationNew (IStateOperation newState, boolean preClean) {
		varSet(VarMetaGroupCore.StateOperation,newState.ord()); 
		if (preClean) {
			stateInstrInitNew();
			stateInstruction = 0;
			stateInstructionReceived = 0;
		}
		
		visual.printOperationState();
	}
	
	
	
	protected void stateInstrBroadcastNext(int newState) {
		stateInstruction = (byte) newState;
		
		nextInstrState();
	}
	
	protected void stateInstrBroadcastNext() {
		stateInstruction++;
		nextInstrState();
		
	}
	
	private void nextInstrState() {
		visual.printInstrState();
		
		stateInstrInitNew();	
		scheduler.invokeNow("broadcastDiscover");
	}

	public float time() {
		return getModule().getSimulation().getTime();
	}


	
	public void discoverNeighbors () {
		scheduler.invokeNow("broadcastDiscover");
		delay(500);
	}

	

	public NeighborSet nbs(int connectors) {
		return context.nbs().filter(connectors);
	}
	
	public NeighborSet nbs(Grouping g) {
		return context.nbs().onGroup(g);
	}
	
	public NeighborSet nbs(int connectors, IRole r) {
		return context.nbs().filter(connectors).isModRole(r);
	}
	
	public NeighborSet nbs(int connectors, Grouping g) {
		return context.nbs().filter(connectors).onGroup(g);
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
		return Module.valueOf(getName());
	}
	
	public Grouping getGrouping() {
		return getId().getGrouping();
	}

	

	protected void consensusIfCompletedNextState () {
		if (consensusReached()) {
			visual.print("Consensus reached!");
			stateInstrBroadcastNext();
		}
	}
	

	
	
	protected void consensusIfCompletedNextState (int count) {
		if (consensusReached(count)) {
			visual.print("Consensus " + count + " reached!");
			stateInstrBroadcastNext();
		}
	}
	
	protected void consensusIfCompletedNextState (int count, int degradePerc) {
		if (consensusReached(count, degradePerc)) {
			stateInstrBroadcastNext();
		}
	}
	
	public boolean consensusReached(int count) {
		return consensusReached(count, 98);
	}
	
	public boolean consensusReached() {
		return varGet(VarMetaGroupCore.GroupSize) >= 4 && consensusReached(varGet(VarMetaGroupCore.GroupSize));
	}
	
	
	
	public boolean consensusReached(int count, int degradePerc) {
		float consensusToReach = count - (timeSpentInState() / 100) * (100-degradePerc)/100 * count;
		if (freqLimit(4000, 21)) {
			visual.print("Waiting for consensus - " + consensus.bitCount() + " >= " + consensusToReach);
		}
		
		return consensus.bitCount() >= consensusToReach;
	}
	
	public void commit () {
		debugForceMetaBossId();
		boolean modified = false;
		if (!consensus.setBit(getId().ordinal()).equals(consensus)) {
			consensus = consensus.setBit(getId().ordinal());
			scheduler.invokeNow("broadcastConsensus");
			modified = true;
		}
		visual.print(".commit() count:" + consensus.bitCount() + " modified:" + modified + " " + Module.fromBits(consensus));
		
	}
	
	public boolean consensusMyself () {
		return consensus.testBit(getId().ordinal());
	}
	
	
	
	public void broadcastConsensus() {
		if (!consensus.equals(BigInteger.ZERO) && metaBossIdExists()) {
			debugForceMetaBossId();
			broadcast(new Packet(getId()).setType(Type.CONSENSUS).setData(consensus));
		}
	}
		
	private void debugForceMetaBossId() {
		if (!metaBossIdExists()) {
			try {
				throw new Error("Consensus may only be used when having boss ID!");
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
		for (IVar v:vars.keySet()) {
			if (v.isMeta() || v.isMetaGroup()) 
				broadcastMetaVar(v);
		}
	}
	
	public void broadcastMetaVar (IVar id) {
		if (id.isLocal()) {
			throw new Error("Local vars should not be shared!");
		}
//		System.out.println("META VAR SYNC " + id.index());
		if (metaIdExists() && id.isMeta() || metaBossIdExists() && id.isMetaGroup()) {
			broadcast(new Packet(getId()).setType(Type.META_VAR_SYNC).setData(new byte[]{id.index(),varGet(id),varGetSequenceNr(id)}));
		}
		else {
			visual.print("Refuse to share meta var " + id + " due to lack of metaId or metaBossId - " + metaIdGet() + " - " + metaBossIdGet());
		}
	}
	

	protected abstract void receiveMessage (Type type, byte stateInstr, boolean isReq, byte sourceCon, byte destCon, byte sourceMetaId, byte[] data);

	protected abstract void receiveMetaMessage (MetaType type, byte source, byte dest, byte[] data); 
	
	

	public DebugInformationProvider info() {
		if (info == null) {
			info = this.getModule().getDebugInformationProvider();
		}
		return info;
	}
	
	

	public int getStateInstruction() {
		return stateInstruction;
	}
	
	

	
	
	public void renameSwitch (Module m1, Module m2) {
		if (getId() == m1 && !consensusMyself()) {
			setId(m2);
			commit();
		}
		if (getId() == m2 && !consensusMyself()) {
			setId(m1);
			commit();
		}
	}
	
	
	public void renameTo(Module to) {
//		previousName = getId();
		setId(to);
		scheduler.invokeNow("broadcastDiscover");
	}
	
	public void renameGroup(Grouping to) {
		visual.print("rename group");
		setId(getId().swapGrouping(to));
		scheduler.invokeNow("broadcastDiscover");
	}
	
	private void setId(Module id) {
		visual.print("$$$ rename " + getId() + " to " + id);
		getModule().setProperty("name", id.name());
		visual.colorize();
	}
	
	public void renameStore () {
		previousName = getId();
	}
	
	public void renameRestore () {
		visual.print("$$$ restore name " + getId() + " to " + previousName);
		getModule().setProperty("name", previousName.name());
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
	
	public void metaBossIdUnlock() {
		visual.print(".metaBossIdUnlock ");

		metaIdSet(0);
		
		for (IVar v:vars.keySet()) {
			if (!v.isLocal())
				varSet(v,0);
		}
	}
	


	public void metaBossIdSetTo(IStateOperation operation, byte[] metaIds) {
		visual.print(".setMetaBossId " + metaId + ": " + operation);
		if (getStateOperation().ord() == 0) {
			stateOperationNew(operation, false);
		}
		varSet(VarMetaCore.BossId, metaIdGet());
		for (byte metaId : metaIds) {
			broadcast(new MetaPacket(metaIdGet(),metaId).setType(MetaType.SET_BOSS));
		}
		varSet(VarMetaGroupCore.GroupSize, moduleRoleGet().size() * (metaIds.length+1));
	}

	
	
	public void handleMessage(byte[] message, int messageLength, int connectorNr) {
		// Translate absolute connector to relative connector (symmetry feature)
		byte connector = context.abs2rel(connectorNr);
		

		if (message[0] == -1) {
			MetaPacket p = new MetaPacket(message);
			
			if (p.getDest() == metaId) {
				visual.print(p,".RECEIVE " + p);
			}
			
			// TODO: The packet should go to an other meta module immediately
			if (!p.isDying() ) {
				broadcast(p);
			}
			
			if (p.getDest() == metaIdGet()) {
				if (p.getType() == MetaType.SET_BOSS && varGet(VarMetaCore.BossId) != p.getSource()) {
					// the origin will become the boss
					varSet(VarMetaCore.BossId, p.getSource());
					commit();
				}
				else {
					receiveMetaMessage (p.getType(),p.getSource(),p.getDest(),p.getData());
				}
				 
			}
		}
		else {
			
			Packet p = new Packet(message);
			

			if ((p.getMetaBossId() != metaBossIdGet() || metaBossIdGet() == 0  || p.getMetaBossId() == 0) && p.getType() == Type.CONSENSUS) {
				visual.print("Packet consensus ignore from outside broadcast field: " +p);
				return;
			}
			
			
			if (p.getMetaBossId() != metaBossIdGet() && p.getMetaSourceId() != metaIdGet()) {
				visual.print("Packet ignored from outside broadcast field: " +p);
				return;
			}
			
			
			if (isFEMALE(connector)) {
				context.setFemaleConnected(connector,p.getConnectorConnected());
			}
			context.addNeighbor(p.getSource(), connector, p.getSourceConnector(),p.getModRole(),p.getMetaBossId());
			
			if (p.getMetaSourceId() != metaId && metaIdExists() && p.getType() != Type.META_ID) {
				// During a sequence we should not add meta neighbors as the connector numbers can be swapped! So only do this at DEFAULT operation state
				if (getStateOperation().ord() == 0) {
					metaNeighborHook (connector,p.getMetaSourceId());
				}
				
			}
			
			
		
			if (stateInstructionReceived < p.getStateInstruction() && p.getMetaBossId() == metaBossIdGet()  && (stateInstruction != 0 || p.getStateInstruction() == 1)) {
				stateInstructionReceived = p.getStateInstruction();
//				broadcast(new Packet(p),connector);
				scheduler.invokeNow("broadcastDiscover");
			}
			if (stateInstruction != p.getStateInstruction() && p.getType() != Type.DISCOVER && p.getType() != Type.META_VAR_SYNC&& p.getType() != Type.GRADIENT) {
				visual.print("!!! Packet dropped due to state mismatch:");
				visual.print(p.toString());
				return;
			}
			
			
			
			
			if (p.getDest() == getId() || p.getDest() == Module.ALL) {
				visual.print(p,".receive on " + connector + ": " + p);
			}
			
			
			
	
			if (p.getSource() == getId()) {
				try {
					throw new Error("Source cannot be myself (" + getId() + ")!");
				}
				catch (Error e) {
					e.printStackTrace();
				}
				
			}
			
			if (p.getType() == Type.META_VAR_SYNC) {
				IVar v = null;
				if (p.getData()[0] >= 25 && p.getData()[0] < 50) {
					v = varFromByteMeta(p.getData()[0]);
				}
				else if (p.getData()[0] < 75) {
					v = VarMetaCore.NONE.fromByte(p.getData()[0]);
				}
				else if (p.getData()[0] < 100) {
					v = varFromByteMetaGroup(p.getData()[0]);
				}
				else if (p.getData()[0] < 125) {
					v = VarMetaGroupCore.NONE.fromByte(p.getData()[0]);
				}
				else {
					throw new Error("Wrong var index!");
				}
	
//				if (p.getMetaBossId() == 0 || p.getMetaSourceId() == 0) {
//					try {
//						throw new Error(p + " - meta boss id or source id = 0!");
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						System.exit(0);
//					}
//				}
				
				if (metaIdExists() && (v.isMeta() && p.getMetaSourceId() == metaIdGet() || v.isMetaGroup() && p.getMetaBossId() == metaBossIdGet() && metaBossIdExists())) {
				
					if (varGetSequenceNr(v) < p.getData()[2]) {
						varSet(v,p.getData()[1]);
						if (v.equals(VarMetaCore.BossId) || p.getData()[0] == 0) {
							commit();
						}
					}
				}
			}
			
			if (p.getDest() == getId() || p.getDest() == Module.ALL) {
				
				if (p.getType() == Type.CONSENSUS){				
					if (!(p.getDataAsInteger().or(consensus)).equals(consensus)) {
						consensus = consensus.or(p.getDataAsInteger());
						visual.print("consensus update: " + consensus.bitCount() + ": " + Module.fromBits(consensus));
						scheduler.invokeNow("broadcastConsensus");
					}
				}
				else {
					// Custom message 
					receiveMessage (p.getType(),p.getStateInstruction(),p.getDir() == Dir.REQ,p.getSourceConnector(),connector,p.getMetaBossId(),p.getData());
				}
			}
		}
	}
	
	public abstract void metaNeighborHook(int connectorNr, byte metaId);
		

	public void broadcast (MetaPacket p) {
//		if (p.getLastHop() != getMetaId()) {
			p.decreaseTTL();
//		}
		p.setLastHop(metaIdGet());
		
		if (!p.isDying()) {
			visual.print(p,".BROADCAST " + p);
			for (byte c = 0; c < 8; c++) {
				sendMessage(p.getBytes(), (byte) p.getBytes().length, context.abs2rel(c));
			}
		}
		else {
//			visual.print("DIE " + p);
		}
	}
	
	public void broadcast (Packet p) {	
		broadcast (p,-1);
	}
		
	public void broadcast (Type t, boolean isAck, byte[] data) {
		Dir d = isAck ? Dir.ACK : Dir.REQ;
		broadcast (new Packet(getId()).setType(t).setDir(d).setData(data));
	}
	public void unicast (int connectors, Type t, boolean isAck) {
		unicast(connectors, t, isAck,new byte[]{});
	}
	
	public void unicast (int connectors, Type t, boolean isAck, byte[] data) {
		Dir d = isAck ? Dir.ACK : Dir.REQ;
		unicast (new Packet(getId()).setType(t).setDir(d).setData(data),connectors);
	}
	
	public void broadcast(Packet p,int exceptConnector) {
		p.addState(this);
		p.setMetaBossId(metaBossIdGet());
		if (varGet(VarMetaCore.Completed) == 1) {
			p.setMetaSourceId(metaIdGet());
		}
		visual.print(p,".broadcast (" + p.toString() + ") ");

		for (byte c = 0; c < 8; c++) {
			if (c != exceptConnector) {
				send(p, c);
			}
		}
	}
	
	public void unicast (Packet p, int connectors) {
		visual.print(p,".unicast packet " + "(" + p.toString() + ") ");
		for (byte i=0; i<8; i++) {
			if ((connectors&pow2(i))==pow2(i)) {
				p.setSourceConnector(i);
				send(p,i);
				visual.print(".unicast to " + i);
			}
		}
	}
	
	
	
	public void send (Packet p) {
		visual.print(p,".send packet (" + p.toString() + ") ");
		byte c = nbs().getConnectorNrTo(p.getDest());
		send(p, c);
	}

	public void send(Packet p, int connector) {
		p.setSourceConnector((byte) connector);
		p.setSource(getId());
		p.setMetaBossId(metaBossIdGet());
		p.setModRole(moduleRoleGet());
		if (varGet(VarMetaCore.Completed) == 1) {
			p.setMetaSourceId(metaIdGet());
		}
		p.addState(this);

		//if ((msgFilter & p.getType().bit()) != 0) notification(".send = " + p.toString() + " over " + connector);
		
		if (connector < 0 || connector > 7) {
			System.err.println("Connector has invalid nr " + connector);
		}
		if (isMALE(connector)) {
			p.setConnectorConnected(context.isConnConnected(connector));
		}
		
		
		
		sendMessage(p.getBytes(), (byte) p.getBytes().length, context.abs2rel(connector),p.getSource().toString(),p.getDest().toString());
	}
	
	public void metaSetCompleted () {
		varSet(VarMetaCore.Completed, 1);
	}
	
	public boolean metaGetCompleted () {
		return varGet(VarMetaCore.Completed) == 1;
	}

	public MetaformaContext getContext() {
		return context;
	}

	public MetaformaVisualizer getVisual() {
		return visual;
	}
	
	

	public MetaformaScheduler getScheduler() {
		return scheduler;
	}

	public MetaformaVisualizer getVisualizer() {
		return visual;
	}

	public HashMap<Byte,Float> getDoRepeat() {
		return doRepeat;
	}

	public HashMap<String,Boolean> getStateBools() {
		return stateBools;
	}


	public HashMap<String,Byte> getStateBytes() {
		return stateBytes;
	}

	public BigInteger getConsensus() {
		return consensus;
	}

	public HashMap<IVar,Byte> getVars() {
		return vars;
	}

	public HashMap<IVar,Byte> getVarSequenceNrs() {
		return varsSequenceNrs;
	}
	

	public byte getStateInstructionReceived() { 
		return stateInstructionReceived;
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
	
	public abstract IStateOperation getStateOperation();
	
	public void pause() {
		getModule().getSimulation().setPause(true);
	}
}
