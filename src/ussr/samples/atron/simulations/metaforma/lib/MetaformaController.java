package ussr.samples.atron.simulations.metaforma.lib;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import ussr.model.debugging.ControllerInformationProvider;
import ussr.model.debugging.DebugInformationProvider;
import ussr.samples.atron.ATRONController;
import ussr.samples.atron.simulations.metaforma.gen.*;
import ussr.samples.atron.simulations.metaforma.lib.Packet;

public abstract class MetaformaController extends ATRONController implements ControllerInformationProvider{

	
	protected DebugInformationProvider info;
	public HashMap<Grouping, HashMap<Module, Byte[]>> neighbors = new HashMap<Grouping, HashMap<Module, Byte[]>>();
	
	private HashMap<Module, Color[]> moduleColors = new HashMap<Module, Color[]>();
	private Color[] structureColors;
	
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
	
	protected HashMap<Byte,Byte> globals = new HashMap<Byte,Byte>();
	protected HashMap<Byte,Byte> gradients = new HashMap<Byte,Byte>(); 
	
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
	private float stateLastBroadcast;
	private boolean switchEastWest;
	private boolean switchNorthSouth;
	private byte SHOWTRAFFIC = (byte) (Type.FIX_DIRECTION.bit() | Type.GRADIENT.bit());
	
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
		return stateInstruction == state;
	}
	
	public void switchEastWest () {
		switchEastWest =! switchEastWest;
		notification("$$$ Switch East West");
		
		updateNeighborsSymmetryEW();
		
		colorize();

	}
	
	
	
	public void switchNorthSouth () {
		 switchNorthSouth =! switchNorthSouth;
		 notification("$$$ Switch North South");
		 
		 updateNeighborsSymmetryNS();
		 
		 colorize();
	}
	
	private void updateNeighborsSymmetryNS () {
		for (Map.Entry<Module, Byte[]> entry : nbs().entrySet()) {
			addNeighbor(entry.getKey(), (entry.getValue()[0] + 4) % 8, entry.getValue()[1]);
		}
	}
	
	private void updateNeighborsSymmetryEW () {
		for (Map.Entry<Module, Byte[]> entry : nbs().entrySet()) {
			if (entry.getValue()[0] < 4) {
				addNeighbor(entry.getKey(), (entry.getValue()[0] + 2) % 4, entry.getValue()[1]);
			}
			else {
				addNeighbor(entry.getKey(), ((entry.getValue()[0] + 2) % 4) + 4, entry.getValue()[1]);
			}
		}
	}
	
	public void gradientCreate(int id, int value) {
		gradients.put((byte)id, (byte)value);
		broadcast(new Packet(getId()).setType(Type.GRADIENT).setData(id,value + 1));
	}
	
	public void gradientReset(int id) {
		gradients.put((byte)id, (byte)0);
		broadcast(new Packet(getId()).setType(Type.GRADIENT_RESET).setData(id));
	}
	
	
	public int getGradient(int id) {
		if (!gradients.containsKey((byte)id)) {
			gradients.put((byte)id, Byte.MAX_VALUE);
		}
		return gradients.get((byte)id);
	}
	
	
	public void setGlobal (int index, int value) {
		globals.put((byte)index, (byte)value);
		broadcast(new Packet(getId(),Module.ALL).setData(index,value).setType(Type.GLOBAL_VAR));
	}
	
	public byte getGlobal (int index) {
		if (globals.containsKey(index)) {
			return globals.get(index);
		}
		return 0;
	}
	
	
	
	
	public void rotate(int degrees) {
		notification("## rotate " + degrees + ", current = " + angle + "; new = " + (degrees + angle) + "");
		angle = degrees + angle;
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


	public byte getConnectorToNb(Module nb, boolean otherModule) {
		if (getNeighbors(nb.getGrouping()).containsKey(nb)) {
			return getNeighbors(nb.getGrouping()).get(nb)[otherModule? 1 : 0];
		}
		else {
			//notification("Neighbor " + nb + " does not exist in table so can't find its connector number");
			return -1;
		}
	}

	private float round (float f, int decimals) {
		return (float) (Math.round(f * Math.pow(10,decimals)) / Math.pow(10,decimals));
	}
	
	protected float timeSpentInState() {
		return round((time() - stateStartTime),3);
	}

	protected byte oppositeConnector (int c) {
		//TODO: How to take into account rotation of two hemispheres?
		return  (byte)((c+4)%8);
	}
	
	public void connection(Module dest, boolean makeConnection) {
		byte conToNb = getConnectorToNb(dest,false);
		byte conFromNb = getConnectorToNb(dest,true);
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

	public void waiting() {
		yield();
	}
	
	
	public void waitAndDiscover () {
		if (time() - timeLastBc  > 0.5) {
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

	public abstract void handleStates();

	public abstract void setColors();

	public void activate() {
		setup();
		info = this.getModule().getDebugInformationProvider();
		setColors();


		while (true) {
			refresh();

			handleStates();
			yield();
			if (time() - 10 > stateLastBroadcast) {
				broadcast(new Packet(getId()).setType(Type.STATE_INSTR_UPDATE));
				stateLastBroadcast = time();
			}
		}
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
	
	
	protected void notification (String msg) {
		info.addNotification("[" + round(time(),1) + "] - " + msg);
	}

	protected void initInstrState() {
		neighbors.clear();
		stateHasReceivedBroadcast = false;
		stateConnectorNearbyCallDisabled = true;
		stateCurrentFinished = false;
		statePending = 0;
		stateStartTime = time();
		errInCurrentState = 0;
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
		notification("\n\n=================================\nLeft instruction state " + (stateInstruction-1) + ", time spent: " + timeSpentInState() + "\nNew instruction state: " + stateInstruction + "\n=================================");
	}
	
	protected void stateOperationBroadcast (int newState) {
		setNewOperationState(newState);
		stateLastBroadcast = time();
		broadcast(new Packet(getId(), Module.ALL).setType(Type.STATE_OPERATION_NEW).setData(stateOperation));
	}
	
	private void setNewOperationState (int newState) {
		stateOperation = newState;
		initInstrState();
		stateInstruction = 0;
		notification("\n\n#############################\nnew operation state: " + getOpStateName() + "\n#############################\n");
	}
	
	
	public void disableConnectorNearby() {
		stateConnectorNearbyCallDisabled = true;
	}
	
	protected void stateInstructionBroadcastNext(int newState) {
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


	public boolean hasNeighbor(Module m, boolean mustBeConnected) {
		if (getNeighbors(m.getGrouping()).containsKey(m)) {
			//notification(".hasNeighbor " + m);
			return (!mustBeConnected || isConnected(getConnectorToNb(m, false)));
		} else {
			//notification(".hasNeighbor " + m + " NOT");
			return false;
		}
	}
	
	public boolean hasNeighbor (Grouping g, boolean mustBeConnected) {
		if (!mustBeConnected) {
			return (getNeighbors(g).size() > 0);
		}
		else {
			for (Byte conn[]: getNeighbors(g).values()) {
				if (isConnected(conn[0])) {
					return true;
				}
					
			}
			return false;
		}
	} 
	
	
	public void discoverNeighbors () {
		timeLastBc = time();
		broadcast(new Packet(getId()));
		delay(500);
	}

	private void neighborRemove (Module nb) {
		getNeighbors(nb.getGrouping()).remove(nb);
	}
	
	private HashMap<Module, Byte[]> getNeighbors(Grouping g) {
		if (!neighbors.containsKey(g)) {
			neighbors.put(g, new HashMap<Module, Byte[]>());
		}

		return neighbors.get(g);
	}
	
	public HashMap<Module, Byte[]> nbs() {
		HashMap<Module, Byte[]> ret = new HashMap<Module, Byte[]>();
		for (Entry<Grouping, HashMap<Module, Byte[]>> entryGroup : neighbors.entrySet()) {
			for (Entry<Module, Byte[]> entry : entryGroup.getValue().entrySet()) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}
	
	
	public HashMap<Module, Byte[]> connected(HashMap<Module, Byte[]>neighbors) {
		return connected(neighbors,true);
	}
	
	public HashMap<Module, Byte[]> disconnected(HashMap<Module, Byte[]>neighbors) {
		return connected(neighbors,false);
	}
	
	private HashMap<Module, Byte[]> connected(HashMap<Module, Byte[]>neighbors, boolean connected) {
		HashMap<Module, Byte[]> ret = new HashMap<Module, Byte[]>();
		for (Map.Entry<Module, Byte []> e : neighbors.entrySet()) {
			if (isConnected(e.getValue()[0]) == connected) {
				ret.put(e.getKey(),e.getValue());
			}
		}
		return ret;
	}
	
	public HashMap<Module, Byte[]> maleAligned (HashMap<Module, Byte[]> neighbors) {
		HashMap<Module, Byte[]> ret = new HashMap<Module, Byte[]>();
		for (Map.Entry<Module, Byte[]>entry : neighbors.entrySet()) {
			if (entry.getValue()[0]%2 == 0 && entry.getValue()[1]%2 == 1) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}
	
	public HashMap<Module, Byte[]> onGroup (HashMap<Module, Byte[]> neighbors, Grouping g) {
		HashMap<Module, Byte[]> ret = new HashMap<Module, Byte[]>();
		for (Map.Entry<Module, Byte[]>entry : neighbors.entrySet()) {
			if (entry.getKey().getGrouping().equals(g)) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}
	
	public HashMap<Module, Byte[]> male (HashMap<Module, Byte[]> neighbors) {
		HashMap<Module, Byte[]> ret = new HashMap<Module, Byte[]>();
		for (Map.Entry<Module, Byte[]>entry : neighbors.entrySet()) {
			if (MALE(entry.getValue()[0])) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}
	
	public HashMap<Module, Byte[]> female (HashMap<Module, Byte[]> neighbors) {
		HashMap<Module, Byte[]> ret = new HashMap<Module, Byte[]>();
		for (Map.Entry<Module, Byte[]>entry : neighbors.entrySet()) {
			if (FEMALE(entry.getValue()[0])) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}
	
	public HashMap<Module, Byte[]> west (HashMap<Module, Byte[]> neighbors) {
		HashMap<Module, Byte[]> ret = new HashMap<Module, Byte[]>();
		for (Map.Entry<Module, Byte[]>entry : neighbors.entrySet()) {
			if (WEST(entry.getValue()[0])) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}
	
	public HashMap<Module, Byte[]> north (HashMap<Module, Byte[]> neighbors) {
		HashMap<Module, Byte[]> ret = new HashMap<Module, Byte[]>();
		for (Map.Entry<Module, Byte[]>entry : neighbors.entrySet()) {
			if (NORTH(entry.getValue()[0])) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}
	
	public HashMap<Module, Byte[]> east (HashMap<Module, Byte[]> neighbors) {
		HashMap<Module, Byte[]> ret = new HashMap<Module, Byte[]>();
		for (Map.Entry<Module, Byte[]>entry : neighbors.entrySet()) {
			if (EAST(entry.getValue()[0])) {
				ret.put(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}

	public Module onConnector (HashMap<Module, Byte[]> neighbors,int c) {
		for (Map.Entry<Module, Byte[]>entry : neighbors.entrySet()) {
			if (entry.getValue()[0] == c) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public boolean contains (HashMap<Module, Byte[]> neighbors,Module m) {
		return neighbors.containsKey(m);
	}
	
	public boolean exists (HashMap<Module, Byte[]> neighbors) {
		return !neighbors.isEmpty();
	}
	
	public boolean exists (Module m) {
		return m != null;
	}
	

	public void addNeighbor(Module nb, int conToNb, int conFromNb) {
		if (getConnectorToNb(nb, false) != conToNb || getConnectorToNb(nb, true) != conFromNb) {
			if (exists(onConnector(nbs(),conToNb))) {
				neighborRemove(onConnector(nbs(),conToNb));
			}
			notification(".addNeighbor " + nb + " [" + conToNb + "," + conFromNb + "]");
			getNeighbors(nb.getGrouping()).put(nb,
					new Byte[] { (byte) conToNb, (byte) conFromNb });
		}
	}
	
	
	

	public String getNeighborsString() {
		String r = "  \n";

		for (Map.Entry<Grouping, HashMap<Module, Byte[]>> e : neighbors
				.entrySet()) {
			if (!getNeighbors(e.getKey()).isEmpty()) {
				for (Map.Entry<Module, Byte[]> f : e.getValue().entrySet()) {
					String m = f.getKey() + " [" + f.getValue()[0] + ", "
							+ f.getValue()[1] + "], ";
					if (isConnected(f.getValue()[0])) {
						m = m.toUpperCase();
					}
					r += m;
					
				}
				r = r.substring(0, r.length() - 2);
			}
			r += "\n ";
		}
		return r.substring(0, r.length() - 2);
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

		notification("NORTH_MALE_WEST " + NORTH_MALE_WEST + " - " + C(NORTH_MALE_WEST) + " - " + module.getConnectors().get(C(NORTH_MALE_WEST)).hashCode());
		notification("NORTH_MALE_EAST " + NORTH_MALE_EAST + " - " + C(NORTH_MALE_EAST) + " - " + module.getConnectors().get(C(NORTH_MALE_EAST)).hashCode());
		module.getConnectors().get(C(NORTH_MALE_WEST)).setColor(Color.BLUE);
		module.getConnectors().get(C(NORTH_FEMALE_WEST)).setColor(Color.BLACK);
		module.getConnectors().get(C(NORTH_MALE_EAST)).setColor(Color.RED);
		module.getConnectors().get(C(NORTH_FEMALE_EAST)).setColor(Color.WHITE);
		
		module.getConnectors().get(C(SOUTH_MALE_WEST)).setColor(Color.GREEN);
		module.getConnectors().get(C(SOUTH_FEMALE_WEST)).setColor(Color.BLACK);
		module.getConnectors().get(C(SOUTH_MALE_EAST)).setColor(Color.RED);
		module.getConnectors().get(C(SOUTH_FEMALE_EAST)).setColor(Color.WHITE);
		if (switchEastWest) {
			notification("switchEastWest");
			module.getConnectors().get(0).setColor(Color.RED);
			module.getConnectors().get(4).setColor(Color.RED);
		}
		notification("NORTH_MALE_WEST " + NORTH_MALE_WEST + " - " + C(NORTH_MALE_WEST) + " - " + module.getConnectors().get(C(NORTH_MALE_WEST)).hashCode());
		notification("NORTH_MALE_EAST " + NORTH_MALE_EAST + " - " + C(NORTH_MALE_EAST) + " - " + module.getConnectors().get(C(NORTH_MALE_EAST)).hashCode());
	}
	
	public boolean NORTH(int c) {
		return c >= 0 && c <= 3;
	}
	
	public boolean SOUTH(int c) {
		return c >= 4 && c <= 7;
	}
	
	public boolean WEST(int c) {
		return c == 0 || c == 1 || c == 4 || c == 5;
	}
	
	public boolean EAST(int c) {
		return c == 2 || c == 3 || c == 6 || c == 7;
	}
	
	public boolean MALE(int c) {
		return c%2 == 0;
	}
	
	public boolean FEMALE(int c) {
		return c%2 == 1;
	}


	public void handleMessage(byte[] message, int messageLength, int connectorNr) {
		byte connector = C(connectorNr);
		Packet p = new Packet(message);
		
//		if (p.getType() != Type.p.getStateInstruction() < stateInstruction) {
//			notification("");
//		}
		
		if (p.getType() == Type.CONNECTOR_NR && p.getDir() == Dir.REQ && p.getDest() == getId()) {
			if (p.getDest() == getId() || p.getDest() == Module.ALL) {
				if ((SHOWTRAFFIC & p.getType().bit()) != 0) notification(".handleMessage = " + p.toString()	+ " over " + connector);
			}
			
			// Indicates that sender is waiting for an answer, but it does not
			// come. So try a broadcast.
			broadcast(new Packet(getId(), p.getSource()).setType(Type.CONNECTOR_NR).setData(p.getSourceConnector()).setDir(Dir.ACK));
			notification("Answer to " + p.getSource()
					+ " does not arrive, so try broadcast.");
		}
		

		if (p.getType() == Type.CONNECTOR_NR && p.getDir() == Dir.ACK && stateHasReceivedBroadcast == false) {
			stateHasReceivedBroadcast = true;
			if (p.getDest() != getId()) {
				notification("spread broadcast!");
				broadcast(p,connector);
			}
			else {
				addNeighbor(p.getSource(), p.getData()[0], 1);	// We use an odd number 1 here for a female connector
			}
		}
		
		if (p.getType() != Type.CONNECTOR_NR) {
			addNeighbor(p.getSource(), connector, p.getSourceConnector());

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
			
				// TODO
				if (!stateIsFinished()) {
					if (p.getType() == Type.FIX_DIRECTION) {
						
						if (FEMALE(connector)) {
							if (!SOUTH(connector)) {
								switchNorthSouth();
							}
							if (NORTH(p.getSourceConnector()) && !WEST(connector) || SOUTH(p.getSourceConnector()) && !EAST(connector)) {
								switchEastWest();
							}
							
							send(p.getBytes(),(NORTH_FEMALE_WEST));
							send(p.getBytes(),(NORTH_FEMALE_EAST));
						}
						else if (MALE(connector)) {
							if (!EAST(connector)) {
								switchEastWest();
							}
							if (EAST(p.getSourceConnector()) && !SOUTH(connector) || WEST(p.getSourceConnector()) && !NORTH(connector)) {
								switchNorthSouth();
							}
							
							send(p.getBytes(),(NORTH_MALE_WEST));
							send(p.getBytes(),(SOUTH_MALE_WEST));
						}
						
						stateFinish();
					}
				}
					
				if (p.getType() == Type.GRADIENT && getGradient(p.getData()[0]) > p.getData()[1]) {
					gradientCreate(p.getData()[0],p.getData()[1]);
				}
				
				if (p.getType() == Type.GRADIENT_RESET && p.getData()[0] != Byte.MAX_VALUE) {
					gradients.put(p.getData()[0], Byte.MAX_VALUE);
				}
				
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
				
				if (p.getType() == Type.STATE_OPERATION_NEW && stateOperation != p.getData()[0]) {
					setNewOperationState(p.getData()[0]);
					broadcast(new Packet(p),connector);
				}
								
				if (p.getType() == Type.GLOBAL_VAR && getGlobal(p.getData()[0]) != p.getData()[1]) {
					globals.put(p.getData()[0],p.getData()[1]);
					broadcast(new Packet(p),connector);
				}

				


				if (p.getType() == Type.DISCOVER && p.getDir() == Dir.REQ) {
					send(p.getAck().getBytes(), connector);
				}
			}
		}
	}


	public void broadcast (Packet p) {	
		broadcast (p,-1);
	}
		
	public void broadcast(Packet p,int exceptConnector) {
		if ((SHOWTRAFFIC & p.getType().bit()) != 0)  notification(".broadcast packet (" + p.toString() + ") ");
		for (byte c = 0; c < 8; c++) {
			if (c != exceptConnector) {
				send(p.getBytes(), c);
			}
		}
	}

	public void send(Packet p) {
		if ((SHOWTRAFFIC & p.getType().bit()) != 0) notification(".send packet (" + p.toString() + ") ");
		byte c = getConnectorToNb(p.getDest(),false);
		send(p.setSourceConnector(c).getBytes(), c);
	}

	public void send(byte[] bs, int connector) {
		Packet p = new Packet(bs);
		if (p.getType() != Type.CONNECTOR_NR) {
			p.setSourceConnector((byte) connector);
			p.setSource(getId());
			p.addState(this);
		}

		if ((SHOWTRAFFIC & p.getType().bit()) != 0) notification(".send = " + p.toString() + " over " + connector);
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

	public int getStateInstruction() {
		return stateInstruction;
	}
	
	public int getStateOperation() {
		return stateOperation;
	}
	
	public int getStatePending() {
		return statePending;
	}
	

	
	public void addStructureColors(Color[] colors) {
		structureColors = colors;
	}

	public void setModuleColors(Module m, Color[] colors) {
		moduleColors.put(m, colors);
	}

	public Color[] getColors() {
		if (moduleColors.containsKey(getId())) {
			return moduleColors.get(getId());
		} else {
			return structureColors;
		}

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
		
		
		
		if (getId() == m2) {
			if (statePending(PENDING1 + PENDING2)) {
				stateInstrBroadcastNext();
			}
		}
	}
	
	
	public void renameTo(Module to) {
		previousName = getId();
		setId(to);
		discoverNeighbors();
	}
	
	private void setId(Module id) {
		notification("$$$ rename " + getId() + " to " + id);
		getModule().setProperty("name", id.name());
		colorize();
	}
	
	
	public void renameRestore () {
		notification("$$$ restore name " + getId() + " to " + previousName);
		getModule().setProperty("name", previousName.name());
		discoverNeighbors();
	}


	public String getModuleInformation() {
		StringBuffer out = new StringBuffer();
		out.append("ID: " + getId());
		
		out.append("\n");
		
		out.append("angle: " + getAngle());

		out.append("\n");
		
		out.append("orientationSwitched NS: " + switchNorthSouth);
		out.append("\n");
		out.append("orientationSwitched EW: " + switchEastWest);
		
		out.append("\n");
		
		out.append("currentFinished: " + stateCurrentFinished);
		
		out.append("\n");
		
		out.append("connectornearbydisabled: " + isOtherConnectorNearbyCallDisabled());
		
		out.append("\n");
		
		out.append("globals: " + globals);
		
		out.append("\n");
		
		out.append("gradients: " + gradients);
		
		out.append("\n");
		
		out.append("isrotating: " + isRotating());

		out.append("\n");
		
		
		out.append("\n");
		
		out.append("current state: ");
		out.append("[" + getOpStateName() + " # " + stateInstruction + " @ " + statePending + "]");

		
		out.append("\n");

		out.append("colors: ");
		if (getColors() != null) {
			out.append("(" + getColors()[0].getRed() + "," + getColors()[0].getGreen() + "," + getColors()[0].getBlue()
					+ "),(" + getColors()[1].getRed() + ","
					+ getColors()[1].getGreen() + "," + getColors()[1].getBlue());
		}
		else {
			out.append("<not set>");
		}

		out.append("\n");

		out.append("neighbors: ");

		out.append(getNeighborsString());

		out.append("\n");

		return out.toString();
	}

	public abstract String getOpStateName ();
	
	public boolean isOtherConnectorNearbyCallDisabled() {
		//TODO: This is a hack to work around the communication issue. 
		return stateConnectorNearbyCallDisabled;
	}


}
