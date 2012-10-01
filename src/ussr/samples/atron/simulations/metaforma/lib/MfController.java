package ussr.samples.atron.simulations.metaforma.lib;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import ussr.model.debugging.ControllerInformationProvider;
import ussr.model.debugging.DebugInformationProvider;
import ussr.samples.atron.simulations.metaforma.gen.BrandtController;
import ussr.samples.atron.simulations.metaforma.gen.BrandtController.StateOperation;
import ussr.samples.atron.simulations.metaforma.lib.NeighborSet;
import ussr.samples.atron.simulations.metaforma.lib.Packet.*;
import ussr.util.Pair;

public abstract class MfController extends MfApi implements ControllerInformationProvider{

	public final static byte NORTH = (byte) (pow2(0) + pow2(1) + pow2(2) + pow2(3));
	public final static byte SOUTH = (byte) (pow2(4) + pow2(5) + pow2(6) + pow2(7));
	public final static byte WEST = (byte) (pow2(0) + pow2(1) + pow2(4) + pow2(5));
	public final static byte EAST = (byte) (pow2(2) + pow2(3) + pow2(6) + pow2(7));
	public final static byte MALE = (byte) (pow2(0) + pow2(2) + pow2(4) + pow2(6));
	public final static byte FEMALE = (byte) (pow2(1) + pow2(3) + pow2(5) + pow2(7));
	
	public int QUART = 90;
	public int HALF = 180;
	
	protected final static boolean REQ = false;
	protected final static boolean ACK = true;

	protected DebugInformationProvider info;
	
	// Does not seem to work!
	protected MfContext context;// = new MfContext(this);
	protected MfVisualizer visual;// = new MfVisualizer(this);
	protected MfScheduler scheduler;// = new MfScheduler(this);	
	protected MfStateManager stateMngr;// = new MfStateManager(this);
	protected MfActuation actuation;// = new MfActuation(this);

		
	
		
	private HashMap<String,Float> doRepeat = new HashMap<String,Float>();
	
	protected SettingsBase set;

//	protected BagRegionCore regionBag;
	
	
	public MfController(SettingsBase s) {
		this();
		set = s;
	}
	
	public MfController() {
		visual = new MfVisualizer(this);
		context = new MfContext(this);
		scheduler = new MfScheduler(this);	
		stateMngr = new MfStateManager(this);
		actuation = new MfActuation(this);
	}

	
		
	public boolean freqLimit (String key,float interval) {
		if (!doRepeat.containsKey(key) || time() - doRepeat.get(key)  > interval) {
			doRepeat.put(key, time()); 
			return true;
		}
		else {
			return false;
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
		
		init();
		
		module().setID(getID());
		
		System.out.println(getName() + ": " + getID());
		
		System.out.println("my new id: " + module().getID());
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
			rotateToDegreeInDegrees(angle % 360);
			
			scheduler.sync();
			
			handleStates();
			
			yield();
			
			stateMngr.merge();

			
			if (freqLimit("colorize",0.5f)) {		
				visual.colorize();
			}
			
		}
	} 
	
	public void addMetaNeighborhood (StringBuffer out) {
		out.append(String.format("% 3d  % 3d  % 3d",meta().getVar("TopLeft"),meta().getVar("Top"),meta().getVar("TopRight")) + "\n");
		out.append(String.format("% 3d        % 3d",meta().getVar("Left"),meta().getVar("Right")) + "\n");
		out.append(String.format("% 3d  % 3d  % 3d",meta().getVar("BottomLeft"),meta().getVar("Bottom"),meta().getVar("BottomRight")) + "\n");
	}
	
	
	public void handleMessage(byte[] message, int messageLength, int connectorNr) {
		// Translate absolute connector to relative connector (symmetry feature)
		byte connector = context.abs2rel(connectorNr);
		
		makePacket(message,connector);
//			if () {
//				visual.print(base, "handleMessage " + base);
//			}
//			else {
//				visual.print(base, "unhandled! " + base);
//			}
//		
	}
	
