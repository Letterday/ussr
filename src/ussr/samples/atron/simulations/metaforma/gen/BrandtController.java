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
	
	
	class Settings extends SettingsBase {
		
	}
	public SettingsBase set = new Settings();
	
	
	public static void main(String[] args) {
		MfSimulation.initSimulator();		
		new BrandtSimulation().main();
	
	}

	protected Robot getRobot() {
		ATRON a = new ATRON() {
			public Controller createController() {
				return new BrandtController(set);
			}
		};
		return a;
	}

	protected ArrayList<ModulePosition> buildRobot() {
		return new MfBuilder().buildGrid(set, BrandtController.Mod.F);
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
}

class PacketGradient extends Packet {
	public static byte getTypeNr() {return 6;}
	
	public byte h;
	public byte v;
	
	public PacketGradient (MfController c) {
		super(c);
		setType(getTypeNr());
	}
	
	public String toStringPayload () {
		return "[" + h + "," + v + "]";
	}
	
	public byte[] serializePayload () {
		return new byte[]{h,v};
	}
	
	public PacketGradient deserializePayload (byte[] b) {
		h = b[0];
		v = b[1];
		return this;
	}

	
	
}



public class BrandtController extends MfController implements ControllerInformationProvider {
	
	public enum StateOperation implements IStateOperation {
		INIT, CHOOSE, FLIP_UP, FLIP_BOTTOM, FLIP_TOP;
		public byte ord() {return (byte) ordinal();	}
		public IStateOperation fromByte(byte b) {return values()[b];}
	}
	class BagModule extends BagModuleCore {
		public byte gradH;
		public byte gradV;
		public boolean isRef;
		
