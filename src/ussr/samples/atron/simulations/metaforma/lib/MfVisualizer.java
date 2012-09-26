package ussr.samples.atron.simulations.metaforma.lib;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import ussr.samples.atron.simulations.metaforma.lib.Packet.*;


public class MfVisualizer {
	
	private MfController ctrl;
	
	protected byte msgFilter;
	private byte msgFilterMeta;
	
	private Map<IModuleHolder, Color> colorsModuleHolder = new HashMap<IModuleHolder, Color>();
	
	private Map<IStateOperation,Color> colorsOperation = new HashMap<IStateOperation, Color>();


	public MfVisualizer (MfController c) {
		ctrl = c;
	}
	
	
	public void colorize() {
//		print(".colorize();");
		Color north = getColors()[0];
		Color south = getColors()[1];
		
		if (ctrl.module().role != null) {
			for (int i=0; i< ctrl.module().role.index()-1;i++) {
				north = north.darker().darker();
			}
		}
		
		ctrl.getModule().getComponent(0).setModuleComponentColor(ctrl.getContext().isSwitchedNorthSouth() ? south : north);
		ctrl.getModule().getComponent(1).setModuleComponentColor(ctrl.getContext().isSwitchedNorthSouth() ? north : south);

		ctrl.getModule().getConnectors().get(ctrl.getContext().abs2rel(0)).setColor(Color.BLUE);
		ctrl.getModule().getConnectors().get(ctrl.getContext().abs2rel(1)).setColor(Color.BLACK);
		ctrl.getModule().getConnectors().get(ctrl.getContext().abs2rel(2)).setColor(Color.RED);
		ctrl.getModule().getConnectors().get(ctrl.getContext().abs2rel(3)).setColor(Color.WHITE);
		
		ctrl.getModule().getConnectors().get(ctrl.getContext().abs2rel(4)).setColor(Color.BLUE);
		ctrl.getModule().getConnectors().get(ctrl.getContext().abs2rel(5)).setColor(Color.BLACK);
		ctrl.getModule().getConnectors().get(ctrl.getContext().abs2rel(6)).setColor(Color.RED);
		ctrl.getModule().getConnectors().get(ctrl.getContext().abs2rel(7)).setColor(Color.WHITE);
	}
	
	private String getIdString () {
		String ret = "";
		ret += ctrl.getId() + " ";
		ret += ctrl.module().role + " (";
		ret += ctrl.module().metaID + " // ";
		ret += ctrl.meta().regionID() + ") ";
		ret += ctrl.getStateMngr().getState() + " ";
		ret += Module.fromBits(ctrl.getStateMngr().getConsensus()) + ": " + ctrl.getStateMngr().getConsensus().bitCount();
		return ret; 
	}

	public String getModuleInformation() {
		StringBuffer out = new StringBuffer();
		
		String flipStr = "";
		if (ctrl.getContext().isSwitchedNorthSouth()) flipStr += "NORTH-SOUTH ";
		if (ctrl.getContext().isSwitchedEastWestN()) flipStr += "EAST-WEST-N ";
		if (ctrl.getContext().isSwitchedEastWestS()) flipStr += "EAST-WEST-S ";
		
		if (flipStr.equals("")) {
			flipStr = "<none>";
		}
		
		out.append("ID: " + getIdString() + (ctrl.getStateMngr().committed() ? " // fnshd" : "") + " rcvd: " + ctrl.getStateMngr().getStateRcvd() + " " + ctrl.getStateMngr().getConsensusRcvd().bitCount() );
		out.append("\n");

		out.append("angle: " + ctrl.getContext().getAngle() + " ("+ctrl.getAngle()+")"  + "  flips: " + flipStr);
		out.append("\n");
		
		out.append("intervals: " + ctrl.getScheduler().intervalMs);
		out.append("\n");
		out.append("prevs: " + ctrl.getScheduler().previousAction);
		out.append("\n");
		
		out.append("time: in state:" + ctrl.getStateMngr().timeSpentInState() + "  total:" + ctrl.time() + "\n");
		
//		out.append("female conns: " + ctrl.getContext().getFemaleConnsAsString());
//		out.append("\n");
		
		
		out.append("=== module ===\n" + ctrl.module() + "\n");
		out.append("===  meta  ===\n" + ctrl.meta() + "\n");
	
		out.append("do repeat: " + ctrl.getDoRepeat());
		out.append("\n"); 
		
		

		out.append(ctrl.getContext().nbs());

	
		out.append("\n");
		
		ctrl.addMetaNeighborhood(out);
		
		return out.toString();
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
		
		
		if (colorsModuleHolder.containsKey(ctrl.getId())) {
			ret[1] = colorsModuleHolder.get(ctrl.getId());
		}
		else if (colorsModuleHolder.containsKey(ctrl.getGrouping())) {
			ret[1] =  colorsModuleHolder.get(ctrl.getGrouping());
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
	
	public void setMessageFilterMeta (int msg) {
		msgFilterMeta = (byte) msg;
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
		System.err.println(ctrl.getId() + " ERROR:\n" + msg);
		ctrl.pause();
	}
	
	
	public void printStatePost() {
		print("\n\n===========  "+ ctrl.getId() + "  ==============\nLeft state, time spent: " + ctrl.getStateMngr().timeSpentInState() + "");
	}
	
	
	public void printStatePre() {
		StringBuffer metaNbs = new StringBuffer();
		ctrl.addMetaNeighborhood(metaNbs);
		print("New state: " + ctrl.getStateMngr().getState() + "\n=================================\n" + ctrl.getContext().nbs()+"\n" + metaNbs +"\n");
	}
	

	public String getTitle() {
		return getIdString();
	}


	

}