	public boolean preprocessPacket (Packet p) {
		if (MfApi.isFEMALE(p.connDest)) {
			getContext().setFemaleConnected(p.connDest,p.getConnectorConnected());
		}
		getContext().addNeighbor(p.getSource(), p.connDest, p.getSourceConnector(),p.getModRole(),p.getMetaID(),p.getRegionID());
	
		if ((p.regionID == meta().regionID() && meta().regionID() != 0) || (p.metaID == module().metaID && module().metaID != 0)) {
			// If packet is in region or in meta-module, then update state!
			if (getStateMngr().update(BigInteger.ZERO, p.getState())) {
				visual.print("state update from " + p.getSource() + " : " + p.getState());
			}
			visual.print(p,".receive: " + p);
			return true;
		}
		
//		visual.print(p,".reject: " + p);
		return false;
	}
	
	
	
	public void makePacket(byte[] msg, byte connector) {
		if (Packet.isPacket(msg)) {
			byte typeNr = Packet.getType(msg);
			if (typeNr == PacketDiscover.getTypeNr()) {
				PacketDiscover p = (PacketDiscover)new PacketDiscover(this).deserialize(msg,connector);
				preprocessPacket(p);
				receivePacket((Packet)p);
				System.out.println(".receivediscover " + p);
			}
			else if (typeNr == PacketSymmetry.getTypeNr()) {
				PacketSymmetry p = (PacketSymmetry)new PacketSymmetry(this).deserialize(msg,connector);
				if (preprocessPacket(p)) {
					receivePacket(p);
				}
				receivePacket((Packet)p);
			}
			else if (typeNr == PacketSetMetaId.getTypeNr()) {
				PacketSetMetaId p = (PacketSetMetaId)new PacketSetMetaId(this).deserialize(msg,connector);
				// This packet is used to create a meta-module, so we cant do the preprocess check before receiving!
				visual.print(p,".receive: " + p);
				receivePacket(p);
				receivePacket((Packet)p);
				
			}
			else if (typeNr == PacketConsensus.getTypeNr()) {
				PacketConsensus p = (PacketConsensus)new PacketConsensus(this).deserialize(msg,connector);
				if (p.regionID == meta().regionID() && preprocessPacket(p)) {
					getStateMngr().update((BigInteger)p.getVar("consensus"),p.getState());
				}
				receivePacket((Packet)p);
			}
			else if (typeNr == PacketMetaVarSync.getTypeNr()) {
				PacketMetaVarSync p = (PacketMetaVarSync)new PacketMetaVarSync(this).deserialize(msg,connector);
				// Meta var sync only within meta module
				if (preprocessPacket(p) && module().metaID == p.metaID) {
					receivePacket(p);
				}
				receivePacket((Packet)p);
			}
			else if (typeNr == PacketRegion.getTypeNr()) {
				PacketRegion p = (PacketRegion)new PacketRegion(this).deserialize(msg,connector);
				preprocessPacket(p);
				receivePacket(p);
				receivePacket((Packet)p);
			}
			else {
				visual.error("Unknown packet with nr !" + typeNr);
				receiveCustomPacket(typeNr,msg,connector);
			}
			
			
		}
		else {
			visual.error("META packet received??");
//			receivePacket(MetaPacket(msg));
		}

	}

	

	public abstract void receiveCustomPacket(byte typeNr, byte[] msg, byte connector);

	public abstract boolean receivePacket (Packet p);
	public abstract boolean receivePacket (PacketSymmetry p);
	public abstract boolean receivePacket (PacketSetMetaId p);
	
