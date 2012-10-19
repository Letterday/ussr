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
import ussr.samples.atron.simulations.metaforma.lib.Packet.Packet;
import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketDiscover;
import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketSetMetaId;
import ussr.samples.atron.simulations.metaforma.lib.Packet.PacketSymmetry;

class ChristensenSimulation extends MfSimulation {
	class Settings extends SettingsBase {
		
	}
	public Settings set = new Settings();
	
	public static void main(String[] args) {
		MfSimulation.initSimulator();		
		new ChristensenSimulation().main();	
	}

	protected Robot getRobot() {
		ATRON a = new ATRON() {
			public Controller createController() {
				return new ChristensenController(set);
			}
		};
		return a;
	}

	protected ArrayList<ModulePosition> buildRobot() {
		return new MfBuilder().buildRectangle(3,9, ChristensenController.Mod.Floor);
	}

}


class PacketAbsorb extends Packet {
	public static byte getTypeNr() {return 8;}
	
	public PacketAbsorb (MfController c) {
		super(c);
		setType(getTypeNr());
	}
	
	public String toStringPayload () {
		return "";
	}
	
	public byte[] serializePayload () {
		return new byte[]{};
	}
	
	public PacketAbsorb deserializePayload (byte[] b) {
		return this;
	}
}

public class ChristensenController extends MfController implements ControllerInformationProvider {
		
	enum StateOperation implements IStateOperation {
		INIT, CHOOSE, GET_UP, GET_DOWN,WALK_STEP;

		public byte ord() {return (byte) ordinal();	}

		public IStateOperation fromByte(byte b) {return values()[b];}
	}
	
	
	class BagModule extends BagModuleCore {
		public byte gradH;
		public byte gradV;
		public boolean isRef;
		public boolean sourceH;
		public boolean sourceV;
		
		
		public void gradientPropagate () { 
			if (sourceH){
				visual.print("I am source for H");
				setVar("gradH",(byte)0);
			}
			
			if (sourceV) {
				visual.print("I am source for V");
				setVar("gradV",(byte)0);
			}
			broadcast((PacketGradient)new PacketGradient(ctrl).setVar("h", gradH).setVar("v", gradV));
		}
		
		public void gradientInit() {
			if (stateMngr.getState().getOrientation() == Orientation.BOTTOM_LEFT) {
				sourceV = module().atBottom();
				sourceH = module().atLeft();
			}
			if (stateMngr.getState().getOrientation() == Orientation.TOP_LEFT) {
				sourceV = module().atTop();
				sourceH = module().atLeft();
			}
			if (stateMngr.getState().getOrientation() == Orientation.BOTTOM_RIGHT) {
				sourceV = module().atBottom();
				sourceH = module().atRight();
			}
			if (stateMngr.getState().getOrientation() == Orientation.TOP_RIGHT) {
				sourceV = module().atTop();
				sourceH = module().atRight();
			}
			
			visual.print("gradientInit()");
			setVar("gradH",(byte)MAX_BYTE);
			setVar("gradV",(byte)MAX_BYTE);			
		}
		
