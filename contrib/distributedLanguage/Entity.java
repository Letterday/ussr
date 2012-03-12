package distributedLanguage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Entity {

    private int integerID = createNewIntegerID();
    private RoCoProgram program;
    private Set<String> activeBehaviors = new HashSet<String>();
    
    public Entity(RoCoProgram program) {
		this.program = program;
		getInitialActiveBehaviors(activeBehaviors);
	}
    
    public RoCoProgram getProgram() {
    	return program;
    }

	public abstract boolean runBehavior(String name, SharedState sharedState);

    public boolean verifyRequirements(Context context, SharedState sharedState) { return true; }

    public abstract boolean update(String name, ContextManager tracker, SharedState sharedState);

    public int getIntegerID() {
        return integerID;
    }

    public abstract int sizeof(SharedMemberID member);
    
    public boolean isPrimaryRole() { return false; }
    
    public abstract boolean isAbstract();
    
    public abstract String getName();
    
    public String getEnsembleName() { return RoCoEnsemble.GLOBAL; }
    
    public abstract List<String> getFieldNames();

    protected abstract void getInitialActiveBehaviors(Collection<String> storage);
    
    public Set<String> getActiveBehaviorNames() {
    	return activeBehaviors;
    }
    
    public abstract Set<String> getSharedFieldNames();
    
    public boolean specializes(Entity candidate) {
        return candidate.getClass().isAssignableFrom(this.getClass());
    }    

    private static int nextIntegerID = 1;
    private static synchronized int createNewIntegerID() {
        return nextIntegerID++;
    }

}
