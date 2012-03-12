package distributedLanguage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Activation {
    /**
     * Complete set of active entities, including the active role which is the first element
     */
    private List<Entity> activeEntities = new ArrayList<Entity>();
    /**
     * Program describing the behaviors of this activation
     */
    private RoCoProgram program;
    
    public Activation(RoCoProgram program) {
        this.program = program;
    }

    /**
     * Run all active behaviors
     * @param sharedState state of the shared behaviors of the system
     * @param localState 
     */
    public void runBehaviors(SharedState sharedState) {
    	Set<String> allBehaviorNames = new HashSet<String>();
        for(Entity entity: activeEntities)
        	allBehaviorNames.addAll(entity.getActiveBehaviorNames());
        nameLoop: for(String behaviorName: allBehaviorNames) {
        	for(Entity entity: activeEntities)
        		if(entity.runBehavior(behaviorName,sharedState)) continue nameLoop;
        	throw new Error("Internal error: behavior name not handled");
        }
    }

    /**
     * Verify that all active roles and ensembles satisfy their requirements
     * @param context
     * @param sharedState
     * @param localState
     */
    public void verifyRequirements(Context context, SharedState sharedState) {
        if(!activeEntities.get(0).verifyRequirements(context,sharedState)) {
            activeEntities.set(0,program.findMatchingRole(context,sharedState));
        }
        Iterator<Entity> entities = activeEntities.iterator();
        while(entities.hasNext()) {
            Entity entity = entities.next();
            if(!entity.verifyRequirements(context,sharedState)) entities.remove();
        }
        sharedState.cleanup(activeEntities);
    }

    /**
     * Add any relevant entities (those that satisfy their requirements)
     * @param context
     * @param sharedState
     * @param localState
     */
    public void addNewEntities(Context context, SharedState sharedState) {
        for(Entity entity: program.getAllSecondaryEntities()) {
            if(entity.verifyRequirements(context,sharedState) && !activeEntities.contains(entity))
                activeEntities.add(0,entity); // newest entities at beginning, priority for dispatches
        }
    }

    /**
     * Run update behaviors in all entities
     * @param tracker
     * @param sharedState
     * @param localState
     */
    public void update(ContextManager tracker, SharedState sharedState) {
    	// Find the complete set of fields that needs to be updated
    	Set<String> allSharedFields = new HashSet<String>();
        for(Entity entity: activeEntities)
        	allSharedFields.addAll(entity.getSharedFieldNames());
        // Update one field at a time
        fieldLoop: for(String field: allSharedFields) {
        	for(Entity entity: activeEntities)
        		if(entity.update(field,tracker,sharedState)) continue fieldLoop;
        	sharedState.updateFieldIfNotAssigned(field,tracker);
        }
    }

    public List<Entity> getEntities() {
        return activeEntities;
    }

}
