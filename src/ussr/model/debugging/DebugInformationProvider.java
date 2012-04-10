package ussr.model.debugging;

/**
 * An object that provides debug information to the user, can be called from controller
 * code to provide additional information in addition to what is automatically provided.
 * @author ups
 */
public interface DebugInformationProvider {

    /**
     * Display the available debug information to the user
     */
    public void displayInformation();
    
    /**
     * Add state information
     * @param key identifies the name of the state
     * @param value the value of the state
     */
    public void putStateInformation(String key, Object value);
    
    /**
     * Add notification information
     * @param text the information to add
     */
    public void addNotification(String text);

    /**
     * A default information provider that simply prints notification to standard out
     */
    DebugInformationProvider DEFAULT = new DebugInformationProvider() {
        @Override public void addNotification(String text) { System.out.println(text); }
        @Override public void displayInformation() { }
        @Override public void putStateInformation(String key, Object value) { }
    };
    
}
