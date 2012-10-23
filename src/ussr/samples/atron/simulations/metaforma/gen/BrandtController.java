package ussr.samples.atron.simulations.metaforma.gen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ussr.description.Robot;
import ussr.description.setup.ModulePosition;
import ussr.model.Controller;
import ussr.model.debugging.ControllerInformationProvider;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.simulations.metaforma.lib.*;
import ussr.samples.atron.simulations.metaforma.lib.Packet.*;

class BrandtSimulation extends MfSimulation {
	
	
	public static void main(String[] args) {
		MfSimulation.initSimulator();		
		new BrandtSimulation().main();
	
	}

	protected Robot getRobot() {
		ATRON a = new ATRON() {
			public Controller createController() {
				return new BrandtController();
			}
		};
		return a;
	}

	protected ArrayList<ModulePosition> buildRobot() {
		return new MfBuilder().buildGrid(BrandtController.Mod.F);
	}
}

class PacketAddNeighbor extends Packet {
	public static byte getTypeNr() {return 7;}
	
	public PacketAddNeighbor(MfController c) {
		super(c);
		setType(getTypeNr());
	}
	
	public byte first;
	public byte second;
	
	@Override
	public String toStringPayload() {
		return "[" + first + "," + second + "]";
	}

	@Override
	public Packet deserializePayload(byte[] payload) {
		first = payload[0];
		second = payload[1];
		return this;
	}

	@Override
	public byte[] serializePayload() {
		return new byte[]{first,second};
	}
}


class PacketGradient extends Packet {
	public static byte getTypeNr() {return 6;}
	
	public byte pri;
	public byte sec;
	
	public PacketGradient (MfController c) {
		super(c);
		setType(getTypeNr());
	}
	
	public String toStringPayload () {
		return "[" + pri + "," + sec + "]";
	}
	
	public byte[] serializePayload () {
		return new byte[]{pri,sec};
	}
	
	public PacketGradient deserializePayload (byte[] b) {
		pri = b[0];
		sec = b[1];
		return this;
	}
}



public class BrandtController extends MfController implements ControllerInformationProvider {
	
	public enum StateOperation implements IStateOperation {
		NONE, FLIPOVER, FLIPTHROUGH, FLIPALONG;
		public byte ord() {return (byte) (ordinal() - 1 + GenState.values().length);	}
		public IStateOperation fromByte(byte b) {return values()[b+1 - GenState.values().length];}
	}
	
	class BagModule extends BagModuleCore {
		public byte gradPri;
		public byte gradSec;
		public boolean isRef;
		public boolean sourcePri;
		public boolean sourceSec;
		
		
		public void gradientPropagate () { 
			if (sourcePri){
				visual.print("I am source for Pri");
				setVar("gradPri",(byte)0);
			}
			
			if (sourceSec) {
				visual.print("I am source for Sec");
				setVar("gradSec",(byte)0);
			}
			broadcast((PacketGradient)new PacketGradient(ctrl).setVar("pri", gradPri).setVar("sec", gradSec));
		}
		
		public boolean at(BorderLine d) {
			boolean ret = false;
			
			if (d.equals(BorderLine.LEFT)) {
				ret = nbs(EAST&MALE).nbsInRegion(true).size() == 2 && nbs(WEST&NORTH&MALE).nbsInRegion(true).isEmpty() || nbs(WEST&FEMALE).nbsInRegion(true).size() == 2 && nbs(EAST&NORTH&FEMALE).nbsInRegion(true).isEmpty();
			}
			if (d.equals(BorderLine.RIGHT)) {
				ret = nbs(WEST&MALE).nbsInRegion(true).size() == 2 && nbs(EAST&SOUTH&MALE).nbsInRegion(true).isEmpty() || nbs(EAST&FEMALE).nbsInRegion(true).size() == 2 && nbs(WEST&SOUTH&FEMALE).nbsInRegion(true).isEmpty();
			}
			if (d.equals(BorderLine.TOP)) {
				ret = nbs(WEST&MALE).nbsInRegion(true).size() == 2 && nbs(EAST&NORTH&MALE).nbsInRegion(true).isEmpty() || nbs(WEST&FEMALE).nbsInRegion(true).size() == 2 && nbs(EAST&SOUTH&FEMALE).nbsInRegion(true).isEmpty();
			}
			if (d.equals(BorderLine.BOTTOM)) {
				ret = nbs(EAST&MALE).nbsInRegion(true).size() == 2 && nbs(WEST&SOUTH&MALE).nbsInRegion(true).isEmpty() || nbs(EAST&FEMALE).nbsInRegion(true).size() == 2 && nbs(WEST&NORTH&FEMALE).nbsInRegion(true).isEmpty();
			}
			return ret;
		}

