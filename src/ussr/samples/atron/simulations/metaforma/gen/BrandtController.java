package ussr.samples.atron.simulations.metaforma.gen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import ussr.description.Robot;
import ussr.description.setup.ModulePosition;
import ussr.model.Controller;
import ussr.model.debugging.ControllerInformationProvider;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.simulations.metaforma.gen.BrandtController.Mod;
import ussr.samples.atron.simulations.metaforma.lib.*;

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
		BitSet b = new BitSet();
		b.set(0, 3);
		return new MfBuilder().buildGrid(b, Mod.F);
	}
}

public class BrandtController extends MfRuntime implements ControllerInformationProvider {

	enum StateOperation implements IStateOperation {
		INIT, CHOOSE, FLIP_UP, FLIP_BOTTOM, FLIP_TOP;
		public byte ord() {return (byte) ordinal();	}
		public IStateOperation fromByte(byte b) {return values()[b];}
	}
	enum VarLocal implements IVar {
		NONE, gradH, gradV, isRef;
		public byte index() {return (byte) ordinal();}
		public VarLocal fromByte(byte b) {	return values()[b];	}
		public boolean isLocal() {return true;	}
		public boolean isLocalState() {return false;	}
		public boolean isMeta() {return false;	}
		public boolean isMetaRegion() {return false;	}
	}
	enum VarMeta implements IVar {
		NONE,Top, Bottom, Left, Right, TopLeft,TopRight,BottomLeft,BottomRight;
		public byte index() {return (byte) (ordinal() + 25);}
		public VarMeta fromByte(byte b) {return values()[b-25];}
		public boolean isLocal() {return false;	}
		public boolean isLocalState() {return false;	}
		public boolean isMeta() {return true;	}
		public boolean isMetaRegion() {return false;	}
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
	enum Mod  implements IModule,IModEnum{
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
	
	public void addNeighborhood (StringBuffer out) {
		out.append(String.format("% 3d  % 3d  % 3d",varGet(VarMeta.TopLeft),varGet(VarMeta.Top),varGet(VarMeta.TopRight)) + "\n");
		out.append(String.format("% 3d        % 3d",varGet(VarMeta.Left),varGet(VarMeta.Right)) + "\n");
		out.append(String.format("% 3d  % 3d  % 3d",varGet(VarMeta.BottomLeft),varGet(VarMeta.Bottom),varGet(VarMeta.BottomRight)) + "\n");
	}

	public void init() {
		Module.Mod = Mod.NONE;
		Module.Group = Group.NONE;
		stateMngr.init(StateOperation.INIT);
		moduleRoleSet(ModuleRole.NONE);
		
		
		visual.setColor(Mod.Clover_North, Color.PINK);
		visual.setColor(Mod.Clover_South, Color.PINK.darker());
		visual.setColor(Mod.Clover_West, Color.PINK.darker().darker());
		visual.setColor(Mod.Clover_East, Color.PINK.darker().darker().darker());
		visual.setColor(Group.Uplifter, Color.WHITE);

		visual.setColor(StateOperation.FLIP_TOP,Color.CYAN);
		visual.setColor(StateOperation.FLIP_BOTTOM,Color.YELLOW);
		visual.setColor(StateOperation.FLIP_UP,Color.GREEN);
		visual.setColor(StateOperation.INIT,Color.WHITE);
		
		visual.setMessageFilter(255);
		visual.setMessageFilterMeta(255);
		
				
		scheduler.setInterval("broadcastMetaNeighbors", 10000);
	}

	public IPacketType getMetaPacketType (int index){
		return MetaPacketCoreType.values()[index];
	}
	
	public IPacketType getPacketType (int index){
		return PacketCoreType.values()[index];
	}
	
