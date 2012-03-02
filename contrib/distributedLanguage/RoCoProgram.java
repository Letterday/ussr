package distributedLanguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RoCoProgram {

    public static final int DEFAULT_HEARTBEAT_TIMEOUT = 1000;
    
    private int heartbeatTimeout = DEFAULT_HEARTBEAT_TIMEOUT;
    
    private Map<SharedMemberID,Entity> memberID2entity = new HashMap<SharedMemberID,Entity>();
    private Map<Integer,Entity> intID2entity = new HashMap<Integer,Entity>();
    private List<Entity> allEntities = this.getAllEntities();
    private List<Entity> allSecondaryEntities;
    
    protected abstract List<Entity> getAllEntities();
    
    public RoCoRole findMatchingRole(Context context, SharedState sharedState) {
        Entity candidate = RoCoRole.NONE;
        for(Entity entity: allEntities) {
            if(entity instanceof RoCoRole && entity.isPrimaryRole() && entity.verifyRequirements(context))
                if(entity.specializes(candidate)) candidate = entity;
        }
        return (RoCoRole)candidate;
    }

    public synchronized List<Entity> getAllSecondaryEntities() {
        if(allSecondaryEntities==null) {
            allSecondaryEntities = new ArrayList<Entity>();
            for(Entity entity: allEntities)
                if(!entity.isPrimaryRole()) allSecondaryEntities.add(entity);
        }
        return allSecondaryEntities;
    }

    public Entity id2entity(Integer id) {
        Entity e = intID2entity.get(id);
        if(e==null) throw new Error("Illegal entity int id: "+id);
        return e;
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public int getSharedMemberSize(SharedMemberID member) {
        Entity entity = memberID2entity.get(member);
        if(entity==null) throw new Error("Illegal member ID");
        return entity.sizeof(member);
    }

}