		public boolean atTop() {return !nbs(WEST&FEMALE).nbsInRegion(true).isEmpty() && nbs(EAST&FEMALE).nbsInRegion(true).isEmpty() ;}
		public boolean atLeft() {return !nbs(EAST&MALE).nbsInRegion(true).isEmpty() && nbs(WEST&MALE).nbsInRegion(true).isEmpty()  ;}
		public boolean atBottom() {return !nbs(EAST&FEMALE).nbsInRegion(true).isEmpty() && nbs(WEST&FEMALE).nbsInRegion(true).isEmpty() || !nbs(NORTH&MALE).nbsInRegion(true).isEmpty() && nbs(SOUTH&MALE).nbsInRegion(true).isEmpty();}
		public boolean atRight() {return !nbs(WEST&MALE).nbsInRegion(true).isEmpty() && nbs(EAST&MALE).nbsInRegion(true).isEmpty() ;}
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
		public byte continueWalk;
		
		
		public void neighborHook (Packet p) {
			if (p.metaID != ctrl.module().metaID && ctrl.module().metaID != 0 && p.metaID != 0) {
//				if (module().getPart()== MetaPart.Dummy) {
//					if (isNORTH(p.connDest)) {
//						meta().setVar("Top", p.metaID);
//					}
//					else {
//						if (isEAST(p.connDest) ) {
//							meta().setVar("TopRight", p.metaID);
//						}
//						else {
//							meta().setVar("Right", p.metaID);
//						}
//					}
//					
//				}
				if (module().getPart() == MetaPart.Top) {
					meta().setVar("Top", p.metaID);
				}
				if (module().getPart() == MetaPart.Right) {
					meta().setVar("Bottom", p.metaID);
				}
				if (module().getPart() == MetaPart.Left) {
					if (isNORTH(p.connDest)) {
						meta().setVar("Left", p.metaID);
					}
					else {
						if (isWEST(p.connDest)) {
							meta().setVar("BottomLeft", p.metaID);
						}
						else {
							meta().setVar("Bottom", p.metaID);
						}
					}
				}
			}
		}


		public void absorb() {
			visual.print("Absorbing lonely module!");
			broadcast(new PacketAbsorb(ctrl));
			setVar("absorbed",1);
		}


	
	}
	
	enum MetaPart implements IMetaPart {
		NONE,Left,Top,Right;//,Dummy;


		public IMetaPart fromByte(byte b) {
			return values()[b];
		}

		public byte index() {return (byte) ordinal();}

		public byte size() {
			// None is no role
			return (byte) (values().length - 1);
		}		
	} 
	
	enum Mod  implements IModule,IModEnum {
		ALL,
		NONE,
		Floor(100),
		Walker_Left, Walker_Right, Walker_Top, Walker_Dummy,
		Uplifter_Left, Uplifter_Right, Uplifter_Top, Uplifter_Bottom,Uplifter_DR,Uplifter_DT;

		byte count;
		
		private Mod () {
			count = 1;
		}
		
