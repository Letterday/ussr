package ussr.samples.atron.simulations.metaforma.lib;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import ussr.model.debugging.ControllerInformationProvider;
import ussr.model.debugging.DebugInformationProvider;
import ussr.samples.atron.ATRONController;
import ussr.samples.atron.simulations.metaforma.gen.*;

public abstract class MetaformaController extends ATRONController implements ControllerInformationProvider{

	protected DebugInformationProvider info;
	private HashMap<Grouping, HashMap<Module, Byte[]>> neighbors = new HashMap<Grouping, HashMap<Module, Byte[]>>();
	private HashMap<Module, Color[]> moduleColors = new HashMap<Module, Color[]>();
	protected int state = 0;
	boolean hasReceivedBroadcast = false;
	private Color[] structureColors;
	private boolean connectorNearbyCallDisabled;

	public void rotate(Module m, int degrees) {
		if (getId() == m) {
			info.addNotification("#rotate " + degrees + ", current = " + angle + "; new = " + (degrees + angle) + "");
			angle = degrees + angle;
			rotateToDegreeInDegrees(angle);
			while (isRotating()) {
				yield();
			}

			nextState();
		}

	}

	public void connectionOnConn(byte cSource, boolean makeConnection) {
		if (makeConnection)
			connect(cSource);
		else
			disconnect(cSource);
		info.addNotification("make (dis)connection on nr");
		while (makeConnection && !isConnected(cSource) || !makeConnection
				&& isConnected(cSource)) {
			waiting();
		}
		info.addNotification("done (dis)connection on nr");
		nextState();
	}

	public byte getConnectorToNb(Module nb) {
		return neighbors.get(nb.getGrouping()).get(nb)[0];
	}

	public byte getConnectorFromNb(Module nb) {
		return neighbors.get(nb.getGrouping()).get(nb)[1];
	}

	public void connection(Module m1, Module m2, boolean makeConnection,boolean autoNextState) {
		if (getId() == m1) {
			info.addNotification("#(dis)connection " + m1 + "," + m2);
			keepSending(m2);
			info.addNotification("done keepsending");
			byte conToNb = getConnectorToNb(m2);
			byte conFromNb = getConnectorFromNb(m2);
			if (conToNb % 2 == 0 && conFromNb % 2 == 1) {
				// Male wants to connect to female. No further action needed
				if (makeConnection)
					connect(conToNb);
				else
					disconnect(conToNb);
				info.addNotification("make (dis)connection");
				while (makeConnection && !isConnected(conToNb)
						|| !makeConnection && isConnected(conToNb)) {
					waiting();
				}
				info.addNotification("done (dis)connection");
				if (autoNextState) {
					nextState();
				}
			}
			if (conToNb % 2 == 1 && conFromNb % 2 == 0) {
				// Female can not connect to male, so lets wait for other module
				while (makeConnection && !isConnected(conToNb)
						|| !makeConnection && isConnected(conToNb)) {
					yield();
				}
			}
			if (conToNb % 2 == conFromNb % 2) {
				// Same gender for both connectors. Outch, one of the hemispheres needs to
				// rotate!
				info.addNotification("###I cant connect, connector numbers are: " + conToNb + "," + conFromNb);
				if (getId().ord() < m2.ord()) {
					angle = 90 + angle;
					rotateToDegreeInDegrees(angle);
					while (isRotating()) {
						yield();
					}
					
					neighbors.clear();
					keepSending(m2);
				}
				else {
					neighbors.clear();
					delay(1000);
					keepSending(m2);
					
				}
				if (makeConnection)
					connect(conToNb);
				else
					disconnect(conToNb);
				while (makeConnection && !isConnected(conToNb)
						|| !makeConnection && isConnected(conToNb)) {
					waiting();
				}
				if (autoNextState) {
					nextState();
				}
			}
		}
	}

	public void waiting() {
		yield();
	}

	public HashMap<Module, Byte[]> getNeighbors(Grouping s) {
		if (!neighbors.containsKey(s)) {
			neighbors.put(s, new HashMap<Module, Byte[]>());
		}

		return neighbors.get(s);
	}

	public void addNeighbor(Module nb, int conToNb, int conFromNb) {
		getNeighbors(nb.getGrouping()).put(nb,
				new Byte[] { (byte) conToNb, (byte) conFromNb });
	}

	public byte getNbConnector(Module nb) {
		return neighbors.get(nb.getGrouping()).get(nb)[0];
	}

	

	public void disconnect(Module m1, Module m2) {
		//info.addNotification(".disconnect(" + m1 + "," + m2 + ")");
		connection(m1, m2, false, true);
		connection(m2, m1, false, true);
	}