		public void gradient () { 
			if (nbs(EAST&MALE).nbsInRegion(true).size() == 2 && nbs(WEST&SOUTH&MALE).nbsInRegion(true).isEmpty() || nbs(EAST&FEMALE).nbsInRegion(true).size() == 2 && nbs(WEST&NORTH&FEMALE).nbsInRegion(true).isEmpty()){
				gradH = 0;
			}
			if (nbs(EAST&MALE).nbsInRegion(true).size() == 2 && nbs(WEST&NORTH&MALE).nbsInRegion(true).isEmpty() || nbs(WEST&FEMALE).nbsInRegion(true).size() == 2 && nbs(EAST&NORTH&FEMALE).nbsInRegion(true).isEmpty()) {
				gradV = 0;
			}
			broadcast((PacketGradient)new PacketGradient(ctrl));
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

	
	
	enum ModuleRole implements IRole {
		NONE,Left,Bottom,Right,Top;
		public IRole fromByte(byte b) {
			return values()[b];
		}
		public byte index() {return (byte) ordinal();}
		public byte size() {
			// None is no role
			return (byte) (values().length - 1);
		}
		
	} 
	public enum Mod  implements IModule,IModEnum{
		ALL,
		NONE,
		F(100),
		Clover_North, Clover_South, Clover_West, Clover_East, 
		Left(5),
		Right(5), 
		Uplifter_Left, Uplifter_Right, Uplifter_Top, Uplifter_Bottom;

		byte count;
		
		private Mod () {
			count = 1;
		}
		
		private Mod (int c) {
			count = (byte) c;
		}
		
		
		
		public IModule module () {
			return new Module (this);
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
		public Group getGrouping () {
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
	enum Group implements IGroupEnum,IModuleHolder{ALL, NONE, F, Clover, Left, Right, Uplifter;
		public boolean contains(IModule m) {
			return equals(m.getGrouping());
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

	public BrandtController(SettingsBase set) {
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
		module().role = ModuleRole.NONE;
		
		
		visual.setColor(Mod.Clover_North, Color.PINK);
		visual.setColor(Mod.Clover_South, Color.PINK.darker());
		visual.setColor(Mod.Clover_West, Color.PINK.darker().darker());
		visual.setColor(Mod.Clover_East, Color.PINK.darker().darker().darker());
		visual.setColor(Group.Uplifter, Color.WHITE);

		visual.setColor(StateOperation.FLIP_TOP,Color.CYAN);
		visual.setColor(StateOperation.FLIP_BOTTOM,Color.YELLOW);
		visual.setColor(StateOperation.FLIP_UP,Color.GREEN);
		visual.setColor(StateOperation.INIT,Color.WHITE);
		
		visual.setMessageFilter(255 ^ pow2(PacketDiscover.getTypeNr()));
		visual.setMessageFilterMeta(255);				
	}

	public void handleStates() {
		
		if (stateMngr.at(StateOperation.INIT)) {
			// Make groupings of 4
			if (stateMngr.doUntil(0)) {
				if (nbs(EAST&MALE, ModuleRole.NONE).size() == 2 && !nbs(WEST, ModuleRole.NONE).exists()) {
					module().setRole(ModuleRole.Left);
					module().setVar("metaID",module().getId().ord());
				}
				if (module().role == ModuleRole.Left)	{	
					unicast((PacketSetMetaId)new PacketSetMetaId(this).setVar("newMetaID", module().metaID),EAST&MALE&NORTH);
				}
			}
			
			if (stateMngr.doWait(1)) {
				
				stateMngr.commit();
			}
 
			if (stateMngr.doUntil(2,set.getMetaDirectDiscoverInterval())) {
				// Share meta neighborhood hor + ver
				scheduler.invokeNow("broadcastMetaVars");
				scheduler.invokeNow("broadcastDiscover");
				stateMngr.spend(set.getMetaDirectDiscoverTime());
			}
			
			if (stateMngr.doUntil(3,set.getMetaIndirectDiscoverInterval())) {
				// Share meta neighborhood diag
				meta().broadcastNeighbors();
				stateMngr.spend(set.getMetaIndirectDiscoverTime());
			}
				
			if (stateMngr.doWait(4)) {
				stateMngr.nextOperation(StateOperation.CHOOSE);
			}
			
			
		}
		if (stateMngr.at(StateOperation.CHOOSE)) {
			if (stateMngr.doUntil(0)) {
				
				if (meta().Top != 0 && meta().Left == 0 && meta().Right == 0 && meta().TopLeft == 0 && meta().Bottom == 0) {
					meta().createRegion(new byte[]{meta().Top});
					stateMngr.setAfterConsensus(StateOperation.FLIP_BOTTOM);
					stateMngr.commit();
				}
				
				if (meta().Top == 0 && meta().Right != 0) {
					if (meta().TopRight == 0) {
						meta().createRegion(new byte[]{meta().Right});
						stateMngr.setAfterConsensus(StateOperation.FLIP_TOP);
						stateMngr.commit();
					}
					else {
						meta().createRegion(new byte[]{meta().TopRight,meta().Right});
						stateMngr.setAfterConsensus(StateOperation.FLIP_UP);
						stateMngr.commit();
					}
					
				}
				
			}
		}

		
		if (stateMngr.at(StateOperation.FLIP_TOP)) {
			if (stateMngr.doWait(0)) {
				if (module().metaID == meta().regionID) {
					if (module.role == ModuleRole.Bottom) {
						module().isRef = true;
					}
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(1)) {
				module().gradH = MAX_BYTE;
				module().gradV = MAX_BYTE;
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(2,set.getGradientInterval()))  {
				module().gradient();
				stateMngr.spend(set.getGradientTime());
			}
	
			
			
			if (stateMngr.doWait(3)) {
				assign (1,1,Mod.Uplifter_Left);
				assign (0,2,Mod.Uplifter_Right);
				assign (0,0,Mod.Uplifter_Top);
				assign (1,3,Mod.Uplifter_Bottom);
				stateMngr.commitMyselfIfNotUsed();
			}
	
			if (stateMngr.doWait(4)) {
				actuation.disconnectPart (Mod.Uplifter_Left, NORTH&MALE&EAST|SOUTH&MALE&WEST);
				actuation.disconnectPart (Mod.Uplifter_Right, NORTH&MALE&EAST);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(5)) {
				actuation.rotate(new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				stateMngr.commitMyselfIfNotUsed();
			}
	
			if (stateMngr.doWait(6)) {
				actuation.disconnectPart (Mod.Uplifter_Left,SOUTH);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(7)) {
				actuation.rotate(Mod.Uplifter_Bottom,-180);
	//			actuation.rotate(new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				stateMngr.commitMyselfIfNotUsed();
			}
	
			if (stateMngr.doWait(8)) {
				actuation.rotate(Mod.Uplifter_Right,-90);
				actuation.rotate(Mod.Uplifter_Left,-90);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(9)) {
				actuation.rotate(Mod.Uplifter_Top,-180);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(10)) {
				actuation.connect(Group.F,Group.Uplifter);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(11)) {
				if (getGrouping() == Group.Uplifter) {
					renameRestore();
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(12)) {
	//			discoverNeighbors();
				if (module().isRef) {
					broadcast(new PacketSymmetry(this));
					stateMngr.commit("symmetry initiated");
				}
			}
			
			if (stateMngr.doWait (13)) {
				if (module().isRef) {
					module().isRef = false;
				}
				finish();
			}
		}
		
		
//		if (stateMngr.at(StateOperation.FLIP_BOTTOM)) {
//			if (stateMngr.doWait(0)) {
//				if (!metaBossMyself()) {
//					if (module.role== ModuleRole.Right) {
//						varSet(VarLocal.isRef,10);
//					}
//				}
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(1)) {
//				module.gradH = MAX_BYTE;
//				module.gradV = MAX_BYTE;
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doUntil(2,400))  {
//				gradientCreate();
//				stateMngr.spend(5);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//	
//			
//			
//			if (stateMngr.doWait(3)) {
//				assign (1,0,Mod.Uplifter_Left);
//				assign (2,1,Mod.Uplifter_Right);
//				assign (3,0,Mod.Uplifter_Top);
//				assign (0,1,Mod.Uplifter_Bottom);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//	
//			if (stateMngr.doWait(4)) {
//				disconnectPart (Mod.Uplifter_Left, NORTH&FEMALE&EAST|SOUTH&FEMALE&WEST);
//				disconnectPart (Mod.Uplifter_Right, NORTH&FEMALE&EAST|SOUTH&FEMALE&WEST);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(5)) {
//				actuation.rotate(new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//	
//			if (stateMngr.doWait(6)) {
//				disconnectPart (Mod.Uplifter_Left,SOUTH);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(7)) {
//				actuation.rotate(Mod.Uplifter_Top,-180);
//	//			actuation.rotate(new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//	
//			if (stateMngr.doWait(8)) {
//				actuation.rotate(Mod.Uplifter_Right,-90);
//				actuation.rotate(Mod.Uplifter_Left,-90);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			
//			if (stateMngr.doWait(9)) {
//				actuation.rotate(Mod.Uplifter_Bottom,-180);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			
//			if (stateMngr.doWait(10)) {
//				actuation.connect(Group.F,Group.Uplifter);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(11)) {
//				if (getGrouping() == Group.Uplifter) {
//					renameRestore();
//				}
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doUntil(12)) {
//				if (varGet(VarLocal.isRef) == 10) {
//					unicast(WEST&MALE,PacketCoreType.SYMMETRY,false);
//					stateMngr.commit("symmetry initiated");
//				}
//			}
//			
//			if (stateMngr.doWait (13)) {
//				if (varGet(VarLocal.isRef) == 10) {
//					varSet(VarLocal.isRef, 2);
//				}
//				finish();
//			}
//		}
		
		if (stateMngr.at(StateOperation.FLIP_UP)) {
			if (stateMngr.doWait(0)) {
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(1)) {
				if (module().metaID == meta().regionID) {
					if (module().role == ModuleRole.Left){
						renameStore();
						renameTo(Mod.Clover_West);
					}
					if (module().role == ModuleRole.Bottom){
						renameStore();
						renameTo(Mod.Clover_South);
					}
					if (module().role == ModuleRole.Top){
						renameStore();
						renameTo(Mod.Clover_North);
					}
					if (module().role == ModuleRole.Right){
						renameStore();
						renameTo(Mod.Clover_East);
					}
				}
				else {
					if (module().role== ModuleRole.Bottom) {
						module().setVar("isRef",true);
					}
				}
				
				stateMngr.commitMyselfIfNotUsed();
				
			}
			
			if (stateMngr.doWait(2)) {
				actuation.disconnect(Mod.Clover_West, Mod.Clover_South);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(3)) {
				actuation.rotate(Mod.Clover_East,-90);
				stateMngr.commitMyselfIfNotUsed();
			}
				
			if (stateMngr.doWait(4)) {
				actuation.rotate(Mod.Clover_North,-180);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(5)) {
				actuation.rotate(Mod.Clover_East,-90);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(6)) {
				actuation.connect(Mod.Clover_North,Group.F);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(7)) {
				actuation.connect(Mod.Clover_West,Group.F);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(8)) {
				actuation.disconnect(new ModuleSet().add(Mod.Clover_South).add(Mod.Clover_East),Group.F);
				stateMngr.commitMyselfIfNotUsed();
			}
	
			if (stateMngr.doWait(9)) {
				actuation.rotate(Mod.Clover_North,-180);
				stateMngr.commitMyselfIfNotUsed();
			}
						
	
			if (stateMngr.doWait(10)) {
				actuation.rotate(Mod.Clover_East,-180);
				stateMngr.commitMyselfIfNotUsed();
			}
			
	
			if (stateMngr.doWait(11)) {
				actuation.connect(Mod.Clover_South,Mod.Clover_West);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(12)) {
				if (getGrouping() == Group.Clover) {
					context.switchEastWest();
					context.switchNorthSouth();
				}
//				if (nbs(EAST&FEMALE).sizeEquals(2,true)) {
//					unicast(EAST&FEMALE,PacketCoreType.SYMMETRY,false);
//					stateMngr.commit();
//				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			
			if (stateMngr.doWait (13)) {
				if (getGrouping() == Group.Clover) {
					renameRestore();
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait (14)) {
				finish();
				
			}
		
		}
		

	}

	private void assign(int h, int v, Mod m) {
		if (module().gradH == h && module().gradV == v) {
			module().setId(m);
		}
	}



	public void finish () {
		meta().releaseRegion();
		meta().disable();
		stateMngr.nextOperation(StateOperation.INIT);
	}
	
	
//	private void assign(int h, int v, IModule m) {
//		visual.print("## " + h + "," + v + " --- " + varGet(VarLocal.gradH) + "," + varGet(VarLocal.gradV));
//		if (varGet(VarLocal.gradH) == h && varGet(VarLocal.gradV) == v){
//			renameStore();
//			renameTo(m);
//			
//		}
//		
//	}
		
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
		if (module().gradH > p.h) {
			module().gradH = p.h;
			updated = true;
		}
		if (module().gradV > p.v) {
			module().gradV = p.v;
			updated = true;
		}
		if (updated) {
			scheduler.invokeNow("gradientCreate");
		}
		return true;
	}
	
	public void gradientCreate() {
		module().gradient();
	}
	
	
	public boolean receivePacket (Packet p) {
		boolean handled = false;
		
		if (stateMngr.at(p.getState())) {
			if (p.getState().match(StateOperation.CHOOSE) ) {
				meta().neighborHook(p);
				handled = true;
			}
			if (p.getState().match(StateOperation.INIT) ) {
				meta().neighborHook(p);
				handled = true;
			}
		}
		return handled;
	}
	
	public boolean receivePacket (PacketSetMetaId p) {
		boolean handled = false;

		if (stateMngr.at(p.getState())) {
			if (p.getState().match(new State(StateOperation.INIT,0))) {
		
				handled = true;
				if (module().metaID == 0) {
					module().metaID = p.newMetaID;
					
					if (isMALE(p.connDest)) {
						module().setRole(ModuleRole.Right);
					}
					else {
						if (isWEST(p.connDest)) {
							module().setRole(ModuleRole.Top);
						}
						else {
							module().setRole(ModuleRole.Bottom);
						}
					}
				}
				
				if (module().getRole() == ModuleRole.Left) {
					meta().enable(); // must be here, not in next state!
					stateMngr.nextInstruction();
				}
				else {
					if (freqLimit("META_ID_SET",set.getMetaIndirectDiscoverInterval())) {
						unicast((PacketSetMetaId)new PacketSetMetaId(this).setVar("newMetaID", p.newMetaID),pow2((p.connDest + 4) % 8));
					}
				}
			}	
		}
		
		receivePacket((Packet)p);
		return handled;
	}
	
	


	public boolean receivePacket (PacketSymmetry p) {
		boolean handled = false;
		if (stateMngr.at(p.getState())) {
			if (p.getState().match(new State(StateOperation.INIT,4))) {
				symmetryFix(p);
				handled = true;
			}
		}
		
		
		return handled;
	}

	protected boolean receivePacket(PacketAddNeighbor p) {
		if (p.metaID == meta().Left) {
			meta().TopLeft = p.first;
			meta().BottomLeft  = p.second;
		}
		if (p.metaID == meta().Right) {
			meta().TopRight = p.first;
			meta().BottomRight = p.second;
		}
		if (p.metaID == meta().Top) {
			meta().TopLeft = p.first;
			meta().TopRight = p.second;
		}
		if (p.metaID == meta().Bottom) {
			meta().BottomLeft = p.first;
			meta().BottomRight = p.second;
		}
		receivePacket((Packet)p);
		return true;
	}
	
	
	

	@Override
	public IStateOperation getInstOperation() {
		return StateOperation.INIT;
	}

	@Override
	public IRole getInstRole() {
		return ModuleRole.NONE;
	}



	@Override
	public BagModule module() {
		if (module == null) {
//			module = (BagModule)new BagModule().setController(this);
		}
		return (BagModule) module;

	}
	
	@Override
	public BagMeta meta() {
		if (meta == null) {
//			meta = (BagMeta)new BagMeta().setController(this);
		}
		return meta;

	}
	
	public void makePacket(byte[] msg, byte connector) {
		if (Packet.isPacket(msg)) {
			byte typeNr = Packet.getType(msg);
			if (typeNr == PacketGradient.getTypeNr()) {
				receivePacket(new PacketGradient(this).deserialize(msg,connector));
			}
			else if (typeNr == PacketAddNeighbor.getTypeNr()) {
				receivePacket(new PacketAddNeighbor(this).deserialize(msg,connector));
			}
			else {
				super.makePacket(msg, connector);
			}
			
		}
		else {
//			receivePacket(MetaPacket(msg));
		}
	}
	
}