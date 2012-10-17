//package ussr.samples.atron.simulations.metaforma.gen;
//
//import java.awt.Color;
//import java.lang.reflect.Field;  
//import java.util.ArrayList;
//import java.util.BitSet;
//import java.util.HashSet;
//import java.util.Set;
//
//import ussr.description.Robot;
//import ussr.description.setup.ModulePosition;
//import ussr.model.Controller;
//import ussr.model.debugging.ControllerInformationProvider;
//import ussr.samples.atron.ATRON;
//import ussr.samples.atron.simulations.metaforma.lib.*;
//
//class Meta {public byte gradH;public byte gradV = 4;
//	public String toString() {
//		String ret = "";
//		for (Field f :this.getClass().getFields()) {
//			try {
//				ret += f.getName() + ": " + f.get(this) + "\n";
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return ret;
//	}
//}
//class ChristensenSimulation extends MfSimulation {
//	
//	
//	public static void main(String[] args) {
//		Meta m = new Meta();
//		System.out.println(m);
//		
////		MfSimulation.initSimulator();		
////		new ChristensenSimulation().main();
//	
//	}
//
//	protected Robot getRobot() {
//		ATRON a = new ATRON() {
//			public Controller createController() {
//				return new ChristensenControllerOptimized();
//			}
//		};
//		return a;
//	}
//
//	protected ArrayList<ModulePosition> buildRobot() {
//		BitSet b = new BitSet();
//		b.set(0, 3);
//		return new MfBuilder().buildRectangle(12,7, ChristensenControllerOptimized.Mod.Floor);
//	}
//
//}
//
//public class ChristensenControllerOptimized extends MfController implements ControllerInformationProvider {
//		
//	enum StateOperation implements IStateOperation {
//		INIT, CHOOSE, GET_UP;
//
//		public byte ord() {return (byte) ordinal();	}
//
//		public IStateOperation fromByte(byte b) {return values()[b];}
//	}
//	
//	enum ModuleRole implements IRole {
//		NONE,Left,Top,Right,Dummy;
//
//
//		public IRole fromByte(byte b) {
//			return values()[b];
//		}
//
//		public byte index() {return (byte) ordinal();}
//
//		public byte size() {
//			// None is no role
//			return (byte) (values().length - 1);
//		}		
//	} 
//	
//	enum Mod  implements IModule,IModEnum {
//		ALL,
//		NONE,
//		Floor(100),
//		Walker_Left, Walker_Right, Walker_Top, Walker_Dummy,
//		Uplifter_Left, Uplifter_Right, Uplifter_Top, Uplifter_Bottom,Uplifter_DR,Uplifter_DT;
//
//		byte count;
//		
//		private Mod () {
//			count = 1;
//		}
//		
//		private Mod (int c) {
//			count = (byte) c;
//		}
//		
//		public byte getCount() {
//			return count;
//		}
//		
//		
//		public Set<IModule> modules() {
//			Set<IModule> m = new HashSet<IModule>();
//			m.add(this);
//			return m;
//		}
//
//		public boolean equals(IModule m) {
//			return this.ordinal() == m.getMod().ordinal();
//		}
//
//		@Override
//		public IModEnum getMod() {
//			return this;
//		}
//
//		@Override
//		public byte getNr() {
//			return 0;
//		}
//
//		@Override
//		public Group getGroup () {
//			return Group.valueOf(name().split("_")[0]);
//		}
//
//		@Override
//		public byte ord() {
//			byte ret = 0;
//			for (Mod m:values()) {
//				if (m.ordinal() != ordinal()) {
//					ret+=m.count;
//				}
//				else {
//					return ret;
//				}
//			}
//			throw new Error ("Enum not found!");
//		}
//
//		@Override
//		public boolean contains(IModule m) {
//			return equals(m);
//		}
//
//
//		@Override
//		public IModEnum getNone() {
//			return NONE;
//		}
//		
//		@Override
//		public IModEnum valueFrom(String string) {
//			return valueOf(string);
//		}
//
//		@Override
//		public IModEnum[] getValues() {
//			return values();
//		}
//
//	}
//	
//	 enum Group implements IGroupEnum,IModuleHolder{ALL, NONE, Floor, Walker, Uplifter;
//
//		public boolean contains(IModule m) {
//			return equals(m.getGroup());
//		}
//
//
//		public Set<IModule> modules() {
//			Set<IModule> mods = new HashSet<IModule>();
//			for (IModule m: Mod.values()) {
//				if (m.toString().startsWith(toString() + "_")) {
//					mods.add(m);
//				}
//			}
//			return mods;
//			
//		}
//
//
//		@Override
//		public IGroupEnum valueFrom(String string) {
//			return valueOf(string);
//		}
//	}
//	
//	
//	public void init() {
//		Module.Mod = Mod.NONE;
//		Module.Group = Group.NONE;
//		stateMngr.init(StateOperation.INIT);
//	
//		moduleRoleSet(ModuleRole.NONE);
//		
//		visual.setColor(Mod.Walker_Left, Color.PINK);
//		visual.setColor(Mod.Walker_Top, Color.PINK.darker());
//		visual.setColor(Mod.Walker_Right, Color.PINK.darker().darker());
//		visual.setColor(Mod.Walker_Dummy, Color.PINK.darker().darker().darker());
//		visual.setColor(Group.Uplifter, Color.WHITE);
//
//		visual.setColor(StateOperation.GET_UP,Color.GREEN);
//		visual.setColor(StateOperation.INIT,Color.WHITE);
//		
//		visual.setMessageFilter(255);
//						
//	}
//
//	
//	
//	public void handleStates() {
//		
//		if (stateMngr.at(StateOperation.INIT)) {
//			// Make groupings of 4
//			if (stateMngr.doUntil(0)) {
//				if (!metaIdExists() && nbs(EAST&MALE&NORTH, ModuleRole.NONE).exists() && !nbs(WEST&MALE&NORTH).nbsWithoutMetaId().exists()) {
//					moduleRoleSet(ModuleRole.Left);
//					metaIdSet(getId().ord());
//				}
//				if (metaIdExists() && moduleRoleGet() == ModuleRole.Left) {
//					unicast(EAST&MALE&NORTH,PacketCoreType.META_ID_SET,false, new byte[]{getId().ord(),0});
//				}
//			}
//			
//			if (stateMngr.doWait(1)) {
//				metaSetCompleted();
////				scheduler.setInterval("broadcastMetaNeighbors", 5000);
//				stateMngr.commit();
//			}
//
//			if (stateMngr.doUntil(2)) {
//				// Share meta neighborhood hor + ver
//				scheduler.invokeNow("broadcastMetaVars");
//				broadcastDiscover();
//				stateMngr.spend(5);
//				stateMngr.setAfterConsensus(StateOperation.CHOOSE);
//			}
//		}
//		
//		if (stateMngr.at(StateOperation.CHOOSE)) {
//			if (stateMngr.doUntil(0)) {
//				if (varGet(VarMeta.Top) == 0 && varGet(VarMeta.Left) != 0 && varGet(VarMeta.Right) == 0 && varGet(VarMeta.Bottom) != 0) {
//					metaBossIdSetTo(new byte[]{varGet(VarMeta.Bottom),varGet(VarMeta.BottomLeft),varGet(VarMeta.Left)});
//					visual.print("GOING TO GET UP");
//					stateMngr.setAfterConsensus(StateOperation.GET_UP);
//					stateMngr.commit();
//					
//					
//				}
//			}
//			
//		}
//		
//		if (stateMngr.at(StateOperation.GET_UP)) {
//			if (stateMngr.doWait(0)) {
//				varSet(VarLocal.gradH, MAX_BYTE);
//				varSet(VarLocal.gradV, MAX_BYTE);
//				if (metaBossMyself()) {
//					if (moduleRoleGet() == ModuleRole.Left) {
//						renameTo(Mod.Walker_Left);
//					}
//					if (moduleRoleGet() == ModuleRole.Top) {
//						renameTo(Mod.Walker_Top);
//					}
//					if (moduleRoleGet() == ModuleRole.Right) {
//						renameTo(Mod.Walker_Right);
//					}
//					if (moduleRoleGet() == ModuleRole.Dummy) {
//						renameTo(Mod.Walker_Dummy);
//					}
//				}
//				
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doUntil(1,200))  {
//				gradientCreate();
//				stateMngr.spend(5);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(2)) {
//				assign (4,2,Mod.Uplifter_Left);
//				assign (2,2,Mod.Uplifter_Right);
//				assign (0,2,Mod.Uplifter_DR);
//				assign (4,0,Mod.Uplifter_DT);
//				
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(3)) {
//				disconnectPart(Group.Uplifter, (NORTH&EAST&FEMALE)|(SOUTH&WEST)&FEMALE);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(4)) {
//				rotate(Mod.Uplifter_Left,-90);
//				rotate(Mod.Uplifter_Right,-90);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(5)) {
//				disconnect(Mod.Walker_Left,Group.Uplifter);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(6)) {
//				rotate(Mod.Walker_Top,-45);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(7)) {
//				rotate(Mod.Walker_Right,-90);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(8)) {
//				rotate(Mod.Walker_Top,45);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(9)) {
//				connect(Mod.Walker_Left,Group.Floor,true);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//					
//			
//			if (stateMngr.doWait(10)) {
//				disconnect(Mod.Walker_Right,Group.Uplifter,true);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(11)) {
//				rotate(Mod.Uplifter_Left,90);
//				rotate(Mod.Uplifter_Right,90);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(12)) {
//				connect(Mod.Uplifter_Left,Group.Floor);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(13)) {
//				if (Group.Uplifter.contains(getId())) {
//					renameRestore();
//				}
//				varSet(VarLocal.metaIdPrevious,metaBossIdGet());
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(14)) {
//				
//				metaRegionRelease(false);
//				
//				if (metaIdGet() != varGet(VarLocal.metaIdPrevious)) {
//					stateMngr.nextOperation(StateOperation.INIT);
//				}
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(15)) {
//				rotate(Mod.Walker_Top,-45);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(16)) {
//				rotate(Mod.Walker_Left,180);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(17)) {
//				rotate(Mod.Walker_Top,45);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doUntil(18)) {
//				connect(Mod.Walker_Right,Group.Floor,true);
//			}
//			
//			if (stateMngr.doUntil(19)) {
//				disconnect(Mod.Walker_Left,Group.Floor,true);
//			}
//			
//			if (stateMngr.doWait(20)) {
//				rotate(Mod.Walker_Top,-45);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			if (stateMngr.doWait(21)) {
//				rotate(Mod.Walker_Left,90);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//			if (stateMngr.doWait(22)) {
//				rotate(Mod.Walker_Top,45);
//				stateMngr.commitMyselfIfNotUsed();
//			}
//			
//		}
//
//	}
//
//	public void finish () {
//		metaRegionRelease(true);
//		moduleRoleSet(ModuleRole.NONE);
//		stateMngr.nextOperation(StateOperation.INIT);
//		
//	}
//	
//	
//	private void assign(int h, int v, IModule m) {
//		visual.print("## " + h + "," + v);
//		if (varGet(VarLocal.gradH) == h && varGet(VarLocal.gradV) == v){
//			renameStore();
//			renameTo(m);
//			
//		}
//		
//	}
//		
//
//	public void receiveMessage(IPacketType type, State state, boolean isReq, byte sourceCon, byte destCon, byte sourceMetaId, byte[] data) {
//
//		if (type == PacketCoreType.GRADIENT) {
//			visual.print("Receiving gradient " + varFromByteLocal(data[0]) + "=" + varGet(varFromByteLocal(data[0])) + " --> "  + data[1]);
//			if (varGet(varFromByteLocal(data[0])) > data[1]) {
//				varSet(varFromByteLocal(data[0]),data[1]);
//				scheduler.invokeNow("gradientCreate");
//			}
//		}
//				
//		
//		if (type == PacketCoreType.META_ID_SET) {
//			if (!metaIdExists()) {
//				metaIdSet(data[0]);
//			}
//			if (data[1]==2) {
//				if (!moduleRoleExists()){
//					moduleRoleSet(ModuleRole.Dummy);
//					stateMngr.nextInstruction();
//				}
//			}
//			else if (data[1]==1) {
//				if (!moduleRoleExists()){
//					moduleRoleSet(ModuleRole.Right);
//				}
//				data[1] = 2;
//			}
//			else if (data[1]==0) {
//				if (!moduleRoleExists()){
//					moduleRoleSet(ModuleRole.Top);
//				}
//				data[1] = 1;
//			}
//			
//		
//			if (freqLimit("META_ID_SET",450)) {
//				byte dest;
//
//				if (isMALE(destCon)) {
//					dest = (byte) ((destCon + 2) % 4);
//				}
//				else {
//					dest = (byte) ((destCon + 4) % 8);
//				}
//				if(moduleRoleGet() != ModuleRole.Dummy) {
//					unicast(pow2(dest),PacketCoreType.META_ID_SET,false,data);
//				}
//			}
//		
//			
//			
//		}
//		
//		if (type == PacketCoreType.SYMMETRY) {
//			if (stateMngr.at(state)) {
//				if (freqLimit("SYMMETRY",500)) {
//					symmetryFix (isReq,sourceCon, destCon);
//				}
//			}
//		}
//	} 
//
//	
//	
//	
//	public void gradientCreate () {
//		
//		boolean sourceH = false;
//		boolean sourceV = false;
//		
//		if (nbs(NORTH&WEST&FEMALE).sizeEquals(1,true) || nbs(WEST&FEMALE).sizeEquals(2,true)) {
//			sourceV = true;
//			if (nbs(NORTH&WEST&FEMALE).sizeEquals(1,true))
//				visual.print("SOURCE V1");
//			if (nbs(WEST&FEMALE).sizeEquals(2,true))
//				visual.print("SOURCE V2");
//		}
//		if (nbs(NORTH&WEST&FEMALE).sizeEquals(1,true) || nbs(NORTH).sizeEquals(2,true) && nbs().sizeEquals(2,false)) {
//			sourceH = true;
//			if (nbs(NORTH&WEST&FEMALE).sizeEquals(1,true))
//				visual.print("SOURCE H1");
//			if (nbs(NORTH).sizeEquals(2,true))
//				visual.print("SOURCE H2");
//		}
//		
//		gradientSend(VarLocal.gradH,sourceH);
//		gradientSend(VarLocal.gradV,sourceV);			
//	}
//	
//
//	public boolean metaNeighborHook(int connectorNr,byte metaId) {
//		boolean changed = false;
//		if (moduleRoleGet() == ModuleRole.Dummy) {
//			if (isNORTH(connectorNr)) {
//				changed = changed | varSet(VarMeta.Top, metaId);
//			}
//			else {
//				if (isEAST(connectorNr) ) {
//					changed = changed | varSet(VarMeta.TopRight, metaId);
//				}
//				else {
//					changed = changed | varSet(VarMeta.Right, metaId);
//				}
//			}
//			
//		}
//		if (moduleRoleGet() == ModuleRole.Top) {
//			changed = changed | varSet(VarMeta.Top, metaId);
//		}
//		if (moduleRoleGet() == ModuleRole.Right) {
//			changed = changed | varSet(VarMeta.Bottom, metaId);
//		}
//		if (moduleRoleGet() == ModuleRole.Left) {
//			if (isNORTH(connectorNr)) {
//				changed = changed | varSet(VarMeta.Left, metaId);
//			}
//			else {
//				if (isWEST(connectorNr)) {
//					changed = changed | varSet(VarMeta.BottomLeft, metaId);
//				}
//				else {
//					changed = changed | varSet(VarMeta.Bottom, metaId);
//				}
//			}
//		}
//		return changed;
//	}
//
//
//}