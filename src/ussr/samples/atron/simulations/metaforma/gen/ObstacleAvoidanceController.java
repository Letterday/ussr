package ussr.samples.atron.simulations.metaforma.gen;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ussr.description.Robot;
import ussr.description.setup.ModulePosition;
import ussr.description.setup.WorldDescription;
import ussr.model.Controller;
import ussr.model.debugging.ControllerInformationProvider;
import ussr.samples.ObstacleGenerator;
import ussr.samples.atron.ATRON;

import ussr.samples.atron.simulations.metaforma.lib.*;
import ussr.samples.atron.simulations.metaforma.lib.Packet.*;



class ObstacleAvoidanceSimulation extends MfSimulation {

		
	public static void main( String[] args ) {
		ObstacleAvoidanceSimulation.initSimulator();
        new ObstacleAvoidanceSimulation().main();
    }
	
	
	protected Robot getRobot() {
        ATRON a = new ATRON() {
            public Controller createController() {
            	return new ObstacleAvoidanceController();
            }
        };
        a.setRubberRing();
        a.setGentle();
        return a;
    }
	
	protected ArrayList<ModulePosition> buildRobot() {
		return new MfBuilder().buildCar(2,ObstacleAvoidanceController.Mod.F);
	}
	
	protected void changeWorldHook(WorldDescription world) {
        ObstacleGenerator generator = new ObstacleGenerator();
        generator.obstacalize(ObstacleGenerator.ObstacleType.LINE, world);
        world.setPlaneTexture(WorldDescription.GREY_GRID_TEXTURE);
    }
}


public class ObstacleAvoidanceController extends MfController implements ControllerInformationProvider {
	
	public enum StateOperation implements IStateOperation {
		NONE, DRIVE, TurnFourWheeler,TurnTwoWheeler;
		public byte ord() {return (byte) ordinal();	}
		public IStateOperation fromByte(byte b) {return values()[b];}
	}
	class BagModule extends BagModuleCore {

		
		
	}
	
	class BagMeta extends BagMetaCore implements IMetaBag {

		
	}
	
	
	public enum Mod  implements IModule,IModEnum{
		ALL,
		NONE,
		AXIS_DRIVER,
		F(10),
		AXIS_FRONT,
		AXIS_BACK,
		LEFTWHEEL(10),
		RIGHTWHEEL(10);

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
	
	public enum Group implements IGroupEnum,IModuleHolder{ALL, NONE, F,AXIS, LEFTWHEEL, RIGHTWHEEL;
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
	
