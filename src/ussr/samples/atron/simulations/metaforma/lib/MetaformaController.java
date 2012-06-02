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

	private static final boolean SHOWTRAFFIC = false;
	protected DebugInformationProvider info;
	public HashMap<Grouping, HashMap<Module, Byte[]>> neighbors = new HashMap<Grouping, HashMap<Module, Byte[]>>();
	
	private HashMap<Module, Color[]> moduleColors = new HashMap<Module, Color[]>();
	private Color[] structureColors;
	
	protected int stateOperation = 0;
	protected int stateInstr = 0;
	protected int statePending = 0;
	private float stateStartTime;
	
	protected int errInPreviousState;
	protected int errInCurrentState;
	
	private boolean stateHasReceivedBroadcast = false;
	private boolean stateConnectorNearbyCallDisabled = false; // For connecting to neighbors
	
	private float timeLastBc = 0;
	protected boolean stateCurrentFinished;
	
	protected HashMap<Byte,Byte> globals = new HashMap<Byte,Byte>();
	protected HashMap<Byte,Byte> gradients = new HashMap<Byte,Byte>(); 
	private boolean northSouthSwitched = false; 
	
	
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
			gradients.put((byte)id, (byte)100);
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
		info.addNotification("## rotate " + degrees + ", current = " + angle + "; new = " + (degrees + angle) + "");
		angle = degrees + angle;
		doRotate(angle);
	}
	
	public void rotateTo(int degrees) {
		info.addNotification("## rotateTo " + degrees + ", current = " + angle);
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
		return super.isConnected(c);
	}


	public byte getConnectorToNb(Module nb, boolean otherModule) {
		if (getNeighbors(nb.getGrouping()).containsKey(nb)) {
			return getC(getNeighbors(nb.getGrouping()).get(nb)[otherModule? 1 : 0]);
		}
		else {
			//info.addNotification("Neighbor " + nb + " does not exist in table so can't find its connector number");
			return -1;
		}
	}

	protected float timeSpentInState() {
		return time() - stateStartTime;
	}

	protected byte oppositeConnector (int c) {
		//TODO: How to take into account rotation of two hemispheres?
		return  (byte)((c+4)%8);
	}
	
	public void connection(Module dest, boolean makeConnection) {
//		if (makeConnection) {
//			info.addNotification("#connection to " + dest);
//		}
//		else {
//			info.addNotification("#disconnection to " + dest);
//		}
		//keepSending(dest);
		//info.addNotification("done keepsending");
		byte conToNb = getConnectorToNb(dest,false);
		byte conFromNb = getConnectorToNb(dest,true);
		if (conToNb % 2 == 0 && conFromNb % 2 == 1) {
			// Male wants to connect to female. No further action needed
			if (makeConnection)
				connect(conToNb);
			else
				disconnect(conToNb);
//			info.addNotification("make (dis)connection to " + conToNb);
//			while (makeConnection && !isConnected(conToNb)
//					|| !makeConnection && isConnected(conToNb)) {
//				waiting();
//			}
//			info.addNotification("done (dis)connection to " + conToNb);
//			if (autoNextState) {
//				nextInstrState();
//			}
		}
//		if (conToNb % 2 == 1 && conFromNb % 2 == 0) {
//			// Female can not connect to male, so lets wait for other module
//			while (makeConnection && !isConnected(conToNb)
//					|| !makeConnection && isConnected(conToNb)) {
//				yield();
//			}
//		}
//		if (conToNb % 2 == conFromNb % 2) {
//			// Same gender for both connectors. Outch, one of the hemispheres needs to
//			// rotate!
//			info.addNotification("###I cant connect, connector numbers are: " + conToNb + "," + conFromNb);
//			if (getId().ord() < dest.ord()) {
//				angle = 90 + angle;
//				rotateToDegreeInDegrees(angle);
//				while (isRotating()) {
//					yield();
//				}
//				
//				neighbors.clear();
//				keepSending(dest);
//			}
//			else {
//				neighbors.clear();
//				delay(1000);
//				keepSending(dest);
//				
//			}
//			if (makeConnection)
//				connect(conToNb);
//			else
//				disconnect(conToNb);
//			while (makeConnection && !isConnected(conToNb)
//					|| !makeConnection && isConnected(conToNb)) {
//				waiting();
//			}
//			if (autoNextState) {
//				nextInstrState();
//			}
//		}
	}
	
	public void connect (Module m) {
		info.addNotification("## connect to " + m);
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
	
	
	protected boolean isC (int c, boolean only) {
		HashSet<Integer> s = new HashSet<Integer>();
		s.add(c);
		return isC(s, only);
	}
	
	protected boolean isC (int c1, int c2, boolean only) {
		HashSet<Integer> s = new HashSet<Integer>();
		s.add(c1);
		s.add(c2);
		return isC(s, only);
	}
	
	protected boolean isC (HashSet<Integer> conns, boolean only) {
		for (int i=0; i<8; i++) {
			if (isConnected(getC(i)) && only && !conns.contains(getC(i))) {
				return false;
			}
			if (!isConnected(getC(i)) && conns.contains(getC(i))) {
				return false;
			}
		}
		return true;
	}
	
	public void waitAndDiscover () {
		if (time() - timeLastBc  > 0.5) {
			discoverNeighbors();
		}
		delay(500);
		//info.addNotification(timeLastBc + " - " + time());
		yield();
	}
	
	
	
	public void disconnect(Module m) {
		info.addNotification("## disconnect to " + m);
		connection(m, false);
	}
	
	
	
//	public void connection (Module m1, Grouping g, boolean connection) {
//		if (connection) {
//			info.addNotification(".connect(" + m1 + "," + g + ")");
//			disableConnectorNearby();
//		}
//		else {
//			info.addNotification(".disconnect(" + m1 + "," + g + ")");
//		}
//
//		int initialstate = stateInstr;
//		if (m1 == getId()) {
//				broadcast(new Packet(getId(), Module.ALL)
//						.setType(Type.DISCOVER));
//				
//			delay(2000);
//			info.addNotification(getNeighbors(g).toString());
//			for (Module m2 : getNeighbors(g).keySet()) {
//				connection(getId(), m2, connection);
//			}
//			nextInstrState();
//		} else {
//			broadcast(new Packet(getId(), Module.ALL)
//					.setType(Type.DISCOVER));
//			delay(1000);
//			// Note: We have to wait here for some time, since we do not know
//			// how much modules in the structure are connected. 
//
//			if (getId().getGrouping() == g) {
//				if (hasNeighbor(m1,false)) {
//					connection(getId(), m1, connection);
//					connection(m1, getId(), connection);
//					//TODO: A module in a grouping does not know if the other modules are still connected, so we wait 1 second here.
//					// Ideally, m1 should do the nextState() call when all neighbors are (dis)connected.
//					// EDIT: this should be done by nextState(); in the above section
//				}
//			} else {
//				info.addNotification(getId().getGrouping() + " != " + g);
//			}
//		}
//
//		while (stateInstr == initialstate) {
//			yield();
//		}
//	}

//	public void connect(Module m, Grouping s) {
//		connection(m,s,true);
//	}
//	
//	public void disconnect(Module m, Grouping s) {
//		connection(m,s,false);
//	}
	
//	public void disconnect(Grouping dest) {
//		disconnect(getId(),dest);
//	}

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
		}
	}
	
	protected void nextPendingState(int newState) {
		if (statePending != (statePending|newState)) {
			statePending = statePending|newState;
			info.addNotification("## Pending state to " + newState);
		
			//delay(1000);
			broadcast(new Packet(getId()).setType(Type.STATE_PENDING_INCR).setData(statePending,stateInstr));
		}
	}
	
	protected boolean isPendingState (int state) {
		return (state&statePending) != 0;
	}
	
	protected void waitForPendingState (int state) {
		info.addNotification("# wait for pending state " + state);
		while (!isPendingState(state)) {yield();}
		//TODO: Since we are waiting for a next pending state update, it might have happened that it was not received, so send out a request for a new pending state update here
		info.addNotification("# wait for pending state done.");
	}

	protected void initInstrState() {
		neighbors.clear();
		stateHasReceivedBroadcast = false;
		stateConnectorNearbyCallDisabled = true;
		statePending = 0;
		stateStartTime = time();
		errInCurrentState = 0;
		colorize();
	}
	
	protected void increaseInstrState(int newState) {
		if (newState > stateInstr + 1) {
			info.addNotification("!!! I might have missed a state, from " + stateInstr + " to " + newState);
		}
		stateInstr = newState;
		info.addNotification("\nLeave instruction state " + (stateInstr-1) + ", time spent: " + timeSpentInState() + "\n----------------------------\nNew instruction state: " + stateInstr);
		initInstrState();
	}
	
		
	
	protected void increaseInstrState() {
		initInstrState();
		stateInstr++;
		info.addNotification("\n\ntime spent: " + timeSpentInState() + "\n=====================================\nnew instruction state: " + stateInstr);
	}
	
	protected void broadcastNewOperationState (int newState) {
		setNewOperationState(newState);
		broadcast(new Packet(getId(), Module.ALL).setType(Type.STATE_OPERATION_NEW).setData(stateOperation));
	}
	
	private void setNewOperationState (int newState) {
		stateOperation = newState;
		initInstrState();
		stateInstr = 0;
		stateCurrentFinished = false;
		info.addNotification("\n#############################\nnew operation state: " + getOpStateName() + "\n#############################\n");
	}
	
	
	public void disableConnectorNearby() {
		stateConnectorNearbyCallDisabled = true;
	}
	
	protected void nextInstrState() {
		stateInstr++;
		info.addNotification("\ntime spent: " + timeSpentInState() + "\n----------------------------\nNEW INSTRUCTION STATE: " + stateInstr);
		if (errInCurrentState == 1) {
			info.addNotification("\n(the following error occured in this state: " + errInCurrentState + ") ");
		}
		initInstrState();	
		//delay(1000);
		broadcast(new Packet(getId(), Module.ALL).setType(Type.STATE_INSTR_INCR).setData(stateInstr,stateOperation,errInCurrentState));
		
	}

	public float time() {
		return getModule().getSimulation().getTime();
	}