		public void gradientInit() {
			sourcePri = at(stateMngr.getState().getOrientation().getPrimaryBorder());
			sourceSec = at(stateMngr.getState().getOrientation().getSecondaryBorder());
			
			visual.print("gradientInit()");
			setVar("gradPri",(byte)MAX_BYTE);
			setVar("gradSec",(byte)MAX_BYTE);			
		}
		
	}
	
	class BagMeta extends BagMetaCore implements IMetaBag {
		public byte Top;
		public byte Bottom;
		public byte Left;
		public byte Right;
		public byte TopLeft;
		public byte TopRight;
		public byte BottomLeft;
		public byte BottomRight;
		
		
		public void neighborHook (Packet p) {
//			visual.print(getID() + ".nbhook!!");
			if (p.metaID != ctrl.module().metaID && ctrl.module().metaID != 0) {
				if ( p.connDest == 5 || p.connDest == 6) {
					setVar("Right",p.metaID); 
				}
				if ( p.connDest == 2 ||  p.connDest == 7) {
					setVar("Top",p.metaID); 
				}
				if ( p.connDest == 0 ||  p.connDest == 3) {
					setVar("Left",p.metaID); 
				}
				if ( p.connDest == 1 ||  p.connDest == 4) {
					setVar("Bottom",p.metaID); 
				}
			}
		}
		
		public void broadcastNeighbors () {
			broadcastNeighborsTo(new byte[]{Top,Bottom},Left,Right);
			broadcastNeighborsTo(new byte[]{Left,Right},Top,Bottom);
		}
		
		private void broadcastNeighborsTo (byte[] dests, byte nb1, byte nb2) {
			if (ctrl.module().metaID != 0) { 
				for (byte destID:dests) {
//					Dest could be: 	-1 = uninitialized metamodule    0 = no metamodule
					if (destID > 0) {
						ctrl.unicast((PacketAddNeighbor)new PacketAddNeighbor(ctrl).setVar("first",nb1).setVar("second",nb2),ctrl.nbs().nbsWithMetaId(destID).connectors());
					}
				}
			}
		}		
	}

	
	
	enum MetaPart implements IMetaPart {
		NONE,Left,Bottom,Right,Top;
		public IMetaPart fromByte(byte b) {
			return values()[b];
		}
		public byte index() {return (byte) ordinal();}
		public byte size() {
			// None is no part
			return (byte) (values().length - 1);
		}
	} 
	

	
	public enum Mod  implements IModule,IModEnum{
		ALL,
		NONE,
		Dummy_Left,
		Dummy_Right,
		F(48),
		Clover_North, Clover_South, Clover_West, Clover_East, 
		Outside(48),
		Inside(48),
		OutsideLifter_Top,
		OutsideLifter_Bottom,
		InsideLifter_Top,
		InsideLifter_Bottom,
		Uplifter_Left, Uplifter_Right, Uplifter_Top, Uplifter_Bottom,
		Dummy_Ref;

		byte count;
		
		private Mod () {
			count = 1;
		}
		
