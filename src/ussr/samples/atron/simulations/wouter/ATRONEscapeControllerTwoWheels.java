package ussr.samples.atron.simulations.wouter;

import ussr.model.Sensor;
import ussr.samples.atron.ATRONController;

public class ATRONEscapeControllerTwoWheels extends ATRONController {

	public static void main(String[] args) {
		new ATRONEscapeSimulation(2).main();
	}

	private static double turnaroundTime = 2;
	int dir = 1;
	private double backwardTime = 2.5;

	ATRONBus bus;

	public void activate() {
		setup();
		bus = new ATRONBus(this);
		bus.state = State.FORWARD;

		if (bus.moduleName.contains("right"))
			dir = -dir;

		while (true) {
			bus.maintainConnection();
			handleStates();

			for (Sensor s : module.getSensors()) {
				if (s.getName().startsWith("Proximity")) {
					float v = s.readValue();
					if (v > 0.1f)
						bus.printLimited(bus.moduleName + ".v-" + s.getName()
								+ " = " + v);

				}
			}

			if (bus.moduleName.contains("driver")) {

				for (Sensor s : module.getSensors()) {
					if (s.getName().startsWith("Proximity")) {
						float v = s.readValue();

						if (bus.state != State.BACKWARD && v > 0.2) {
							System.out.println("object found");
							bus.broadcastState(State.BACKWARD);
						}

					}
				}

				if (bus.state == State.BACKWARD && bus.waitedFor(backwardTime)) {
					bus.broadcastState(State.TURNING_RIGHT);
					// backwardTime = 10 * Math.random();

				}

				if (bus.state == State.TURNING_RIGHT
						&& bus.waitedFor(turnaroundTime)) {
					bus.broadcastState(State.AROUND);
					// turnaroundTime = 2 * Math.random();
				}

				if (bus.state == State.AROUND && bus.waitedFor(turnaroundTime)) {
					bus.broadcastState(State.TURNING_LEFT);
					// turnaroundTime = 2 * Math.random();
					// offset = time;
				}

				if (bus.state == State.TURNING_LEFT
						&& bus.waitedFor(turnaroundTime * 1.6))
					bus.broadcastState(State.FORWARD);
			}

			bus.printState();

			yield();
		}
	}

	private void handleStates() {
		if (bus.state == State.FORWARD || bus.state == State.AROUND)
			if (bus.moduleName.contains("wheel"))
				rotateContinuous(dir);
			else
				centerStop();

		if (bus.state == State.BACKWARD)
			if (bus.moduleName.contains("wheel"))
				rotateContinuous(-dir);
			else
				centerStop();

		if (bus.state == State.TURNING_LEFT) {
			if (bus.moduleName.contains("right"))
				rotateContinuous(dir);
			else
				centerBrake();
		}

		if (bus.state == State.TURNING_RIGHT) {
			if (bus.moduleName.contains("left"))
				rotateContinuous(dir);
			else if (bus.moduleName.contains("right"))
				rotateContinuous(-dir);
			else
				centerBrake();
		}

	}

	public void handleMessage(byte[] message, int messageLength, int connector) {
		bus.handleMessage(message, messageLength, connector);
	}

}