//	private void keepSending(Module dest) {
//		info.addNotification(".keepsending to " + dest);
//		if (dest == Module.ALL) {
//			System.err.println("keepsending all not allowed");
//			System.exit(0);
//		}
//
//		float t = time();
//		if (!hasNeighbor(dest)) {
//			System.err.println("***BROADCAST");
//			//this.getModule().getSimulation().setPause(true);
//
//			
//			while (!hasNeighbor(dest)) {
//				waiting();
//				delay(1000);
//				broadcast(new Packet(getId(), dest));
////				if (time() - 4 > t) {
////					broadcast(new Packet(getId(), dest).setData(99)); // 99 Discover packet
////				}
//			}
//		}
//		// info.addNotification("GOT nr " + dest + ": " + neighbors.get(dest)[0]
//		// + "," + neighbors.get(dest)[1]);
//	}

	public boolean hasNeighbor(Module m, boolean mustBeConnected) {
		if (getNeighbors(m.getGrouping()).containsKey(m)) {
			info.addNotification(".hasNeighbor " + m);
			return (!mustBeConnected || isConnected(getConnectorToNb(m, false)));
		} else {
			info.addNotification(".hasNeighbor " + m + " NOT");
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
		broadcast(new Packet(getId(), Module.ALL).setType(Type.DISCOVER).setData(12,getId().ordinal()));
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
			info.addNotification(".addNeighbor " + nb + " [" + conToNb + "," + conFromNb + "]");
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

	public void setId(Module id) {
		info.addNotification("$$$ rename " + getId() + " to " + id);
		getModule().setProperty("name", id.name());
		colorize();
	}

	public void colorize() {
		getModule().getComponent(0).setModuleComponentColor(getColors()[0]);
		getModule().getComponent(1).setModuleComponentColor(getColors()[1]);

		colorizeConnectors();
	}

	public void handleMessage(byte[] message, int messageLength, int connector) {
		Packet p = new Packet(message);
		
		connector = getC(connector);
		
		if (p.getType() == Type.CONNECTOR_NR && p.getDir() == Dir.REQ && p.getDest() == getId()) {
			// Indicates that sender is waiting for an answer, but it does not
			// come. So try a broadcast.
			broadcast(new Packet(getId(), p.getSource()).setType(Type.CONNECTOR_NR).setData(p.getSourceConnector()).setDir(Dir.ACK));
			info.addNotification("Answer to " + p.getSource()
					+ " does not arrive, so try broadcast.");
		}
		

		if (p.getType() == Type.CONNECTOR_NR && p.getDir() == Dir.ACK && stateHasReceivedBroadcast == false) {
			stateHasReceivedBroadcast = true;
			if (p.getDest() != getId()) {
				info.addNotification("spread broadcast!");
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
				if (p.getType() == Type.GRADIENT && getGradient(p.getData()[0]) > p.getData()[1]) {
					gradientCreate(p.getData()[0],p.getData()[1]);
				}
				
				if (p.getType() == Type.GRADIENT_RESET && p.getData()[0] != Byte.MAX_VALUE) {
					gradients.put(p.getData()[0], Byte.MAX_VALUE);
				}
				
				if (p.getType() == Type.STATE_INSTR_INCR && stateInstr < p.getData()[0] && stateOperation == p.getData()[1]) {
					increaseInstrState(p.getData()[0]);
					broadcast(new Packet(p),connector);
				}
				
				if (p.getType() == Type.STATE_OPERATION_NEW && stateOperation != p.getData()[0]) {
					setNewOperationState(p.getData()[0]);
					broadcast(new Packet(p),connector);
				}
								
				if (p.getType() == Type.GLOBAL_VAR && getGlobal(p.getData()[0]) != p.getData()[1]) {
					globals.put(p.getData()[0],p.getData()[1]);
					broadcast(new Packet(p),connector);
				}

				if (p.getType() == Type.STATE_PENDING_INCR && (p.getData()[0]|statePending)!= statePending  && stateInstr == p.getData()[1]) {
					statePending = statePending|p.getData()[0];
					broadcast(new Packet(p).setData(statePending),connector);
				}

				if (SHOWTRAFFIC)info.addNotification(".handleMessage = " + p.toString()	+ " over " + connector);

				if (p.getType() == Type.DISCOVER && p.getDir() == Dir.REQ) {
					send(p.getAck().getBytes(), connector);
				}
			}
		}
	}

	private byte getC(int connector) {
		if (northSouthSwitched) {
			return (byte) ((connector + 4) % 8);
		}
		else {
			return (byte)connector;
		}
	}

	public void broadcast (Packet p) {	
		broadcast (p,-1);
	}
		
	public void broadcast(Packet p,int exceptConnector) {
		if (SHOWTRAFFIC)info.addNotification(".broadcast packet (" + p.toString() + ") ");
		for (byte c = 0; c < 8; c++) {
			if (c != exceptConnector) {
				send(p.getBytes(), c);
			}
		}
	}

	public void send(Packet p) {
		if (SHOWTRAFFIC)info.addNotification(".send packet (" + p.toString() + ") ");
		byte c = getConnectorToNb(p.getDest(),false);
		send(p.setSourceConnector(c).getBytes(), c);
	}

	public void send(byte[] bs, int connector) {
		Packet p = new Packet(bs);
		if (p.getType() != Type.CONNECTOR_NR) {
			p.setSourceConnector((byte) connector);
			p.setSource(getId());
		}

		if (SHOWTRAFFIC)info.addNotification(".send = " + p.toString() + " over " + connector);
		if (connector < 0 || connector > 7) {
			System.err.println("Connector has invalid nr " + connector);
		}
		
		sendMessage(p.getBytes(), (byte) p.getBytes().length, (byte) connector,p.getSource().toString(),p.getDest().toString());
	}

	public DebugInformationProvider info() {
		if (info == null) {
			info = this.getModule().getDebugInformationProvider();
		}
		return info;
	}

	public int getStateInstruction() {
		return stateInstr;
	}
	
	public int getStateOperation() {
		return stateOperation;
	}
	
	public int getStatePending() {
		return statePending;
	}
	
	public void nextStatePending (int i) {
		statePending|=i;
		broadcast(new Packet(getId(), Module.ALL).setData(statePending).setType(Type.STATE_PENDING_INCR));
	}
	
	

	private void colorizeConnectors() {
		module.getConnectors().get(0).setColor(Color.BLUE);
		module.getConnectors().get(1).setColor(Color.BLACK);
		module.getConnectors().get(2).setColor(Color.RED);
		module.getConnectors().get(3).setColor(Color.WHITE);
		module.getConnectors().get(4).setColor(Color.BLUE);
		module.getConnectors().get(5).setColor(Color.BLACK);
		module.getConnectors().get(6).setColor(Color.RED);
		module.getConnectors().get(7).setColor(Color.WHITE);
	}

	// for (int i=3; i<=9; i++) {
	// renameFromTo
	// (Module.getOnNumber(Structure.Structure,i),Module.getOnNumber(Structure.Structure,i-3));
	// }
	// colorize();

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
	
	public void renameSwitch (Module m1, Module m2) {
		if (getId() == m1 && !stateCurrentFinished) {
			setId(m2);
			stateCurrentFinished = true;
		}
		if (getId() == m2 && !stateCurrentFinished) {
			setId(m1);
			stateCurrentFinished = true;
			nextInstrState();
		}
	}
	
	
	public void renameTo(Module to) {
		setId(to);
		discoverNeighbors();
		
		//System.out.println("###DOOO rename from " + getId() + " to " + to);
	}

	public void renameFromTo(Module from, Module to) {
		if (getId() == from) {
			renameTo(to);
		}
	}

	public String getModuleInformation() {
		StringBuffer out = new StringBuffer();
		out.append("ID: " + getId());
		
		out.append("\n");
		
		out.append("angle: " + getAngle());

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
		out.append("[" + getOpStateName() + " # " + stateInstr + " @ " + statePending + "]");

		
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
