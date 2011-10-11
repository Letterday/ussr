/**
 * Unified Simulator for Self-Reconfigurable Robots (USSR)
 * (C) University of Southern Denmark 2008
 * This software is distributed under the BSD open-source license.
 * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
 */
package atower;

import ussr.samples.atron.ATRONController;

/**
 * A controller for a two-wheeler ATRON robot
 * 
 * @author Modular Robots @ MMMI
 *
 */
public class ATRONSimpleVehicleController extends ATRONController {
	
    private static final byte[] MSG_STOP = new byte[] { 2 };
    
    private boolean stop = false;
    private boolean isConnected = false;
    private boolean isCar;
    private String name;
    
    @Override
    public void activate() {
        super.yield();
        name = module.getProperty("name");
        if(name.contains("driver")||name.contains("Wheel")) isCar = true;
        if(!isCar) return;
        while(true) {
            super.yield();
            if(name.contains("RightWheel")) {
                if(isConnected) ;
                else if(stop) {
                    rotateContinuous(0.1f);
                    tryConnect(4);
                }
                else if(super.isOtherConnectorNearby(0)||super.isOtherConnectorNearby(4))
                    sendStop();
                else
                    rotateContinuous(1);
            }
            if(name.contains("LeftWheel")) {
                if(isConnected)
                    disconnect(4);
                else if(stop) {
                    rotateContinuous(-0.1f);
                    tryConnect(2);
                }
                else if(super.isOtherConnectorNearby(2)||super.isOtherConnectorNearby(6))
                    sendStop();
                else
                    rotateContinuous(-1);
            }
        }
    }

    public void sendStop() {
        stop = true;
        System.out.println("Triggering stop at "+name);
        super.sendMessageAll(MSG_STOP,MSG_STOP.length);
    }
    
    public void tryConnect(int c) {
        super.connect(c);
        if(super.isConnected(c)) {
            isConnected = true;
            System.out.println("Module "+name+" connected");
        }
    }
    
    @Override
    public void handleMessage(byte[] message, int messageSize, int channel) {
        if(isCar && message.length>0)
            if(message[0]==MSG_STOP[0]) {
                super.sendMessageExcept(message,messageSize,channel);
                stop = true;
                System.out.println("Module "+name+" stopped");
            }
    }

}
