package ussr.samples.atron.simulations.metaforma.gen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;

import ussr.description.Robot;
import ussr.description.setup.ModulePosition;
import ussr.model.Controller;
import ussr.model.debugging.ControllerInformationProvider;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONBuilder;
import ussr.samples.atron.simulations.metaforma.lib.*;

class CloverMoveParalellSimulation extends MetaformaSimulation {

	
	public static void main(String[] args) {
		
		MetaformaSimulation.initSimulator();		
		new CloverMoveParalellSimulation().main();
	
	}

	protected Robot getRobot() {
		ATRON a = new ATRON() {
			public Controller createController() {
				return new CloverMoveParalellController();
			}
		};
		return a;
	}

	protected ArrayList<ModulePosition> buildRobot() {
		BitSet b = new BitSet();
		b.set(0, 3);
		return new ATRONBuilder().buildGrid(b, "Floor_", false);
	}

}

public class CloverMoveParalellController extends MetaformaRuntime implements ControllerInformationProvider {

		
	enum StateOperation implements IStateOperation {
		INIT, CHOOSE, FLIP_UP, FLIP_BOTTOM, FLIP_TOP;

		public byte ord() {return (byte) ordinal();	}

		public IStateOperation fromByte(byte b) {return values()[b];}
	}
 
	enum VarLocal implements IVar {
		NONE, gradH, gradV, nextOperation;

		public byte index() {return (byte) ordinal();}

		public VarLocal fromByte(byte b) {	return values()[b];	}
		
		public boolean isLocal() {return true;	}
		public boolean isLocalState() {return false;	}
		public boolean isMeta() {return false;	}
		public boolean isMetaGroup() {return false;	}
	}

	enum VarMeta implements IVar {
		NONE,Top, Bottom, Left, Right, TopLeft,TopRight,BottomLeft,BottomRight;

		public byte index() {return (byte) (ordinal() + 25);}

		public VarMeta fromByte(byte b) {return values()[b-25];}

		public boolean isLocal() {return false;	}
		public boolean isLocalState() {return false;	}
		public boolean isMeta() {return true;	}
		public boolean isMetaGroup() {return false;	}
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
	
	public void addNeighborhood (StringBuffer out) {
		out.append(String.format("% 3d  % 3d  % 3d",varGet(VarMeta.TopLeft),varGet(VarMeta.Top),varGet(VarMeta.TopRight)) + "\n");
		out.append(String.format("% 3d        % 3d",varGet(VarMeta.Left),varGet(VarMeta.Right)) + "\n");
		out.append(String.format("% 3d  % 3d  % 3d",varGet(VarMeta.BottomLeft),varGet(VarMeta.Bottom),varGet(VarMeta.BottomRight)) + "\n");
	}

	public void init() {
		stateOperationInit(StateOperation.INIT);
		moduleRoleSet(ModuleRole.NONE);
		
		visual.setColor(Mod.Clover_North, Color.PINK);
		visual.setColor(Mod.Clover_South, Color.PINK.darker());
		visual.setColor(Mod.Clover_West, Color.PINK.darker().darker());
		visual.setColor(Mod.Clover_East, Color.PINK.darker().darker().darker());
		visual.setColor(Grouping.Uplifter, Color.WHITE);

		visual.setColor(StateOperation.FLIP_TOP,Color.CYAN);
		visual.setColor(StateOperation.FLIP_BOTTOM,Color.YELLOW);
		visual.setColor(StateOperation.FLIP_UP,Color.GREEN);
		visual.setColor(StateOperation.INIT,Color.WHITE);
		
		visual.setMessageFilter(PacketCoreType.CONSENSUS.bit());
		visual.setMessageFilterMeta(0);
		
				
		scheduler.setInterval("broadcastMetaNeighbors", 10000);
	}

	public IPacketType getMetaPacketType (int index){
		return MetaPacketCoreType.values()[index];
	}
	
	public IPacketType getPacketType (int index){
		return PacketCoreType.values()[index];
	}
	