		private Mod (int c) {
			count = (byte) c;
		}
		
		
		
		public IModule module () {
			return new Module (this.getMod());
		}
		
		public byte getCount() {
			return count;
		}
		
		public Set<IModule> modules() {
			Set<IModule> m = new HashSet<IModule>();
			m.add(this);
			return m;
		}

		public boolean equals(IModule m) {
			return this.ordinal() == m.getMod().ordinal();
		}

		@Override
		public IModEnum getMod() {
			return this;
		}

		@Override
		public byte getNr() {
			return 0;
		}

		@Override
		public Group getGroup () {
			return Group.valueOf(name().split("_")[0]);
		}

		@Override
		public int ord() {
			int ret = 0;
			for (Mod m:values()) {
				if (m.ordinal() != ordinal()) {
					ret+=m.count;
				}
				else {
					if (ret > 255) {
						throw new Error("Enum overflow: 255<" + ret + "  " + this);
					}
					return (byte)ret;
				}
			}
			throw new Error ("Enum value not found!");
		}
		
		@Override
		public boolean contains(IModule m) {
			return equals(m);
		}

		@Override
		public IModEnum getNone() {
			return NONE;
		}
		
		@Override
		public IModEnum valueFrom(String string) {
			return valueOf(string);
		}

		@Override
		public IModEnum[] getValues() {
			return values();
		}

	}
	
	public enum Group implements IGroupEnum,IModuleHolder{ALL, NONE, F, Clover, Outside, Inside, InsideLifter,OutsideLifter,Uplifter,Dummy;
		public boolean contains(IModule m) {
			return equals(m.getGroup());
		}

		public Set<IModule> modules() {
			Set<IModule> mods = new HashSet<IModule>();
			for (IModule m: Mod.values()) {
				if (m.toString().startsWith(toString() + "_")) {
					mods.add(m);
				}
			}
			return mods;
		}

		@Override
		public IGroupEnum valueFrom(String string) {
			return valueOf(string);
		}
	}	
	
	
	
	
	private BagModule module;
	private BagMeta meta;

	

	public void init() {		
		module = new BagModule();
		meta = new BagMeta();
		module.setController(this);
		meta.setController(this);
		
		Module.Mod = Mod.NONE;
		Module.Group = Group.NONE;
		
		module().part = MetaPart.NONE;
		
		
		visual.setColor(Mod.Clover_North, Color.WHITE);
		visual.setColor(Mod.Clover_South, Color.BLACK);
		visual.setColor(Mod.Clover_West, Color.YELLOW);
		visual.setColor(Mod.Clover_East, Color.GREEN);
		
		visual.setColor(Group.Outside, Color.CYAN);
		visual.setColor(Group.Inside, Color.MAGENTA);
		visual.setColor(Group.OutsideLifter, Color.CYAN.darker().darker().darker());
		visual.setColor(Group.InsideLifter, Color.MAGENTA.darker().darker().darker());

		visual.setColor(Mod.Uplifter_Top, Color.YELLOW);
		visual.setColor(Mod.Uplifter_Bottom, Color.MAGENTA);
		visual.setColor(Mod.Uplifter_Left, Color.GREEN);
		visual.setColor(Mod.Uplifter_Right, Color.PINK);
		visual.setColor(Mod.Dummy_Left, Color.WHITE);
		visual.setColor(Mod.Dummy_Right, Color.BLACK);
		
		visual.setColor(StateOperation.FLIPOVER,Color.YELLOW);
		visual.setColor(StateOperation.FLIPTHROUGH,Color.CYAN);
		visual.setColor(StateOperation.FLIPALONG,Color.MAGENTA);
		visual.setColor(GenState.INIT,Color.WHITE);
		
		visual.setMessageFilter(255);//^ pow2(PacketDiscover.getTypeNr()));		
	}

