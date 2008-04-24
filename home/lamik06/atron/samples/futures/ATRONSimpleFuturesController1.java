/**
 * Unified Simulator for Self-Reconfigurable Robots (USSR)
 * (C) University of Southern Denmark 2008
 * This software is distributed under the BSD open-source license.
 * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
 */
package atron.samples.futures;

import java.awt.Color;

import atron.delegate.ATRONDelegateAPI;
import atron.futures.ATRONFutures;
import atron.futures.ATRONFuturesConnectors;
import atron.futures.ATRONFuturesCenterMotor;
import atron.futures.ICommand;
import ussr.model.Module;
import ussr.samples.atron.ATRONController;

/**
 * A controller for a two-wheeler ATRON robot
 * 
 * @author Modular Robots @ MMMI
 *
 */
public class ATRONSimpleFuturesController1 extends ATRONController {
	
	int i = 0;

    public void activate() {
        yield();
        String name = module.getProperty("name");
        if(name=="RearRightWheel"){
        	System.out.println("test");
        	ATRONDelegateAPI atronDelegateAPI = new ATRONDelegateAPI(module);
        	System.out.println("Start pos. =" + atronDelegateAPI.getAngularPositionDegrees());
            ATRONFuturesCenterMotor f = atronDelegateAPI.rotateToDegreeInDegreesFutures(90);
            ATRONFuturesConnectors f1 = atronDelegateAPI.connectFuture(1);
            System.out.println("f + f1 running");
            ATRONFutures.wait(f1, f);
            System.out.println("f + f1 completed");
 
            // Demo count
            while(i < 30){
            	System.out.println("Demo count = " + i++);
            	yield();
            }

        }
        
            
        while(true){
        	yield();
        }
        
    }
    @Override
    public void handleMessage(byte[] message, int messageSize, int channel) {
    	// TODO Auto-generated method stub
    	super.handleMessage(message, messageSize, channel);
    }
}
