package distributedLanguage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedState {

    private Map<SharedMemberID,SharedData> shared = new HashMap<SharedMemberID,SharedData>();
    private Map<String,List<Integer>> local = new HashMap<String,List<Integer>>();
    
    /**
     * Remove any state not described by the given set of entities
     * @param all
     */
    public void cleanup(List<Entity> all) {
        throw new Error("foo");
    }

    public List<SharedMemberID> getSharedMemberIDs() {
        // TODO Auto-generated method stub
        // return null;
        throw new Error("Method not implemented");
    }

    public List<Integer> getSerializedData(SharedMemberID id) {
        // TODO Auto-generated method stub
        // return null;
        throw new Error("Method not implemented");
    }

    private class SharedData {
        private List<Integer> data;
        private boolean isLocallyAssigned;
    }
    
}
