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

	public enum CoreVar implements IVar {
		NONE, Completed,BossId;

		public byte index() {
			return (byte) (127-ordinal());
		}

		public CoreVar fromByte(byte b) {
			return values()[127-b];
		}
	}
	
	protected DebugInformationProvider info;
	protected MetaformaContext context = new MetaformaContext(this);
	protected MetaformaVisualizer visual = new MetaformaVisualizer(this);
	protected MetaformaScheduler scheduler = new MetaformaScheduler(this);	
	
	private IStateOperation stateOperation;
	private byte stateInstruction = 0;
	private byte stateInstructionReceived;


	private float stateStartTime;
	protected float stateStartNext; // Used to wait before entering new state
	
	private boolean stateCurrentFinished = false;
	
	private HashMap<IVar,Byte> varsLocal = new HashMap<IVar,Byte>(); 
	private HashMap<IVar,Byte> varsMeta = new HashMap<IVar,Byte>();
	private HashMap<IVar,Byte> varsMetaGroup = new HashMap<IVar,Byte>();
	private HashMap<IVar,Byte> varsMetaSequenceNrs = new HashMap<IVar,Byte>();
	private HashMap<IVar,Byte> varsMetaGroupSequenceNrs = new HashMap<IVar,Byte>();
	
	private BigInteger consensus = BigInteger.ZERO; 
	
	private Module previousName;
		
	
	
	
	private IVar varHolder;
	
	private HashMap<Byte,Float> doRepeat = new HashMap<Byte,Float>();
	
	public IVar Ivar;
	public IStateOperation IstateOperation;
	
	private HashMap<String,Boolean> stateBools = new HashMap<String,Boolean>();
	private HashMap<String,Byte> stateBytes = new HashMap<String,Byte>();
	
	
	protected byte metaId = 0;
	protected IRole moduleRole = null;
	private int metaModuleSize = 4;
	
	
	public byte metaVarGet(IVar id) {
		return metaVarGet(id,false);
	}
	
	public byte metaVarGet(IVar id,boolean getSequenceNr) {
		if (!getSequenceNr && varsMeta.containsKey(id)) {
			return varsMeta.get(id);
		} 
		else if (getSequenceNr && varsMetaSequenceNrs.containsKey(id)) {
			return varsMetaSequenceNrs.get(id);
		}
		else {
			return 0;
		}

	}
	
//	public byte metaGroupVarGet(IVar id,boolean getSequenceNr) {
//		if (!getSequenceNr && varsMetaGroup.containsKey(id)) {
//			return varsMetaGroup.get(id);
//		} 
//		else if (getSequenceNr && varsMetaGroupSequenceNrs.containsKey(id)) {
//			return varsMetaGroupSequenceNrs.get(id);
//		}
//		else {
//			return 0;
//		}
//
//	}
	
	public boolean metaVarSet(IVar id, int value) {
		visual.print(".metaVarSet " + id + ", " + value);
		if (metaVarGet(id) != value) {
			varsMeta.put(id,(byte) value);
			varsMetaSequenceNrs.put(id,(byte)(metaVarGet(id,true)+1));
			broadcastMetaVar(id);
			return true;
		}
		else {
			return false;
		}
	}
	
