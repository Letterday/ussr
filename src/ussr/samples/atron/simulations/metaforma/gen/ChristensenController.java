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
	class Settings extends ConfigurationParams {
		
	}
	public Settings set = new Settings();
	
	public static void main(String[] args) {
		MfSimulation.initSimulator();		
		new ChristensenSimulation().main();	
	}

	protected Robot getRobot() {
		ATRON a = new ATRON() {
			public Controller createController() {
				return new ChristensenController();
			}
		};
		return a;
	}

	protected ArrayList<ModulePosition> buildRobot() {
		return new MfBuilder().buildRectangle(3,17, ChristensenController.Mod.Floor);
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
		NONE, GETUP, GETDOWN,WALKSTEP;

		public byte ord() {return (byte) (ordinal() - 1 + GenState.values().length);	}
		public IStateOperation fromByte(byte b) {return values()[b+1 - GenState.values().length];}
	}
	
	
	class BagModule extends BagModuleCore {
		public byte gradPri;
		public byte gradSec;
		public boolean isRef;
		public boolean sourceH;
		public boolean sourceV;
		
		
		public void gradientPropagate () { 
			if (sourceH){
				visual.print("I am source for H");
				setVar("gradPri",(byte)0);
			}
			
			if (sourceV) {
				visual.print("I am source for V");
				setVar("gradSec",(byte)0);
			}
			broadcast((PacketGradient)new PacketGradient(ctrl).setVar("pri", gradPri).setVar("sec", gradSec));
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
			setVar("gradPri",(byte)MAX_BYTE);
			setVar("gradSec",(byte)MAX_BYTE);			
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
		public Collection getGroup () {
			return Collection.valueOf(name().split("_")[0]);
		}

		@Override
		public int ord() {
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
	
	 enum Collection implements ICollectionEnum,IModuleHolder{ALL, NONE, Floor, Walker, Uplifter;

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
		public ICollectionEnum valueFrom(String string) {
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
		Module.Group = Collection.NONE;
	
		module().setPart(MetaPart.NONE);
		
		visual.setColor(Mod.Walker_Left, Color.PINK);
		visual.setColor(Mod.Walker_Top, Color.PINK.darker());
		visual.setColor(Mod.Walker_Right, Color.PINK.darker().darker());
		visual.setColor(Mod.Walker_Dummy, Color.PINK.darker().darker().darker());
		visual.setColor(Collection.Uplifter, Color.WHITE);

		visual.setColor(StateOperation.WALKSTEP,Color.YELLOW);
		visual.setColor(StateOperation.GETDOWN,Color.MAGENTA);
		visual.setColor(StateOperation.GETUP,Color.GREEN);
		visual.setColor(GenState.INIT,Color.WHITE);
		
		visual.setMessageFilter(255^ pow2(PacketDiscover.getTypeNr()));
						
	}

	
	
	public void handleStates() {
				
		if (stateMngr.at(GenState.INIT)) {
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
		
		if (stateMngr.at(GenState.CHOOSE)) {
			if (stateMngr.doUntil(0)) {
				stateMngr.spend("meta.broadcastNeighbors");		
			}
				
			if (stateMngr.doUntil(1)) {
				meta().tryRegion(new byte[]{meta().Bottom}, new byte[]{meta().Top,meta().Left,meta().Right}, StateOperation.GETUP,Orientation.BOTTOM_LEFT);
			}
			
		}
		
		if (stateMngr.at(StateOperation.GETUP)) {
			
			if (stateMngr.doWait(0))  {
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
				
				assign (0,2,Mod.Walker_Left);
				assign (1,3,Mod.Walker_Top);
				assign (2,2,Mod.Walker_Right);
//				assign (1,2,Mod.Walker_Dummy);
				
				assign (1,1,Mod.Uplifter_Left);
//				assign (2,2,Mod.Uplifter_Right);
//				assign (0,2,Mod.Uplifter_DR);
//				assign (4,0,Mod.Uplifter_DT);
				
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(4)) {
				actuation.disconnectPart(Collection.Uplifter, (NORTH&EAST&FEMALE)|(SOUTH&WEST)&FEMALE);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(5)) {
				actuation.rotate(Mod.Walker_Right,-QUART);
//				actuation.rotate(Mod.Uplifter_Right,-QUART);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(6)) {
				actuation.rotate(Mod.Uplifter_Left,-QUART);
				stateMngr.commitEnd();
			}
			
			
			
			if (stateMngr.doWait(7)) {
				actuation.connect(Mod.Walker_Left,Collection.Floor,true);
				stateMngr.commitEnd();
			}
					
			
			if (stateMngr.doWait(8)) {
				actuation.disconnect(Mod.Walker_Right,Collection.Uplifter,true);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(9)) {
				actuation.rotate(Mod.Uplifter_Left,QUART);
				actuation.rotate(Mod.Walker_Right,QUART);
//				actuation.rotate(Mod.Uplifter_Right,QUART);
				stateMngr.commitEnd();
			}			
			
			if (stateMngr.doWait(10)) {
				actuation.connect(Mod.Uplifter_Left,Collection.Floor);
				actuation.rotate(Mod.Walker_Top,-EIGHT);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(11)) {
				actuation.rotate(Mod.Walker_Left,HALF);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(12)) {
				actuation.rotate(Mod.Walker_Top,EIGHT);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(13)) {
				if (Collection.Uplifter.contains(module().getID())) {
					module().restoreID();
				}
				if (meta().regionID == module().metaID) {
					meta().setVar("continueWalk", 1);
				}
				
				stateMngr.commitEnd();
			}
			
			
			
			if (stateMngr.doWait(14)) {
				if (meta().continueWalk == 0) {
					finish();
				}
				else {
					meta().addStatsEnd(Finish.SUCCESS);
					meta().releaseRegion();
					stateMngr.commitEnd();
				}
				
				
			}
			
			if (stateMngr.doWait(15)) {
				stateMngr.nextOperation(StateOperation.WALKSTEP);
			}
		}
		
		
		
		
		if (stateMngr.at(StateOperation.WALKSTEP)) {
			if (stateMngr.doWait(0)) {
				if (module().getID().equals(Mod.Walker_Right) && nbs(SOUTH).isEmpty()) {
					stateMngr.nextOperation(StateOperation.GETDOWN);
					return;
				}
				actuation.connect(Mod.Walker_Right,Collection.Floor,false);
				stateMngr.commitEnd();
			}		
	
			
			if (stateMngr.doWait(1)) {
				actuation.disconnectPart(Mod.Walker_Left,SOUTH);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(2)) {
				actuation.rotate(Mod.Walker_Top,-EIGHT);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(3)) {
				actuation.rotate(Mod.Walker_Right,-HALF);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(4)) {
				actuation.rotate(Mod.Walker_Top,EIGHT);
				stateMngr.commitEnd();
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
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(6)) {
				meta().addStatsEnd(Finish.SUCCESS);

				stateMngr.nextOperation(StateOperation.WALKSTEP);
			}
			
		}
		
		if (stateMngr.at(StateOperation.GETDOWN)) {
			if (stateMngr.doWait(0)) {
				stateMngr.spend(settings.getStatePostTransitionDiscoverTime());
			}
			
			if (stateMngr.doWait(1)) {
				actuation.rotate(Mod.Walker_Left,QUART);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doUntil(2)) {
				meta().absorb();
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doUntil(3)) {
				if (Collection.Walker.contains(module.getID())) {
					meta().createRegion(new byte[]{meta().Bottom});
				}
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(4)) {
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(5)) {
				actuation.disconnectPart(Mod.Uplifter_Left, SOUTH);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(6)) {
				actuation.connect(Mod.Walker_Left,Mod.Uplifter_Left);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(7)) {
				actuation.disconnect(Mod.Walker_Left,Collection.Floor);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(8)) {
				actuation.rotate(Mod.Uplifter_Left,-QUART);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(9)) {
				actuation.connect(Mod.Walker_Right,Mod.Uplifter_Left);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(10)) {
				actuation.disconnect(Mod.Walker_Left,Mod.Uplifter_Left);
				stateMngr.commitEnd();
			}
			
			
			if (stateMngr.doWait(11)) {
				actuation.rotate(Mod.Uplifter_Left,-QUART);
				actuation.rotate(Mod.Walker_Left,-QUART);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(12)) {
				actuation.connect(Mod.Walker_Left,Mod.Uplifter_Left);
				actuation.connect(Collection.Floor,Mod.Uplifter_Left);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doUntil(13)) {
				if (module().metaID != meta().regionID && module().atTop()) {
					broadcast(new PacketSymmetry(this));
					stateMngr.commit("symmetry initiated");
				}
			}
			
			if (stateMngr.doWait(14)) {
				module().restoreID();
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(15)) {
				meta().addStatsEnd(Finish.SUCCESS);
				finish();
			}
			
		}
		
	}
	

	
	
	
	public void assign(int h, int v, Mod m) {
		if (module().gradPri == h && module().gradSec == v) {
			module().storeID();
			module().setID(m);
		}
		else {
			visual.print("Coordinate dismatch for " + m + " - " + h + "," + v + " ! = " + module().gradPri + "," + module().gradSec);
		}
	}
		


	
	
	
	
	public boolean receivePacket (PacketAbsorb p) {
		if (stateMngr.at(GenState.INIT)) {
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

		if (stateMngr.at(GenState.INIT) || stateMngr.at(GenState.CHOOSE) || stateMngr.at(StateOperation.GETDOWN)) {
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
		
		if (stateMngr.check(p,new State(GenState.INIT,0))) {
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
						stateMngr.nextOperation(GenState.CHOOSE);
					}
					else {
						visual.print(nbs(NORTH&EAST&MALE).toString());
					}
				}
				else if (p.index==2) {
//					module().setPart(MetaPart.Dummy);
					meta().enable(); 
					stateMngr.nextOperation(GenState.CHOOSE);
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
		
		if (stateMngr.check(p,new State(StateOperation.GETDOWN,13))) {	
			module().fixSymmetry(p.connSource, p.connDest);
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



	@Override
	public IStateOperation getStateInst() {
		return StateOperation.NONE;
	}
	
}