package ussr.samples.atron.simulations.metaforma.lib;

public class State {
	private byte instruction;
	private IStateOperation operation;
	private byte operationCounter;
	private static MfController ctrl;
	
	public static void setController (MfController c) {
		ctrl = c;
	}
	
	public State (State s) {
		instruction = s.getInstruction();
		operation = s.getOperation();
		operationCounter = s.getOperationCounter();		
	}
	
	public State () {
		instruction = 0;
		operation = null;
		operationCounter = 0;
	}
	
	public State (IStateOperation op, int opCounter,int in) {
		instruction = (byte) in;
		operation = op;
		operationCounter = (byte) opCounter;
	}
	
	public boolean merge (State s) {
		return merge (s,true);
	}
	
	public boolean isNewer (State s) {
		return merge (s,false);
	}
	
	private boolean merge (State s,boolean modify) {
		if (getOperationCounter() == s.getOperationCounter() && getInstruction() < s.getInstruction()) {
			if (modify) {
				if (getInstruction() + 1 < s.getInstruction()) {
					ctrl.visual.print("!!! I might have missed a state, from " + instruction  + " to " + s.getInstruction());
				}	
				instruction = s.getInstruction();
			}
			return true;
		}
		else if (getOperationCounter() < s.getOperationCounter()) {
			if (modify) {
				operationCounter = s.getOperationCounter();
				operation = s.getOperation();
				instruction = s.getInstruction();
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	public byte getInstruction () {
		return instruction;
	}
	
	public IStateOperation getOperation () {
		return operation;
	}
	
	public State nextOperation (IStateOperation op) {
		operation = op;
		operationCounter++;
		instruction = 0;
		return this;
	}
	
	public byte getOperationCounter () {
		return operationCounter;
	}
	
	public boolean matchOperation (IStateOperation op) {
		return operation.equals(op);
	}

	public State setInstruction(byte instr) {
		instruction = instr;
		return this;
	}
	
	public String toString () {
		return "[" + operation + "(" + operationCounter + ") #" + instruction + "]";
	}
	
	public boolean equals (State s) {
		return operation != null && operationCounter == s.getOperationCounter()  && instruction == s.getInstruction(); // TODO: && operation.equals(s.getOperation()) ??
		
	}
}