	enum MetaPart implements IMetaPart {
		NONE;
		public IMetaPart fromByte(byte b) {
			return values()[b];
		}
		public byte index() {return (byte) ordinal();}
		public byte size() {
			// None is no part
			return (byte) (values().length - 1);
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

		visual.setColor(GenState.INIT,Color.WHITE);
		
		visual.setMessageFilter(255);//^ pow2(PacketDiscover.getTypeNr()));			
	}

	
	public void handleStates () {
		if (stateMngr.at(GenState.INIT)) {
			if (stateMngr.doUntil(0)) {
				meta.enable();
				stateMngr.spend(settings.get("assignTime"));
				if (!nbs(SOUTH&EAST&FEMALE, EAST).isEmpty()) {
					visual.print("I will swap leftwheel!");
					module().swapGroup(Group.LEFTWHEEL);
				}
				if (!nbs(SOUTH&EAST&FEMALE,WEST).isEmpty()) {
					visual.print("I will swap RIGHTWHEEL!");
					module().swapGroup(Group.RIGHTWHEEL);
				}
				if (!nbs(Group.RIGHTWHEEL).isEmpty() && !nbs(Group.LEFTWHEEL).isEmpty() && nbs(NORTH&EAST&FEMALE).isEmpty()) {
					visual.print("I will swap AXIS_FRONT!");
					module().setID(Mod.AXIS_FRONT);
				}
				if (!nbs(Group.RIGHTWHEEL).isEmpty() && !nbs(Group.LEFTWHEEL).isEmpty() && nbs(NORTH&EAST&FEMALE).size() == 1) {
					visual.print("I will swap AXIS_BACK!");
					module().setID(Mod.AXIS_BACK);
				}
				if (nbs(Group.AXIS).size() == 2) {
					visual.print("I will swap DRIVER!");
					module().setID(Mod.AXIS_DRIVER);
				}
			}
			
			if (stateMngr.doWait(1)) {
				stateMngr.nextOperation(StateOperation.DRIVE);
			}
			
		}
		int FORWARD = 1;
		int BACKWARD = -1;
		
		if (stateMngr.at(StateOperation.DRIVE)) {
			if (module().getGroup().equals(Group.AXIS) && module().proximitySensor() > settings.get("proximity")) {
				visual.print("event!!!!!" + nbs().size());
				
				if (module().getID().equals(Mod.AXIS_FRONT) && nbs().size() == 3) {
					stateMngr.nextOperation(StateOperation.TurnFourWheeler);
				}	
				if (module().getID().equals(Mod.AXIS_FRONT) && nbs().size() == 2) {
					stateMngr.nextOperation(StateOperation.TurnTwoWheeler);
				}
			}
			
			if (stateMngr.doWait(0)) {
				drive(FORWARD,FORWARD);
				stateMngr.commitEnd();
			} 
		}
		
		if (stateMngr.at(StateOperation.TurnTwoWheeler)) {
			if (stateMngr.doWait(0)) {
				meta().setVar("size", 3);
				stateMngr.spend(settings.get("backwardTime"));
				drive(BACKWARD,BACKWARD);
			}
			if (stateMngr.doWait(1)) {
				stateMngr.spend(settings.get("turnAroundTime"));
				drive(FORWARD,BACKWARD);
			}
			
			if (stateMngr.doWait(2)) {
				stateMngr.nextOperation(StateOperation.DRIVE);
			}
		}
		
		if (stateMngr.at(StateOperation.TurnFourWheeler)) {
			
			if (stateMngr.doWait(0)) {
				steer(10);
				meta().setVar("size", 7);
			}
			
			if (stateMngr.doWait(1)) {
				drive (BACKWARD,BACKWARD);
				stateMngr.spend(settings.get("backwardTime"));
			}
			
			if (stateMngr.doWait(2)) {
				steer(-20);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(3)) {
				drive (FORWARD,FORWARD);
				stateMngr.spend(settings.get("backwardTime"));
			}
			
			if (stateMngr.doWait(4)) {
				steer(10);
				stateMngr.commitEnd();
			}
			
			if (stateMngr.doWait(5)) {
				stateMngr.nextOperation(StateOperation.DRIVE);
			}
			
			
		}
    
	}
  
	
	private void steer (int degrees) {
		actuation.rotate(Mod.AXIS_FRONT,degrees);
		actuation.rotate(Mod.AXIS_BACK,-degrees);
		stateMngr.commitEnd();
	}
 
	private void drive(int left, int right) {
		actuation.rotate_continuous(Group.LEFTWHEEL,-left);
		actuation.rotate_continuous(Group.RIGHTWHEEL,right);
	}

	@Override
	public IMetaPart getMetaPart() {
		// TODO Auto-generated method stub
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


	@Override
	public void receiveCustomPacket(byte typeNr, byte[] msg, byte connector) {
		// TODO Auto-generated method stub
		
	}
	


	@Override
	public boolean receivePacket(Packet p) {
		// TODO Auto-generated method stub
		return false;
	}




	@Override
	public boolean receivePacket(PacketSymmetry p) {
		// TODO Auto-generated method stub
		return false;
	}




	@Override
	public boolean receivePacket(PacketSetMetaId p) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public IStateOperation getStateInst() {
		return StateOperation.NONE;
	}









}