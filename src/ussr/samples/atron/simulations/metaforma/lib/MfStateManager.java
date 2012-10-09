package ussr.samples.atron.simulations.metaforma.lib;

import java.math.BigInteger;

import ussr.samples.atron.simulations.metaforma.lib.Packet.Packet;

public class MfStateManager {
	private State stateCurrent = new State();
	private State stateReceived = new State();
	
	private float stateStartTime;
	private float seqStartTime;
	
	private BigInteger consensus = BigInteger.ZERO; 
	private BigInteger consensusReceived = BigInteger.ZERO;
	
	private MfController ctrl;
	
	private boolean stateNeighborsDiscovered;


	private IStateOperation stateOperationNext;
	private Orientation orientationNext;
	
	private boolean commitAutoAfterState = true;
	private int commitCountToReach;
	
	float stateTimeToSpend; 

	
	public MfStateManager (MfController c) {
		ctrl = c;
	}
	
	public void init (IStateOperation op) {
		stateCurrent = new State (op,0,0,Orientation.TOPLEFT);
		stateReceived = new State (op,0,0,Orientation.TOPLEFT);
	}
	
	public void commitMyselfIfNotUsed () {
		if (commitAutoAfterState && commitCountToReach == 0) {
			commit("AUTO commit at the end of state " + stateCurrent + "!");
		}
	}
	
	public void consensusSetCount(int count) {
		commitCountToReach = count;
	}
	
	public void commitNotAutomatic (IModuleHolder g) {
		if (g.contains(ctrl.getID()) && commitAutoAfterState) {
			ctrl.visual.print("Disable auto commit");
			commitAutoAfterState = false;
		}
	}
	
	public void commitNotAutomatic (IModuleHolder g,IModuleHolder g2) {
		if (commitAutoAfterState && (g.contains(ctrl.getID()) && !ctrl.nbs().nbsIn(g2).isEmpty() || g2.contains(ctrl.getID()) && !ctrl.nbs().nbsIn(g).isEmpty())) {
			ctrl.visual.print("Disable auto commit");
			commitAutoAfterState = false;
		}
	}
	
	

	
	public float timeSpentInState() {
		return MfController.round((ctrl.time() - stateStartTime),3);
	}
	
	public float timeSpentInSequence() {
		return MfController.round((ctrl.time() - seqStartTime),3);
	}
	
	
	public void spend (String action) {
		spend(ctrl.getSettings().getDuration(action));
	}
	
	public void spend(float time) {
		commitNotAutomatic(ctrl.getID());
		stateTimeToSpend = time;
	}
	
	public BigInteger getConsensus() {
		return consensus;
	}
	
	public BigInteger getConsensusRcvd() {
		return consensusReceived;
	}
	
	
	public void merge () {		
		if (stateCurrent.equals(stateReceived) && !((consensus).or(consensusReceived)).equals(consensus)) {
			if (consensusReceived.testBit(ctrl.getID().ord()) && !committed()) {
				ctrl.visual.error("I have NOT committed but my consensus bit is SET!?");
			}
			consensus = consensus.or(consensusReceived);
			ctrl.visual.print("consensus update: " + consensus.bitCount() + ": " + Module.fromBits(consensus));
			ctrl.scheduler.invokeNowConsensus();
		}
		
		
//		 Consensus to next state, when:
//			- meta = 4
//			- region = 8 or 12
		if (consensusReached()) {
			if (stateOperationNext == null) {
				ctrl.visual.print("CONSENSUS REACHED - go to next instr");
				nextInstruction();
			}
			else {
				ctrl.visual.print("CONSENSUS REACHED - " + stateOperationNext + " - " + orientationNext);
				nextOperation(stateOperationNext,orientationNext);
			}
		}
		
		if (stateCurrent.isNewer (stateReceived)) {
			if (!stateCurrent.isConsecutive (stateReceived)) {
				ctrl.visual.print("!!! I might have missed a state, from " + stateCurrent  + " to " + stateReceived);
			}	
			nextState(stateReceived);
		}
	}
	
	
	public void cleanConsensus() {
		ctrl.visual.print(".cleanConsensus()");
		consensus = BigInteger.ZERO;
		consensusReceived = BigInteger.ZERO;
	}

	protected void cleanForNew() {
		ctrl.prepareNextState();
		stateTimeToSpend = 0;
		stateNeighborsDiscovered = false;
		stateStartTime = ctrl.time();
		commitCountToReach = 0;
		stateOperationNext = null;
		orientationNext = Orientation.TOPLEFT;
		commitAutoAfterState = true;
		
		cleanConsensus();
		
	}
	
	public int getStateInstruction() {
		return stateCurrent.getInstruction();
	}
	

	public boolean consensusReached() {
		// Consensus is always inside at least one meta-module
		if (ctrl.module().metaID != 0) {
			// If I am part of a region, I must be boss of that region!
			// Consensus on meta-module level may only happen at INIT state, in other states Consensus must happen on region level!!
			if (commitCountToReach > 0) {
				return consensusReached(commitCountToReach);
			}
			else if (((ctrl.meta().regionID() != 0 && ctrl.meta().regionID() == ctrl.module().metaID))){// || at(new State(StateOperation.CHOOSE,0)) || at(new State(StateOperation.CHOOSE,1)))) {
				return consensusReached(ctrl.meta().getCountInRegion() * ctrl.getInstRole().size());
			}
		}
		return false;
	}
	
	
	private boolean consensusReached(int count) {
		return consensusReached(count, 95);
	}
	
