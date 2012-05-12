package ussr.model.debugging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import ussr.model.Actuator;
import ussr.model.Connector;
import ussr.model.Module;
import ussr.model.Sensor;
import ussr.physics.PhysicsFactory.DebugProviderFactory;

/**
 * A debug information provider that displays information on the console.
 * Inherits standard java.util.Observable behavior
 * @author ups
 */
public class ConsoleInformationProvider extends Observable implements DebugInformationProvider {

    protected Module module;
    private boolean verbose;
    private Map<String,Object> stateInformation = new HashMap<String,Object>();
    private List<String> allMessages = new ArrayList<String>();
    
    /**
     * Create information provider attached to given module
     * @param module to report debug information from
     * @param verbose if true all notification are immediately echoed on the console
     */
    protected ConsoleInformationProvider(Module module, boolean verbose) {
        this.module = module;
        this.verbose = verbose;
    }
    
    /**
     * Hook method called when debug information is updated, default behavior is to notify all observers
     */
    protected void notificationHook() {
        super.notifyObservers();
    }
    
    /**
     * @see DebugInformationProvider#addNotification(String)
     */
    @Override
    public void addNotification(String text) {
    	synchronized (allMessages) {
            allMessages.add(text);
            notificationHook();
            if(verbose) System.out.println(module.getProperty("name") + ":" + text);
		}

    }

    /**
     * Display information on console
     * @see DebugInformationProvider#displayInformation()
     */
    @Override
    public void displayInformation() {
        System.out.println("---------------------------");
        System.out.print(getInformation());
    }

    /**
     * Get a textual representation of the information contained within this provider
     * @return
     */
    public String getInformation() {
        StringBuffer out = new StringBuffer();
        String name = module.getProperty("name");
        if(name==null) name = "?";

        out.append("state: ");
        for(Map.Entry<String,Object> entry: stateInformation.entrySet())
            out.append("["+entry.getKey()+"="+entry.getValue().toString()+"] ");
        out.append("\n");
      
        
        if(module.getController() instanceof ControllerInformationProvider)
            out.append(((ControllerInformationProvider)module.getController()).getModuleInformation());
        else
            getModuleInformation(out);
        out.append("-------------------------------\nLog information:\n");
        
        synchronized (allMessages) {  
        	for(String line: allMessages) out.append(line+"\n");
        }
        return out.toString();
    }

    /**
     * Get generic module status information on actuators and sensors
     * @param out
     */
    private void getModuleInformation(StringBuffer out) {
        out.append(" actuators: ");
        for(Actuator actuator: module.getActuators())
            out.append(actuator.getEncoderValue()+ " ");
        out.append("\n");
        
        out.append(" sensors: ");
        for(Sensor sensor: module.getSensors())
            out.append(sensor.readValue()+" ");
        out.append("\n");
        
        out.append(" connectors: ");
        for(Connector con: module.getConnectors())
            out.append(con.isConnected()? "1 " : "0 ");
        out.append("\n");
        
       
    }

    /**
     * @see DebugInformationProvider#putStateInformation(String, Object)
     */
    @Override
    public void putStateInformation(String key, Object value) {
        stateInformation.put(key, value);
        notificationHook();
    }

    /**
     * Return factory instantiating ConsoleInformationProvider objects with the given verboseness level
     * @param verbose if true echo all notifications to console
     * @return the factory
     */
    public static DebugProviderFactory getFactory(final boolean verbose) {
        return new DebugProviderFactory() {
            @Override
            public DebugInformationProvider getDebugProvider(Module module) {
                return new ConsoleInformationProvider(module,verbose);
            }
        };
    }
    
}
