package ussr.model.debugging;

import ussr.samples.atron.simulations.metaforma.gen.Grouping;

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
