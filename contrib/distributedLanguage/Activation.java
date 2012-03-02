package distributedLanguage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        for(Entity entity: activeEntities)
            entity.runBehaviors(sharedState);
    }

    /**
     * Verify that all active roles and ensembles satisfy their requirements
     * @param context
     * @param sharedState
     * @param localState
     */
    public void verifyRequirements(Context context, SharedState sharedState) {
        if(!activeEntities.get(0).verifyRequirements(context)) {
            activeEntities.set(0,program.findMatchingRole(context,sharedState));
        }
        Iterator<Entity> entities = activeEntities.iterator();
        while(entities.hasNext()) {
            Entity entity = entities.next();
            if(!entity.verifyRequirements(context)) entities.remove();
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
            if(entity.verifyRequirements(context) && !activeEntities.contains(entity))
                activeEntities.add(entity);
        }
    }

    /**
     * Run update behaviors in all entities
     * @param tracker
     * @param sharedState
     * @param localState
     */
    public void update(ContextManager tracker, SharedState sharedState) {
        for(Entity entity: activeEntities)
            entity.update(tracker,sharedState);
    }

    public List<Entity> getEntities() {
        return activeEntities;
    }

}
