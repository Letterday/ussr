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
		INIT, CHOOSE, FLIPALONG_UPLEFT, FLIPALONG_UPRIGHT, FLIPTHROUGH_TOPLEFT, FLIPTHROUGH_TOPRIGHT, FLIPTHROUGH_BOTTOMLEFT, FLIPTHROUGH_BOTTOMRIGHT;
		public byte ord() {return (byte) ordinal();	}
		public IStateOperation fromByte(byte b) {return values()[b];}
	}
	class BagModule extends BagModuleCore {
		public byte gradH;
		public byte gradV;
		public boolean isRef;
		public boolean sourceH;
		public boolean sourceV;
//		public boolean isT;
//		public boolean isB;
//		public boolean isL;
//		public boolean isR;
		
		
		
		
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
		
		public boolean atL() {return nbs(EAST&MALE).nbsInRegion(true).size() == 2 && nbs(WEST&NORTH&MALE).nbsInRegion(true).isEmpty() || nbs(WEST&FEMALE).nbsInRegion(true).size() == 2 && nbs(EAST&NORTH&FEMALE).nbsInRegion(true).isEmpty();}
		public boolean atR() {return nbs(WEST&MALE).nbsInRegion(true).size() == 2 && nbs(EAST&SOUTH&MALE).nbsInRegion(true).isEmpty() || nbs(EAST&FEMALE).nbsInRegion(true).size() == 2 && nbs(WEST&SOUTH&FEMALE).nbsInRegion(true).isEmpty();}
		public boolean atT() {return nbs(WEST&MALE).nbsInRegion(true).size() == 2 && nbs(EAST&NORTH&MALE).nbsInRegion(true).isEmpty() || nbs(WEST&FEMALE).nbsInRegion(true).size() == 2 && nbs(EAST&SOUTH&FEMALE).nbsInRegion(true).isEmpty();}
		public boolean atB() {return nbs(EAST&MALE).nbsInRegion(true).size() == 2 && nbs(WEST&SOUTH&MALE).nbsInRegion(true).isEmpty() || nbs(EAST&FEMALE).nbsInRegion(true).size() == 2 && nbs(WEST&NORTH&FEMALE).nbsInRegion(true).isEmpty();}

		public void gradientInit(boolean sH, boolean sV) {
			sourceH = sH;
			sourceV = sV;
			visual.print("gradientInit()");
			setVar("gradH",(byte)MAX_BYTE);
			setVar("gradV",(byte)MAX_BYTE);			
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
		Dummy_Left,
		Dummy_Right,
		F(100),
		Clover_North, Clover_South, Clover_West, Clover_East, 
		Left(5),
		Right(5), 
		Uplifter_Left, Uplifter_Right, Uplifter_Top, Uplifter_Bottom,Dummy_Ref;

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
	enum Group implements IGroupEnum,IModuleHolder{ALL, NONE, F, Clover, Left, Right, Uplifter,Dummy;
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

		visual.setColor(StateOperation.FLIPTHROUGH_TOPLEFT,Color.CYAN);
		visual.setColor(StateOperation.FLIPTHROUGH_TOPRIGHT,Color.YELLOW);
		visual.setColor(StateOperation.FLIPTHROUGH_BOTTOMLEFT,Color.PINK);
		visual.setColor(StateOperation.FLIPTHROUGH_BOTTOMRIGHT,Color.RED);
		visual.setColor(StateOperation.FLIPALONG_UPLEFT,Color.GREEN);
		visual.setColor(StateOperation.FLIPALONG_UPRIGHT,Color.MAGENTA);
		visual.setColor(StateOperation.INIT,Color.WHITE);
		
		visual.setMessageFilter(255^ pow2(PacketDiscover.getTypeNr()));
		visual.setMessageFilterMeta(255);				
	}

	public void handleStates() {
		
		if (stateMngr.at(StateOperation.INIT)) {
			// Make groupings of 4
			if (stateMngr.doUntil(0)) {
				if (nbs(EAST&MALE, ModuleRole.NONE).size() == 2 && !nbs(WEST, ModuleRole.NONE).exists()) {
					module().setRole(ModuleRole.Left);
					module().setVar("metaID",module().getID().ord());
				}
				if (module().role == ModuleRole.Left)	{	
					unicast((PacketSetMetaId)new PacketSetMetaId(this).setVar("newMetaID", module().metaID),EAST&MALE&NORTH);
				}
				broadcast(new PacketDiscover(this));
			}
		}
		
		if (stateMngr.at(StateOperation.CHOOSE)) {
			
			if (stateMngr.doUntil(0,set.getMetaDirectDiscoverInterval())) {
				// Share meta neighborhood hor + ver
				scheduler.invokeNow("broadcastMetaVars");
				scheduler.invokeNow("broadcastDiscover");
				stateMngr.spend(set.getMetaDirectDiscoverTime());
			}
			
			if (stateMngr.doUntil(1,set.getMetaIndirectDiscoverInterval())) {
				// Share meta neighborhood diag
				meta().broadcastNeighbors();
				stateMngr.spend(set.getMetaIndirectDiscoverTime());
			}
				
			
			if (stateMngr.doUntil(2)) {
				if (meta().regionID() != 0 && stateMngr.timeSpentInState() > 25) {
					meta().releaseRegion();
					stateMngr.nextOperation(StateOperation.CHOOSE);
				}
				
				
				
				if (meta().Top == 0 && meta().Bottom == 0 && meta().Right != 0) {
					if (meta().TopRight == 0) {
						meta().createRegion(new byte[]{meta().Right},(byte)0);
						stateMngr.setAfterConsensus(StateOperation.FLIPTHROUGH_TOPLEFT);
						stateMngr.commit();
					}
					else {
						meta().createRegion(new byte[]{meta().TopRight,meta().Right},(byte)0);
						stateMngr.setAfterConsensus(StateOperation.FLIPALONG_UPLEFT);
						stateMngr.commit();
					}
				}
				
				if (meta().Top == 0  && meta().Bottom == 0 && meta().Left != 0) {
					if (meta().TopLeft == 0) {
						meta().createRegion(new byte[]{meta().Left},(byte)0);
						stateMngr.setAfterConsensus(StateOperation.FLIPTHROUGH_TOPRIGHT);
						stateMngr.commit();
					}
					else {
						meta().createRegion(new byte[]{meta().TopLeft,meta().Left},(byte)0);
						stateMngr.setAfterConsensus(StateOperation.FLIPALONG_UPRIGHT);
						stateMngr.commit();
					}
				}
				
				if (meta().Bottom == 0  && meta().Top != 0) {
//					if (meta().Left == 0  && meta().TopLeft == 0) {
//						meta().createRegion(new byte[]{meta().Top},(byte)0);
//						stateMngr.setAfterConsensus(StateOperation.FLIPTHROUGH_BOTTOMLEFT);
//						stateMngr.commit();
//					}
					if (meta().Right == 0  && meta().TopRight == 0) {
						meta().createRegion(new byte[]{meta().Top},(byte)0);
						stateMngr.setAfterConsensus(StateOperation.FLIPTHROUGH_BOTTOMRIGHT);
						stateMngr.commit();
					}
				}
			}
		}

		
		if (stateMngr.at(StateOperation.FLIPTHROUGH_TOPLEFT) || stateMngr.at(StateOperation.FLIPTHROUGH_TOPRIGHT) || stateMngr.at(StateOperation.FLIPTHROUGH_BOTTOMLEFT) || stateMngr.at(StateOperation.FLIPTHROUGH_BOTTOMRIGHT)) {
			if (stateMngr.doWait(0)) {
				
				boolean sourceH = false;
				boolean sourceV = false; 
				
				if (stateMngr.at(StateOperation.FLIPTHROUGH_TOPLEFT)) {
					sourceH = module().atB();
					sourceV = module().atL();
					QUART = -90;
					HALF = -180;
				}
				
				if (stateMngr.at(StateOperation.FLIPTHROUGH_TOPRIGHT)) {
					sourceH = module().atB();
					sourceV = module().atR();
					QUART = 90;
					HALF = 180;
				}
				
				if (stateMngr.at(StateOperation.FLIPTHROUGH_BOTTOMLEFT)) {
					sourceH = module().atR();
					sourceV = module().atB();
					QUART = -90;
					HALF = -180;
				}
				
				if (stateMngr.at(StateOperation.FLIPTHROUGH_BOTTOMRIGHT)) {
					sourceH = module().atL();
					sourceV = module().atB();
					QUART = 90;
					HALF = 180;
				}
				
				module().gradientInit(sourceH,sourceV);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(1,set.getGradientInterval()))  {
				
				module().gradientPropagate();
				stateMngr.spend(set.getGradientTime());
			}
			
			if (stateMngr.doWait(2)) {
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(3)) {
				assign (1,1,Mod.Uplifter_Left);
				assign (0,2,Mod.Uplifter_Right);
				assign (0,1,Mod.Dummy_Left);
				assign (0,0,Mod.Uplifter_Top);
				assign (1,2,Mod.Dummy_Right);
				assign (1,3,Mod.Uplifter_Bottom);
				if (module().gradH == 0 && module().gradV == 3) {
					module().setVar("isRef",true);
				}
				else {
					module().setVar("isRef",false);
				}
				stateMngr.commitMyselfIfNotUsed();
			}
	
			if (stateMngr.doWait(4)) {
//				actuation.disconnectPart (Mod.Uplifter_Left, NORTH&MALE&EAST|SOUTH&MALE&WEST);
//				actuation.disconnectPart (Mod.Uplifter_Right, NORTH&MALE&EAST);
//				actuation.disconnectDiagonal(Mod.Uplifter_Right);
//				actuation.disconnectDiagonal(Mod.Uplifter_Left);
				actuation.disconnect(Mod.Dummy_Left,Mod.Uplifter_Left);
				actuation.disconnect(Mod.Dummy_Right,Mod.Uplifter_Right);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(5)) {
				actuation.rotate(new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
	
			if (stateMngr.doWait(6)) {
//				actuation.disconnectPart (Mod.Uplifter_Left,SOUTH);
				actuation.disconnect(Mod.Dummy_Right,Mod.Uplifter_Left);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(7)) {
				actuation.rotate(Mod.Uplifter_Bottom,HALF);
	//			actuation.rotate(new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				stateMngr.commitMyselfIfNotUsed();
			}
	
			if (stateMngr.doWait(8)) {
				actuation.rotate(Mod.Uplifter_Right,QUART);
				actuation.rotate(Mod.Uplifter_Left,QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(9)) {
				actuation.rotate(Mod.Uplifter_Top,HALF);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(10)) {
				actuation.connect(Mod.Uplifter_Right,Mod.Uplifter_Top);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(11)) {
				if (!module().getGroup().equals(Group.F)) {
					module().restoreID();
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(12)) {
				actuation.connect(Group.F,Group.F);
				stateMngr.commitMyselfIfNotUsed();
			}
		
		
			
			if (stateMngr.doUntil(13)) {
	//			discoverNeighbors();
				if (module().isRef) {
					broadcast(new PacketSymmetry(this));
					stateMngr.commit("symmetry initiated");
				}
				stateMngr.spend(6);
			}
			
			if (stateMngr.doWait (14)) {
				if (module().isRef) {
					module().setVar("isRef",false);
				}
				stateMngr.spend(3);
			}
			
			if (stateMngr.doWait (15)) {
				finish();
			}
			
			
		}
	
		
		if (stateMngr.at(StateOperation.FLIPALONG_UPLEFT) || stateMngr.at(StateOperation.FLIPALONG_UPRIGHT)) {
			if (stateMngr.doWait(0)) {
				boolean sourceH = false;;
				boolean sourceV = false; 
				
				if (stateMngr.at(StateOperation.FLIPALONG_UPRIGHT)) {
					sourceH = module().atL();
					sourceV = module().atB();
					QUART = 90;
					HALF = 180;
				}
				
				if (stateMngr.at(StateOperation.FLIPALONG_UPLEFT)) {
					sourceH = module().atR();
					sourceV = module().atB();
					QUART = -90;
					HALF = -180;
				}
				
				module().gradientInit(sourceH,sourceV);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(1,set.getGradientInterval()))  {
				module().gradientPropagate();
				stateMngr.spend(set.getGradientTime());
			}
			
			if (stateMngr.doWait(2)) {
				assign (3,0,Mod.Clover_West);
				assign (2,0,Mod.Clover_South);
				assign (3,1,Mod.Clover_North);
				assign (2,1,Mod.Clover_East);
				if (module().gradH == 0 && module().gradV == 3) {
					module().setVar("isRef",true);
				}
				else {
					module().setVar("isRef",false);
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(3)) {
				actuation.disconnect(Mod.Clover_West, Mod.Clover_South);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(4)) {
				actuation.rotate(Mod.Clover_East,QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
				
			if (stateMngr.doWait(5)) {
				actuation.rotate(Mod.Clover_North,HALF);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(6)) {
				actuation.rotate(Mod.Clover_East,QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(7)) {
				actuation.connect(Mod.Clover_North,Group.F);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(8)) {
				actuation.connect(Mod.Clover_West,Group.F);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(9)) {
				actuation.disconnect(new ModuleSet().add(Mod.Clover_South).add(Mod.Clover_East),Group.F);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(10)) {
				actuation.rotate(Mod.Clover_East,-QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
	
			if (stateMngr.doWait(11)) {
				actuation.rotate(Mod.Clover_North,QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
						
	
			if (stateMngr.doWait(12)) {
				actuation.rotate(Mod.Clover_East,-QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(13)) {
				actuation.rotate(Mod.Clover_North,QUART);
				stateMngr.commitMyselfIfNotUsed();
			}
			
	
			if (stateMngr.doWait(14)) {
				actuation.connect(Mod.Clover_South,Mod.Clover_West);
				stateMngr.commitMyselfIfNotUsed();
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
			if (stateMngr.doUntil(15)) {
				if (module().isRef) {
					broadcast(new PacketSymmetry(this));
					stateMngr.commit();
				}
				stateMngr.spend(6);
			}
			if (stateMngr.doWait (16)) {
				if (module().getGroup() == Group.Clover) {
					module().restoreID();
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait (17)) {
				finish();
				
			}
		
		}
		

	}

	private void assign(int h, int v, Mod m) {
		if (module().gradH == h && module().gradV == v) {
			module().storeID();
			module().setID(m);
		}
	}



	public void finish () {
		meta().disable();
		meta().releaseRegion();
		module().setMetaID(0);
		module().setRole(ModuleRole.NONE);
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
		if (module().gradH > p.h + 1) {
			module().setVar("gradH",(byte)(p.h + 1));
			updated = true;
		}
		if (module().gradV > p.v + 1) {
			module().setVar("gradV",(byte)(p.v + 1));
			updated = true;
		}
		if (updated) {
			scheduler.invokeNow("gradientCreate");
		}
		return true;
	}
	
	public void gradientCreate() {
		module().gradientPropagate();
	}
	
	
	public boolean receivePacket (Packet p) {
		boolean handled = false;
//		visual.print("RECEIVE " + p);
		if (stateMngr.check(p,StateOperation.INIT) ) {
//			visual.print("RECEIVE YESSSS1 " + p);
			meta().neighborHook(p);
			handled = true;
		}
		if (stateMngr.check(p,StateOperation.CHOOSE) ) {
//			visual.print("RECEIVE YESSSS2 " + p);
			meta().neighborHook(p);
			handled = true;
		}
		return handled;
	}
	
	public boolean receivePacket (PacketSetMetaId p) {
		boolean handled = false;

		if (stateMngr.check(p,new State(StateOperation.INIT,0))) {
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
				stateMngr.nextOperation(StateOperation.CHOOSE);
//					stateMngr.nextInstruction();
			}
			else {
				if (freqLimit("META_ID_SET",set.getMetaIndirectDiscoverInterval())) {
					unicast((PacketSetMetaId)new PacketSetMetaId(this).setVar("newMetaID", p.newMetaID),pow2((p.connDest + 4) % 8));
				}
			}	
		}
		
		receivePacket((Packet)p);
		return handled;
	}
	
	


	public boolean receivePacket (PacketSymmetry p) {
		boolean handled = false;
		
		if (stateMngr.check(p,new State(StateOperation.FLIPTHROUGH_TOPLEFT,13)) || stateMngr.check(p,new State(StateOperation.FLIPTHROUGH_TOPRIGHT,13)) || stateMngr.check(p,new State(StateOperation.FLIPTHROUGH_BOTTOMLEFT,13)) || stateMngr.check(p,new State(StateOperation.FLIPTHROUGH_BOTTOMRIGHT,13))) {	
			symmetryFix(p);
			handled = true;
		}
		
		
		if (stateMngr.check(p,new State(StateOperation.FLIPALONG_UPRIGHT,15)) || stateMngr.check(p,new State(StateOperation.FLIPALONG_UPRIGHT,10)) || stateMngr.check(p,new State(StateOperation.FLIPALONG_UPRIGHT,8))) {	
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
		else {
//			receivePacket(MetaPacket(msg));
		}
	}
	
}