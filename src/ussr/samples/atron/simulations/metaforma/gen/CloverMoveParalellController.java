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
	

	public static void main( String[] args ) {
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
		b.set(0,3);
		return new ATRONBuilder().buildGrid(b, "Floor_",false);
	}
	
}



public class CloverMoveParalellController extends MetaformaRuntime implements ControllerInformationProvider {
	
	enum StateOperation implements IStateOperation {
		DEFAULT;

		public byte ord() {
			return (byte)ordinal();
		}

		public IStateOperation fromByte(byte b) {
			return values()[b];
		}
	}
	
	enum Var implements IVar {
		NONE,gradH,gradV,metaGradH, metaGradV ;

		public byte index() {
			return (byte)ordinal();
		}

		public Var fromByte(byte b) {
			return values()[b];
		}
	}
	
	private void assign(int h, int v, Module m) {
		if (varGet(Var.gradH) == h && varGet(Var.gradV) == v){
			renameStore();
			renameTo(m);
			commit();
		}
		
	}
	protected void gradientSendMeta(IVar v, boolean isSource) {
		if (isSource) {
			varSet(v,0);
			notification("I am source for " + v);
		}
		
		broadcast(Type.GRADIENT,REQ,new byte[]{v.index(),varGet(v)});
		
	}
	
	
	
	public void handleStates () {
		if (stateOperation(StateOperation.DEFAULT)) {

					
			if (doRepeat(0,2000,99,20)) {				
				if (nbs(EAST&MALE,Grouping.Floor).size() == 2 && !nbs(WEST,Grouping.Floor).exists() && nbs(EAST,Grouping.Floor).exists()){
					if (!metaIdExists()) {
						metaIdSet(getId().ord());
					}
					unicast(new Packet(getId()).setType(Type.META_ID).setData(metaId),EAST&MALE);
					renameGroup(Grouping.Struct);
					commit();
				}
				if (nbs(FEMALE,Grouping.Struct).size() == nbs(Grouping.Struct).size() && metaIdExists()){
					renameGroup(Grouping.Struct);
					commit();
				}
			}
			if (doOnce(1))  {
				scheduler.setInterval("gradientCreate",1000);
				if (stateStartNext == 0) {
					stateStartNext = time() + 5;
				}
				if (stateStartNext< time()) {
					stateInstrBroadcastNext();
				}
			}
	
			 
			if (doOnce(2) )  {
				stateInstrBroadcastNext();
				scheduler.setInterval("gradientCreate",10000);
			}
			
//				if (nbs(WEST&FEMALE,Grouping.Floor).size() == 2 && nbs(Grouping.Floor).size() == 2){
//					renameTo(Module.Clover_North);
//					unicast(new Packet(getId()).setType(Type.META_ID).setData(metaId),WEST);
//				}
//				if (nbs(EAST&FEMALE,Grouping.Floor).size() == 2 && nbs(Grouping.Floor).size() == 2){
//					renameTo(Module.Clover_South);
//					unicast(new Packet(getId()).setType(Type.META_ID).setData(metaId),EAST);
//				}
//				assign (1,1,Module.Uplifter_Left);
//				assign (0,2,Module.Uplifter_Right);
//				assign (0,0,Module.Uplifter_Top);
//				assign (1,3,Module.Uplifter_Bottom);
//				if (nbs(FEMALE&EAST).exists() && !nbs(WEST).exists()){
//					renameStore();
//					renameTo(Module.Clover_South);
//					commit();
//				}
//				if (nbs(EAST).contains(Module.Clover_South)){
//					renameStore();
//					renameTo(Module.Clover_West);
//					commit();
//				}
//				if (nbs(WEST).contains(Module.Clover_South)){
//					renameStore();
//					renameTo(Module.Clover_East);
//					commit();
//				}
//				if (nbs().contains(Module.Clover_East) && nbs().contains(Module.Clover_West)){
//					renameStore();
//					renameTo(Module.Clover_North);
//					commit();
//				}

			}

		
		
   }
	
	
	public void init () {
		setModuleColors (Module.Clover_North,new Color[]{Color.decode("#00FFFF"),Color.decode("#FFFF00")}); 
		setModuleColors (Module.Clover_South,new Color[]{Color.decode("#00AAAA"),Color.decode("#AAAA00")}); 
		setModuleColors (Module.Clover_West,new Color[]{Color.decode("#006666"),Color.decode("#666600")}); 
		setModuleColors (Module.Clover_East,new Color[]{Color.decode("#002222"),Color.decode("#222200")}); 
		
		setDefaultColors (new Color[]{Color.decode("#0000FF"),Color.decode("#FF0000")});
		setGroupingColors(Grouping.Struct, new Color[]{Color.decode("#000066"),Color.decode("#660000")});
		setMessageFilter(Type.STATE_OPERATION_NEW.bit() | Type.GRADIENT.bit() | Type.META_ID.bit());
		stateOperationInit(StateOperation.DEFAULT);
		
		IstateOperation = StateOperation.DEFAULT;
		Ivar = Var.NONE;
		Packet.setController(this);
		
		varSet(Var.metaGradH, MAX_BYTE);
		varSet(Var.metaGradV, MAX_BYTE);
		
		
	
	}


	protected void receiveMessage(Type type, IStateOperation stateOperation, byte stateInstruction, boolean isReq, byte sourceCon, byte destCon, byte sourceMetaId, byte[] data) {
			
		if (type == Type.GRADIENT) {
			if (varGet(Var.NONE.fromByte(data[0])) > data[1] && sourceMetaId == metaId){
				varSet(Var.NONE.fromByte(data[0]),data[1]);
				scheduler.invokeNow("gradientCreate");
			}
			else if (varGet(Var.NONE.fromByte(data[0])) > data[1] + 1 && sourceMetaId != metaId){
				varSet(Var.NONE.fromByte(data[0]),data[1] + 1);
				scheduler.invokeNow("gradientCreate");
			}
			
		}
		if (type == Type.META_ID) {
			if (!metaIdExists()) {
				metaIdSet(data[0]);
				if (isMALE(destCon)) {
					renameGroup(Grouping.Struct);
					commit();
				}
				
			}
			if (freqLimit(1000,1)) {
				unicast(new Packet(getId()).setType(Type.META_ID).setData(data[0]),pow2((destCon + 4)% 8) );
			}
		}
	}
	
	public void gradientCreate () {
		//discoverNeighbors();
		
		boolean sourceH = nbs(EAST&MALE).size() == 2 && nbs(WEST&SOUTH&MALE).isEmpty() || nbs(EAST&FEMALE).size() == 2 && nbs(WEST&NORTH&FEMALE).isEmpty();
		boolean sourceV = nbs(EAST&MALE).size() == 2 && nbs(WEST&NORTH&MALE).isEmpty() || nbs(WEST&FEMALE).size() == 2 && nbs(EAST&NORTH&FEMALE).isEmpty();
		gradientSendMeta(Var.metaGradH,sourceH);
		gradientSendMeta(Var.metaGradV,sourceV);			
	}


}