		private Mod (int c) {
			count = (byte) c;
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
		public byte ord() {
			byte ret = 0;
			for (Mod m:values()) {
				if (m.ordinal() != ordinal()) {
					ret+=m.count;
				}
				else {
					return ret;
				}
			}
			throw new Error ("Enum not found!");
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
	
	 enum Group implements IGroupEnum,IModuleHolder{ALL, NONE, Floor, Walker, Uplifter;

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
	
	

	public ChristensenController(SettingsBase set) {
		super(set); 
	}
	
	public void init() {
		module = new BagModule();
		meta = new BagMeta();
		module.setController(this);
		meta.setController(this);
		
		Module.Mod = Mod.NONE;
		Module.Group = Group.NONE;
		stateMngr.init(StateOperation.INIT);
	
		module().setPart(MetaPart.NONE);
		
		visual.setColor(Mod.Walker_Left, Color.PINK);
		visual.setColor(Mod.Walker_Top, Color.PINK.darker());
		visual.setColor(Mod.Walker_Right, Color.PINK.darker().darker());
		visual.setColor(Mod.Walker_Dummy, Color.PINK.darker().darker().darker());
		visual.setColor(Group.Uplifter, Color.WHITE);

		visual.setColor(StateOperation.WALK_STEP,Color.YELLOW);
		visual.setColor(StateOperation.GET_DOWN,Color.MAGENTA);
		visual.setColor(StateOperation.GET_UP,Color.GREEN);
		visual.setColor(StateOperation.INIT,Color.WHITE);
		
		visual.setMessageFilter(255^ pow2(PacketDiscover.getTypeNr()));
						
	}

	
	
	public void handleStates() {
				
		if (stateMngr.at(StateOperation.INIT)) {
			// Make groupings of 4
			if (stateMngr.doUntil(0)) {
				if (module().metaID == 0 && nbs(EAST&MALE&NORTH, MetaPart.NONE).exists() && !nbs(WEST&MALE&NORTH).nbsWithMetaId((byte) 0).exists()) {
					module().setPart(MetaPart.Left);
					module().setMetaID (module().getID().ord());
				}
				if (module().metaID != 0 && module().part == MetaPart.Left) {
					unicast((PacketSetMetaId)new PacketSetMetaId(this).setVar("newMetaID", module().metaID).setVar("index",(byte) 0),EAST&MALE&NORTH);
				}
			}
		}
		
		if (stateMngr.at(StateOperation.CHOOSE)) {
			if (stateMngr.doUntil(0)) {
				stateMngr.spend("meta.broadcastNeighbors");		
			}
				
			if (stateMngr.doUntil(1)) {
//				if (meta().Top == 0 && meta().Left != 0 && meta().Right == 0 && meta().Bottom != 0) {
				if (meta().Top == 0 && meta().Left == 0 && meta().Right == 0 && meta().Bottom != 0) {
					meta().createRegion(new byte[]{meta().Bottom},StateOperation.GET_UP,Orientation.BOTTOM_LEFT);
					stateMngr.commit();
				}
			}
			
		}
		
		if (stateMngr.at(StateOperation.GET_UP)) {
			
			if (stateMngr.doWait(0))  {
				module().gradientInit();
				stateMngr.commitMyselfIfNotUsed();
			}
		
			if (stateMngr.doWait(1))  {
				scheduler.enable("module.gradientPropagate");
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(2)) {
				stateMngr.spend("module.gradientPropagate");
			}
			
			if (stateMngr.doWait(3)) {
				scheduler.disable("module.gradientPropagate");
				
				assign (0,2,Mod.Walker_Left);
				assign (1,3,Mod.Walker_Top);
				assign (2,2,Mod.Walker_Right);
//				assign (1,2,Mod.Walker_Dummy);
				
				assign (1,1,Mod.Uplifter_Left);
//				assign (2,2,Mod.Uplifter_Right);
//				assign (0,2,Mod.Uplifter_DR);
//				assign (4,0,Mod.Uplifter_DT);
				
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(4)) {
				actuation.disconnectPart(Group.Uplifter, (NORTH&EAST&FEMALE)|(SOUTH&WEST)&FEMALE);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(5)) {
				actuation.rotate(Mod.Walker_Right,-QUART);
//				actuation.rotate(Mod.Uplifter_Right,-QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(6)) {
				actuation.rotate(Mod.Uplifter_Left,-QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			
			if (stateMngr.doWait(7)) {
				actuation.connect(Mod.Walker_Left,Group.Floor,true);
				stateMngr.commitMyselfIfNotUsed();
			}
					
			
			if (stateMngr.doWait(8)) {
				actuation.disconnect(Mod.Walker_Right,Group.Uplifter,true);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(9)) {
				actuation.rotate(Mod.Uplifter_Left,QUART);
				actuation.rotate(Mod.Walker_Right,QUART);
//				actuation.rotate(Mod.Uplifter_Right,QUART);
				stateMngr.commitMyselfIfNotUsed();
			}			
			
			if (stateMngr.doWait(10)) {
				actuation.connect(Mod.Uplifter_Left,Group.Floor);
				actuation.rotate(Mod.Walker_Top,-EIGHT);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(11)) {
				actuation.rotate(Mod.Walker_Left,HALF);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(12)) {
				actuation.rotate(Mod.Walker_Top,EIGHT);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(13)) {
				if (Group.Uplifter.contains(module().getID())) {
					module().restoreID();
				}
				if (meta().regionID == module().metaID) {
					meta().setVar("continueWalk", 1);
				}
				
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(14)) {
				stateMngr.spend(settings.get("assignTime"));
			}
			
			if (stateMngr.doWait(15)) {
				
				meta().releaseRegion();
				
				if (meta().continueWalk == 0) {
					finish();
				}
				
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(16)) {
				stateMngr.nextOperation(StateOperation.WALK_STEP);
			}
		}
		
		
		
		
		if (stateMngr.at(StateOperation.WALK_STEP)) {
			if (stateMngr.doWait(0)) {
				if (module().getID().equals(Mod.Walker_Right) && nbs(SOUTH).isEmpty()) {
					stateMngr.nextOperation(StateOperation.GET_DOWN);
					return;
				}
				actuation.connect(Mod.Walker_Right,Group.Floor,false);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(1)) {
				actuation.disconnectPart(Mod.Walker_Left,SOUTH);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(2)) {
				actuation.rotate(Mod.Walker_Top,-EIGHT);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(3)) {
				actuation.rotate(Mod.Walker_Right,-HALF);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(4)) {
				actuation.rotate(Mod.Walker_Top,EIGHT);
				stateMngr.commitMyselfIfNotUsed();
			}
						
			if (stateMngr.doWait(5)) {
				if (module().getID().equals(Mod.Walker_Left) && !stateMngr.committed()) {
					module().setID(Mod.Walker_Right);
					stateMngr.commit();
				}
				if (module().getID().equals(Mod.Walker_Right) && !stateMngr.committed()) {
					module().setID(Mod.Walker_Left);
					stateMngr.commit();
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(6)) {
				stateMngr.nextOperation(StateOperation.WALK_STEP);
			}
			
		}
		
		if (stateMngr.at(StateOperation.GET_DOWN)) {
			if (stateMngr.doWait(0)) {
				stateMngr.spend(settings.get("assignTime"));
			}
			
			if (stateMngr.doWait(1)) {
				actuation.rotate(Mod.Walker_Left,QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(2)) {
				meta().absorb();
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(3)) {
				if (Group.Walker.contains(module.getID())) {
					meta().createRegion(new byte[]{meta().Bottom});
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(4)) {
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(5)) {
				actuation.disconnectPart(Mod.Uplifter_Left, SOUTH);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(6)) {
				actuation.connect(Mod.Walker_Left,Mod.Uplifter_Left);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(7)) {
				actuation.disconnect(Mod.Walker_Left,Group.Floor);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(8)) {
				actuation.rotate(Mod.Uplifter_Left,-QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(9)) {
				actuation.connect(Mod.Walker_Right,Mod.Uplifter_Left);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(10)) {
				actuation.disconnect(Mod.Walker_Left,Mod.Uplifter_Left);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(11)) {
				actuation.rotate(Mod.Uplifter_Left,-QUART);
				actuation.rotate(Mod.Walker_Left,-QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(12)) {
				actuation.connect(Mod.Walker_Left,Mod.Uplifter_Left);
				actuation.connect(Group.Floor,Mod.Uplifter_Left);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(13)) {
				if (module().metaID != meta().regionID && module().atTop()) {
					broadcast(new PacketSymmetry(this));
					stateMngr.commit("symmetry initiated");
				}
			}
			
			if (stateMngr.doUntil(14)) {
				if (!module().getGroup().equals(Group.Floor)) {
					module().restoreID();
				}
				finish();
			}
			
		}
		
	}
	

	
	
	
	public void assign(int h, int v, Mod m) {
		if (module().gradH == h && module().gradV == v) {
			module().storeID();
			module().setID(m);
		}
		else {
			visual.print("Coordinate dismatch for " + m + " - " + h + "," + v + " ! = " + module().gradH + "," + module().gradV);
		}
	}
		


	
	
	
	
	public boolean receivePacket (PacketAbsorb p) {
		if (stateMngr.at(StateOperation.INIT)) {
			if (module().metaID == 0) {
				module().setMetaID(p.metaID);
				module().storeID();
				module().setID(Mod.Uplifter_Left);
			}
		}
		return false;
	}
	
	public boolean receivePacket (PacketGradient p) {
		boolean updated = false;
		if (module().gradH > p.h + 1) {
			module().setVar("gradH",(byte)(p.h + 1));
			updated = true;
		}
		if (module().gradV > p.v + 1) {
			module().setVar("gradV",(byte)(p.v + 1));
			updated = true;
		}
		if (updated) {
			scheduler.invokeNow("module.gradientPropagate");
		}
		return true;
	}
	
	
	
	
	public boolean receivePacket (Packet p) {
		boolean handled = false;

		if (stateMngr.at(StateOperation.INIT) || stateMngr.at(StateOperation.CHOOSE) || stateMngr.at(StateOperation.GET_DOWN)) {
//			visual.print("RECEIVE YESSSS2 " + p);
			meta().neighborHook(p);
			handled = true;
		}
		return handled;
	}
	
	public boolean receivePacket (PacketSetMetaId p) {
		boolean handled = false;
		
		byte dest;
		byte index = 0;
		
		if (stateMngr.check(p,new State(StateOperation.INIT,0))) {
			handled = true;
			
				module().metaID = p.newMetaID;
				
				if (p.index==0) {
					module().setPart(MetaPart.Top);
					index = 1;
				}
				else if (p.index==1) {
					module().setPart(MetaPart.Right);
					index = 2;
					if (!nbs(NORTH&EAST&MALE).exists()) {
						meta().enable(); 
						stateMngr.nextOperation(StateOperation.CHOOSE);
					}
					else {
						visual.print(nbs(NORTH&EAST&MALE).toString());
					}
				}
				else if (p.index==2) {
//					module().setPart(MetaPart.Dummy);
					meta().enable(); 
					stateMngr.nextOperation(StateOperation.CHOOSE);
				} 				
			
			

			if (isMALE(p.connDest)) {
				dest = (byte) ((p.connDest + 2) % 4);
			}
			else {
				dest = (byte) ((p.connDest + 4) % 8);
			}
//			if(module().getPart() != MetaPart.Dummy) {
				if (freqLimit("META_ID_SET",settings.getPropagationRate())) {
					unicast((PacketSetMetaId)new PacketSetMetaId(this).setVar("newMetaID", p.newMetaID).setVar("index", index),pow2(dest));
				}
//			}
		}
		
		receivePacket((Packet)p);
		return handled;
	}
	
	


	public boolean receivePacket (PacketSymmetry p) {
		boolean handled = false;
		
		if (stateMngr.check(p,new State(StateOperation.GET_DOWN,13))) {	
			symmetryFix(p);
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
	public IStateOperation getStateChoose() {
		return StateOperation.CHOOSE;
	}
	

	@Override
	public IStateOperation getStateInit() {
		return StateOperation.INIT;
	}

	@Override
	public IMetaPart getMetaPart() {
		return MetaPart.NONE;
	}



	@Override
	public BagModule module() {
		return (BagModule) module;

	}
	
	@Override
	public BagMeta meta() {
		return meta;
	}
	
	public void receiveCustomPacket(byte typeNr, byte[] msg, byte connector) {	
		visual.print("RECEIVE " + typeNr);
		if (typeNr == PacketGradient.getTypeNr()) {
			PacketGradient p = (PacketGradient)new PacketGradient(this).deserialize(msg,connector);
			if (preprocessPacket(p)) {
				receivePacket(p);
				receivePacket((Packet)p);
			}
		}
		
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
		else if (typeNr == PacketAbsorb.getTypeNr()) {
			PacketAbsorb p = (PacketAbsorb)new PacketAbsorb(this).deserialize(msg,connector);
			preprocessPacket(p);
			receivePacket(p);
		}
		else {
			super.makePacket(msg, connector);
		}
			
		
	}
	
}