	public boolean receivePacket (PacketMetaVarSync p) {
		for (Map.Entry<String,Pair<Byte,Byte>> e:p.vars.entrySet()) {
			String var = e.getKey();
			if (meta().getVarSeqNr(var) < e.getValue().snd()) {
				// TODO: Merge when seq nrs are the same.
				meta().setVar(var, e.getValue().fst(),e.getValue().snd());
			}
			if (meta().getVarSeqNr(var) == e.getValue().snd() && e.getValue().fst() != meta().getVar(var)) {
				meta().setVar(var, MfApi.max(e.getValue().fst(),meta().getVar(var)));
				visual.print("Conflict in " + var + " between " + e.getValue().fst() + " and " + meta().getVar(var) + ", take highest");
			}
			
			//TODO: BrandtController.StateOperation.CHOOSE needs to be converted to independent state
			if (var.equals("regionID")) {
				// The new region ID may not be zero!
				if (stateMngr.at(BrandtController.StateOperation.CHOOSE) && e.getValue().fst() != 0) {
					getStateMngr().cleanConsensus();
					getStateMngr().commit("BOSS ID received through meta sync");
//					visual.print("COMMMMIIITT");
				}
				else {
//					visual.print("no consensus commit because in faulty state....");
				}
			}
			
		}
		
		return true;
	}
	
	
	
	public Module getID() {
		return Module.value(getName());
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
		float stopTime = getModule().getSimulation().getTime() + ms / 1000f;
		
		while (stopTime > getModule().getSimulation().getTime()) {
			yield();
		}

	}

	


	public void broadcastConsensus() {
//		visual.print(".broadcastConsensus()");
		if (!stateMngr.getConsensus().equals(BigInteger.ZERO) && module().metaID != 0) {
			debugForceMetaId();
			broadcast((PacketConsensus)new PacketConsensus(this).setVar("consensus", stateMngr.getConsensus()));
		}
	}

	public void debugForceMetaId() {
		if (module().metaID == 0) {
			try {
				throw new Error("Consensus may only be used when having meta ID!");
			}
			catch (Error e) {
				e.printStackTrace();
			}
		}
	}

	public void broadcastDiscover () {
		module().discover();
	}
	
	public void broadcastMetaVars () {
		if (module().metaID != 0) {
			broadcastMetaVars(meta().getVars());
		}
	}
	
	public void broadcastMetaVars (ArrayList<String> names) {
//		visual.print(".....broadcastMetaVars " + names);
		PacketMetaVarSync p = new PacketMetaVarSync(this);
		p.setVarList(names);
		broadcast(p);
	}

	

	public abstract void handleStates();
	public abstract void init();

	public DebugInformationProvider info() {
		if (info == null) {
			info = this.getModule().getDebugInformationProvider();
		}
		return info;
	}
	
	
	

	
	
//	public void send(MetaPacket p, int destID) {
//		p.setDest((byte) destID); 
//		
//		if (destID == module().metaID) {
//			visual.error("Already at !! " + destID);
//		}
//		
//		for (byte c=0; c< 8; c++) {
//			if (nbs().getMetaIdByConnector(c) == destID || (module().metaID == p.source && nbs().getRegionIdByConnector(c) == p.source && nbs().getMetaIdByConnector(c) != module().metaID)) {
//				sendMessage(p.getBytes(), (byte) p.getBytes().length, context.abs2rel(c));
//			}
//		}
//	}
//	
	
	
//	
//	private boolean isRegionBoss() {
//		return module().getId().ord() == module().metaID;
//	}

	private void addToPacket (Packet p) {
		p.addState(stateMngr.getState());
		p.setRegionID(meta().regionID());
		if (meta().completed() == 1) {
			p.setMetaID(module().metaID);
		}
//		else {
//			// -1 means that a meta module is present but not fully initialised with a meta id yet
//			p.setMetaSourceId(-1);
//		}
		p.setSource(getID());
		p.setModRole(module().role);
	}
	
	public void broadcast (Packet p) {	
		broadcast (p,-1);
	}
	
	private void broadcast(Packet p,int exceptConnector) {
		addToPacket(p);
		
//		visual.print("SEND: "+Packet.getType(p.setSourceConnector((byte) 1).serialize()));
		visual.print(p,".broadcast (" + p.toString() + ") ");

		for (byte c = 0; c < 8; c++) {
			if (c != exceptConnector) {
				send(p, c);
			}
		}
	}
	