	public void handleStates() {
		
		if (stateMngr.at(GenState.INIT)) {
			
			// Make groupings of 4
			if (stateMngr.doUntil(0)) {
				if (nbs(EAST&MALE, MetaPart.NONE).size() == 2 && !nbs(WEST, MetaPart.NONE).exists()) {
					module().setPart(MetaPart.Left);
					module().setMetaID (module().getID().ord());
				}
				if (module().part == MetaPart.Left)	{	
					unicast((PacketSetMetaId)new PacketSetMetaId(this).setVar("newMetaID", module().metaID),EAST&MALE&NORTH);
				}
//				broadcast(new PacketDiscover(this));
			}
		}
		
		if (stateMngr.at(GenState.CHOOSE)) {
			if (stateMngr.doUntil(0)) {
				stateMngr.spend("meta.broadcastNeighbors");		
			}
				
			if (stateMngr.doUntil(1)) {
				if (meta().regionTakesTooLong()) {
					meta().retractRegion();
					stateMngr.nextOperation(GenState.CHOOSE);
					return;
				}
				
				
				if (freqLimit("printMetaNB",1f)){
					visual.print(meta().getVarsString());
				}
//				if (meta().Top != 0 && meta().Left != 0) {
//					meta().createRegion(new byte[]{meta().Top,meta().Left});
//					stateMngr.setAfterConsensus(StateOperation.FLIPOVER,Orientation.BOTTOMRIGHT);
//					stateMngr.commit();
//				}
				// TOP or MIDDLE LEFT
				if (meta().Top == 0 && meta().Bottom == 0 && meta().Right != 0 && meta().Left == 0) {
					if (meta().TopRight == 0) {
						meta().createRegion(new byte[]{meta().Right},StateOperation.FLIPTHROUGH,Orientation.LEFT_BOTTOM);
						stateMngr.commit();
					}
					else {
						meta().createRegion(new byte[]{meta().TopRight,meta().Right},StateOperation.FLIPALONG,Orientation.BOTTOM_RIGHT);
						stateMngr.commit();
					}
				}
				
				// TOP or MIDDLE RIGHT
				if (meta().Top == 0  && meta().Bottom == 0 && meta().Left != 0  && meta().Right == 0) {
					if (meta().TopLeft == 0) {
						meta().createRegion(new byte[]{meta().Left},StateOperation.FLIPTHROUGH,Orientation.RIGHT_BOTTOM);
						stateMngr.commit();
					}
					else {
						meta().createRegion(new byte[]{meta().TopLeft,meta().Left},StateOperation.FLIPALONG,Orientation.BOTTOM_LEFT);
						stateMngr.commit();
					}
				}
				
				
				// BOTTOM OF STRUCT
				if (meta().Bottom == 0  && meta().Top != 0 && meta().Left == 0  && meta().Right == 0 ) {
//					if (meta().TopLeft == 0) { 
//						meta().createRegion(new byte[]{meta().Top},StateOperation.FLIPTHROUGH,Orientation.BOTTOM_RIGHT);
//						stateMngr.commit();
//					}
					if (meta().TopRight == 0) {
						meta().createRegion(new byte[]{meta().Top},StateOperation.FLIPTHROUGH,Orientation.BOTTOM_LEFT);
						stateMngr.commit();
					}
				}
			}
		}

		
		if (stateMngr.at(StateOperation.FLIPTHROUGH)) {
			if (stateMngr.doWait(0)) {
								
				if (stateMngr.getState().getOrientation().is(BorderLine.RIGHT)) {
					QUART = 90;
					HALF = 180;
				}
				
				if (stateMngr.getState().getOrientation().equals(Orientation.BOTTOM_RIGHT)) {
					QUART = -90;
					HALF = -180;
				}
				
				if (stateMngr.getState().getOrientation().equals(Orientation.BOTTOM_LEFT)) {
					QUART = 90;
					HALF = 180;
				}
				
				
				
				
				module().gradientInit();
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(1))  {
				scheduler.enable("module.gradientPropagate");
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doUntil(2)) {
				stateMngr.spend("module.gradientPropagate");
			}
			
			if (stateMngr.doWait(3)) {
				scheduler.disable("module.gradientPropagate");
				assign (1,1,Mod.Uplifter_Left);
				assign (0,2,Mod.Uplifter_Right);
				assign (0,1,Mod.Dummy_Left);
				assign (1,3,Mod.Uplifter_Top);
				assign (1,2,Mod.Dummy_Right);
				assign (0,0,Mod.Uplifter_Bottom);
				if (module().gradPri == 0 && module().gradSec == 3) {
					module().setVar("isRef",true);
				}
				else {
					module().setVar("isRef",false);
				}
				stateMngr.commit("ALL DONE in state 3");
			}
	
			if (stateMngr.doWait(4)) {
//				actuation.disconnectPart (Mod.Uplifter_Left, NORTH&MALE&EAST|SOUTH&MALE&WEST);
//				actuation.disconnectPart (Mod.Uplifter_Right, NORTH&MALE&EAST);
//				actuation.disconnectDiagonal(Mod.Uplifter_Right);
//				actuation.disconnectDiagonal(Mod.Uplifter_Left);
				actuation.disconnect(Mod.Dummy_Left,Mod.Uplifter_Left);
				actuation.disconnect(Mod.Dummy_Right,Mod.Uplifter_Right);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(5)) {
				actuation.rotate(new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),QUART);
				stateMngr.commitEnd();
			}
	
			if (stateMngr.doWait(6)) {
//				actuation.disconnectPart (Mod.Uplifter_Left,SOUTH);
				actuation.disconnect(Mod.Dummy_Right,Mod.Uplifter_Left);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(7)) {
				actuation.rotate(Mod.Uplifter_Top,HALF);
	//			actuation.rotate(new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				stateMngr.commitEnd();
			}
	
			if (stateMngr.doWait(8)) {
				actuation.rotate(Mod.Uplifter_Right,QUART);
				actuation.rotate(Mod.Uplifter_Left,QUART);
				stateMngr.commitEnd();
			}
			
			
			if (stateMngr.doWait(9)) {
				actuation.rotate(Mod.Uplifter_Bottom,HALF);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(10)) {
//				actuation.connect(Mod.Uplifter_Right,Mod.Uplifter_Top);
				stateMngr.commitEnd();
			}
			
			
			if (stateMngr.doWait(11)) {
				module().restoreID();
				stateMngr.commitEnd();
			}
			
			
			if (stateMngr.doWait(12)) {
				actuation.connect(Group.F,Group.F);
				stateMngr.commitEnd();
			}
		
		
			
			if (stateMngr.doUntil(13)) {
	//			discoverNeighbors();
				if (module().isRef) {
					broadcast(new PacketSymmetry(this));
					stateMngr.commit("symmetry initiated");
				}
			}
			
			if (stateMngr.doWait (14)) {
				if (module().isRef) {
					module().setVar("isRef",false);
				}
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait (15)) {
				finish();
			}
			
			
		}
	
		
		if (stateMngr.at(StateOperation.FLIPOVER)) {
			if (stateMngr.doWait(0)) {
								
				if (stateMngr.getState().getOrientation().is(BorderLine.TOP)) {
					QUART = -90;
					HALF = -180;
				}
				else {
					QUART = 90;
					HALF = 180;
				}
								
				module().gradientInit();
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(1))  {
				scheduler.enable("module.gradientPropagate");
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doUntil(2)) {
				stateMngr.spend("module.gradientPropagate");
			}
			
			if (stateMngr.doWait(3)) {
				scheduler.disable("module.gradientPropagate");
				
				if (module().gradPri == 0 && module().gradSec == 3) {
					module().setVar("isRef",true);
				}
				else {
					module().setVar("isRef",false);
				}
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(4)) {
				module().storeID();
				if (module().getVar("gradPri") == 0 && module().getVar("gradSec") < 3 || module().getVar("gradSec") == 0 && module().getVar("gradPri") < 3) {
					module().swapGroup(Group.Outside);
				}
				if (module().getVar("gradPri") > 0 && module().getVar("gradSec") > 0 ) {
					module().swapGroup(Group.Inside);
				}	
				if (module().getVar("gradPri") == 3 && module().getVar("gradSec") == 1 ) {
					module().setID(Mod.InsideLifter_Top);
				}
				if (module().getVar("gradPri") == 1 && module().getVar("gradSec") == 3 ) {
					module().setID(Mod.InsideLifter_Bottom);
				}
				if (module().getVar("gradPri") == 2 && module().getVar("gradSec") == 0 ) {
					module().setID(Mod.OutsideLifter_Top);
				}
				if (module().getVar("gradPri") == 0 && module().getVar("gradSec") == 2 ) {
					module().setID(Mod.OutsideLifter_Bottom);
				}
				stateMngr.commitEnd();
				
			}	
			
			if (stateMngr.doWait(5)) {
				actuation.disconnect(Group.Outside, Group.Inside);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(6)) {
				actuation.disconnect(Group.OutsideLifter, Group.Inside);
				stateMngr.commitEnd();
			}
		
			if (stateMngr.doWait(7)) {
				actuation.rotate(Mod.InsideLifter_Top,HALF);
				actuation.rotate(Mod.InsideLifter_Bottom,-HALF);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(8)) {
				actuation.rotate(Mod.OutsideLifter_Top,HALF);
				actuation.rotate(Mod.OutsideLifter_Bottom,-HALF);
				stateMngr.commitEnd();
			}

			if (stateMngr.doWait(9)) {
				actuation.connect(Group.Outside,Group.Inside);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(10)) {
				actuation.connect(Group.InsideLifter,Group.Outside);
				stateMngr.commitEnd();
			}
			
			
			if (stateMngr.doUntil(11)) {
				if (module().isRef) {
					broadcast(new PacketSymmetry(this));
					stateMngr.commit();
				}
			}
			
			if (stateMngr.doWait (12)) {
				module().restoreID();
				stateMngr.commitEnd();
			}
			
			
			if (stateMngr.doWait (13)) {
				finish();
				
			}


			
		}
		
