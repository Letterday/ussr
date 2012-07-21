package ussr.samples.atron.simulations.metaforma.lib;

import java.awt.Color;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.HashMap;

import java.util.Map;
import java.util.Random;

import ussr.model.debugging.ControllerInformationProvider;
import ussr.model.debugging.DebugInformationProvider;
import ussr.samples.atron.ATRONController;
import ussr.samples.atron.simulations.metaforma.gen.*;
import ussr.samples.atron.simulations.metaforma.lib.Packet;
import ussr.samples.atron.simulations.metaforma.lib.NeighborSet;



public abstract class MetaformaController extends ATRONController implements ControllerInformationProvider{

	
	protected DebugInformationProvider info;
	public NeighborSet neighbors = new NeighborSet((MetaformaRuntime) this);
	
	private HashMap<Module, Color[]> moduleColors = new HashMap<Module, Color[]>();
	private HashMap<Grouping, Color[]> groupingColors = new HashMap<Grouping, Color[]>();
	private Color[] defaultColors;
	
	private IStateOperation stateOperation;
	private int stateInstruction = 0;

	private float stateStartTime;
	
	protected int errInPreviousState;
	protected int errInCurrentState;
	
	protected int pushPull = 0;
	
	private boolean stateHasReceivedBroadcast = false;
	private boolean stateConnectorNearbyCallDisabled = false; // For connecting to neighbors
	
	protected float stateStartNext; // Used to wait before entering new state
	
	private boolean stateCurrentFinished = false;
	
	protected Map<IVar,Byte> varsLocal = new HashMap<IVar,Byte>(); 
	
	protected BigInteger consensus = BigInteger.ZERO; 
		
	protected static final int MAX_BYTE = Byte.MAX_VALUE;
	
	private Module previousName;
	
	private boolean switchEastWestN;
	private boolean switchEastWestS;
	private boolean switchNorthSouth;
	
	private byte msgFilter;
	private boolean stateOperationCoordinate = false;

	protected boolean stateChangeConsensus;
	private int stateInstructionReceived;
 
	
	protected Scheduler scheduler = new Scheduler(this);
	private IStateOperation stateOperationReceived;
	private float timeLastPrintConsensus;
	private IVar varHolder;
	private IStateOperation stateOperationHolder;
	
	protected boolean fixedYet;
	private HashMap<Byte,Float> doRepeat = new HashMap<Byte,Float>();
	public IVar Ivar;
	public IStateOperation IstateOperation;
	private HashMap<String,Boolean> stateBools = new HashMap<String,Boolean>();
	private HashMap<String,Byte> stateBytes = new HashMap<String,Byte>();
	
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
	
	public boolean doRepeat (int state, float interval, int nr, int consensusCount) {
		consensusIfCompletedNextState(consensusCount);
		return (stateInstruction == state && freqLimit(interval, nr));
	}
	
	public boolean stateOperation(IStateOperation state) {
		return stateOperation == state;
	}
	
	public boolean doOnce(int state, int consensusCount) {
		if (getStateInstruction() == state) {
			consensusIfCompletedNextState(consensusCount);
		}
		return doOnce(state);
	}
		
	public boolean doOnce(int state) {
		if (stateInstructionSimple(state)) {
			discoverNeighbors();
			return true;
		}
		return false;
	}
	
	private boolean stateInstructionSimple (int state) {
		return stateInstruction == state && !consensusMyself();	
	}
	
	public void switchEastWest () {
		switchEastWestN =! switchEastWestN;
		switchEastWestS =! switchEastWestS;
	}
	

	public void setMessageFilter (int msg) {
		msgFilter = (byte) msg;
	}
	
	protected byte C (int nr) {
		byte ret = (byte) nr;
		if (switchNorthSouth) {
			ret = (byte) ((ret + 4)%8);
		}
		if (switchEastWestN && ret <= 3) {
			ret = (byte) ((ret + 2)%4);
		}
		if (switchEastWestS && ret >= 4) {
			ret = (byte) ((ret + 2)%4 + 4);
		}
		
		return ret;
	}
	
