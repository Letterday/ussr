package distributedLanguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import distributedLanguage.Message.Heartbeat;
import distributedLanguage.Message.Subscriber;

public class ModuleController {

    /**
     * The activation = roles and ensembles that are active for this module
     */
    private Activation activation;
    /**
     * Shared state and program points of shared behaviors
     */
    private SharedState sharedState = new SharedState();
    /**
     * The program which this controller is running
     */
    private RoCoProgram program;
    /**
     * Context tracker for this module (knows about neighbors and roles)
     */
    private ContextManager tracker = new ContextManager(this);
    /**
     * Subscribes notified when incoming packets arrive
     */
    private List<Subscriber> subscribers = new ArrayList<Subscriber>();
    /**
     * ID of local module
     */
    private ModuleID id = new ModuleID();
    
    /**
     * New controller running the given program
     */
    public ModuleController(RoCoProgram program) {
        this.program = program;
        activation = new Activation(program);
    }
    
    /**
     * Activate the controller for a single step
     */
    public void activate(Time currentTime) {
        Context context = tracker.computeContextInformation(currentTime);
        activation.verifyRequirements(context,sharedState);
        activation.addNewEntities(context,sharedState);
        activation.update(tracker,sharedState); 
        activation.runBehaviors(sharedState);
        sendHeartBeat();
        tracker.sendSharedState(sharedState);
    }

    private void sendHeartBeat() {
        List<Entity> active = activation.getEntities();
        List<Integer> ids = new ArrayList<Integer>();
        for(Entity e: active) ids.add(e.getIntegerID());
        sendMessage(new Message.Heartbeat(id,ids));
    }

    public void sendMessage(Message message) {
        byte[] bytes = message.serialize();
        throw new Error("Method not implemented");
    }

    /**
     * Add subscriber to be notified when packets arrive
     * @param subscriber
     */
    public void addMessageSubscriber(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    public RoCoProgram getProgram() {
        return program;
    }

    public ModuleID getID() {
        return id;
    }

}
