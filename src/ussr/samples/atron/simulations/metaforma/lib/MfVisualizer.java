package ussr.samples.atron.simulations.metaforma.lib;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import ussr.samples.atron.simulations.metaforma.lib.Packet.*;


public class MfVisualizer {
	
	private MfController ctrl;
	protected byte msgFilter;
	private Map<IModuleHolder, Color> colorsModuleHolder = new HashMap<IModuleHolder, Color>();
	private Map<IStateOperation,Color> colorsOperation = new HashMap<IStateOperation, Color>();

	public MfVisualizer (MfController c) {
		ctrl = c;
	}
	
	
	public void colorize() {
//		print(".colorize();");
		Color north = getColors()[0];
		Color south = getColors()[1];
		
		if (ctrl.module().part != null && !ctrl.getStateMngr().getState().isInSequence()) {
			for (int i=0; i< ctrl.module().part.index()-1;i++) {
				north = north.darker().darker();
			}
		}
		
		ctrl.getModule().getComponent(0).setModuleComponentColor(ctrl.getContext().isSwitchedNorthSouth() ? south : north);
		ctrl.getModule().getComponent(1).setModuleComponentColor(ctrl.getContext().isSwitchedNorthSouth() ? north : south);

		ctrl.getModule().getConnectors().get(translate(0)).setColor(Color.BLUE);
		ctrl.getModule().getConnectors().get(translate(1)).setColor(Color.BLACK);
		ctrl.getModule().getConnectors().get(translate(2)).setColor(Color.RED);
		ctrl.getModule().getConnectors().get(translate(3)).setColor(Color.WHITE);
		
		ctrl.getModule().getConnectors().get(translate(4)).setColor(Color.BLUE);
		ctrl.getModule().getConnectors().get(translate(5)).setColor(Color.BLACK);
		ctrl.getModule().getConnectors().get(translate(6)).setColor(Color.RED);
		ctrl.getModule().getConnectors().get(translate(7)).setColor(Color.WHITE);
	}
	
	private int translate(int i) {
		return ctrl.getContext().rel2abs(i);
	}


	private String getIdString () {
		String ret = "";
		ret += ctrl.getID() + " ";
		ret += ctrl.module().part + " (";
		ret += ctrl.module().metaID + " // ";
		ret += ctrl.meta().regionID() + ") ";
		ret += ctrl.getStateMngr().getState() + " ";
		ret += Module.fromBits(ctrl.getStateMngr().getConsensus()) + ": " + ctrl.getStateMngr().getConsensus().bitCount();
		return ret; 
	}
	
	public String getFlipString() {
		String flipStr = "";
		if (ctrl.getContext().isSwitchedNorthSouth()) flipStr += "NORTH-SOUTH ";
		if (ctrl.getContext().isSwitchedEastWestN()) flipStr += "EAST-WEST-N ";
		if (ctrl.getContext().isSwitchedEastWestS()) flipStr += "EAST-WEST-S ";
		
		if (flipStr.equals("")) {
			flipStr = "<none>";
		}
		return flipStr;
	}


	
	public String getModuleInformation() {
		StringBuffer out = new StringBuffer();
		
		

		
		out.append("ID: " + getIdString() + (ctrl.getStateMngr().committed() ? " // fnshd" : "") + " rcvd: " + ctrl.getStateMngr().getStateRcvd() + " " + ctrl.getStateMngr().getConsensusRcvd().bitCount() );
		out.append("\n");

		out.append("angle: " + ctrl.getContext().getAngle() + " ("+ctrl.getAngle()+")"  + "  flips: " + getFlipString());
		out.append("\n");
	
		
		out.append("intervals: " + ctrl.getScheduler().intervals);
		out.append("\n");
		out.append("prevs: " + ctrl.getScheduler().previousAction);
		out.append("\n");
		
		out.append("time: in state:" + ctrl.getStateMngr().timeSpentInState() + "  total:" + ctrl.time() + "  create region: " + ctrl.meta().getTimeInitRegion() + "\n");
		
//		out.append("female conns: " + ctrl.getContext().getFemaleConnsAsString());
//		out.append("\n");
		
		
		out.append("=== module ===\n" + ctrl.module() + "\n");
		out.append("Proximity: " + ctrl.module().proximitySensor() + "\n");
		
		out.append("===  meta  ===\n" + ctrl.meta() + "\n");
	
		out.append("do repeat: " + ctrl.getDoRepeat().toString());
		out.append("\n"); 
		
		out.append("    " + ctrl.module().at(Direction.TOP) + "\n");
		out.append(ctrl.module().at(Direction.LEFT) + "   " + ctrl.module().at(Direction.RIGHT) + "\n");
		out.append("    " + ctrl.module().at(Direction.BOTTOM) + "\n");

		out.append(ctrl.getContext().nbs());

	
		out.append("\n");
		
		addMetaNeighborhood(out);
		
		
		out.append(MfStats.getInst() + "\n");
		
		return out.toString();
	}