	public void connect(Module m1, Module m2) {
		info.addNotification(".connect(" + m1 + "," + m2 + ")");
		disableConnectorNearby();
	
		connection(m1, m2, true, true);
		connection(m2, m1, true, true);
	}
	
	public void connection (Module m1, Grouping s, boolean connection) {
		if (connection) {
			info.addNotification(".connect(" + m1 + "," + s + ")");
			disableConnectorNearby();
		}
		else {
			info.addNotification(".disconnect(" + m1 + "," + s + ")");
		}

		int initialstate = state;
		if (m1 == getId()) {
				broadcast(new MetaformaPacket(getId(), Module.ALL)
						.setType(Type.DISCOVER));
				
			delay(2000);
			info.addNotification(getNeighbors(s).toString());
			for (Module m2 : getNeighbors(s).keySet()) {
				connection(getId(), m2, connection,false);
			}
			nextState();
		} else {
			broadcast(new MetaformaPacket(getId(), Module.ALL)
					.setType(Type.DISCOVER));
			delay(1000);
			// Note: We have to wait here for some time, since we do not know
			// how much modules in the structure are connected. 

			if (getId().getGrouping() == s) {
				if (hasNeighbor(m1)) {
					connection(getId(), m1, connection, false);
					connection(m1, getId(), connection, false);
					//TODO: A module in a grouping does not know if the other modules are still connected, so we wait 1 second here.
					// Ideally, m1 should do the nextState() call when all neighbors are (dis)connected.
					// EDIT: this should be done by nextState(); in the above section
				}
			} else {
				info.addNotification(getId().getGrouping() + " != " + s);
			}
		}

		while (state == initialstate) {
			yield();
		}
	}

	public void connect(Module m, Grouping s) {
		connection(m,s,true);
	}
	
	public void disconnect(Module m, Grouping s) {
		connection(m,s,false);
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

		float t = time();
		while (true) {
			refresh();

			handleStates();
			yield();
		}
	}

	protected void initNewState() {
		neighbors.clear();
		hasReceivedBroadcast = false;
		connectorNearbyCallDisabled = false;
		colorize();
		state++;
	}
	
	protected void increaseState(int newState) {
		if (newState > state + 1) {
			info.addNotification("!!! I have missed a state, from " + newState + " to " + state);
			state = newState-1;
		}
		increaseState();
	}

	
	protected void increaseState() {
		initNewState();
//		info.putStateInformation(Integer.toString(state));
		
		info.addNotification("\n----------------------------\nnew state: " + state);
	}
	
	public void disableConnectorNearby() {
		connectorNearbyCallDisabled = true;
	}
	
	protected void nextState() {
		initNewState();
		
		info.addNotification("\n----------------------------\nNEW STATE: " + state);
		
		delay(1000);
		broadcast(new MetaformaPacket(getId(), Module.ALL).setType(Type.STATE).setData(state));
	}

	public float time() {
		return getModule().getSimulation().getTime();
	}

	private void keepSending(Module dest) {
		info.addNotification(".keepsending to " + dest);
		if (dest == Module.ALL) {
			System.err.println("keepsending all not allowed");
			System.exit(0);
		}

		float t = time();
		if (!hasNeighbor(dest)) {
			System.err.println("***BROADCAST");
			//this.getModule().getSimulation().setPause(true);

			
			while (!hasNeighbor(dest)) {
				waiting();
				delay(1000);
				broadcast(new MetaformaPacket(getId(), dest));
//				if (time() - 4 > t) {
//					broadcast(new MetaformaPacket(getId(), dest).setData(99)); // 99 Discover packet
//				}
			}
		}
		// info.addNotification("GOT nr " + dest + ": " + neighbors.get(dest)[0]
		// + "," + neighbors.get(dest)[1]);
	}

	private boolean hasNeighbor(Module n) {
		if (getNeighbors(n.getGrouping()).containsKey(n)) {
			info.addNotification(".hasNeighbor " + n);
			return true;
		} else {
			info.addNotification(".hasNeighbor " + n + " NOT");
			return false;
		}

	}

	private HashMap<Module, Byte[]> getNbConnectors() {
		return neighbors.get(getId().getGrouping());
	}

	public String getNeighborsString() {
		String r = "  \n";

		for (Map.Entry<Grouping, HashMap<Module, Byte[]>> e : neighbors
				.entrySet()) {
			if (!getNeighbors(e.getKey()).isEmpty()) {
				for (Map.Entry<Module, Byte[]> f : e.getValue().entrySet()) {
					r += f.getKey() + " [" + f.getValue()[0] + ", "
							+ f.getValue()[1] + "], ";
				}
				r = r.substring(0, r.length() - 2);
			}
			r += "\n ";
		}
		return r.substring(0, r.length() - 2);
	}

