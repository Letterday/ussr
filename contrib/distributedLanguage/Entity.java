package distributedLanguage;

import java.util.List;
import java.util.Map;

public abstract class Entity {

    private int integerID = createNewIntegerID();
    private RoCoProgram program;
    
    public Entity(RoCoProgram program) {
		this.program = program;
	}
    
    public RoCoProgram getProgram() {
    	return program;
    }

	public abstract void runBehaviors(SharedState sharedState);

    public boolean verifyRequirements(Context context, SharedState sharedState) { return true; }

    public abstract void update(ContextManager tracker, SharedState sharedState);

    public int getIntegerID() {
        return integerID;
    }

    public int sizeof(SharedMemberID member) { return 1; }
    
    public boolean isPrimaryRole() { return false; }
    public abstract boolean isAbstract();
    public abstract String getName();
    public String getEnsembleName() { return RoCoEnsemble.GLOBAL; }
    public abstract List<String> getFieldNames();

    public boolean specializes(Entity candidate) {
        return candidate.getClass().isAssignableFrom(this.getClass());
    }    

    private static int nextIntegerID = 1;
    private static synchronized int createNewIntegerID() {
        return nextIntegerID++;
    }

}