//	public boolean metaGroupVarSet(IVar id, int value) {
//		visual.print(".metaGroupVarSet " + id + ", " + value);
//		if (metaVarGet(id) != value) {
//			varsMetaGroup.put(id,(byte) value);
//			varsMetaGroupSequenceNrs.put(id,(byte)(metaVarGet(id,true)+1));
//			broadcastMetaGroupVar(id);
//			return true;
//		}
//		else {
//			return false;
//		}
//	}
//		
	
	protected boolean checkState(IStateOperation stateOperation,	byte stateInstruction) {
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
	
	public boolean doRepeatMeta(int state, float interval, int nr) {
		return doRepeat(state,interval,nr,metaModuleSize);
	}

	
	public boolean doRepeat (int state, float interval, int nr, int consensusCount) {
		consensusCount = 12;
		consensusIfCompletedNextState(consensusCount);
		return (stateInstruction == state && freqLimit(interval, nr));
	}
	
	public boolean stateOperation(IStateOperation state) {
		return stateOperation == state;
	}
	
	public boolean doOnce(int state, int consensusCount) {
		consensusCount = 12;
		// TODO: This number should be specified as default, now ignored to try whether it works
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
		return doOnce(state,1);
	}
	
	private boolean stateInstructionSimple (int state) {
		return stateInstruction == state && !consensusMyself();	
	}
	
	
	
	
	
	protected void varSet(IVar id, int v) {
		if (v < Byte.MAX_VALUE) {
			varsLocal.put(id, (byte)v);
		}
		else {
			varsLocal.put(id, Byte.MAX_VALUE);
		}
	}


	protected byte varGet(IVar id) {
		if (!varsLocal.containsKey(id)) {
			varsLocal.put(id, (byte)0); 
		}
		return varsLocal.get(id);
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
		 waitAndDiscover();
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
		
		scheduler.setInterval("broadcastDiscover",10000);
		scheduler.setInterval("broadcastConsensus",4000);
		scheduler.setInterval("broadcastMetaVars",5000);
		
//		scheduler.setInterval("broadcastSymmetry",2000);
		
		visual.setDefaultColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		
		setCommFailureRisk(0.25f,0.25f,0.98f,0.125f);
		
		init();
		
		
		for (int i=0; i<5; i++) {
			discoverNeighbors();
		}
		
		while (true) {
			refresh();
			handleStates();
			scheduler.sync();
			if (stateOperation.ord() == 0) {
				// Default state
				if (stateInstructionReceived <= 1 && stateInstructionReceived > stateInstruction) {
					stateInstrIncr(stateInstructionReceived);
				}
				
			}
			else {
				if (stateInstructionReceived > stateInstruction) {
					stateInstrIncr(stateInstructionReceived);
				}
			}
			
			yield();
			if (freqLimit(500, 73)) {		
				visual.colorize();
			}
			
		}
	}
	
	
	
	
	

	protected void stateOperationInit (IStateOperation state) {
		stateOperation = state;
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
		stateCurrentFinished = false;
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
		stateOperation = newState; 
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

	

	
	

	
	
	protected void consensusIfCompletedNextState (int count) {
		if (consensusReached(count)) {
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
		if (consensus.setBit(getId().ordinal()) != consensus) {
			consensus = consensus.setBit(getId().ordinal());
			scheduler.invokeNow("broadcastConsensus");
			modified = true;
		}
		visual.print(".commit() " + consensus.bitCount() + " " + modified);
		
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
				throw new Exception("Consensus may only be used when having boss ID!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public void broadcastDiscover () {
		broadcast(new Packet(getId()));
	}
	
	public void metaVarCheckUpdate (IVar var, byte value, byte sequenceNr) {
		if (metaVarGet(var,true) < sequenceNr) {
			varsMeta.put(var, value);
			varsMetaSequenceNrs.put(var, sequenceNr);
			broadcastMetaVar(var);
			if (var.equals(CoreVar.BossId) || value == 0) {
				commit();
			}
		}
	}
	
	public void broadcastMetaVars () {
		for (IVar v:varsMeta.keySet()) {
			broadcastMetaVar(v);
		}
	}
	
	public void broadcastMetaVar (IVar id) {
		broadcast(new Packet(getId()).setType(Type.META_VAR_SYNC).setData(new byte[]{id.index(),metaVarGet(id,false),metaVarGet(id,true)}));
	}
	
//	public void broadcastMetaGroupVar (IVar id) {
//		broadcast(new MetaPacket().setType(Type.META_GROUP_VAR_SYNC).setData(new byte[]{id.index(),metaGroupVarGet(id,false),metaGroupVarGet(id,true)}));
//	}

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
	
	public IStateOperation getStateOperation() {
		return stateOperation;
	}

	
	public boolean stateIsFinished () {
		return stateCurrentFinished;
	}
	
	public void stateFinish () {
		if (!stateCurrentFinished) {
			stateCurrentFinished = true;
			visual.print("%% State finished!");
		}
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

	

	public IVar getVarHolder() {
		return varHolder;
	}
	
	public void setVarHolder(IVar v) {
		varHolder = v;
	}

	public IStateOperation getOperationHolder() {
		return stateOperation;
	}

	protected void moduleRoleSet(IRole r) {
		visual.print(".setModuleRole " + r);
		moduleRole = r;
		
	}
	

	public boolean isLocked() {
		return metaVarGet(CoreVar.BossId) != 0;
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
	
	
	public byte getMetaBossId() {
		return metaVarGet(CoreVar.BossId);
	}
	
	public boolean metaBossIdExists() {
		return metaVarGet(CoreVar.BossId) != 0;
	}
	
	
	public boolean metaBossMyself() {
		return metaIdGet() == getMetaBossId();
	}

	
	public IRole moduleRoleGet () {
		return moduleRole;
	}
	
	public void metaBossIdUnlock() {
		visual.print(".metaBossIdUnlock ");
		metaVarSet(CoreVar.BossId, 0);
	}

	public void metaBossIdSetMyself(byte metaId) {
		visual.print(".setMetaBossId " + metaId + ": " + stateOperation);
		metaVarSet(CoreVar.BossId, metaIdGet());
		broadcast(new MetaPacket(metaIdGet(),metaId).setType(MetaType.SET_BOSS).setData(stateOperation.ord()));
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
				if (p.getType() == MetaType.SET_BOSS && !stateOperation(IstateOperation.fromByte(p.getData()[0]))) {
					// the origin will become the boss
					metaVarSet(CoreVar.BossId, p.getSource());
					stateOperationNew(IstateOperation.fromByte(p.getData()[0]),false);
					commit();
				}
				else {
					receiveMetaMessage (p.getType(),p.getSource(),p.getDest(),p.getData());
				}
				 
			}
		}
		else {
			Packet p = new Packet(message);
			

			if ((p.getMetaBossId() != getMetaBossId() || getMetaBossId() == 0  || p.getMetaBossId() == 0) && p.getType() == Type.CONSENSUS) {
				visual.print("Packet consensus ignore from outside broadcast field: " +p);
				return;
			}
			
			
			if (p.getMetaBossId() != getMetaBossId() && p.getMetaSourceId() != metaIdGet()) {
				visual.print("Packet ignore from outside broadcast field: " +p);
				return;
			}
			
			
			if (isFEMALE(connectorNr)) {
				context.setFemaleConnected(connectorNr,p.getConnectorConnected());
			}
			
			if (p.getMetaSourceId() != metaId && metaIdExists() && p.getType() != Type.META_ID) {
				metaNeighborHook (connectorNr,p.getMetaSourceId());
				
			}
			
		
		
			if (stateInstructionReceived < p.getStateInstruction() && p.getMetaBossId() == getMetaBossId()) {
				stateInstructionReceived = p.getStateInstruction();
//				broadcast(new Packet(p),connector);
				scheduler.invokeNow("broadcastDiscover");
			}
			if (stateInstruction != p.getStateInstruction() && p.getType() != Type.DISCOVER && p.getType() != Type.META_VAR_SYNC) {
				visual.print("!!! Packet dropped due to state mismatch:");
				visual.print(p.toString());
				return;
			}
			
			
			
			
			if (p.getDest() == getId() || p.getDest() == Module.ALL) {
				visual.print(p,".receive on " + connector + ": " + p);
			}
			
			context.addNeighbor(p.getSource(), connector, p.getSourceConnector(),p.getModRole());
			
	
			if (p.getSource() == getId()) {
				try {
					throw new Exception("Source cannot be myself (" + getId() + ")!");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
			
			if (p.getType() == Type.META_VAR_SYNC && p.getMetaSourceId() == metaIdGet()) {
				IVar v;
				if (p.getData()[0] < 100) {
					v = varHolder.fromByte(p.getData()[0]);
				}
				else {
					v = CoreVar.NONE.fromByte(p.getData()[0]);
				}
				metaVarCheckUpdate(v,p.getData()[1],p.getData()[2]);			
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
		p.setMetaBossId(getMetaBossId());
		p.setModRole(moduleRoleGet());
		if (metaVarGet(CoreVar.Completed) == 1) {
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

	public HashMap<IVar,Byte> getVarsLocal() {
		return varsLocal;
	}

	public HashMap<IVar,Byte>  getVarsMeta() {
		return varsMeta;
	}
	
	public HashMap<IVar,Byte>  getVarsMetaSeqNrs() {
		return varsMetaSequenceNrs;
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
	

	
}
