package ussr.samples.atron.simulations.metaforma.lib;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import ussr.builder.genericTools.RemoveModule;
import ussr.model.debugging.ControllerInformationProvider;
import ussr.model.debugging.DebugInformationProvider;
import ussr.samples.atron.ATRONController;
import ussr.samples.atron.simulations.metaforma.gen.*;
import ussr.samples.atron.simulations.metaforma.lib.Packet;
import ussr.samples.atron.simulations.metaforma.lib.NeighborBag;

public abstract class MetaformaController extends ATRONController implements ControllerInformationProvider{

	
	protected DebugInformationProvider info;
	public NeighborBag neighbors = new NeighborBag(this);
	
	private HashMap<Module, Color[]> moduleColors = new HashMap<Module, Color[]>();
	private HashMap<Grouping, Color[]> groupingColors = new HashMap<Grouping, Color[]>();
	private Color[] defaultColors;
	
	private int stateOperation = 0;
	private int stateInstruction = 0;
	private int statePending = 0;
	private float stateStartTime;
	
	protected int errInPreviousState;
	protected int errInCurrentState;
	
	private boolean stateHasReceivedBroadcast = false;
	private boolean stateConnectorNearbyCallDisabled = false; // For connecting to neighbors
	
	private float timeLastBc = 0;
	private boolean stateCurrentFinished = false;
	
	protected HashMap<Byte,Byte> varsGlobal = new HashMap<Byte,Byte>();
	protected HashMap<Byte,Byte> gradients = new HashMap<Byte,Byte>(); 
	
	protected HashSet <Module> consensus = new HashSet <Module>(); 
	
	protected final int NORTH_MALE_WEST = 0;
	protected final int NORTH_MALE_EAST = 2;
	protected final int SOUTH_MALE_WEST = 4;
	protected final int SOUTH_MALE_EAST = 6;
	protected final int NORTH_FEMALE_WEST = 1;
	protected final int NORTH_FEMALE_EAST = 3;
	protected final int SOUTH_FEMALE_WEST = 5;
	protected final int SOUTH_FEMALE_EAST = 7;
	
	protected byte C (int nr) {
		byte ret = (byte) nr;
		if (switchNorthSouth) {
			ret = (byte) ((ret + 4)%8);
		}
		if (switchEastWest) {
			if (ret >= 4) {
				ret = (byte) (((ret + 2)%4 )+ 4);
			}
			else {
				ret = (byte) ((ret + 2)%4);
			}
		}
		return ret;
	}
	
	private Module previousName;
	protected float stateLastBroadcast;
	private boolean switchEastWest;
	private boolean switchNorthSouth;
	private byte msgFilter;
	private boolean stateOperationCoordinate = false;
	private Random random;
	private byte stateOperationMessageIdentifier;
	private Grouping previousGrouping;
	protected boolean stateChangeConsensus;
	
	
	public void setMessageFilter (int msg) {
		msgFilter = (byte) msg;
	}
	
	public static final int PENDING1 = 1;
	public static final int PENDING2 = 2;
	public static final int PENDING3 = 4;
	public static final int PENDING4 = 8;
	public static final int PENDING5 = 16;
	public static final int PENDING6 = 32;
	public static final int PENDING7 = 64;
	public static final int PENDING8 = 128;
	
	public boolean stateOperation(int state) {
		return stateOperation == state;
	}
	
	public boolean stateInstruction(int state) {
		if (stateInstructionSimple(state)) {
			discoverNeighbors();
			// stateInstruction might be changed during discoverNeighbors!
			return stateInstructionSimple(state);
		}
		return stateInstructionSimple(state);
	}
	
	public boolean stateInstructionSimple (int state) {
		return stateInstruction == state && !stateIsFinished();	
	}
	
	public void switchEastWest () {
		switchEastWest =! switchEastWest;
		notification("$$$ Switch East West");
		
		neighbors.updateSymmetryEW();
		
		colorize();

	}
	
	
	
	
	public void switchNorthSouth () {
		 switchNorthSouth =! switchNorthSouth;
		 notification("$$$ Switch North South");
		 
		 neighbors.updateSymmetryNS();
		 
		 colorize();
	}
	
