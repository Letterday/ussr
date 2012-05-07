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
    
    public void assign(Map<String,Integer> localVariables, String name, Integer value) {
    	if(shared.containsKey(name) && local.containsKey(name)) throw new Error("Unresolved name precedence");
    	if(shared.containsKey(name)) {
    		if(!(shared.get(name).data.size()==1)) throw new Error("Bad data size");
    		shared.get(name).data.set(0,value);
    		shared.get(name).isLocallyAssigned = true;
    	} else if(local.containsKey(name)) {
    		local.get(name).set(0, value);
    	} else
    		localVariables.put(name, value);
    }

    public Integer get(Map<String,Integer> localVariables, String name) {
    	if(localVariables.containsKey(name)) return localVariables.get(name);
    	List<Integer> result;
    	if(local.containsKey(name)) result = local.get(name);
    	else if(shared.containsKey(name)) result = shared.get(name).data;
    	else throw new Error("Variable not found: "+name);
    	if(result.size()!=1) throw new Error("Illegal data size");
    	return result.get(0);
    }
    
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
