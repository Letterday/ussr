/**
 * Unified Simulator for Self-Reconfigurable Robots (USSR)
 * (C) University of Southern Denmark 2008
 * This software is distributed under the BSD open-source license.
 * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
 */
package ussr.samples.atron.simulations;

import java.awt.Color;
import java.util.List;

import com.jme.math.Matrix3f;
import com.jmex.physics.DynamicPhysicsNode;

import ussr.comm.Packet;
import ussr.comm.Receiver;
import ussr.comm.Transmitter;
import ussr.model.Sensor;
import ussr.physics.jme.JMEModuleComponent;
import ussr.samples.GenericSimulation;
import ussr.samples.atron.ATRONController;

/**
 * A simple controller for an ATRON car that reports data from the proximity sensors
 * 
 * @author Modular Robots @ MMMI
 *
 */
public class ATRONCarController1 extends ATRONController {
	
    /**
     * @see ussr.model.ControllerImpl#activate()
     */
    public void activate() {
    	setup(); 
    	this.delay(1000); 
        float lastProx = Float.NEGATIVE_INFINITY; 
        
     // Basic control: first time we enter the loop start rotating
        String name = module.getProperty("name");
        if(name.contains("Wheel1")) rotateContinuous(-1);
        if(name.contains("Wheel2")) rotateContinuous(1);
        
        while(true) {
        	 
        

            // Print out proximity information
            float max_prox = Float.NEGATIVE_INFINITY;
            for(Sensor s: module.getSensors()) {
                if(s.getName().startsWith("Proximity")) {
                    max_prox = Math.max(max_prox, s.readValue());
                }
            }
            if(name.contains("driver")&&Math.abs(lastProx-max_prox)>0.1) {
                System.out.println(max_prox);
                lastProx = max_prox; 
            }

            // Always call yield sometimes
        	yield();
        }
    }
}