	public void handleStates() {
		
		if (stateOperation(StateOperation.INIT)) {
			// Make groupings of 4
			if (doRepeat(0)) {
				if (nbs(EAST&MALE, ModuleRole.NONE).size() == 2 && !nbs(WEST, ModuleRole.NONE).exists() && !metaIdExists()) {
					moduleRoleSet(ModuleRole.Left);
					metaIdSet(getId().ord());
				}
				if (moduleRoleGet() == ModuleRole.Left && metaIdExists()) {
					unicast(EAST&MALE&NORTH,PacketCoreType.META_ID_SET,false, new byte[]{getId().ord()});
				}
			}
			
			if (doOnce(1)) {
				metaSetCompleted();
				scheduler.setInterval("broadcastMetaVars", 3000);
				scheduler.setInterval("broadcastMetaNeighbors", 3000);
				commit();
			}

			if (doRepeat(2)) {
				// Share meta neighborhood hor + ver
				scheduler.invokeNow("broadcastMetaVars");
				broadcastDiscover();
				stateSpendAtMax(5);
			}
			
			if (doRepeat(3,600)) {
				// Share meta neighborhood diag
				broadcastMetaNeighbors();
				stateSpendAtMax(7);
			}
				
			if (doOnce(4)) {
				stateOperationNew(StateOperation.CHOOSE);
			}
			
			
		}
		if (stateOperation(StateOperation.CHOOSE)) {
			if (doRepeat(0)) {
				
				if (varGet(VarMeta.Top) != 0 && varGet(VarMeta.Left) == 0 && varGet(VarMeta.Right) == 0 && varGet(VarMeta.TopLeft) == 0 && varGet(VarMeta.Bottom) == 0) {
					metaBossIdSetTo(new byte[]{varGet(VarMeta.Top)});
					varSet(VarLocal.nextOperation,StateOperation.FLIP_BOTTOM.ord());
					commit();
				}
				
				if (varGet(VarMeta.Top) == 0 && varGet(VarMeta.Right) != 0) {
					if (varGet(VarMeta.TopRight) == 0) {
						metaBossIdSetTo(new byte[]{varGet(VarMeta.Right)});
						varSet(VarLocal.nextOperation,StateOperation.FLIP_TOP.ord());
						commit();
					}
					else {
						metaBossIdSetTo(new byte[]{varGet(VarMeta.TopRight),varGet(VarMeta.Right)});
						varSet(VarLocal.nextOperation,StateOperation.FLIP_UP.ord());
						commit();
					}
					
				}
				
			}
			
			if (doOnce(1)) {
				if (varGet(VarLocal.nextOperation) != 0) {
					stateOperationNew(StateOperation.values()[varGet(VarLocal.nextOperation)]);
					scheduler.setInterval("broadcastMetaVars", 8000);
					scheduler.setInterval("broadcastMetaNeighbors", 8000);
				}
			}
		}

		
		if (stateOperation(StateOperation.FLIP_UP)) {
			if (doOnce(0)) {
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(1)) {
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
				
				commitMyselfIfNotUsed();
				
			}
			
			if (doOnce(2)) {
				disconnect (Mod.Clover_West, Mod.Clover_South);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(3)) {
				rotate (Mod.Clover_East,-90);
				commitMyselfIfNotUsed();
			}
				
			if (doOnce(4)) {
				rotate (Mod.Clover_North,-180);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(5)) {
				rotate (Mod.Clover_East,-90);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(6)) {
				connect (Mod.Clover_North,Grouping.Floor);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(7)) {
				connect (Mod.Clover_West,Grouping.Floor);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(8)) {
				disconnect (new ModuleSet().add(Mod.Clover_South).add(Mod.Clover_East),Grouping.Floor);
				commitMyselfIfNotUsed();
			}
	
			if (doOnce(9)) {
				rotate (Mod.Clover_North,-180);
				commitMyselfIfNotUsed();
			}
						
	
			if (doOnce(10)) {
				rotate (Mod.Clover_East,-180);
				commitMyselfIfNotUsed();
			}
			
	
			if (doOnce(11)) {
				connect (Mod.Clover_South,Mod.Clover_West);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(12)) {
				if (getGrouping() == Grouping.Clover) {
					context.switchEastWest();
					context.switchNorthSouth();
				}
				commitMyselfIfNotUsed();
			}
			
			
			
			if (doOnce (13)) {
				if (getGrouping() == Grouping.Clover) {
					renameRestore();
				}
				commitMyselfIfNotUsed();
			}
			
			
			if (doOnce (14)) {
				finish();
				
			}
		
		}
		
		if (stateOperation(StateOperation.FLIP_TOP)) {
			if (doOnce(0)) {
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(1)) {
				varSet(VarLocal.gradH, MAX_BYTE);
				varSet(VarLocal.gradV, MAX_BYTE);
				commitMyselfIfNotUsed();
			}
			
			if (doRepeat(2,200))  {
				gradientCreate();
				stateSpendAtMax(5);
				commitMyselfIfNotUsed();
			}
	
			
			
			if (doOnce(3)) {
				assign (1,1,Mod.Uplifter_Left);
				assign (0,2,Mod.Uplifter_Right);
				assign (0,0,Mod.Uplifter_Top);
				assign (1,3,Mod.Uplifter_Bottom);
				commitMyselfIfNotUsed();
			}

			if (doOnce(4)) {
				disconnectPart (Mod.Uplifter_Left, NORTH&MALE&EAST|SOUTH&MALE&WEST);
				disconnectPart (Mod.Uplifter_Right, NORTH&MALE&EAST);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(5)) {
				rotate (new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				commitMyselfIfNotUsed();
			}
 
			if (doOnce(6)) {
				disconnectPart (Mod.Uplifter_Left,SOUTH);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(7)) {
				rotate (Mod.Uplifter_Bottom,-180);
//				rotate (new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				commitMyselfIfNotUsed();
			}

			if (doOnce(8)) {
				rotate (Mod.Uplifter_Right,-90);
				rotate (Mod.Uplifter_Left,-90);
				commitMyselfIfNotUsed();
			}
			
			
			if (doOnce(9)) {
				rotate (Mod.Uplifter_Top,-180);
				commitMyselfIfNotUsed();
			}
			
			
			if (doOnce(10)) {
				connect (Grouping.Floor,Grouping.Uplifter);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(11)) {
				if (getGrouping() == Grouping.Uplifter) {
					renameRestore();
				}
				commitMyselfIfNotUsed();
			}
			
			if (doRepeat(12)) {
//				discoverNeighbors();
				if (nbs(EAST&FEMALE).nbsInMetaGoup().size() == 2 && nbs().nbsInMetaGoup().size() == 2) {
					unicast(EAST&FEMALE,PacketCoreType.SYMMETRY,false);
					commit();
				}
			}
			
			if (doOnce (13)) {
				finish();
			}
		}
		
		
		if (stateOperation(StateOperation.FLIP_BOTTOM)) {
			if (doOnce(0)) {
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(1)) {
				varSet(VarLocal.gradH, MAX_BYTE);
				varSet(VarLocal.gradV, MAX_BYTE);
				commitMyselfIfNotUsed();
			}
			
			if (doRepeat(2,400))  {
				gradientCreate();
				stateSpendAtMax(5);
				commitMyselfIfNotUsed();
			}
	
			
			
			if (doOnce(3)) {
				assign (1,0,Mod.Uplifter_Left);
				assign (2,1,Mod.Uplifter_Right);
				assign (3,0,Mod.Uplifter_Top);
				assign (0,1,Mod.Uplifter_Bottom);
				commitMyselfIfNotUsed();
			}

			if (doOnce(4)) {
				disconnectPart (Mod.Uplifter_Left, NORTH&FEMALE&EAST|SOUTH&FEMALE&WEST);
				disconnectPart (Mod.Uplifter_Right, NORTH&FEMALE&EAST|SOUTH&FEMALE&WEST);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(5)) {
				rotate (new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				commitMyselfIfNotUsed();
			}
 
			if (doOnce(6)) {
				disconnectPart (Mod.Uplifter_Left,SOUTH);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(7)) {
				rotate (Mod.Uplifter_Top,-180);
//				rotate (new ModuleSet().add(Mod.Uplifter_Left).add(Mod.Uplifter_Right),-90);
				commitMyselfIfNotUsed();
			}

			if (doOnce(8)) {
				rotate (Mod.Uplifter_Right,-90);
				rotate (Mod.Uplifter_Left,-90);
				commitMyselfIfNotUsed();
			}
			
			
			if (doOnce(9)) {
				rotate (Mod.Uplifter_Bottom,-180);
				commitMyselfIfNotUsed();
			}
			
			
			if (doOnce(10)) {
				connect (Grouping.Floor,Grouping.Uplifter);
				commitMyselfIfNotUsed();
			}
			
			if (doOnce(11)) {
				if (getGrouping() == Grouping.Uplifter) {
					renameRestore();
				}
				commitMyselfIfNotUsed();
			}
			
			if (doRepeat(12)) {
				if (nbs(EAST&MALE).nbsInMetaGoup().size() == 2 && nbs().nbsInMetaGoup().size() == 2) {
					unicast(EAST&MALE,PacketCoreType.SYMMETRY,false);
					commitMyselfIfNotUsed();
				}
			}
			
			if (doOnce (13)) {
				finish();
			}
		}
	}

	public void finish () {
		metaBossIdUnlock();
		moduleRoleSet(ModuleRole.NONE);
		stateOperationInit(StateOperation.INIT);
		
	}
	
	
	private void assign(int h, int v, IModule m) {
		visual.print("## " + h + "," + v);
		if (varGet(VarLocal.gradH) == h && varGet(VarLocal.gradV) == v){
			renameStore();
			renameTo(m);
			
		}
		
	}
		

	public void receiveMessage(IPacketType type, byte stateInstruction, boolean isReq, byte sourceCon, byte destCon, byte sourceMetaId, byte[] data) {

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
				stateInstrBroadcastNext();
			}
			else {
				if (freqLimit("META_ID_SET",450)) {
					unicast(pow2((destCon + 4) % 8),PacketCoreType.META_ID_SET,false,data);
				}
			}
			
			
		}
		
		if (type == PacketCoreType.SYMMETRY) {
			if (getStateInstruction() == stateInstruction) {
				if (freqLimit("SYMMETRY",500)) {
					symmetryFix (isReq,sourceCon, destCon);
				}
			}
		}
	} 

	
	
	
	public void gradientCreate () {
		
		boolean sourceH = nbs(EAST&MALE).nbsInMetaGoup().size() == 2 && nbs(WEST&SOUTH&MALE).nbsInMetaGoup().isEmpty() || nbs(EAST&FEMALE).nbsInMetaGoup().size() == 2 && nbs(WEST&NORTH&FEMALE).nbsInMetaGoup().isEmpty();
		boolean sourceV = nbs(EAST&MALE).nbsInMetaGoup().size() == 2 && nbs(WEST&NORTH&MALE).nbsInMetaGoup().isEmpty() || nbs(WEST&FEMALE).nbsInMetaGoup().size() == 2 && nbs(EAST&NORTH&FEMALE).nbsInMetaGoup().isEmpty();
		gradientSend(VarLocal.gradH,sourceH);
		gradientSend(VarLocal.gradV,sourceV);			
	}
	
