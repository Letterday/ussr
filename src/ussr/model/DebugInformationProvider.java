package ussr.model;

public interface DebugInformationProvider {

    public void displayInformation();
    
    public void putStateInformation(String key, Object value);
    
    public void addNotification(String text);

    DebugInformationProvider DEFAULT = new DebugInformationProvider() {
        @Override public void addNotification(String text) { System.out.println(text); }
        @Override public void displayInformation() { }
        @Override public void putStateInformation(String key, Object value) { }
    };
    
}
