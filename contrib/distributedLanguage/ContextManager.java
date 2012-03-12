package distributedLanguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextManager implements Message.Subscriber {

    public class ContextInformation {
        private ModuleID id;
        private Time stamp;
        private List<Entity> activationState;
        private Map<SharedMemberID,List<Integer>> sharedState = new HashMap<SharedMemberID,List<Integer>>();
        
        public ContextInformation(ModuleID id) {
            this.id = id;
        }

        public void updateActivationState(Time arrivalTime, List<Entity> activationState) {
            this.stamp = arrivalTime;
            this.activationState = activationState;
        }

        public Time getUpdateTime() {
            return stamp;
        }

        public ModuleID getID() {
            return id;
        }

        public void updateSharedState(Time arrivalTime, SharedMemberID member, List<Integer> value) {
            sharedState.put(member, value);
        }
        
        public List<Integer> get(SharedMemberID id) { return sharedState.get(id); }

    }

    private ModuleController controller;
    private Map<ModuleID,ContextInformation> context = new HashMap<ModuleID,ContextInformation>();
    
    public ContextManager(ModuleController moduleController) {
        this.controller = moduleController;
        controller.addMessageSubscriber(this);
    }

    /**
     * Update context information wrt current time, and return encapsulated version
     * @param currentTime
     * @return
     */
    public Context computeContextInformation(Time currentTime) {
        flushCache(currentTime);
        return new Context(context);
    }

    /**
     * Flush elements that are too old (as defined by heartbeat timeout)
     * @param currentTime
     */
    private void flushCache(Time currentTime) {
        for(ContextInformation info: context.values()) {
            if(currentTime.difference(info.getUpdateTime())>controller.getProgram().getHeartbeatTimeout())
                context.remove(info.getID());
        }
    }

    /**
     * Send shared state to all neighbors
     * @param sharedState
     */
    public void sendSharedState(SharedState sharedState) {
        Message.SharedState message = new Message.SharedState(controller.getID());
        for(SharedMemberID id: sharedState.getSharedMemberIDs()) {
            List<Integer> data = sharedState.getSerializedData(id);
            message.addStateId(id);
            message.addData(data);
        }
        controller.sendMessage(message);
    }

    /**
     * Process incoming messages
     */
    @Override
    public void messageArrived(Message message, Time arrivalTime) {
        ModuleID id = message.getSenderID();
        if(message instanceof Message.Heartbeat) {
            Message.Heartbeat m = (Message.Heartbeat)message;
            List<Integer> activation = m.getActivation();
            List<Entity> activationState = new ArrayList<Entity>(); 
            for(Integer raw: activation)
                activationState.add(controller.getProgram().id2entity(raw));
            checkCreateModuleEntry(id);
            context.get(id).updateActivationState(arrivalTime,activationState);
        } else if(message instanceof Message.SharedState) {
            Message.SharedState m = (Message.SharedState)message;
            int length = m.getSharedStateCount();
            checkCreateModuleEntry(id);
            m.resetPositionIterator();
            for(int i=0; i<length; i++) {
                SharedMemberID member = m.getStateID();
                int size = controller.getProgram().getSharedMemberSize(member);
                List<Integer> data = m.getData(size);
                context.get(id).updateSharedState(arrivalTime,member,data);
            }
        } 
    }

    private void checkCreateModuleEntry(ModuleID id) {
        if(context.get(id)==null) context.put(id, new ContextInformation(id));
    }

	public List<Integer> getSharedMemberData(SharedMemberID id) {
		for(ContextInformation ci: context.values()) {
			if(ci.get(id)!=null) return ci.get(id);
		}
		return null;
	}

}