	public void addMetaNeighborhood (StringBuffer out) {
		out.append(String.format("% 3d % 3d % 3d",ctrl.meta().getVar("TopLeft"),ctrl.meta().getVar("Top"),ctrl.meta().getVar("TopRight")) + "\n");
		out.append(String.format("% 3d     % 3d",ctrl.meta().getVar("Left"),ctrl.meta().getVar("Right")) + "\n");
		out.append(String.format("% 3d % 3d % 3d",ctrl.meta().getVar("BottomLeft"),ctrl.meta().getVar("Bottom"),ctrl.meta().getVar("BottomRight")) + "\n");
	}
	
	public void setColor(IModuleHolder m, Color color) {
		colorsModuleHolder.put(m, color);
		colorize();
	}
	
	public void setColor(IStateOperation m, Color color) {
		colorsOperation.put(m, color);
		colorize();
	}

	public Color[] getColors() {
		// Color[0] = Operation or blue, darkness = role
		// Color[1] = Module or group
		
		Color[] ret = new Color[2];
		
		if (colorsOperation.containsKey(ctrl.getStateMngr().getState().getOperation())) {
			ret[0] = colorsOperation.get(ctrl.getStateMngr().getState().getOperation());
		}
		else {
			ret[0] = Color.BLUE;
		}
		
		
		if (colorsModuleHolder.containsKey(ctrl.getID())) {
			ret[1] = colorsModuleHolder.get(ctrl.getID());
		}
		else if (colorsModuleHolder.containsKey(ctrl.module().getGroup())) {
			ret[1] =  colorsModuleHolder.get(ctrl.module().getGroup());
		}
		else {
			ret[1] = Color.RED;;
		}

		return ret;
	}
	
	public void setMessageFilter (int msg) {
		msgFilter = (byte) msg;
		print("Message filter " + msg);
	}
	
	
	public void print (PacketBase p, String msg) {
		if ((MfController.pow2(p.getType()) & msgFilter) != 0) {
			print(msg);
		}
	}
	
	
	public void print (String msg) {
		ctrl.info.addNotification("[" + new DecimalFormat("0.00").format(ctrl.time()) + "] - " + msg);
	}
	
	public void error (String msg) {
		print("ERROR: " + msg);
		System.err.println(ctrl.getID() + " ERROR:\n" + msg);
		ctrl.pause();
	}
	
	
	public void printStatePost() {
		print("\n\n===========  "+ ctrl.getID() + "  ==============\nLeft state, time spent: " + ctrl.getStateMngr().timeSpentInState() + "");
	}
	
	
	public void printStatePre() {
		StringBuffer metaNbs = new StringBuffer();
		addMetaNeighborhood(metaNbs);
		print("New state: " + ctrl.getStateMngr().getState() + "\n=================================\n" + ctrl.getContext().nbs()+"\n" + metaNbs +"\n");
	}
	

	public String getTitle() {
		return getIdString();
	}

}
