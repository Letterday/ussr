package distributedLanguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SharedState {
	
    private class SharedData {
    	List<Integer> data;
    	boolean isLocallyAssigned;
    }

    private Map<SharedMemberID,SharedData> shared = new HashMap<SharedMemberID,SharedData>();
    private Map<String,List<Integer>> local = new HashMap<String,List<Integer>>();
    private Map<String,SharedMemberID> fieldName2id = new HashMap<String,SharedMemberID>(); // note: memory leak
    
    /**
     * Remove any state not described by the given set of entities
     * @param all
     */
    public void cleanup(List<Entity> all) {
    	// Compute active fields
        Set<SharedMemberID> allSharedIDs = new HashSet<SharedMemberID>();
        Set<String> allLocalIDs = new HashSet<String>();
        for(Entity e: all) {
    		List<String> fields = e.getFieldNames();
    		for(String f: fields) {
    			if(e instanceof RoCoEnsemble)
    				allSharedIDs.add(new SharedMemberID(e.getName(),f));
    			else
    				allLocalIDs.add(mkLocalID(e.getName(),f));
    		}
        }
        // Remove others
        for(SharedMemberID s: shared.keySet()) {
        	if(!allSharedIDs.contains(s)) shared.remove(s);
        }
        for(String s: local.keySet()) {
        	if(!allLocalIDs.contains(s)) local.remove(s);
        }
    }

    private static String mkLocalID(String name, String f) {
    	return name+"."+f;
	}

	public List<SharedMemberID> getSharedMemberIDs() {
		return new ArrayList<SharedMemberID>(shared.keySet());
    }

    public List<Integer> getSerializedData(SharedMemberID id) {
    	List<Integer> data = shared.get(id).data;
    	if(data==null) throw new Error("Unknown member id");
    	return data;
    }

	public void updateFieldIfNotAssigned(String field, ContextManager tracker) {
		SharedMemberID id = fieldName2id.get(field);
		if(id==null) throw new Error("No field def'n");
		SharedData data = shared.get(id);
		if(data==null) throw new Error("No data for field");
		if(!data.isLocallyAssigned) {
			List<Integer> maybe = tracker.getSharedMemberData(id);
			if(maybe!=null) data.data = maybe;
		}
		
	}
    
}
