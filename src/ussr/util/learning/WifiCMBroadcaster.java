package ussr.util.learning;

import ussr.comm.Packet;
import ussr.comm.RadioTransmitter;
import ussr.comm.TransmissionType;
import ussr.model.Module;
import ussr.physics.PhysicsObserver;
import ussr.physics.PhysicsSimulation;

import com.jme.math.Vector3f;
import com.sun.corba.se.impl.ior.ByteBuffer;

public class WifiCMBroadcaster implements PhysicsObserver {
	final byte LEARNING_MESSAGE = 4;
	final byte label = 1;
	PhysicsSimulation simulation;
	double deltaT;
	double nextT;
	CMTracker tracker;
	RadioTransmitter radio;
	Vector3f oldPos;
	byte stateCount;
	public WifiCMBroadcaster(PhysicsSimulation simulation, double deltaT, CMTracker tracker) {
		this.simulation = simulation;
		this.deltaT = deltaT;
		this.tracker = tracker;
		Module dummyModule = new Module();
		dummyModule.setSimulation(simulation);
		radio = new RadioTransmitter(dummyModule, dummyModule,TransmissionType.RADIO, Float.MAX_VALUE);
		nextT = simulation.getTime()+deltaT;
		oldPos = tracker.getRobotCM();
		stateCount = 0;
	}
	
	/**
	 * Transmit robots movement over wifi 
	 */
	public void physicsTimeStepHook(PhysicsSimulation simulation) {
		if(nextT<simulation.getTime()) {
			float dist = tracker.getRobotCM().distance(oldPos);
			byte reward = (byte)(250*dist);
			if(!Float.isNaN(dist)) {				
	 			ByteBuffer bb = new ByteBuffer(3);
				bb.append(LEARNING_MESSAGE);
				bb.append(label);
				bb.append(stateCount);
				bb.append(reward);
				System.out.println(stateCount+": Reward send = "+reward);
				Packet packet = new Packet(bb.toArray());
				radio.send(packet);
			}
			nextT += deltaT;
			stateCount++;
			oldPos = tracker.getRobotCM();
		}
	}
}
