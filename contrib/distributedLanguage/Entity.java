package distributedLanguage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Entity {

	public interface UpdateFunction {
		void update(String name, ContextManager tracker, SharedState sharedState);
	}
	
	public interface BehaviorFunction {
		void run(String name, SharedState sharedState);
	}
	
    private int integerID = createNewIntegerID();
    private RoCoProgram program;
    private Set<String> activeBehaviors = new HashSet<String>();
    protected Map<String,UpdateFunction> updateFunctions = new HashMap<String,UpdateFunction>();
    protected Map<String,BehaviorFunction> behaviorFunctions = new HashMap<String,BehaviorFunction>();
    
    public Entity(RoCoProgram program) {
		this.program = program;
		getInitialActiveBehaviors(activeBehaviors);
		initializeUpdateFunctions();
		initializeBehaviorFunctions();
	}
    
    protected abstract void initializeUpdateFunctions();

    protected abstract void initializeBehaviorFunctions();

	public RoCoProgram getProgram() {
    	return program;
    }

	public boolean runBehavior(String name, SharedState sharedState) {
		if(activeBehaviors.contains(name) && behaviorFunctions.get(name)!=null) {
			behaviorFunctions.get(name).run(name,sharedState);
			return true;
		}
		return false;
	}

    public boolean verifyRequirements(Context context, SharedState sharedState) { return true; }

    public boolean update(String name, ContextManager tracker, SharedState sharedState) {
    	if(getSharedFieldNames().contains(name)) {
    		updateFunctions.get(name).update(name,tracker,sharedState);
    		return true;
    	}
    	return false;
    }

    public int getIntegerID() {
        return integerID;
    }

    /**
     * Default implementation (currently only behaviors have more than size 1)
     * @param member
     * @return
     */
    public int sizeof(SharedMemberID member) { return 1; }
    
    public boolean isPrimaryRole() { return false; }
    
    public abstract boolean isAbstract();
    
    public abstract String getName();
    
    public String getSuperName() { return "."; }

    public String getEnsembleName() { return RoCoEnsemble.GLOBAL; }
    
    public abstract List<String> getFieldNames();

    protected void getInitialActiveBehaviors(Collection<String> storage) { ; }
    
    public Set<String> getActiveBehaviorNames() {
    	return activeBehaviors;
    }
    
    public Set<String> getSharedFieldNames() { return Collections.EMPTY_SET; }
    
    public boolean specializes(Entity candidate) {
        return candidate.getClass().isAssignableFrom(this.getClass());
    }    

    private static int nextIntegerID = 1;
    private static synchronized int createNewIntegerID() {
        return nextIntegerID++;
    }

}