	public void switchEastWestHemisphere (boolean isCorrect, boolean southSide) {
		String side = southSide ? " southside " : " northside ";
		String correct = isCorrect ? " correct " : " incorrect ";
		notification("$$$ Switch East West " + side + correct);
		if (southSide) {
			if (!isCorrect) {
				switchEWS();
				if (super.getAngle() == 0) {
					switchEWN();
				} 
			}
			else { 
				if (super.getAngle() != 0) {
					switchEWN();
				}
			}
			
		}
		else {
			if (!isCorrect) {
				switchEWN();
				if (super.getAngle() == 0) {
					switchEWS();
				} 
			}
			else if (super.getAngle() != 0) {
				switchEWS();
			}
		}
		
		
		
		colorize();
	}	
	
	private void switchEWN () {
		neighbors.updateSymmetryEW(false);
		switchEastWestN =! switchEastWestN;
		notification("switch EW N");
	}
	
	private void switchEWS () {
		neighbors.updateSymmetryEW(true);
		switchEastWestS =! switchEastWestS;
		notification("switch EW S");
	}
	
	public void switchNorthSouth () {
		 switchNorthSouth =! switchNorthSouth;
		 notification("$$$ Switch North South");
		 neighbors.updateSymmetryNS();
		 colorize();
	}
	
	public int getAngle() {
    	return super.getAngle() + ((switchEastWestN ^ switchEastWestS) ? 180 : 0) %360;
    }
	
//	public void gradientTransit(IVar id) {
//		notification("@ Gradient " + id + " transit.");
//		broadcast(new Packet(getId()).setType(Type.GRADIENT).setData(new byte[]{id.ord(),(byte)Math.min(Byte.MAX_VALUE, varGetGradient(id) + 1)}));
//	}
//	
//	
//	public void gradientCreate(IVar id) {
//		varsLocal.put(id, (byte)0);
//		notification("@ Gradient " + id + " start.");
//		// To make sure eventually one gets through (packet loss)
//		broadcast(new Packet(getId()).setType(Type.GRADIENT).setData(new byte[]{id.ord(),(byte) 1}));
//	}
//	
//	
//	
//	public void gradientReset(IVar id) {
//		varsLocal.put(id, Byte.MAX_VALUE);
//		//broadcast(new Packet(getId()).setType(Type.GRADIENT_RESET).setData(id));
//	}
//	
	
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
	
	
	
	
//	public void setGlobal (int index, int value) {
//		varsGlobal.put((byte)index, (byte)value);
//		broadcast(new Packet(getId(),Module.ALL).setData(index,value).setType(Type.GLOBAL_VAR));
//	}
	
//	public byte getGlobal (int index) {
//		if (varsGlobal.containsKey(index)) {
//			return varsGlobal.get(index);
//		}
//		return 0;
//	}
	
	
	public void rotate(int degrees) {
		notification("## rotate " + degrees + ", current = " + angle + "; new = " + (degrees + angle) + "");
		// TODO: There is still a strange issue: when rotating 180 degrees, it is undefined whether it goes CW or CCW (randomly).
		if (Math.abs(degrees) < 180) {
			angle = angle + degrees % 360;
			doRotate(angle);
		}
		else {
			// Therefore we rotate in 2 steps, such that the direction is no longer undefined, but can be chosen by a pos/neg degree
			angle = angle + (degrees /2) % 360;
			doRotate(angle);
			angle = angle + (degrees /2) % 360;
			doRotate(angle);
		}
		
	}
	
	public void rotateTo(int degrees) {
		notification("## rotateTo " + degrees + ", current = " + angle);
		angle = degrees;
		doRotate(angle);
	}
	
	private void doRotate (int degrees) {
		rotateToDegreeInDegrees(degrees);
		while (isRotating()) {
			yield();
		}
	}
	
	public boolean isConnected (int c) {
		if (c == -1) return false;
		return super.isConnected(C(c));
	}
	
	 public void disconnect(int c) {
		 connection(c, false);
	 }
	 
	 public void connect(int c) {
		connection(c, true);
	 }
	 