	public void broadcastMetaNeighbors () {
		broadcastMetaNeighborsTo(VarMeta.Top,VarMeta.Left,VarMeta.Right);
		broadcastMetaNeighborsTo(VarMeta.Bottom,VarMeta.Left,VarMeta.Right);
		broadcastMetaNeighborsTo(VarMeta.Left,VarMeta.Top,VarMeta.Bottom);
		broadcastMetaNeighborsTo(VarMeta.Right,VarMeta.Top,VarMeta.Bottom);
	}
	
	public void broadcastMetaNeighborsTo (VarMeta d, VarMeta v1, VarMeta v2) {
		byte val1 = varGet(v1);
		byte val2 = varGet(v2);
		byte dest = varGet(d);
		
		if (metaIdExists() && dest != 0) {
			send(MetaPacketCoreType.ADD_NEIGHBOR,dest, new byte[]{v1.index(),val1,v2.index(),val2});
		}
		
	}
	

	public void metaNeighborHook(int connectorNr,byte metaId) {
//		visual.print(".metaNeighborHook (" + metaId + ")");
		if ( connectorNr == 5 || connectorNr == 6) {
			varSet(VarMeta.Right, metaId);
		}
		if (connectorNr == 2 || connectorNr == 7) {
			varSet(VarMeta.Top, metaId);
		}
		if (connectorNr == 0 || connectorNr == 3) {
			varSet(VarMeta.Left, metaId);
		}
		if (connectorNr == 1 || connectorNr == 4) {
			varSet(VarMeta.Bottom, metaId);
		}
		
	}