	private boolean consensusReached(int count, int degradePerc) {
		if (ctrl.meta().completed() == 0) {
			return false;
		}
		float consensusToReach = count - (timeSpentInState() / 100) * (100-degradePerc)/100 * count;
		boolean consensusReached = consensus.bitCount() >= consensusToReach;
		if (consensusReached) {
			ctrl.visual.print("Consensus " + count + " reached!");
		}
		else {
			if (ctrl.freqLimit("consensusPrint",5)) {
				ctrl.visual.print("Consensus waiting for " + consensus.bitCount() + " >= " + consensusToReach);
			}
		}
		
		return consensusReached; 
	}
	
	public void commit () {
		commit("");
	}
	
	
	
	public void commit (String reason) {
		ctrl.debugForceMetaId();
		boolean modified = false;
		if (!consensus.setBit(ctrl.getID().ord()).equals(consensus)) {
			consensus = consensus.setBit(ctrl.getID().ord());
			ctrl.scheduler.invokeNowConsensus();
			modified = true;
			ctrl.visual.print(".commit("+reason+") count:" + consensus.bitCount() + " modified:" + modified + " " + Module.fromBits(consensus));
		}
		
		
	}
	
	public boolean committed () {
//		System.out.println(".committed test " + ctrl.getID().ord());
		return consensus.testBit(ctrl.getID().ord());
	}
	
	
	
	private void nextState(State stateNew) {
		ctrl.getVisual().printStatePost();
		cleanForNew();
		stateCurrent.merge(stateNew);
		stateReceived.merge(stateNew); // So we will not see a state update message for this state from another module
		ctrl.getVisual().printStatePre();
		ctrl.scheduler.invokeNowDiscover();
	}
	
	
	
	public void nextInstruction () {
		ctrl.getVisual().print("Next instruction state!");
		nextState(new State(stateCurrent).setInstruction((byte) (stateCurrent.getInstruction() + 1)));
	}
	
	public void nextOperation (IStateOperation op) {
		nextOperation(op,Orientation.TOPLEFT);
	}
	
	public void nextOperation (IStateOperation op,Orientation orient) {
		if (stateCurrent.equals(op)){
			ctrl.visual.error("OPERATION " + op + " equals " + stateCurrent.getOperation());
		}
		nextState(new State(stateCurrent).nextOperation(op,orient));
		
	}
	
	
	public boolean doUntil(int state) {
		return doUntil(state,0.5f);
	}
	
	
	public boolean doUntil (int stateInstr, float interval) {
		checkForTimeSpent();
		if (stateCurrent.getInstruction() == stateInstr && ctrl.freqLimit("doRepeat" + stateInstr,interval)) {
			if (!stateNeighborsDiscovered) {
				stateNeighborsDiscovered = true;
				ctrl.getScheduler().invokeNowDiscover();
			}
			return true;
		}
		return false;
	}
	
	
	public boolean doWait(int state) {
		checkForTimeSpent();
		
		
		if (getStateInstruction() == state && !committed()) {
			ctrl.getScheduler().invokeNowDiscover();
			ctrl.delay();
			return true;
		}
		return false;
	}
			
	
	private void checkForTimeSpent() {
		if (stateTimeToSpend != 0 && timeSpentInState() > stateTimeToSpend) {
//			commit("Spent " + timeToSpend + " in state!");
			ctrl.visual.print("Spent " + stateTimeToSpend + " in state!");
			nextInstruction();
		}
	}

	public boolean at (State s) {
		return stateCurrent.equals(s);
	}
	
	public boolean at (IStateOperation op) {
		return stateCurrent.getOperation().equals(op);
	}	

	public void setAfterConsensus(IStateOperation op,Orientation orient) {
		if (!op.equals(stateOperationNext)) {
			ctrl.visual.print(".setAfterConsensus " + op);
			stateOperationNext = op;
			orientationNext = orient;
			MfStats.getInst().addStart(op,ctrl.module().metaID,ctrl.time());
		}
	}
	
	public State getState () {
		return stateCurrent;
	}
	
	public State getStateRcvd () {
		return stateReceived;
	}

	public boolean update(BigInteger consensusUpd, State stateUpd) {
		boolean ret = false;
		
		if (stateReceived.isNewer(stateUpd)) {
			ctrl.getVisual().print(".upd "  + stateUpd + " " + stateReceived);
			consensusReceived = consensusUpd; 
			stateReceived = stateUpd;
			ret = true;
		}
		if (stateReceived.equals(stateUpd)) {
			if (!consensusReceived.or(consensusUpd).equals(consensusReceived)) {
				consensusReceived = consensusReceived.or(consensusUpd);
				ret = true;
			}
		}
		return ret;
	}

	public boolean check(Packet p, State state) {
		return at(p.getState()) && p.getState().match(state);
	}
	
	public boolean check(Packet p, IStateOperation state) {
		return at(p.getState().getOperation()) && p.getState().match(state);
	}

	

}