	public void gradientTransit(int id, int value) {
		gradients.put((byte)id, (byte)value);
		notification("@ Gradient " + id + " transit.");
		broadcast(new Packet(getId()).setType(Type.GRADIENT).setData(id,value + 1));
	}
	
	
	public void gradientCreate(int id) {
		gradients.put((byte)id, (byte)0);
		notification("@ Gradient " + id + " start.");
		// To make sure eventually one gets through (packet loss)
		broadcast(new Packet(getId()).setType(Type.GRADIENT).setData(id, 1));
	}
	
	public void gradientResetAll() {
		for (Byte g:gradients.keySet()) {
			gradientReset(g);
		}
	}
	
	public void gradientReset(int id) {
		gradients.put((byte)id, (byte)Byte.MAX_VALUE);
		//broadcast(new Packet(getId()).setType(Type.GRADIENT_RESET).setData(id));
	}
	
	
	public int var(int id) {
		if (!gradients.containsKey((byte)id)) {
			gradients.put((byte)id, Byte.MAX_VALUE);
		}
		return gradients.get((byte)id);
	}
	
	
	public void setGlobal (int index, int value) {
		varsGlobal.put((byte)index, (byte)value);
		broadcast(new Packet(getId(),Module.ALL).setData(index,value).setType(Type.GLOBAL_VAR));
	}
	
	public byte getGlobal (int index) {
		if (varsGlobal.containsKey(index)) {
			return varsGlobal.get(index);
		}
		return 0;
	}
	
	
	public void rotate(int degrees) {
		notification("## rotate " + degrees + ", current = " + angle + "; new = " + (degrees + angle) + "");
		angle = degrees + angle % 360;
		doRotate(angle);
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
		super.disconnect(C(c));
	 }
	 
	 public void connect(int c) {
		super.connect(C(c));
	 }

	private float round (float f, int decimals) {
		return (float) (Math.round(f * Math.pow(10,decimals)) / Math.pow(10,decimals));
	}
	
	protected int pow (int base, int exp) {
		return (int)Math.pow(base,exp);
	}
	
	protected float timeSpentInState() {
		return round((time() - stateStartTime),3);
	}

	private byte oppositeConnector (int c) {
		//TODO: How to take into account rotation of two hemispheres?
		return  (byte)((c+4)%8);
	}
	
