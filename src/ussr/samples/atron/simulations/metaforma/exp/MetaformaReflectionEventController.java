package ussr.samples.atron.simulations.metaforma.exp;
//package ussr.samples.atron.simulations.metaforma.lib;
//
//
//import ussr.network.EventConnection;
//import ussr.network.ReflectionConnection;
//import ussr.samples.ReflectionEventController;
//import ussr.samples.ReflectionEventHelper;
//import ussr.samples.atron.simulations.metaforma.lib.MetaformaController;
//
//public abstract class MetaformaReflectionEventController extends MetaformaController implements ReflectionEventController {
//    private ReflectionConnection rcConnection;
//    protected EventConnection eventConnection;
//    private boolean isActive = false;
//
//    
//    public void activate() {
//    	super.setup();
//    	info = this.getModule().getDebugInformationProvider();
//    	
//        ReflectionEventHelper.initializeAndActivate(this);
//    }
//    
//    public void handleMessage(byte[] message, int messageSize, int channel) {
//        if(!isActive) return;
//    	if(eventConnection.isReady()) {
//    		eventConnection.sendEvent("handleMessage", new Object[]{message, messageSize, channel});
//    	}
//    	else {
//    		System.err.println(getName()+": Event connection not ready, throw away package..");
//    	}
//    }
//    public void setEventConnection(EventConnection eventConnection) {
//        this.eventConnection = eventConnection;
//    }
//    public void setRcConnection(ReflectionConnection rcConnection) {
//        this.rcConnection = rcConnection;
//    }
//
//    public void setActive() {
//        this.isActive = true;
//    }
//}
