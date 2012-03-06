package distributedLanguage;

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

    public abstract boolean verifyRequirements(Context context);

    public abstract void update(ContextManager tracker, SharedState sharedState);

    public int getIntegerID() {
        return integerID;
    }

    public abstract int sizeof(SharedMemberID member);
    
    public boolean isPrimaryRole() { return false; }

    public boolean specializes(Entity candidate) {
        return candidate.getClass().isAssignableFrom(this.getClass());
    }    

    private static int nextIntegerID = 1;
    private static synchronized int createNewIntegerID() {
        return nextIntegerID++;
    }

}