	protected void connection(Module dest, boolean makeConnection) {
		byte conToNb = nbs().getConnectorNrTo(dest);
		byte conFromNb = nbs().getConnectorNrFrom(dest);
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
		if (time() - timeLastBc  > 2) {
			discoverNeighbors();
		}
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

	public abstract void handleSyncs();
	public abstract void handleEvents();
	public abstract void handleStates();

	public abstract void init();

	public void activate() {
		setup();
		info = this.getModule().getDebugInformationProvider();
		random = new Random(getId().ord());
		init();
		colorize();
		while (true) {
			refresh();
			handleSyncs();
			handleEvents();
			handleStates();
			yield();
			
			
		}
	}
	
	protected void stateDisseminate() {
		broadcast(new Packet(getId()).setType(Type.STATE_INSTR_UPDATE));
		if (stateOperationCoordinate) {
			delay(100);
			stateOperationMessageIdentifier++;
			broadcast(new Packet(getId()).setType(Type.STATE_OPERATION_NEW).setData(stateOperation,stateOperationMessageIdentifier));
		}
		stateLastBroadcast = time();
		
	}
	
	protected void statePendingBroadcastNr (int nr) {
		statePendingBroadcast(pow(2,nr-1));
	}

	protected void statePendingBroadcast(int newState) {
		if (statePending != (statePending|newState)) {
			statePending = statePending|newState;
			notification("## Pending state to " + newState);
			stateLastBroadcast = time();
			stateFinish();
			//delay(1000);
		}
		broadcast(new Packet(getId()).setType(Type.STATE_INSTR_UPDATE));
		stateLastBroadcast = time();
	}
	
	protected boolean statePending (int state) {
		return (state&statePending) == state;
	}
	
	protected boolean statePendingCount (int nr) {
		return pow(2,nr) == statePending + 1;
	}
	
	protected void notification (String msg) {
		info.addNotification("[" + round(time(),1) + "] - " + msg);
	}

	protected void initInstrState() {
		// Instead of throwing all NB's away, only remove the unconnected ones

		neighbors.deleteUnconnectedOnes();
		
		stateHasReceivedBroadcast = false;
		stateConnectorNearbyCallDisabled = true;
		stateCurrentFinished = false;
		statePending = 0;
		stateStartTime = time();
		errInCurrentState = 0;
		stateChangeConsensus = false;
		consensus.clear();
		colorize();
	}
	
	private void increaseInstrState(int newStateInstruction, int newStatePending) {
		if (newStateInstruction > stateInstruction + 1) {
			notification("!!! I might have missed a state, from " + stateInstruction + " to " + newStateInstruction);
		}
		stateInstruction = newStateInstruction;
		statePending = newStatePending;
		
		printInstrState();
		initInstrState();
	}
	
		
	
	protected void increaseInstrState() {
		initInstrState();
		stateInstruction++;
		printInstrState();
	}
	
	private void printInstrState() {
		notification("\n\n===========  "+ getId() + "  ==============\nLeft instruction state " + (stateInstruction-1) + ", time spent: " + timeSpentInState() + "\nNew instruction state: " + getOpStateName() + ": " + stateInstruction + "\n=================================");
	}
	
	protected void stateOperationBroadcast (int newState) {
		setNewOperationState(newState);
		stateOperationCoordinate  = true;
		notification("I will be operation coordinator!");
		stateLastBroadcast = time();
		stateOperationMessageIdentifier = 0;
		broadcast(new Packet(getId(), Module.ALL).setType(Type.STATE_OPERATION_NEW).setData(stateOperation,stateOperationMessageIdentifier + 1));
	}
	
	private void setNewOperationState (int newState) {
		stateOperation = newState;
		initInstrState();
		stateInstruction = 0;
		stateOperationMessageIdentifier = 0;
		notification("\n\n#######  "+ getId() + "  ##########\nnew operation state: " + getOpStateName() + "\n#############################\n");
	}
	
	
	public void disableConnectorNearby() {
		stateConnectorNearbyCallDisabled = true;
	}
	
	protected void stateInstrBroadcastNext(int newState) {
		stateInstruction = newState;
		
		nextInstrState();
	}
	
	protected void stateInstrBroadcastNext() {
		statePending = 0;
		stateInstruction++;
		nextInstrState();
		
	}
	
	private void nextInstrState() {
		printInstrState();
		if (errInCurrentState == 1) {
			notification("\n(the following error occured in this state: " + errInCurrentState + ") ");
		}
		initInstrState();	
		//delay(1000);
		stateLastBroadcast = time();
		broadcast(new Packet(getId(), Module.ALL).setType(Type.STATE_INSTR_UPDATE));
	}

	public float time() {
		return getModule().getSimulation().getTime();
	}


	public void broadcastDiscover () {
		broadcast(new Packet(getId()));
	}
	
	public void discoverNeighbors () {
		broadcastDiscover();
		delay(1000);
	}

	
	
	public NeighborBag nbs() {
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
		module.getConnectors().get(C(NORTH_MALE_WEST)).setColor(Color.BLUE);
		module.getConnectors().get(C(NORTH_FEMALE_WEST)).setColor(Color.BLACK);
		module.getConnectors().get(C(NORTH_MALE_EAST)).setColor(Color.RED);
		module.getConnectors().get(C(NORTH_FEMALE_EAST)).setColor(Color.WHITE);
		
//		notification(C(NORTH_MALE_EAST) + "(" + module.getConnectors().get(C(NORTH_MALE_EAST)) +") will be red " + NORTH_MALE_EAST + "(" + module.getConnectors().get((NORTH_MALE_EAST)) +")");
		
		module.getConnectors().get(C(SOUTH_MALE_WEST)).setColor(Color.BLUE);
		module.getConnectors().get(C(SOUTH_FEMALE_WEST)).setColor(Color.BLACK);
		module.getConnectors().get(C(SOUTH_MALE_EAST)).setColor(Color.RED);
		module.getConnectors().get(C(SOUTH_FEMALE_EAST)).setColor(Color.WHITE);
//		if (switchEastWest) {
//			notification("switchEastWest");
//			module.getConnectors().get(0).setColor(Color.RED);
//			module.getConnectors().get(4).setColor(Color.RED);
//		}
//		notification("NORTH_MALE_WEST " + NORTH_MALE_WEST + " - " + C(NORTH_MALE_WEST) + " - " + module.getConnectors().get(C(NORTH_MALE_WEST)).hashCode());
//		notification("NORTH_MALE_EAST " + NORTH_MALE_EAST + " - " + C(NORTH_MALE_EAST) + " - " + module.getConnectors().get(C(NORTH_MALE_EAST)).hashCode());
	}
	
	

	public void handleMessage(byte[] message, int messageLength, int connectorNr) {
		// Translate absolute connector to relative connector (symmetry feature)
		byte connector = C(connectorNr);
		Packet p = new Packet(message);
		
//		if (p.getType() != Type.STATE_INSTR_UPDATE && p.getType() != Type.DISCOVER && p.getType() != Type.STATE_OPERATION_NEW && (p.getStateInstruction() < stateInstruction || p.getStateOperation() != stateOperation)) {
//			notification("!!! Packet dropped due to state mismatch:");
//			notification(p.toString());
//			return;
//		}
		
		if (p.getDest() == getId() || p.getDest() == Module.ALL) {
			if ((msgFilter & p.getType().bit()) != 0) notification(".handleMessage = " + p.toString()	+ " over " + connector);
		}
		
		neighbors.add(p.getSource(), connector, p.getSourceConnector());

		if (p.getSource() == getId()) {
			try {
				throw new Exception("Source cannot be myself (" + getId()
						+ ")!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		if (p.getDest() == getId() || p.getDest() == Module.ALL) {
			if (p.getType() == Type.STATE_INSTR_UPDATE && p.getStateOperation() == stateOperation) {
				if (stateInstruction < p.getStateInstruction()) {
					increaseInstrState(p.getStateInstruction(),p.getStatePending());
					broadcast(new Packet(p),connector);
				}
				else if (stateInstruction == p.getStateInstruction()) {
					if ((statePending | p.getStatePending()) != statePending) {
						statePending |=  p.getStatePending();
						notification("## @ " + stateInstruction +" new pending state received: " + statePending);
						broadcast(new Packet(p),connector);
					}
					
				}
			}
			else if (p.getType() == Type.STATE_OPERATION_NEW) {
				if (stateOperation != p.getData()[0]) {
					setNewOperationState(p.getData()[0]);
					stateOperationCoordinate = false;
				}
				if (stateOperationMessageIdentifier < p.getData()[1]) {
					broadcast(new Packet(p),connector);
					stateOperationMessageIdentifier = p.getData()[1];
				}
				
			}
			else if (p.getType() == Type.DISCOVER && p.getDir() == Dir.REQ) {
				send(p.getAck().getBytes(), connector);
			}
			else if (p.getType() == Type.CONSENSUS) {
				if (!consensus.contains(Module.values()[p.getData()[0]])) {
					consensus.add(Module.values()[p.getData()[0]]);
					broadcast(p);
				}
			}
			else {
				// Custom message 
				receiveMessage (p,connectorNr);
			}
		}
	}
	
	protected abstract void receiveMessage (Packet p, int connector);

	
	public void broadcast (Packet p) {	
		broadcast (p,-1);
	}
		
	public void broadcast(Packet p,int exceptConnector) {
		if ((msgFilter & p.getType().bit()) != 0)  notification(".broadcast packet (" + p.toString() + ") ");
		timeLastBc = time();
		for (byte c = 0; c < 8; c++) {
			if (c != exceptConnector) {
				send(p.getBytes(), c);
			}
		}
	}
	

	public void send (Packet p) {
		if ((msgFilter & p.getType().bit()) != 0) notification(".send packet (" + p.toString() + ") ");
		byte c = nbs().getConnectorNrTo(p.getDest());
		send(p.setSourceConnector(c).getBytes(), c);
	}


	public void send(byte[] bs, int connector) {
		Packet p = new Packet(bs);

		p.setSourceConnector((byte) connector);
		p.setSource(getId());
		p.addState(this);

		if ((msgFilter & p.getType().bit()) != 0) notification(".send = " + p.toString() + " over " + connector);
		
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
	
	public int getStateOperation() {
		return stateOperation;
	}
	
	public int getStatePending() {
		return statePending;
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
	
	public void renameSwitch (Module m1, Module m2) {
		if (getId() == m1 && !statePending(PENDING1) && !stateIsFinished()) {
			setId(m2);
			statePendingBroadcast(PENDING1);
		}
		if (getId() == m2 && !statePending(PENDING2) && !stateIsFinished()) {
			setId(m1);
			statePendingBroadcast(PENDING2);
		}
		
		
		
//		if (getId() == m2) {
			if (statePending(PENDING1 + PENDING2)) {
				stateInstrBroadcastNext();
			}
//		}
	}
	
	
	public void renameTo(Module to) {
		previousName = getId();
		setId(to);
		broadcastDiscover();
	}
	
	public void renameGroup(Grouping to) {
		notification("rename group");
		previousGrouping = getGrouping();
		setId(getId().swapGrouping(to));
		broadcastDiscover();
	}
	
	private void setId(Module id) {
		notification("$$$ rename " + getId() + " to " + id);
		getModule().setProperty("name", id.name());
		colorize();
	}
	
	
	public void renameRestore () {
		notification("$$$ restore name " + getId() + " to " + previousName);
		getModule().setProperty("name", previousName.name());
		colorize();
		broadcastDiscover();
	}


	public String getModuleInformation() {
		StringBuffer out = new StringBuffer();
		out.append("ID: " + getId() + "      [" + getOpStateName() + " # " + stateInstruction + " @ " + statePending + "]" + (stateIsFinished()? " // finished" : ""));
		out.append("\n");
		
		out.append("operation coordinator: " + stateOperationCoordinate);
		out.append("\n");
		
		out.append("angle: " + getAngle());
		out.append("\n");
		
		out.append("orientation flips: ");
		String flip = "";
		if (switchNorthSouth) flip = "NORTH-SOUTH ";
		if (switchEastWest) flip = "EAST-WEST";
		
		if (flip.equals("")) {
			flip = "<none>";
		}
		out.append(flip);
		out.append("\n");
		
		out.append("globals: " + varsGlobal);
		out.append("\n");
		
		out.append("gradients: " + gradients);
		out.append("\n");
		
		out.append("consensus (" + consensus.size() + "): " + consensus);
		out.append("\n");
		
		
		out.append("isrotating: " + isRotating());
		out.append("\n");

//		out.append("colors: ");
//		if (getColors() != null) {
//			out.append("(" + getColors()[0].getRed() + "," + getColors()[0].getGreen() + "," + getColors()[0].getBlue()
//					+ "),(" + getColors()[1].getRed() + ","
//					+ getColors()[1].getGreen() + "," + getColors()[1].getBlue());
//		}
//		else {
//			out.append("<not set>");
//		}
//
//		out.append("\n");

		out.append("neighbors: ");

		out.append(nbs().toString());

		out.append("\n");

		return out.toString();
	}

	public abstract String getOpStateName ();
	
	public boolean isOtherConnectorNearbyCallDisabled() {
		//TODO: This is a hack to work around the communication issue. 
		return stateConnectorNearbyCallDisabled;
	}


}