	protected void receiveMetaMessage(IPacketType type, byte source, byte dest, byte[] data) {
		if (type == MetaPacketCoreType.ADD_NEIGHBOR) {
			if (data[0] == VarMeta.Top.index()) {
				if (source == varGet(VarMeta.Left)) {
					varSet(VarMeta.TopLeft,data[1]);
				}
				if (source == varGet(VarMeta.Right)) {
					varSet(VarMeta.TopRight,data[1]);
				}
			}
			if (data[2] == VarMeta.Bottom.index()) {
				if (source == varGet(VarMeta.Left)) {
					varSet(VarMeta.BottomLeft,data[3]);
				}
				if (source == varGet(VarMeta.Right)) {
					varSet(VarMeta.BottomRight,data[3]);
				}
			}
			if (data[0] == VarMeta.Left.index()) {
				if (source == varGet(VarMeta.Top)) {
					varSet(VarMeta.TopLeft,data[1]);
				}
				if (source == varGet(VarMeta.Bottom)) {
					varSet(VarMeta.BottomLeft,data[1]);
				}
			}
			if (data[2] == VarMeta.Right.index()) {
				if (source == varGet(VarMeta.Top)) {
					varSet(VarMeta.TopRight,data[3]);
				}
				if (source == varGet(VarMeta.Bottom)) {
					varSet(VarMeta.BottomRight,data[3]);
				}
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
	
	


}