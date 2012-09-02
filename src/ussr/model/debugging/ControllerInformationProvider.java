package ussr.model.debugging;


/**
 * Controllers that implement this interface can provide additional on-demand information to debug
 * information providers. 
 * @author ups
 */
public interface ControllerInformationProvider {

    /**
     * Get debug information about this module
     * @return
     */
    public String getModuleInformation();



}