	public void handleStates() {
		
		if (stateMngr.at(StateOperation.INIT)) {
			// Make groupings of 4
			if (stateMngr.doUntil(0)) {
				if (!metaIdExists() && nbs(EAST&MALE, ModuleRole.NONE).size() == 2 && !nbs(WEST, ModuleRole.NONE).exists()) {
					moduleRoleSet(ModuleRole.Left);
					metaIdSet(getId().ord());
				}
				if (metaIdExists() && moduleRoleGet() == ModuleRole.Left) {
					unicast(EAST&MALE&NORTH,PacketCoreType.META_ID_SET,false, new byte[]{getId().ord()});
				}
			}
			
			if (stateMngr.doWait(1)) {
				metaSetCompleted();
				scheduler.setInterval("broadcastMetaVars", 3000);
				scheduler.setInterval("broadcastMetaNeighbors", 3000);
				stateMngr.commit();
			}

			if (stateMngr.doUntil(2)) {
				// Share meta neighborhood hor + ver
				scheduler.invokeNow("broadcastMetaVars");
				broadcastDiscover();
				stateMngr.spend(5);
			}
			
			if (stateMngr.doUntil(3,600)) {
				// Share meta neighborhood diag
				broadcastMetaNeighbors();
				stateMngr.spend(7);
			}
				
			if (stateMngr.doWait(4)) {
				stateMngr.nextOperation(StateOperation.CHOOSE);
			}
			
			
		}
		if (stateMngr.at(StateOperation.CHOOSE)) {
			if (stateMngr.doUntil(0)) {
				
//				if (varGet(VarMeta.Top) != 0 && varGet(VarMeta.Left) == 0 && varGet(VarMeta.Right) == 0 && varGet(VarMeta.TopLeft) == 0 && varGet(VarMeta.Bottom) == 0) {
//					metaBossIdSetTo(new byte[]{varGet(VarMeta.Top)});
//					stateMngr.setAfterConsensus(StateOperation.FLIP_BOTTOM);
//					stateMngr.commit();
//				}
				
				if (varGet(VarMeta.Top) == 0 && varGet(VarMeta.Right) != 0) {
					if (varGet(VarMeta.TopRight) == 0) {
//						metaBossIdSetTo(new byte[]{varGet(VarMeta.Right)});
//						stateMngr.setAfterConsensus(StateOperation.FLIP_TOP);
//						stateMngr.commit();
					}
					else {
						metaBossIdSetTo(new byte[]{varGet(VarMeta.TopRight),varGet(VarMeta.Right)});
						stateMngr.setAfterConsensus(StateOperation.FLIP_UP);
						stateMngr.commit();
					}
					
				}
				
			}
		}

		
		if (stateMngr.at(StateOperation.FLIP_UP)) {
			if (stateMngr.doWait(0)) {
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(1)) {
				if (metaBossMyself()) {
					if (moduleRoleGet() == ModuleRole.Left){
						renameStore();
						renameTo(Mod.Clover_West);
					}
					if (moduleRoleGet() == ModuleRole.Bottom){
						renameStore();
						renameTo(Mod.Clover_South);
					}
					if (moduleRoleGet() == ModuleRole.Top){
						renameStore();
						renameTo(Mod.Clover_North);
					}
					if (moduleRoleGet() == ModuleRole.Right){
						renameStore();
						renameTo(Mod.Clover_East);
					}
				}
				else {
					if (moduleRoleGet() == ModuleRole.Bottom) {
						varSet(VarLocal.isRef,1);
					}
				}
				
				stateMngr.commitMyselfIfNotUsed();
				
			}
			
			if (stateMngr.doWait(2)) {
				disconnect (Mod.Clover_West, Mod.Clover_South);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(3)) {
				rotate (Mod.Clover_East,-90);
				stateMngr.commitMyselfIfNotUsed();
			}
				
			if (stateMngr.doWait(4)) {
				rotate (Mod.Clover_North,-180);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(5)) {
				rotate (Mod.Clover_East,-90);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(6)) {
				connect (Mod.Clover_North,Group.F);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(7)) {
				connect (Mod.Clover_West,Group.F);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(8)) {
				disconnect (new ModuleSet().add(Mod.Clover_South).add(Mod.Clover_East),Group.F);
				stateMngr.commitMyselfIfNotUsed();
			}
	
			if (stateMngr.doWait(9)) {
				rotate (Mod.Clover_North,-180);
				stateMngr.commitMyselfIfNotUsed();
			}
						
	
			if (stateMngr.doWait(10)) {
				rotate (Mod.Clover_East,-180);
				stateMngr.commitMyselfIfNotUsed();
			}
			
	
			if (stateMngr.doWait(11)) {
				connect (Mod.Clover_South,Mod.Clover_West);
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
		
		if (stateMngr.at(StateOperation.FLIP_TOP)) {
			if (stateMngr.doWait(0)) {
				if (!metaBossMyself()) {
					if (moduleRoleGet() == ModuleRole.Bottom) {
						varSet(VarLocal.isRef,1);
					}
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(1)) {
				varSet(VarLocal.gradH, MAX_BYTE);
				varSet(VarLocal.gradV, MAX_BYTE);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(2,200))  {
				gradientCreate();
				stateMngr.spend(5);
				stateMngr.commitMyselfIfNotUsed();
			}
	
			
			
			if (stateMngr.doWait(3)) {
				assign (1,1,Mod.Uplifter_Left);
				assign (0,2,Mod.Uplifter_Right);
				assign (0,0,Mod.Uplifter_Top);
				assign (1,3,Mod.Uplifter_Bottom);
				stateMngr.commitMyselfIfNotUsed();
			}

			if (stateMngr.doWait(4)) {
				disconnectPart (Mod.Uplifter_Left, NORTH&MALE&EAST|SOUTH&MALE&WEST);
				disconnectPart (Mod.Uplifter_Right, NORTH&MALE&EAST);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(5)) {
				rotate (new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				stateMngr.commitMyselfIfNotUsed();
			}
 
			if (stateMngr.doWait(6)) {
				disconnectPart (Mod.Uplifter_Left,SOUTH);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(7)) {
				rotate (Mod.Uplifter_Bottom,-180);
//				rotate (new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				stateMngr.commitMyselfIfNotUsed();
			}

			if (stateMngr.doWait(8)) {
				rotate (Mod.Uplifter_Right,-90);
				rotate (Mod.Uplifter_Left,-90);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(9)) {
				rotate (Mod.Uplifter_Top,-180);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(10)) {
				connect (Group.F,Group.Uplifter);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(11)) {
				if (getGrouping() == Group.Uplifter) {
					renameRestore();
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(12)) {
//				discoverNeighbors();
				if (nbs(EAST&FEMALE).sizeEquals(2,true)) {
					unicast(EAST&FEMALE,PacketCoreType.SYMMETRY,false);
					stateMngr.commit();
				}
			}
			
			if (stateMngr.doWait (13)) {
				if (varGet(VarLocal.isRef) == 1) {
					varSet(VarLocal.isRef, 0);
				}
				finish();
			}
		}
		
		
		if (stateMngr.at(StateOperation.FLIP_BOTTOM)) {
			if (stateMngr.doWait(0)) {
				if (!metaBossMyself()) {
					if (moduleRoleGet() == ModuleRole.Right) {
						varSet(VarLocal.isRef,1);
					}
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(1)) {
				varSet(VarLocal.gradH, MAX_BYTE);
				varSet(VarLocal.gradV, MAX_BYTE);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(2,400))  {
				gradientCreate();
				stateMngr.spend(5);
				stateMngr.commitMyselfIfNotUsed();
			}
	
			
			
			if (stateMngr.doWait(3)) {
				assign (1,0,Mod.Uplifter_Left);
				assign (2,1,Mod.Uplifter_Right);
				assign (3,0,Mod.Uplifter_Top);
				assign (0,1,Mod.Uplifter_Bottom);
				stateMngr.commitMyselfIfNotUsed();
			}

			if (stateMngr.doWait(4)) {
				disconnectPart (Mod.Uplifter_Left, NORTH&FEMALE&EAST|SOUTH&FEMALE&WEST);
				disconnectPart (Mod.Uplifter_Right, NORTH&FEMALE&EAST|SOUTH&FEMALE&WEST);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(5)) {
				rotate (new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				stateMngr.commitMyselfIfNotUsed();
			}
 
			if (stateMngr.doWait(6)) {
				disconnectPart (Mod.Uplifter_Left,SOUTH);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(7)) {
				rotate (Mod.Uplifter_Top,-180);
//				rotate (new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				stateMngr.commitMyselfIfNotUsed();
			}

			if (stateMngr.doWait(8)) {
				rotate (Mod.Uplifter_Right,-90);
				rotate (Mod.Uplifter_Left,-90);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(9)) {
				rotate (Mod.Uplifter_Bottom,-180);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			
			if (stateMngr.doWait(10)) {
				connect (Group.F,Group.Uplifter);
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doWait(11)) {
				if (getGrouping() == Group.Uplifter) {
					renameRestore();
				}
				stateMngr.commitMyselfIfNotUsed();
			}
			
			if (stateMngr.doUntil(12)) {
				if (varGet(VarLocal.isRef) == 1) {
					unicast(WEST&MALE,PacketCoreType.SYMMETRY,false);
					stateMngr.commitMyselfIfNotUsed();
				}
			}
			
			if (stateMngr.doWait (13)) {
				if (varGet(VarLocal.isRef) == 1) {
					varSet(VarLocal.isRef, 0);
				}
				finish();
			}
		}
	}

	public void finish () {
		metaRegionRelease(true);
		moduleRoleSet(ModuleRole.NONE);
		stateMngr.nextOperation(StateOperation.INIT);
		
	}
	
	
	private void assign(int h, int v, IModule m) {
		visual.print("## " + h + "," + v + " --- " + varGet(VarLocal.gradH) + "," + varGet(VarLocal.gradV));
		if (varGet(VarLocal.gradH) == h && varGet(VarLocal.gradV) == v){
			renameStore();
			renameTo(m);
			
		}
		
	}
		

	public void receiveMessage(IPacketType type, State state, boolean isReq, byte sourceCon, byte destCon, byte sourceMetaId, byte[] data) {

		if (type == PacketCoreType.GRADIENT) {
			visual.print("Receiving gradient " + varFromByteLocal(data[0]) + "=" + varGet(varFromByteLocal(data[0])) + " --> "  + data[1]);
			if (varGet(varFromByteLocal(data[0])) > data[1]) {
				varSet(varFromByteLocal(data[0]),data[1]);
				scheduler.invokeNow("gradientCreate");
			}
		}
				
		
		if (type == PacketCoreType.META_ID_SET) {
			if (!metaIdExists()) {
				metaIdSet(data[0]);
				
				if (isMALE(destCon)) {
					if (!moduleRoleExists()){
						moduleRoleSet(ModuleRole.Right);
					}
				}
				else {
					if (isWEST(destCon)) {
						moduleRoleSet(ModuleRole.Top);
					}
					else {
						moduleRoleSet(ModuleRole.Bottom);
					}
				}

			}
			
			if (moduleRoleGet() == ModuleRole.Left) {
				stateMngr.nextInstruction();
			}
			else {
				if (freqLimit("META_ID_SET",450)) {
					unicast(pow2((destCon + 4) % 8),PacketCoreType.META_ID_SET,false,data);
				}
			}
			
			
		}
		
		if (type == PacketCoreType.SYMMETRY) {
			if (stateMngr.at(state)) {
				if (freqLimit("SYMMETRY",500)) {
					symmetryFix (isReq,sourceCon, destCon);
				}
			}
		}
	} 

	
	
	
	public void gradientCreate () {
		
		boolean sourceH = false;
		boolean sourceV = false;

		if (nbs(EAST&MALE).nbsInRegion(true).size() == 2 && nbs(WEST&SOUTH&MALE).nbsInRegion(true).isEmpty() || nbs(EAST&FEMALE).nbsInRegion(true).size() == 2 && nbs(WEST&NORTH&FEMALE).nbsInRegion(true).isEmpty()){
			sourceH = true;
		}
		if (nbs(EAST&MALE).nbsInRegion(true).size() == 2 && nbs(WEST&NORTH&MALE).nbsInRegion(true).isEmpty() || nbs(WEST&FEMALE).nbsInRegion(true).size() == 2 && nbs(EAST&NORTH&FEMALE).nbsInRegion(true).isEmpty()) {
			sourceV = true;
		}
		
		
		
		gradientSend(VarLocal.gradH,sourceH);
		gradientSend(VarLocal.gradV,sourceV);			
	}
	
	public void broadcastMetaNeighbors () {
		broadcastMetaNeighborsTo(new byte[]{varGet(VarMeta.Top),varGet(VarMeta.Bottom)},VarMeta.Left,VarMeta.Right);
		broadcastMetaNeighborsTo(new byte[]{varGet(VarMeta.Left),varGet(VarMeta.Right)},VarMeta.Top,VarMeta.Bottom);
	}
	
	private void broadcastMetaNeighborsTo (byte[] dests, VarMeta v1, VarMeta v2) {
		byte val1 = varGet(v1);
		byte val2 = varGet(v2);
		if (metaIdExists()) { 
			for (byte dest:dests) {
//				Dest could be: 
//				-1 = uninitialized metamodule
//				0 = no metamodule
				if (dest > 0) {
					send(MetaPacketCoreType.ADD_NEIGHBOR,dest, new byte[]{val1,val2});
				}
			}
		}
	}
	

	public boolean metaNeighborHook(int connectorNr,byte metaId) {
		boolean changed = false;
		
		if ( connectorNr == 5 || connectorNr == 6) {
			changed = changed || varSet(VarMeta.Right, metaId);
		}
		if (connectorNr == 2 || connectorNr == 7) {
			changed = changed || varSet(VarMeta.Top, metaId);
		}
		if (connectorNr == 0 || connectorNr == 3) {
			changed = changed || varSet(VarMeta.Left, metaId);
		}
		if (connectorNr == 1 || connectorNr == 4) {
			changed = changed || varSet(VarMeta.Bottom, metaId);
		}
		
		return changed;
	}

	protected void receiveMetaMessage(IPacketType type, byte source, byte dest, byte[] data) {
		if (type == MetaPacketCoreType.ADD_NEIGHBOR) {
			if (source == varGet(VarMeta.Left)) {
				varSet(VarMeta.TopLeft,data[0]);
				varSet(VarMeta.BottomLeft,data[1]);
			}
			if (source == varGet(VarMeta.Right)) {
				varSet(VarMeta.TopRight,data[0]);
				varSet(VarMeta.BottomRight,data[1]);
			}
		
			if (source == varGet(VarMeta.Top)) {
				varSet(VarMeta.TopLeft,data[0]);
				varSet(VarMeta.TopRight,data[1]);
			}
			if (source == varGet(VarMeta.Bottom)) {
				varSet(VarMeta.BottomLeft,data[0]);
				varSet(VarMeta.BottomRight,data[1]);
			}
			
		}
		
	}
	
	
	
	public IVar varFromByteLocal (byte index) {
		return VarLocal.NONE.fromByte(index);
	}
	
	public IVar varFromByteMeta (byte index){
		return VarMeta.NONE.fromByte(index);
	}
	
	public IVar varFromByteMetaGroup (byte index){
		return null;
	}

	@Override
	protected boolean metaNeighborHookAllow() {
		// TODO Auto-generated method stub
		return stateMngr.at(StateOperation.CHOOSE) || stateMngr.at(StateOperation.INIT);
	}


}