	 protected void connection(int c, boolean connect) {
		 if (connect && !isConnected(c)) {
			 super.connect(C(c)); 
		 }
		 else if (!connect && isConnected(c)) {
			 super.disconnect(C(c));
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
//
//	private byte oppositeConnector (int c) {
//		//TODO: How to take into account rotation between two hemispheres?
//		return  (byte)((c+4)%8);
//	}
//	
	
	protected void connection(Module dest, boolean makeConnection) {
		byte conToNb = nbs().getConnectorNrTo(dest);
		byte conFromNb = nbs().getConnectorNrFrom(dest);
		String action = makeConnection ? " connect to " : " disconnect from ";
		notification("# " + getId() + action + dest);
		if (conToNb % 2 == 0 && conFromNb % 2 == 1) {
			// Male wants to connect to female. No further action needed
			if (makeConnection)
				connect(conToNb);
			else
				disconnect(conToNb);
		}
	}
	
	public void connect (Module m) {
		notification("## connect to " + m);
		connection(m, true);
	}
	
	
	public void connection(Module m1, Module m2, boolean makeConnection) {
		if (getId() == m1) {
			connection(m2,makeConnection);
		}
			
			
	}

	
	
	public void waitAndDiscover () {
		scheduler.invokeIfScheduled ("broadcastDiscover");
		
		delay(500);
		//notification(timeLastBc + " - " + time());
		yield();
	}
	
	
	
	public void disconnect(Module m) {
		notification("## disconnect to " + m);
		connection(m, false);
	}
	
	

	private void refresh() {
		rotateToDegreeInDegrees(angle);
	}

	
	public abstract void handleStates();

	public abstract void init();

	public void activate() {
		setup();
		info = this.getModule().getDebugInformationProvider();
		
		scheduler.setInterval("broadcastDiscover",5000);
		scheduler.setInterval("broadcastConsensus",5000);
//		scheduler.setInterval("broadcastSymmetry",2000);
		
		setDefaultColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		
		setCommFailureRisk(0.25f,0.25f,0.98f,0.125f);
		
		init();
		colorize();
		
		
		// For push pull algorithm to determine the structure size
		if (getId().ord() == 0) {
			pushPull = 1;
		}
		
		for (int i=0; i<3; i++) {
			discoverNeighbors();
		}
		
		while (true) {
			refresh();
			handleStates();
			scheduler.sync();
			
			if (stateInstructionReceived > stateInstruction) {
				increaseInstrState(stateInstructionReceived);
			}
			//TODO: operation state merge
			
			yield();

			
		}
	}
	
	

	
	protected void notification (String msg) {
		info.addNotification("[" + new DecimalFormat("0.00").format(time()) + "] - " + msg);
	}

	protected void stateOperationInit (IStateOperation state) {
		stateOperation = state;
	}
	
	protected void stateInstrInit(int i) {
		stateInstruction = i;
		
	}
	
	protected void stateInstrInitNew() {
		// Instead of throwing all NB's away, only remove the unconnected ones
		neighbors.deleteUnconnectedOnes();
		stateBools.clear();
		stateBytes.clear();
		doRepeat.clear();
		
		stateStartNext = 0;
		stateHasReceivedBroadcast = false;
		stateConnectorNearbyCallDisabled = true;
		stateCurrentFinished = false;
		stateStartTime = time();
		errInCurrentState = 0;
		stateChangeConsensus = false;
		consensus = BigInteger.ZERO;
		colorize();
	}
	
	private void increaseInstrState(int newStateInstruction) {
		if (newStateInstruction > stateInstruction + 1) {
			notification("!!! I might have missed a state, from " + stateInstruction + " to " + newStateInstruction);
		}
		stateInstruction = newStateInstruction;
		
		printInstrState();
		stateInstrInitNew();
	}
	
		
	
	protected void increaseInstrState() {
		stateInstrInitNew();
		stateInstruction++;
		printInstrState();
	}
	
	private void printInstrState() {
		notification("\n\n===========  "+ getId() + "  ==============\nLeft instruction state, time spent: " + timeSpentInState() + "\nNew instruction state: " + stateOperation + ": " + stateInstruction + "\n=================================");
	}
	
	protected void stateOperationBroadcast (IStateOperation newState) {
		setNewOperationState(newState);
		stateOperationCoordinate  = true;
		notification("I will be operation coordinator!");
		scheduler.invokeNow("broadcastDiscover");

		broadcast(new Packet(getId(), Module.ALL).setType(Type.STATE_OPERATION_NEW));
	}
	
	private void setNewOperationState (IStateOperation newState) {
		stateOperation = newState; 
		stateInstrInitNew();
		stateInstruction = 0;

		notification("\n\n#######  "+ getId() + "  ##########\nnew operation state: " + stateOperation + "\n#############################\n");
	}
	
	
	public void disableConnectorNearby() {
		stateConnectorNearbyCallDisabled = true;
	}
	
	protected void stateInstrBroadcastNext(int newState) {
		stateInstruction = newState;
		
		nextInstrState();
	}
	
	protected void stateInstrBroadcastNext() {
		stateInstruction++;
		nextInstrState();
		
	}
	
	private void nextInstrState() {
		printInstrState();
		if (errInCurrentState == 1) {
			notification("\n(the following error occured in this state: " + errInCurrentState + ") ");
		}
		stateInstrInitNew();	
		scheduler.invokeNow("broadcastDiscover");
	}

	public float time() {
		return getModule().getSimulation().getTime();
	}


	
	public void discoverNeighbors () {
		scheduler.invokeNow("broadcastDiscover");
		delay(100);
	}

	

	public NeighborSet nbs(int connectors) {
		return neighbors.filter(connectors);
	}
	
	public NeighborSet nbs() {
		return neighbors;
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

	

	public void colorize() {
		notification(".colorize();");
		
		getModule().getComponent(0).setModuleComponentColor(getColors()[switchNorthSouth ? 1 : 0]);
		getModule().getComponent(1).setModuleComponentColor(getColors()[switchNorthSouth ? 0 : 1]);

//		notification("NORTH_MALE_WEST " + NORTH_MALE_WEST + " - " + C(NORTH_MALE_WEST) + " - " + module.getConnectors().get(C(NORTH_MALE_WEST)).hashCode());
//		notification("NORTH_MALE_EAST " + NORTH_MALE_EAST + " - " + C(NORTH_MALE_EAST) + " - " + module.getConnectors().get(C(NORTH_MALE_EAST)).hashCode());
		module.getConnectors().get(C(0)).setColor(Color.BLUE);
		module.getConnectors().get(C(1)).setColor(Color.BLACK);
		module.getConnectors().get(C(2)).setColor(Color.RED);
		module.getConnectors().get(C(3)).setColor(Color.WHITE);
		
//		notification(C(NORTH_MALE_EAST) + "(" + module.getConnectors().get(C(NORTH_MALE_EAST)) +") will be red " + NORTH_MALE_EAST + "(" + module.getConnectors().get((NORTH_MALE_EAST)) +")");
		module.getConnectors().get(C(4)).setColor(Color.BLUE);
		module.getConnectors().get(C(5)).setColor(Color.BLACK);
		module.getConnectors().get(C(6)).setColor(Color.RED);
		module.getConnectors().get(C(7)).setColor(Color.WHITE);
//		notification("NORTH_MALE_WEST " + NORTH_MALE_WEST + " - " + C(NORTH_MALE_WEST) + " - " + module.getConnectors().get(C(NORTH_MALE_WEST)).hashCode());
//		notification("NORTH_MALE_EAST " + NORTH_MALE_EAST + " - " + C(NORTH_MALE_EAST) + " - " + module.getConnectors().get(C(NORTH_MALE_EAST)).hashCode());
	}
	
	

	public void handleMessage(byte[] message, int messageLength, int connectorNr) {
		// Translate absolute connector to relative connector (symmetry feature)
		byte connector = C(connectorNr);
		
		
		Packet p = new Packet(message);
		
		if (p.getStateOperation() == stateOperation) {
			if (stateInstructionReceived < p.getStateInstruction()) {
				stateInstructionReceived = p.getStateInstruction();
				broadcast(new Packet(p),connector);
				scheduler.invokeNow("broadcastDiscover");
				
			}
			if (stateInstruction != p.getStateInstruction()) {
				notification("!!! Packet dropped due to state mismatch:");
				notification(p.toString());
				return;
			}
		}
		else if (p.getType() != Type.STATE_OPERATION_NEW) {
			stateOperationReceived = p.getStateOperation(); 
			stateOperationCoordinate = false;
			
			notification("!!! Packet dropped due to state mismatch:");
			notification(p.toString());
			return;
			
//			if (stateOperationMessageIdentifier < p.getData()[1]) {
//				broadcast(new Packet(p),connector);
//				stateOperationMessageIdentifier = p.getData()[1];
//			}
			
		}
		
//		if (p.getType() != Type.DISCOVER && p.getType() != Type.STATE_OPERATION_NEW && p.getType() != Type.GRADIENT) {
//			if (p.getStateOperation() != stateOperation) {
//				notification("!!! Packet dropped due to operation state mismatch:");
//				notification(p.toString());
//				return;
//			}
//			else if (p.getStateInstruction() != stateInstruction) {
//				if (p.getStateInstruction() > stateInstruction) {
//					increaseInstrState(p.getStateInstruction());
//				}
//				else {
//					
//				}
//			}
//			
//		}
		
		if (p.getDest() == getId() || p.getDest() == Module.ALL) {
			if ((msgFilter & p.getType().bit()) != 0) notification(".receive on " + connector + ": " + p);
		}
		
		neighbors.add(p.getSource(), connector, p.getSourceConnector());

		if (p.getSource() == getId()) {
			try {
				throw new Exception("Source cannot be myself (" + getId() + ")!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		if (p.getDest() == getId() || p.getDest() == Module.ALL) {
			
			if (p.getType() == Type.CONSENSUS){				
				if (!p.getDataAsInteger().or(consensus).equals(consensus)) {
					consensus = consensus.or(p.getDataAsInteger()); 
					//scheduler.invokeNow("broadcastConsensus");
				}
			}
			else {
				// Custom message 
				receiveMessage (p.getType(),p.getStateOperation(),p.getStateInstruction(),p.getDir() == Dir.REQ,p.getSourceConnector(),connector,p.getData());
			}
		}
	}
	
	protected void consensusIfCompletedNextState (int count) {
		consensusIfCompletedNextState (count,96);
	}
	
	protected void consensusIfCompletedNextState (int count, int degradePerc) {
		float consensusToReach = count - (timeSpentInState() / 100) * (100-degradePerc)/100 * count;
		if (time() - timeLastPrintConsensus > 4) {
			notification("Waiting for consensus - " + consensus.bitCount() + " >= " + consensusToReach);
			timeLastPrintConsensus = time();
		}
		
		if (consensus.bitCount() >= consensusToReach) {
			stateInstrBroadcastNext();
		}
	}
	
	
	
	public void commit () {
		if (consensus.setBit(getId().ordinal()) != consensus) {
			consensus = consensus.setBit(getId().ordinal());
			scheduler.invokeNow("broadcastConsensus");
		}
		
		
	}
	
	public boolean consensusMyself () {
		return consensus.testBit(getId().ordinal());
	}
	
	
	
	public void broadcastConsensus() {
		if (!consensus.equals(BigInteger.ZERO)) {
			broadcast(new Packet(getId()).setType(Type.CONSENSUS).setData(consensus));
		}
	}
		
	public void broadcastDiscover () {
		broadcast(new Packet(getId()));
	}

	protected abstract void receiveMessage (Type type, IStateOperation stateOp, byte stateInstr, boolean isReq, byte sourceCon, byte destCon, byte[] data);

	
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
		if ((msgFilter & p.getType().bit()) != 0)  notification(".broadcast (" + p.toString() + ") ");

		for (byte c = 0; c < 8; c++) {
			if (c != exceptConnector) {
				send(p, c);
			}
		}
	}
	
	public void unicast (Packet p, int connectors) {
		if ((msgFilter & p.getType().bit()) != 0) notification(".unicast packet " + "(" + p.toString() + ") ");
		for (byte i=0; i<8; i++) {
			if ((connectors&pow2(i))==pow2(i)) {
				p.setSourceConnector(i);
				send(p,i);
				notification(".unicast to " + i);
			}
		}
	}
	
	
	
	public void send (Packet p) {
		if ((msgFilter & p.getType().bit()) != 0) notification(".send packet (" + p.toString() + ") ");
		byte c = nbs().getConnectorNrTo(p.getDest());
		send(p, c);
	}

	public void send(Packet p, int connector) {
		p.setSourceConnector((byte) connector);
		p.setSource(getId());
		p.addState(this);

		//if ((msgFilter & p.getType().bit()) != 0) notification(".send = " + p.toString() + " over " + connector);
		
		if (connector < 0 || connector > 7) {
			System.err.println("Connector has invalid nr " + connector);
		}
		
		sendMessage(p.getBytes(), (byte) p.getBytes().length, C(connector),p.getSource().toString(),p.getDest().toString());
	}
	

	public DebugInformationProvider info() {
		if (info == null) {
			info = this.getModule().getDebugInformationProvider();
		}
		return info;
	}
	
	public void setGroupingColors(Grouping g, Color[] colors) {
		groupingColors.put(g, colors);
	}
	
	public void setDefaultColors(Color[] colors) {
		defaultColors = colors;
	}

	public void setModuleColors(Module m, Color[] colors) {
		moduleColors.put(m, colors);
	}

	public Color[] getColors() {
		if (moduleColors.containsKey(getId())) {
			return moduleColors.get(getId());
		}
		else if (groupingColors.containsKey(getGrouping())) {
			return groupingColors.get(getGrouping());
		}
		else {
			return defaultColors;
		}

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
			notification("%% State finished!");
		}
	}
	
	public void renameSwitch (Module m1, Module m2, RunPar consensus) {
		if (getId() == m1 && !consensus.hasCommitted()) {
			setId(m2);
			consensus.commit();
		}
		if (getId() == m2 && !consensus.hasCommitted()) {
			setId(m1);
			consensus.commit();
		}
	}
	
	
	public void renameTo(Module to) {
//		previousName = getId();
		setId(to);
		scheduler.invokeNow("broadcastDiscover");
	}
	
	public void renameGroup(Grouping to) {
		notification("rename group");
		setId(getId().swapGrouping(to));
		scheduler.invokeNow("broadcastDiscover");
	}
	
	private void setId(Module id) {
		notification("$$$ rename " + getId() + " to " + id);
		getModule().setProperty("name", id.name());
		colorize();
	}
	
	public void renameStore () {
		previousName = getId();
	}
	
	public void renameRestore () {
		notification("$$$ restore name " + getId() + " to " + previousName);
		getModule().setProperty("name", previousName.name());
		colorize();
		scheduler.invokeNow("broadcastDiscover");
	}


	public String getModuleInformation() {
		StringBuffer out = new StringBuffer();
		
		String flipStr = "";
		if (switchNorthSouth) flipStr += "NORTH-SOUTH ";
		if (switchEastWestN) flipStr += "EAST-WEST-N ";
		if (switchEastWestS) flipStr += "EAST-WEST-S ";
		
		if (flipStr.equals("")) {
			flipStr = "<none>";
		}
		
		out.append("ID: " + getId() + "      [" + stateOperation + " #" + stateInstruction + "]" + (stateIsFinished()? " // finished" : "") + " received: " + stateInstructionReceived + "  flips: " + flipStr);
		out.append("\n");

		out.append("operation coordinator: " + stateOperationCoordinate);
		out.append("\n");
		
		out.append("angle: " + getAngle() + " ("+super.getAngle()+")");
		out.append("\n");
		
		out.append("interval: " + scheduler.intervalMs);
		out.append("\n");
		out.append("previous: " + scheduler.previousAction);
		
		out.append("\n");
		
		
		
//		out.append("globals: " + varsGlobal);
//		out.append("\n");
		
		out.append("vars: " + varsLocal);
		out.append("\n");
		out.append("do repeat: " + doRepeat);
		out.append("\n");
		
		out.append("stateVars: " + stateBools + " - " + stateBytes);
		out.append("\n");
		
		out.append("consensus (" + consensus.bitCount() + "): " + Module.fromBits(consensus));
		out.append("\n");
		
		
		out.append("isrotating: " + isRotating());
		out.append("\n");

		out.append("neighbors: ");

		out.append(nbs().toString());

		out.append("\n");

		return out.toString();
	}

	
	public boolean isOtherConnectorNearbyCallDisabled() {
		//TODO: This is a hack to work around the communication issue. 
		return stateConnectorNearbyCallDisabled;
	}

	public IVar getVarHolder() {
		return varHolder;
	}

	public IStateOperation getOperationHolder() {
		return stateOperation;
	}


	protected byte min(int i, int j) {
		return (byte) Math.min(i, j);
	}

	public static boolean isNORTH(int c) {
		return c >= 0 && c <= 3;
	}
	
	public static boolean isSOUTH(int c) {
		return c >= 4 && c <= 7;
	}
	
	public static boolean isWEST(int c) {
		return c == 0 || c == 1 || c == 4 || c == 5;
	}
	
	public static boolean isEAST(int c) {
		return c == 2 || c == 3 || c == 6 || c == 7;
	}
	
	public static boolean isMALE(int c) {
		return c%2 == 0;
	}
	
	public static boolean isFEMALE(int c) {
		return c%2 == 1;
	}
	
}
