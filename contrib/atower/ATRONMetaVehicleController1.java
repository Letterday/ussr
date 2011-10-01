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
public class ATRONMetaVehicleController1 extends ATRONController {
	
    /**
     * Constants for communication
     */
    private static final byte MSG_OBJECT_NEARBY = 1;
    private static final byte MSG_OBJECT_GONE = 2;
    private static final byte MSG_SLOW = 3;
    private static final byte MSG_STOP = 4;
    /**
     * Seconds to wait while going in reverse
     */
    private static final float WAIT_TIME = 4;

    /**
     * Messages used for communication
     */
    private static final byte[] MESSAGE_OBJECT_NEARBY = new byte[] { MSG_OBJECT_NEARBY };
    private static final byte[] MESSAGE_OBJECT_GONE = new byte[] { MSG_OBJECT_GONE };
    private static final byte[] MESSAGE_SLOW = new byte[] { MSG_SLOW };
    private static final byte[] MESSAGE_STOP = new byte[] { MSG_STOP };
    /**
     * Direction the wheels are rotation (-1 = forwards)
     */
    private float dir = -1;
    /**
     * The speed at which the wheels are rotating
     */
    private float speed = 1;
    /**
     * Boolean indicating if the module is currently performing an evasion behavior
     */
    private boolean doingEvasion = false;
    /**
     * Time at which reversing started (to keep track of when it should stop, non-zero means this module initiated the behavior)
     */
    private float startReverseTime = 0;
    /**
     * Connector used for obstacle detection (-1 if none)
     */
    private int obstacleDetector = -1;
    /**
     * Connector used by searcher to connect to waiter (-1 if none)
     */
    private int searcherConnector = -1;
    /**
     * Connector used by waiter to connect to searcher (-1 if none)
     */
    private int waiterConnector = -1;
    /**
     * Full stop: used after initial connection had been made
     */
    private boolean stop = false;
    /**
     * Slow: used when another vehicle has been detected
     */
    private boolean slow = false;
    /**
     * Overall behavior: searcher module?
     */
    private boolean searcher = false;
    /**
     * Name, used to identify the module
     */
    private String name;
    /**
     * @see ussr.model.ControllerImpl#activate()
     */

    /**
     * Main method for each module
     */
    public void activate() {
        // Setup, initialization
    	setup();
        name = module.getProperty("name");
        if(name.contains("_id0.")) searcher = true;
        if(name.contains("right_forward_wheel")) {
            System.out.println("Proximity sensor activated");
            module.getSensors().get(4).setSensitivity(0.5f);
            obstacleDetector = 4;
            searcherConnector = 4;
        }
        else if(name.contains("centerForward")) {
            System.out.println("Proximity sensor activated");
            module.getSensors().get(1).setSensitivity(0.5f);
            obstacleDetector = 1;
        }
        else if(name.contains("left_backward_wheel")) {
            waiterConnector = 4;
        }
        // Continuous behavior
        while(true) {
            if(searcher) {
                if(stop) {
                    centerStop();
                } else {
                    if(slow) {
                        if(searcherConnector!=-1 && this.isConnected(searcherConnector)) stopAndContinue();
                        if(searcherConnector!=-1) this.connect(searcherConnector);
                        speed = 0.1f;
                    } else {
                        speed = 1f;
                    }
                    // Driving
                    if(doingEvasion) {
                        if(name.contains("wheel")) {
                            if(name.contains("right")) rotateContinuous(speed*dir);
                            if(name.contains("left")) rotateContinuous(speed*dir);
                        }
                    } else { 
                        if(name.contains("wheel")) {
                            if(name.contains("right")) rotateContinuous(speed*-dir);
                            if(name.contains("left")) rotateContinuous(speed*dir);
                        }
                    }
                    // Control
                    if(doingEvasion && startReverseTime>0) {
                        if(module.getSimulation().getTime()>startReverseTime+WAIT_TIME) stopEvasion();
                    }
                    if(!slow && !doingEvasion && obstacleDetector!=-1) {
                        if(isObjectNearby(obstacleDetector)) startEvasion();
                        if(this.isOtherConnectorNearby(obstacleDetector)) slowAndConnect();
                    }
                }
            } else {
                if(stop && waiterConnector!=-1) {
                    this.connect(waiterConnector);
                }
            }
            yield();
        }
    }

    /**
     * Stop the searcher, continue connecting
     */
    private void stopAndContinue() {
        System.out.println("Initial connection made, continuing");
        stop = true;
        for(int k=0; k<8; k++) super.sendMessage(MESSAGE_STOP, (byte)MESSAGE_STOP.length, (byte)k);
    }
    
    /**
     * Slow the searcher, trying to connect 
     */
    private void slowAndConnect() {
        System.out.println("Detected another vehicle");
        slow = true;
        for(int k=0; k<8; k++) super.sendMessage(MESSAGE_SLOW, (byte)MESSAGE_SLOW.length, (byte)k);
    }
    
    /**
     * Stop the evasion behavior (going forwards again)
     */
    private void stopEvasion() {
        System.out.println("stopping evasion");
        doingEvasion = false;
        startReverseTime = 0;
        for(int k=0; k<8; k++) super.sendMessage(MESSAGE_OBJECT_GONE, (byte)MESSAGE_OBJECT_GONE.length, (byte)k);
    }

    /**
     * Start the evasion behavior
     */
    private void startEvasion() {
        System.out.println("there is an object nearby");
        for(int k=0; k<8; k++) super.sendMessage(MESSAGE_OBJECT_NEARBY, (byte)MESSAGE_OBJECT_NEARBY.length, (byte)k);
        doingEvasion = true;
        startReverseTime = module.getSimulation().getTime();
    }

    /**
     * Handler invoked when a message arrives, directly reverses direction depending on module identity or
     * resets when done reversing
     */
    @Override
    public void handleMessage(byte[] message, int messageSize, int channel) {
        if(message.length>0) {
            boolean repeat = false; 
            if(searcher) {
                if(message[0]==MSG_OBJECT_NEARBY && !doingEvasion) {
                    doingEvasion = true; repeat = true;
                } else if(message[0]==MSG_OBJECT_GONE && doingEvasion) {
                    doingEvasion = false; repeat = true;
                } if(message[0]==MSG_SLOW) {
                    slow = true; repeat = true;
                } 
            } 
            if(message[0]==MSG_STOP && !stop) {
                stop = true; repeat = true;
            }
            if(repeat) {
                for(byte b=0; b<8; b++)
                    if(b!=channel) this.sendMessage(message, (byte)messageSize, b);
            }
        }
    }
    
}
