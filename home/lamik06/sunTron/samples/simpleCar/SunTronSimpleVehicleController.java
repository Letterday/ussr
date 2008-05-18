/**
 * Unified Simulator for Self-Reconfigurable Robots (USSR)
 * (C) University of Southern Denmark 2008
 * This software is distributed under the BSD open-source license.
 * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
 */
package sunTron.samples.simpleCar;

import sunTron.API.SunTronAPI;
import ussr.samples.atron.ATRONController;

/**
 * A controller for a two-wheeler ATRON robot
 * 
 * @author Modular Robots @ MMMI
 *
 */
public class SunTronSimpleVehicleController extends SunTronAPI {
	
    /**
     * @see ussr.model.ControllerImpl#activate()
     */
    public void activate() {
        yield();
        byte dir = 1;
        while(true) {
            String name = getName();
            if(name=="RearRightWheel") rotateContinuous(dir);
            if(name=="RearLeftWheel") rotateContinuous(-dir);
            yield();
        }
    }
}
