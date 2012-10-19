package ussr.samples.atron.simulations.metaforma.lib;


public class State implements IState,Cloneable {
	private byte instruction;
	private IStateOperation operation;
	private Orientation orient;
	private byte operationCounter;
	
	
	public State (State s) {
		instruction = s.getInstruction();
		operation = s.getOperation();
		operationCounter = s.getOperationCounter();
		orient = s.getOrientation();
	}
	
	public State () {
		this(null,0,0,Orientation.TOP_LEFT);
	}
	
	public State (IStateOperation op, int opCounter,int instr, Orientation o) {
		instruction = (byte) instr;
		operation = op;
		operationCounter = (byte) opCounter;
		orient = o;
	}
	
	public State (IStateOperation op,int instr) {
		this(op,-1,instr,Orientation.BOTTOM_LEFT);
	}
	
	public State(IStateOperation op) {
		this(op,0,0,Orientation.BOTTOM_LEFT);
	}

	public boolean merge (State s) {
		return merge (s,true,false);
	}
	
	public boolean isNewer (State s) {
		return merge (s,false,false);
	}
	
	/**
	 * Checks whether state s is consecutive on this
	 * @param State
	 * @return
	 */
	public boolean isConsecutive (State s) {
		return !merge (s,false,true);
	}
	
	private boolean merge (State s,boolean modify,boolean checkConsecutive) {
		int offset = checkConsecutive ? 1 : 0;
		
		// We need to make an exception here, to allow the state to be reset to counter 0 when completed.
		// The state cannot always be increased (overflow!)
		if (s.getOperation().ord() == 0 && s.getOperationCounter() == 0 && getOperationCounter() > 1) {
			return true;
		}
		
		if (getOperationCounter() == s.getOperationCounter() && getInstruction() < s.getInstruction() - offset) {
			if (modify) {
				instruction = s.getInstruction();
			}
			return true;
		}
		else if (getOperationCounter() < s.getOperationCounter() - offset) {
			if (modify) {
				
				operation = s.getOperation();
				orient = s.getOrientation();
				instruction = s.getInstruction();
				operationCounter = s.getOperationCounter();
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	
	public Orientation getOrientation() {
		return orient;
	}

	public byte getInstruction () {
		return instruction;
	}
	
	public IStateOperation getOperation () {
		return operation;
	}
	
	public State nextOperation (IStateOperation op,Orientation o) {
		operation = op;
		operationCounter++;
		instruction = 0;
		orient = o;
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
		if (operation.ord() > 1) {
			return "[" + operation + " " + orient + " (" + operationCounter + ") #" + instruction + "]";
		}
		else {
			return "[" + operation + " " + "(" + operationCounter + ") #" + instruction + "]";
		}
	}
	
	public boolean equals (State s) {
		return 
			operation != null 
			&& operation.equals(s.getOperation()) 
			&& instruction == s.getInstruction() 
//			&& orient == s.getOrientation() TO MATCH IT WHEN RECEIVING PACKETS 
			&& (
				operationCounter == s.getOperationCounter() 
				|| operationCounter == -1 
				|| s.getOperationCounter() == -1
			);
		
	}

	public boolean isInSequence() {
		return operation.ord() > 1;
	}
}
