package ussr.samples.atron.simulations.metaforma.lib;

public class State implements IState,Cloneable {
	private byte instruction;
	private IStateOperation operation;
	private byte operationCounter;
	
	
	public State (State s) {
		instruction = s.getInstruction();
		operation = s.getOperation();
		operationCounter = s.getOperationCounter();
	}
	
	public State () {
		this(null,0,0);
	}
	
	public State (IStateOperation op, int opCounter,int instr) {
		instruction = (byte) instr;
		operation = op;
		operationCounter = (byte) opCounter;
	}
	
	public State (IStateOperation op,int instr) {
		this(op,-1,instr);
	}
	
	public boolean merge (State s) {
		return merge (s,true);
	}
	
	public boolean isNewer (State s) {
		return merge (s,false);
	}
	
	/**
	 * Checks wheter state s is consecutive on this
	 * @param State
	 * @return
	 */
	public boolean isConsecutive (State s) {
		try {
			return isNewer(s) && !isNewer (((State)s.clone()).setInstruction((byte) (s.getInstruction()-1)));
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean merge (State s,boolean modify) {
		if (getOperationCounter() == s.getOperationCounter() && getInstruction() < s.getInstruction()) {
			if (modify) {
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
	
	public boolean match (IStateOperation op) {
		return operation.equals(op);
	}
	
	public boolean match (State s) {
		return equals(s);
	}

	public State setInstruction(byte instr) {
		instruction = instr;
		return this;
	}
	
	public String toString () {
		return "[" + operation + "(" + operationCounter + ") #" + instruction + "]";
	}
	
	public boolean equals (State s) {
		return operation != null && operation.equals(s.getOperation()) && instruction == s.getInstruction() && (operationCounter == s.getOperationCounter() || operationCounter == -1 || s.getOperationCounter() == -1);
		
	}
}
