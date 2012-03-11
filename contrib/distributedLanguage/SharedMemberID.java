package distributedLanguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedMemberID {
    private List<Integer> id;
    
    public SharedMemberID(List<Integer> data) {
    	verifyExistence(data);
        this.id = data;
    }

	public SharedMemberID(String ensembleName, String memberName) {
    	id = new ArrayList<Integer>();
    	id.add(getEnsembleID(ensembleName));
    	id.add(getMemberName(ensembleName,memberName));
    }
    
	public List<Integer> asData() {
        return id;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SharedMemberID other = (SharedMemberID) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    // Member name to integer id mappings
    private static Map<String,Integer> ensembleName2id = new HashMap<String,Integer>();
    private static Map<String,Integer> memberName2id = new HashMap<String,Integer>();
    private static Map<Integer,String> id2ensembleName = new HashMap<Integer,String>();
    private static Map<Integer,String> id2memberName = new HashMap<Integer,String>();
    private static int nextEnsembleID = 1;
    private static Map<String,Integer> nextMemberID = new HashMap<String,Integer>();
    
    private static synchronized void verifyExistence(List<Integer> data) {
    	if(id2ensembleName.containsKey(data.get(0)) && id2memberName.containsKey(data.get(1))) return;
    	throw new Error("Key not found");
	}

    private static synchronized int getMemberName(String ensembleName, String memberName) {
    	Integer i = memberName2id.get(ensembleName+"."+memberName);
    	if(i==null) {
    		i = nextMemberID.get(ensembleName);
    		if(i==null) {
    			nextMemberID.put(ensembleName, 1);
    			i = 1;
    		}
    		memberName2id.put(ensembleName+"."+memberName, i);
    	}
    	return i;
	}

	private static synchronized int getEnsembleID(String ensembleName) {
    	Integer i = ensembleName2id.get(ensembleName);
    	if(i==null) {
    		i = nextEnsembleID++;
    		ensembleName2id.put(ensembleName, i);
    	}
    	return i;
	}


}
