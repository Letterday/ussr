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
import ussr.samples.atron.simulations.wouter.ATRONMetaModuleController;
import ussr.samples.atron.simulations.wouter.ATRONPacketController;

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
        out.append("Debug information for module "+name+"\n");
        out.append("state: ");
        for(Map.Entry<String,Object> entry: stateInformation.entrySet())
            out.append("["+entry.getKey()+"="+entry.getValue().toString()+"] ");
        out.append("\n");
        
        out.append("angle: " + ((ATRONPacketController)module.getController()).getAngle());
        
        out.append("\n");
        
        if(module.getController() instanceof ControllerInformationProvider)
            out.append(((ControllerInformationProvider)module.getController()).getModuleInformation());
        else
            getModuleInformation(out);
        out.append("Log information:\n");
        
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
        
        out.append(" state: ");
        out.append(((ATRONPacketController)module.getController()).getState());
        
        out.append("\n");
        
        out.append(" colors: ");
        out.append(((ATRONPacketController)module.getController()).getColors()[0].toString());
        out.append("  ");
        out.append(((ATRONPacketController)module.getController()).getColors()[1].toString());
        
        
        out.append("\n");
        
        out.append(" neighbors: ");
        
        
        
        out.append(((ATRONPacketController)module.getController()).getNeighborsString());
         
         out.append("\n");
        
         out.append("------------------------------------\n"); 
         out.append("\n");
         
         /*
          *   out.append(" state: ");
       out.append(((ATRONMetaModuleController)module.getController()).bus.getState());
        
        out.append("\n");
        
        out.append(" neighbors: ");
        out.append(((ATRONMetaModuleController)module.getController()).bus.con.getNeighbors());
         
         out.append("\n");
        
        out.append(((ATRONMetaModuleController)module.getController()).isRotating() ? " busy on rotating\n" : "");
        out.append(((ATRONMetaModuleController)module.getController()).bus.con.isConnecting() ? " busy on connecting " + ((ATRONMetaModuleController)module.getController()).bus.con.getConnecting() + "\n" : "");
        out.append(((ATRONMetaModuleController)module.getController()).bus.con.isDisconnecting() ? " busy on disconnecting" + ((ATRONMetaModuleController)module.getController()).bus.con.getDisconnecting() + "\n" : "");
        out.append("------------------------------------\n"); 
         out.append("\n");
          * */
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
