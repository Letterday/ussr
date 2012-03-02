package distributedLanguage;

public class ModuleID {
    private int id;
    public ModuleID() {
        id = getNextID();
    }
    public ModuleID(int id) {
        this.id = id;
    }
    public int get() {
        return id;
    }
    private static synchronized int getNextID() {
        return nextID++;
    }
    private static int nextID = 1;
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
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
        ModuleID other = (ModuleID) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
