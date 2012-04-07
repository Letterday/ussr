package ussr.model.debugging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import ussr.model.Actuator;
import ussr.model.DebugInformationProvider;
import ussr.model.Module;
import ussr.model.Sensor;
import ussr.physics.PhysicsFactory.DebugProviderFactory;

public class ConsoleInformationProvider extends Observable implements DebugInformationProvider {

    private Module module;
    private boolean verbose;
    private Map<String,Object> stateInformation = new HashMap<String,Object>();
    private List<String> allMessages = new ArrayList<String>();
    
    protected ConsoleInformationProvider(Module module, boolean verbose) {
        this.module = module;
        this.verbose = verbose;
    }
    
    protected void notificationHook() {
        super.notifyObservers();
    }
    
    @Override
    public void addNotification(String text) {
        allMessages.add(text);
        notificationHook();
        if(verbose) System.out.println(text);
    }

    @Override
    public void displayInformation() {
        System.out.println("---------------------------");
        System.out.print(getInformation());
    }

    public String getInformation() {
        StringBuffer out = new StringBuffer();
        String name = module.getProperty("name");
        if(name==null) name = "?";
        out.append("Debug information for module "+name+"\n");
        out.append("state: ");
        for(Map.Entry<String,Object> entry: stateInformation.entrySet())
            out.append("["+entry.getKey()+"="+entry.getValue().toString()+"] ");
        out.append("\n");
        if(module.getController() instanceof ControllerInformationProvider)
            out.append(((ControllerInformationProvider)module.getController()).getModuleInformation());
        else
            getModuleInformation(out);
        out.append("Log information:\n");
        for(String line: allMessages) out.append(line+"\n");
        return out.toString();
    }

    private void getModuleInformation(StringBuffer out) {
        out.append(" actuators: ");
        for(Actuator actuator: module.getActuators())
            out.append(actuator.getEncoderValue()+' ');
        out.append("\n");
        out.append(" sensors: ");
        for(Sensor sensor: module.getSensors())
            out.append(sensor.readValue()+' ');
        out.append("\n");
    }

    @Override
    public void putStateInformation(String key, Object value) {
        stateInformation.put(key, value);
        notificationHook();
    }

    public static DebugProviderFactory getFactory(final boolean verbose) {
        return new DebugProviderFactory() {
            @Override
            public DebugInformationProvider getDebugProvider(Module module) {
                return new ConsoleInformationProvider(module,verbose);
            }
        };
    }
    
}
