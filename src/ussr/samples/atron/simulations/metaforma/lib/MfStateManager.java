package ussr.samples.atron.simulations.metaforma.lib;

import java.math.BigInteger;

import com.sun.swing.internal.plaf.metal.resources.metal;

import ussr.samples.atron.simulations.metaforma.lib.MfController.VarMetaGroupCore;


public class MfStateManager {
	private State stateCurrent;
	
	
	private float stateStartTime;
	private BigInteger consensus = BigInteger.ZERO; 
	
	private MfController ctrl;
	
	private boolean stateNeighborsDiscovered;


	private IStateOperation stateOperationNext;
	
	private boolean commitAutoAfterState = true;
	private int commitCount;
	
	public MfStateManager (MfController c) {
		ctrl = c;
		State.setController(c);
	}
	
	public void init (IStateOperation op) {
		stateCurrent = new State (op,0,0);
	}

	
	
	public void commitMyselfIfNotUsed () {
		if (commitAutoAfterState && commitCount == 0) {
			commit("AUTO commit at the end!");
		}
	}
	
	public void commitSetCount(int count) {
		commitCount = count;
	}
	
	public void commitNotAutomatic (IModuleHolder g) {
		if (g.contains(ctrl.getId()) && commitAutoAfterState) {
			ctrl.visual.print("Disable auto commit");
			commitAutoAfterState = false;
		}
	}
	
	public void commitNotAutomatic (IModuleHolder g,IModuleHolder g2) {
		if (commitAutoAfterState && (g.contains(ctrl.getId()) && !ctrl.nbs().nbsIn(g2).isEmpty() || g2.contains(ctrl.getId()) && !ctrl.nbs().nbsIn(g).isEmpty())) {
			ctrl.visual.print("Disable auto commit");
			commitAutoAfterState = false;
		}
	}
	
	

	
	public float timeSpentInState() {
		return MfController.round((ctrl.time() - stateStartTime),3);
	}
	
	public void spend (float timeToSpend) {
		commitNotAutomatic(ctrl.getId());
		if (timeSpentInState() > timeToSpend) {
			commit("Spent " + timeToSpend + " in state!");
		}
		
	}
	
	public BigInteger getConsensus() {
		return consensus;
	}
	
	
	
	
	public void merge (State stateRec, BigInteger consensusRec) {
		if (stateCurrent.isNewer (stateRec)) {
			next(stateRec);
		}
		
		if (stateCurrent.equals(stateRec) && !((consensus).or(consensusRec)).equals(consensus)) {
			if (consensusRec.testBit(ctrl.getId().ord()) && !committed()) {
				ctrl.visual.error("I have NOT committed but my consensus bit is SET!?");
			}
			consensus = consensus.or(consensusRec);
			ctrl.visual.print("consensus update: " + consensus.bitCount() + ": " + Module.fromBits(consensus));
			ctrl.scheduler.invokeNow("broadcastConsensus");
		}
		
		if ((ctrl.metaBossMyself() || !ctrl.metaBossIdExists() )&& consensusReached()) {
			if (stateOperationNext == null) {
				ctrl.visual.print("CONSENSUS REACHED - go to next instr");
				nextInstruction();
			}
			else {
				ctrl.visual.print("CONSENSUS REACHED - " + stateOperationNext);
				nextOperation(stateOperationNext);
			}
		} 
	}
	
	

	protected void cleanForNew() {
		ctrl.prepareNextState();
				
		stateNeighborsDiscovered = false;
		stateStartTime = ctrl.time();
		stateOperationNext = null;
		commitAutoAfterState = true;

		consensus = BigInteger.ZERO;
		
	}
	
	public int getStateInstruction() {
		return stateCurrent.getInstruction();
	}
	

	public boolean consensusReached() {
		if (commitCount == 0) {
			if (ctrl.metaBossIdExists()) {
				return ctrl.varGet(VarMetaGroupCore.GroupSize) >= ctrl.moduleRoleGet().size() && consensusReached(ctrl.varGet(VarMetaGroupCore.GroupSize));
			} 
			else {
				return stateCurrent.getOperation().ord() == 0 && consensusReached(ctrl.moduleRoleGet().size());
			}
		}
		else {
			return consensusReached(commitCount);
		}
	}
	
	
	private boolean consensusReached(int count) {
		return consensusReached(count, 98);
	}
	
	private boolean consensusReached(int count, int degradePerc) {
		if (!ctrl.metaGetCompleted()) {
			return false;
		}
		float consensusToReach = count - (timeSpentInState() / 100) * (100-degradePerc)/100 * count;
		boolean consensusReached = consensus.bitCount() >= consensusToReach;
		if (consensusReached) {
			ctrl.visual.print("Consensus " + count + " reached!");
		}
		else {
			if (ctrl.freqLimit("consensusPrint",5000)) {
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
		if (!consensus.setBit(ctrl.getId().ord()).equals(consensus)) {
			consensus = consensus.setBit(ctrl.getId().ord());
			ctrl.scheduler.invokeNow("broadcastConsensus");
			modified = true;
		}
		ctrl.visual.print(".commit("+reason+") count:" + consensus.bitCount() + " modified:" + modified + " " + Module.fromBits(consensus));
		
	}
	
	public boolean committed () {
		return consensus.testBit(ctrl.getId().ord());
	}
	
	
	
	private void next(State stateNew) {
		ctrl.getVisual().printStatePost();
		cleanForNew();
		stateCurrent.merge(stateNew);
		ctrl.getVisual().printStatePre();
		ctrl.scheduler.invokeNow("broadcastDiscover");
	}
	
	
	
	public void nextInstruction () {
		next(new State(stateCurrent).setInstruction((byte) (stateCurrent.getInstruction() + 1)));
	}
	
	public void nextOperation (IStateOperation op) {
		
		if (stateCurrent.equals(op)){
			ctrl.visual.error("OPERATION " + op + " equals " + stateCurrent.getOperation());
		}
		next(new State(stateCurrent).nextOperation(op));
		
	}
	
	
	public boolean doUntil(int state) {
		return doUntil(state,500);
	}
	
	
	public boolean doUntil (int stateInstr, int interval) {
		int groupSize = ctrl.varGet(VarMetaGroupCore.GroupSize);
		if (groupSize == 0) {
			groupSize = ctrl.moduleRoleGet().size();
//			System.err.println("Group size equals 0!");
		}
		if (stateCurrent.getInstruction() == stateInstr && ctrl.freqLimit("doRepeat" + stateInstr,interval)) {
			if (!stateNeighborsDiscovered) {
				stateNeighborsDiscovered = true;
				ctrl.discoverNeighbors();
			}
			return true;
		}
		return false;
	}
	
	public boolean doWait(int state) {
		if (getStateInstruction() == state && !committed()) {
			ctrl.discoverNeighbors();
			return true;
		}
		return false;
	}
			
	
	public boolean at (State s) {
		return stateCurrent.equals(s);
	}
	
	public boolean at (IStateOperation op) {
		return stateCurrent.getOperation().equals(op);
	}	

	public void setAfterConsensus(IStateOperation op) {
		if (!op.equals(stateOperationNext)) {
			ctrl.visual.print(".setAfterConsensus " + op);
			stateOperationNext = op;
		}
	}
	
	public State getState () {
		return stateCurrent;
	}
	
}