	public void delay(int ms) {
		float stopTime = module.getSimulation().getTime() + ms / 1000f;
		info.addNotification("start waiting");
		while (stopTime > module.getSimulation().getTime()) {

			yield();
		}
		info.addNotification("end waiting");
	}

	public Module getId() {
		return Module.valueOf(getName());
	}

	public void setId(Module id) {
		System.out.println(".setId()");
		info.addNotification(getId() + " renamed to " + id);
		getModule().setProperty("name", id.name());
		colorize();
	}

	public void colorize() {
		getModule().getComponent(0).setModuleComponentColor(getColors()[0]);
		getModule().getComponent(1).setModuleComponentColor(getColors()[1]);

		colorizeConnectors();
	}

	public void handleMessage(byte[] message, int messageLength, int connector) {

		MetaformaPacket p = new MetaformaPacket(message);
		
		
		if (p.getData() == 99 && p.getDest() == getId()) {
			
			// Indicates that sender is waiting for an answer, but it does not
			// come. So try a broadcast.
			broadcast(new MetaformaPacket(getId(), p.getSource()).setType(Type.BROADCAST).setData(p.getSourceConnector()));
			info.addNotification("Answer to " + p.getSource()
					+ " does not arrive, so try broadcast.");
		}
			
		

		if (p.getType() == Type.BROADCAST && hasReceivedBroadcast == false) {
			hasReceivedBroadcast = true;
			if (p.getDest() != getId()) {
				info.addNotification("spread broadcast!");
				broadcast(p,connector);
			}
		}
		
		if (p.getType() == Type.BROADCAST) {
			if (p.getDest() == getId()) {
				addNeighbor(p.getSource(), p.getData(), 1);	// We use an odd number 1 here for a female connector 
			}
		} 
		else {
			addNeighbor(p.getSource(), connector, p.getSourceConnector());


			if (p.getSource() == getId()) {
				try {
					throw new Exception("Source cannot be myself (" + getId()
							+ ")!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(0);
				}
			}
			
			if (p.getDest() == getId() || p.getDest() == Module.ALL) {
				
				
				if (p.getType() == Type.STATE && state < p.getData()) {
					increaseState(p.getData());
					broadcast(new MetaformaPacket(p).setData(state).setType(Type.STATE).setDest(Module.ALL),connector);
				}

				if (p.getSource() != Module.ALL) {
					info.addNotification("add " + p.getSource() + " "
							+ connector + "," + p.getSourceConnector());
					addNeighbor(p.getSource(), connector, p
							.getSourceConnector());
				}

				info.addNotification(".handleMessage = " + p.toString()	+ " over " + connector);

				if (p.getType() == Type.DISCOVER && p.getDir() == Dir.REQ) {
					send(p.getAck().getBytes(), connector);

				}
			}
		}
	}

	public void broadcast (MetaformaPacket p) {	
		broadcast (p,-1);
	}
		
	public void broadcast(MetaformaPacket p,int exceptConnector) {
		info.addNotification(".broadcast packet (" + p.toString() + ") ");
		for (byte c = 0; c < 8; c++) {
			if (c != exceptConnector) {
				send(p.getBytes(), c);
			}
		}
	}

	public void send(MetaformaPacket p) {
		info.addNotification(".send packet (" + p.toString() + ") ");
		byte c = getNbConnector(p.getDest());
		send(p.setSourceConnector(c).getBytes(), c);
	}

	public void send(byte[] bs, int connector) {
		MetaformaPacket p = new MetaformaPacket(bs);
		if (p.getType() != Type.BROADCAST) {
			p.setSourceConnector((byte) connector);
			p.setSource(getId());
		}

		info.addNotification(".send = " + p.toString() + " over " + connector);
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

	public int getState() {
		return state;
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

	public void renameFromTo(Module from, Module to) {
		if (getId() == from) {
			System.out.println("###trying rename " + from + " to " + to + " MY ID = " + getId());
			setId(to);
			System.out.println("###DOOO rename " + from + " to " + to + " MY ID = " + getId());
		}
	}

	public String getModuleInformation() {
		StringBuffer out = new StringBuffer();

		out.append("angle: " + getAngle());

		out.append("\n");
		
		out.append("connectornearbydisabled: " + isOtherConnectorNearbyCallDisabled());

		out.append("\n");
		
		out.append("isrotating: " + isRotating());

		out.append("\n");


		out.append("current state: ");
		out.append(getState());

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

	public boolean isOtherConnectorNearbyCallDisabled() {
		//TODO: This is a hack to work around the communication issue. 
		return connectorNearbyCallDisabled;
	}

}
