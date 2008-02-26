/**
 * 
 */
package ussr.samples.atron.natives;

import java.util.ArrayList;

import ussr.model.Controller;
import ussr.robotbuildingblocks.ModulePosition;
import ussr.robotbuildingblocks.Robot;
import ussr.robotbuildingblocks.VectorDescription;
import ussr.samples.atron.ATRON;
import ussr.samples.atron.ATRONController;
import ussr.samples.atron.ATRONLatticeSimulation;

/**
 * @author ups
 *
 * TODO Write a nice and user-friendly comment here
 * 
 */
public class ATRONDcdArmSimulation extends ATRONLatticeSimulation {

    public static void main(String argv[]) {
        new ATRONDcdArmSimulation().main();
    }
    
    protected Robot getRobot() {
        return new ATRON() {
            public Controller createController() {
                return new ATRONNativeController("dcdController");
            }
        };
    }

    protected ArrayList<ModulePosition> buildRobot() {
        ArrayList<ModulePosition> positions = buildAsLattice(100,3,1,5);
//        ArrayList<ModulePosition> positions = buildAsLattice(10,2,1,3);
        positions.add(new ModulePosition("shoulder", new VectorDescription(3*ATRON.UNIT,1*ATRON.UNIT,2*ATRON.UNIT), ATRON.ROTATION_UD));
        positions.add(new ModulePosition("elbow", new VectorDescription(2*ATRON.UNIT,2*ATRON.UNIT,2*ATRON.UNIT), ATRON.ROTATION_EW));
        positions.add(new ModulePosition("hand", new VectorDescription(3*ATRON.UNIT,3*ATRON.UNIT,2*ATRON.UNIT), ATRON.ROTATION_UD));
        positions.add(new ModulePosition("finger99", new VectorDescription(4*ATRON.UNIT,4*ATRON.UNIT,2*ATRON.UNIT), ATRON.ROTATION_EW));
        return positions;
    }
    
}