	public void unicast (Packet p, int connectors) {
		addToPacket(p);
		visual.print(p,".unicast packet " + "(" + p.toString() + ") ");
		for (byte i=0; i<8; i++) {
			if ((connectors&pow2(i))==pow2(i)) {
				send(p,i);
				visual.print(p,".unicast to " + i);
			}
		}
	}
	

	public void unicast(Packet p, byte[] metaIDs) {
		for (byte metaID: metaIDs) {
			unicast(p,nbs().nbsWithMetaId(metaID).connectors());
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
//		visual.print(".send " + p);
		
		
		sendMessage(p.serialize(), (byte) p.serialize().length, context.rel2abs(connector));
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
	

	public MfScheduler getScheduler() {
		return scheduler;
	}

	public MfVisualizer getVisualizer() {
		return visual;
	}

	public HashMap<String,Float> getDoRepeat() {
		return doRepeat;
	}

	
	public String getModuleInformation () {
		return visual.getModuleInformation();
	}
	
	public String getTitle() {
		return visual.getTitle();
	}
	
	
	public void pause() {
		getModule().getSimulation().setPause(true);
	}

	public void prepareNextState() {
		// Instead of throwing all NB's away, only remove the unconnected ones
		context.deleteUnconnectedNeighbors();
		doRepeat.clear();
		visual.colorize();
	}
	
	public void receivePacket (PacketRegion p) {					
		// the origin will become the boss
		if (stateMngr.check(p,StateOperation.CHOOSE) && (p.getState().getInstruction() == 2 || (meta().regionID() == 0 || meta().regionID() == p.getRegionID()))) {
			visual.print(p.toString());
			meta().setRegionID(p.getRegionID());
			meta().setCountInRegion (p.sizeMeta);
			meta().setVar("orientation",p.orientation);
			if (p.indirectNb != 0) {
				unicast(new PacketRegion(this),nbs().nbsWithMetaId(p.indirectNb).connectors());
			}	
			stateMngr.commit("BOSS ID received");
		}
		else {
			visual.print("REJECTED " + p.toString());
		}
	}
	
//////////////////////////////////////////////////////////////
	// Generated shared functions
	
	

	protected void symmetryFix(PacketSymmetry p) {
		byte connDest = p.connDest;
		
//		if ((isNORTH(p.connSource) == isNORTH(connDest) && isWEST(p.connSource) == isNORTH(connDest)) || (isNORTH(p.connSource) != isNORTH(connDest) && isWEST(p.connSource) != isSOUTH(connDest))) {
//			if (!stateMngr.committed()) {
//				context.switchNorthSouth();
//				connDest = (byte) ((connDest + 4) % 8);
//			}
//		}
	
		if (isFEMALE(p.connDest)) {
			if (!stateMngr.committed()) {
				if (isWEST(p.connSource) != isSOUTH(connDest)) {
					context.switchNorthSouth();
					connDest = (byte) ((connDest + 4) % 8);
				}
				visual.print(p.connSource + " " + isNORTH(p.connSource) + " == " + isWEST(connDest) + " " + connDest);
				context.switchEastWestHemisphere(isNORTH(p.connSource) == isWEST(connDest), isSOUTH(connDest));
					
				
			}				
		}
		else if (isMALE(p.connDest)) {
			if (!stateMngr.committed()) {
				if (isWEST(p.connSource) != isNORTH(connDest)) {
					context.switchNorthSouth();
					connDest = (byte) ((connDest + 4) % 8);
				}
			
			
//				context.switchEastWestHemisphere(isSOUTH(p.connSource) == isEAST(p.connDest), isSOUTH(p.connDest));

					// sure!!
				context.switchEastWestHemisphere(isNORTH(p.connSource) == isEAST(connDest), isSOUTH(connDest));
				
				
			}
		}
		if (freqLimit("SYMM passthrough", 0.5f)) {
			broadcast(new PacketSymmetry(this));
		}
		stateMngr.commit("Symmetry fix done");
	}

	public abstract IRole getInstRole();
	public abstract IStateOperation getInstOperation();
	

	public void setAngle(int a) {
		angle = a;
	} 

	public abstract BagModuleCore module();
	
	public abstract IMetaBag meta();



	
	
}
