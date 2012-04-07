package ussr.model.debugging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ussr.model.Actuator;
import ussr.model.DebugInformationProvider;
import ussr.model.Module;
import ussr.model.Sensor;
import ussr.physics.PhysicsFactory.DebugProviderFactory;

public class ConsoleInformationProvider implements DebugInformationProvider {

    private Module module;
    private boolean verbose;
    private Map<String,Object> stateInformation = new HashMap<String,Object>();
    private List<String> allMessages = new ArrayList<String>();
    
    private ConsoleInformationProvider(Module module, boolean verbose) {
        this.module = module;
        this.verbose = verbose;
    }
    
    @Override
    public void addNotification(String text) {
        allMessages.add(text);
        if(verbose) System.out.println(text);
    }

    @Override
    public void displayInformation() {
        System.out.println("---------------------------");
        String name = module.getProperty("name");
        if(name==null) name = "?";
        System.out.println("Debug information for module "+name);
        System.out.print(" state: ");
        for(Map.Entry<String,Object> entry: stateInformation.entrySet())
            System.out.print("["+entry.getKey()+"="+entry.getValue().toString()+"] ");
        System.out.println();
        if(module.getController() instanceof ControllerInformationProvider)
            System.out.print(((ControllerInformationProvider)module.getController()).getModuleInformation());
        else
            printModuleInformation();
        System.out.println("Log information:");
        for(String line: allMessages) System.out.println(line);
        System.out.println("---------------------------");
    }

    private void printModuleInformation() {
        System.out.print(" actuators: ");
        for(Actuator actuator: module.getActuators())
            System.out.print(actuator.getEncoderValue()+' ');
        System.out.println();
        System.out.print(" sensors: ");
        for(Sensor sensor: module.getSensors())
            System.out.print(sensor.readValue()+' ');
        System.out.println();
    }

    @Override
    public void putStateInformation(String key, Object value) {
        stateInformation.put(key, value);
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
