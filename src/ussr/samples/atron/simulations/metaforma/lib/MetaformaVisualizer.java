package ussr.samples.atron.simulations.metaforma.lib;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


public class MetaformaVisualizer {
	
	private MetaformaController ctrl;
	private MetaformaContext context;
	
	protected byte msgFilter;
	
	private Map<IModuleHolder, Color> colorsModuleHolder = new HashMap<IModuleHolder, Color>();
	private byte msgFilterMeta;
	private Map<IStateOperation,Color> colorsOperation = new HashMap<IStateOperation, Color>();


	public MetaformaVisualizer (MetaformaController c) {
		ctrl = c;
		context = ctrl.getContext();
	}
	
	
	public void colorize() {
//		print(".colorize();");
		Color north = getColors()[0];
		Color south = getColors()[1];
		
		if (ctrl.moduleRoleGet() != null) {
			for (int i=0; i< ctrl.moduleRoleGet().index()-1;i++) {
				north = north.darker().darker();
			}
		}
		
		ctrl.getModule().getComponent(0).setModuleComponentColor(context.isSwitchedNorthSouth() ? south : north);
		ctrl.getModule().getComponent(1).setModuleComponentColor(context.isSwitchedNorthSouth() ? north : south);

//		notification("NORTH_MALE_WEST " + NORTH_MALE_WEST + " - " + context.abs2rel(NORTH_MALE_WEST) + " - " + module.getConnectors().get(context.abs2rel(NORTH_MALE_WEST)).hashCode());
//		notification("NORTH_MALE_EAST " + NORTH_MALE_EAST + " - " + context.abs2rel(NORTH_MALE_EAST) + " - " + module.getConnectors().get(context.abs2rel(NORTH_MALE_EAST)).hashCode());
		ctrl.getModule().getConnectors().get(context.abs2rel(0)).setColor(Color.BLUE);
		ctrl.getModule().getConnectors().get(context.abs2rel(1)).setColor(Color.BLACK);
		ctrl.getModule().getConnectors().get(context.abs2rel(2)).setColor(Color.RED);
		ctrl.getModule().getConnectors().get(context.abs2rel(3)).setColor(Color.WHITE);
		
//		notification(context.abs2rel(NORTH_MALE_EAST) + "(" + module.getConnectors().get(context.abs2rel(NORTH_MALE_EAST)) +") will be red " + NORTH_MALE_EAST + "(" + module.getConnectors().get((NORTH_MALE_EAST)) +")");
		ctrl.getModule().getConnectors().get(context.abs2rel(4)).setColor(Color.BLUE);
		ctrl.getModule().getConnectors().get(context.abs2rel(5)).setColor(Color.BLACK);
		ctrl.getModule().getConnectors().get(context.abs2rel(6)).setColor(Color.RED);
		ctrl.getModule().getConnectors().get(context.abs2rel(7)).setColor(Color.WHITE);
//		notification("NORTH_MALE_WEST " + NORTH_MALE_WEST + " - " + context.abs2rel(NORTH_MALE_WEST) + " - " + module.getConnectors().get(context.abs2rel(NORTH_MALE_WEST)).hashCode());
//		notification("NORTH_MALE_EAST " + NORTH_MALE_EAST + " - " + context.abs2rel(NORTH_MALE_EAST) + " - " + module.getConnectors().get(context.abs2rel(NORTH_MALE_EAST)).hashCode());
	}
	
	private String getIdString () {
		return ctrl.getId() + " "+ctrl.moduleRoleGet()+" (" + ctrl.metaIdGet() + " // " + ctrl.metaBossIdGet() + ")" + "   " + ctrl.getStateMngr().getState();
	}

	public String getModuleInformation() {
		StringBuffer out = new StringBuffer();
		
		String flipStr = "";
		if (context.isSwitchedNorthSouth()) flipStr += "NORTH-SOUTH ";
		if (context.isSwitchedEastWestN()) flipStr += "EAST-WEST-N ";
		if (context.isSwitchedEastWestS()) flipStr += "EAST-WEST-S ";
		
		if (flipStr.equals("")) {
			flipStr = "<none>";
		}
		
		out.append("ID: " + getIdString() + (ctrl.getStateMngr().committed()? " // finished" : "") + " received: " + ctrl.getStateReceived());
		out.append("\n");

		out.append("angle: " + context.getAngle() + " ("+ctrl.getAngle()+")"  + "  flips: " + flipStr);
		out.append("\n");
		
		out.append("intervals: " + ctrl.getScheduler().intervalMs);
		out.append("\n");
		out.append("prevs: " + ctrl.getScheduler().previousAction);
		out.append("\n");
		
		out.append("time in state:" + ctrl.getStateMngr().timeSpentInState() + "\n");
		
		out.append("female conns: " + context.getFemaleConnsAsString());
		out.append("\n");
		
		
		out.append("vars: " + ctrl.getVars() + "\n        " + ctrl.getVarSequenceNrs());
		out.append("\n");
	
		out.append("do repeat: " + ctrl.getDoRepeat());
		out.append("\n"); 
		
		out.append("consensus " + (ctrl.getStateMngr().committed()?"!!":"") + "(" + ctrl.getStateMngr().getConsensus().bitCount() + "): " + Module.fromBits(ctrl.getStateMngr().getConsensus()));
		out.append("\n");
		

		out.append(context.nbs());

	
		out.append("\n");
		
		ctrl.addNeighborhood(out);
		
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
	}
	
	public void setMessageFilterMeta (int msg) {
		msgFilterMeta = (byte) msg;
	}
	
	public void print (Packet p, String msg) {
		if ((msgFilter & p.getType().bit()) != 0) {
			print(msg);
		}
	}
	
	public void print (MetaPacket p, String msg) {
		if ((msgFilterMeta & p.getType().bit()) != 0) {
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
		print("\n\n===========  "+ ctrl.getId() + "  ==============\nLeft instruction state, time spent: " + ctrl.getStateMngr().timeSpentInState() + "");
	}
	
	
	public void printStatePre() {
		print("New instruction state: " + ctrl.getStateMngr().getState() + "\n=================================\n" + context.nbs()+"\n");
	}
	

	public String getTitle() {
		return getIdString();
	}


	

}