		if (stateMngr.at(StateOperation.FLIPALONG)) {
			if (stateMngr.doWait(0)) {
				
				if (stateMngr.getState().getOrientation() == Orientation.BOTTOM_LEFT) {
					QUART = 90;
					HALF = 180;
				}
				
				if (stateMngr.getState().getOrientation() == Orientation.BOTTOM_RIGHT) {
					QUART = -90;
					HALF = -180;
				}
				
				module().gradientInit();
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(1))  {
				scheduler.enable("module.gradientPropagate");
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doUntil(2))  {
				
				stateMngr.spend("module.gradientPropagate");
			}
			
			if (stateMngr.doWait(3)) {
				scheduler.disable("module.gradientPropagate");
			
				assign (3,0,Mod.Clover_West);
				assign (2,0,Mod.Clover_South);
				assign (3,1,Mod.Clover_North);
				assign (2,1,Mod.Clover_East);
				if (module().gradPri == 0 && module().gradSec == 3) {
					module().setVar("isRef",true);
				}
				else {
					module().setVar("isRef",false);
				}
				stateMngr.commitEnd();
			}
			
			
			if (stateMngr.doWait(4)) {
				actuation.disconnect(Mod.Clover_West, Mod.Clover_South);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(5)) {
				actuation.rotate(Mod.Clover_East,QUART);
				stateMngr.commitEnd();
			}
				
			if (stateMngr.doWait(6)) {
				actuation.rotate(Mod.Clover_North,HALF);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(7)) {
				actuation.rotate(Mod.Clover_East,QUART);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(8)) {
				actuation.connect(Mod.Clover_North,Group.F);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(9)) {
				actuation.connect(Mod.Clover_West,Group.F);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(10)) {
				actuation.disconnect(new ModuleSet().add(Mod.Clover_South).add(Mod.Clover_East),Group.F);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(11)) {
				actuation.rotate(Mod.Clover_East,-QUART);
				stateMngr.commitEnd();
			}
	
			if (stateMngr.doWait(12)) {
				actuation.rotate(Mod.Clover_North,QUART);
				stateMngr.commitEnd();
			}
						
	
			if (stateMngr.doWait(13)) {
				actuation.rotate(Mod.Clover_East,-QUART);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(14)) {
				actuation.rotate(Mod.Clover_North,QUART);
				stateMngr.commitEnd();
			}
			
	
			if (stateMngr.doWait(15)) {
				actuation.connect(Mod.Clover_South,Mod.Clover_West);
				stateMngr.commitEnd();
			}
		
			
//			
//			if (stateMngr.doWait(4)) {
//				actuation.disconnect(Mod.Clover_East,Group.F);
//				actuation.disconnect(Mod.Clover_South,Mod.Clover_East);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			if (stateMngr.doWait(5)) {
//				actuation.rotate(Mod.Clover_West,-180);
//				actuation.rotate(Mod.Clover_South,-180);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doUntil(6)) {
//				if (module().isRef) {
//					broadcast(new PacketSymmetry(this));
//					stateMngr.commit();
//				}
////				stateMngr.commit();
//			}
//			if (stateMngr.doWait(7)) {
//				actuation.rotate(Mod.Clover_South,180);
//				delay(3000);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			if (stateMngr.doUntil(8)) {
//				if (module().isRef) {
//					delay(1000);
//					broadcast(new PacketSymmetry(this));
//					stateMngr.commit();
//				}
//			}
//			if (stateMngr.doWait(9)) {
//				actuation.rotate(Mod.Clover_West,180);
//				delay(3000);
//				stateMngr.commitMyselfIfNotUsed();
//			}
			if (stateMngr.doUntil(16)) {
				if (module().isRef) {
					broadcast(new PacketSymmetry(this));
					stateMngr.commit();
				}
			}
			
			if (stateMngr.doWait (17)) {
				module().restoreID();
		
				stateMngr.commitEnd();
			}
			
			
			if (stateMngr.doWait (18)) {
				finish();
				
			}
	
		}
		

	}

	private void assign(int h, int v, Mod m) {
		if (module().gradPri == h && module().gradSec == v) {
			module().storeID();
			module().setID(m);
		}
		else {
			visual.print("Coordinate dismatch for " + m + " - " + h + "," + v + " ! = " + module().gradPri + "," + module().gradSec);
		}
	}



	
	
	
		
	public void receiveCustomPacket(byte typeNr, byte[] msg, byte connector) {
		if (typeNr == PacketGradient.getTypeNr()) {
			PacketGradient p = (PacketGradient)new PacketGradient(this).deserialize(msg,connector);
			if (preprocessPacket(p)) {
				receivePacket(p);
				receivePacket((Packet)p);
			}
		}
	}
	
	public boolean receivePacket (PacketGradient p) {
		boolean updated = false;
		if (module().gradPri > p.pri + 1) {
			module().setVar("gradPri",(byte)(p.pri + 1));
			updated = true;
		}
		if (module().gradSec > p.sec + 1) {
			module().setVar("gradSec",(byte)(p.sec + 1));
			updated = true;
		}
		if (updated) {
			scheduler.invokeNow("module.gradientPropagate");
		}
		return true;
	}
	
	
	
	
	public boolean receivePacket (Packet p) {
		boolean handled = false;
//		visual.print("RECEIVE " + p);
		if (stateMngr.check(p,GenState.INIT) ) {
//			visual.print("RECEIVE YESSSS1 " + p);
			meta().neighborHook(p);
			handled = true;
		}
		if (stateMngr.check(p,GenState.CHOOSE) ) {
//			visual.print("RECEIVE YESSSS2 " + p);
			meta().neighborHook(p);
			handled = true;
		}
		return handled;
	}
	
	public boolean receivePacket (PacketSetMetaId p) {
		boolean handled = false;

		if (stateMngr.check(p,new State(GenState.INIT,0))) {
			handled = true;
			if (module().metaID == 0) {
				module().setMetaID(p.newMetaID);
				
				if (isMALE(p.connDest)) {
					module().setPart(MetaPart.Right);
				}
				else {
					if (isWEST(p.connDest)) {
						module().setPart(MetaPart.Top);
					}
					else {
						module().setPart(MetaPart.Bottom);
					}
				}
			}
			
			if (module().getPart() == MetaPart.Left) {
				meta().enable(); // must be here, not in next state!
				stateMngr.nextOperation(GenState.CHOOSE);
//					stateMngr.nextInstruction();
			}
			else {
				if (freqLimit("META_ID_SET",settings.getPropagationRate())) {
					unicast((PacketSetMetaId)new PacketSetMetaId(this).setVar("newMetaID", p.newMetaID),pow2((p.connDest + 4) % 8));
				}
			}	
		}
		
		receivePacket((Packet)p);
		return handled;
	}
	
	


	public boolean receivePacket (PacketSymmetry p) {
		boolean handled = false;
		
		if (stateMngr.check(p,new State(StateOperation.FLIPTHROUGH,13))) {	
			module().fixSymmetry(p.connSource,p.connDest);
			handled = true;
		}
		
		
		if (stateMngr.check(p,new State(StateOperation.FLIPALONG,16))) {	
			module().fixSymmetry(p.connSource,p.connDest);
			handled = true;
		}
		
		if (stateMngr.check(p,new State(StateOperation.FLIPOVER,11))) {
			module().fixSymmetry(p.connSource,p.connDest);
			handled = true;
		}
		
		return handled;
	}

	protected boolean receivePacket(PacketAddNeighbor p) {
		visual.print(".PacketAddNeighbor" + p);
		if (p.metaID == meta().Left) {
			meta().setVar("TopLeft",p.first);
			meta().setVar("BottomLeft",p.second);
		}
		if (p.metaID == meta().Right) {
			meta().setVar("TopRight", p.first);
			meta().setVar("BottomRight", p.second);
		}
		if (p.metaID == meta().Top) {
			meta().setVar("TopLeft", p.first);
			meta().setVar("TopRight", p.second);
		}
		if (p.metaID == meta().Bottom) {
			meta().setVar("BottomLeft", p.first);
			meta().setVar("BottomRight", p.second);
		}

		
		return true;
	}
	
	@Override
	public IMetaPart getMetaPart() {
		return MetaPart.NONE;
	}

	@Override
	public IStateOperation getStateInst() {
		return StateOperation.NONE;
	}

	@Override
	public BagModule module() {
		return (BagModule) module;

	}
	
	@Override
	public BagMeta meta() {
		return meta;
	}
	
	public void makePacket(byte[] msg, byte connector) {

		byte typeNr = Packet.getType(msg);
		if (typeNr == PacketGradient.getTypeNr()) {
			PacketGradient p = (PacketGradient)new PacketGradient(this).deserialize(msg,connector);
			if (preprocessPacket(p)) {
				receivePacket(p);
			}
		}
		else if (typeNr == PacketAddNeighbor.getTypeNr()) {
			PacketAddNeighbor p = (PacketAddNeighbor)new PacketAddNeighbor(this).deserialize(msg,connector);
			preprocessPacket(p);
			receivePacket(p);
		}
		else {
			super.makePacket(msg, connector);
		}
			
		
	}
	
}