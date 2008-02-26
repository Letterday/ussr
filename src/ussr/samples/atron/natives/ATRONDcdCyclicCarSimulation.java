package ussr.samples.atron.natives;

import java.util.ArrayList;

import ussr.model.Controller;
import ussr.robotbuildingblocks.ModulePosition;
import ussr.robotbuildingblocks.Robot;
import ussr.robotbuildingblocks.VectorDescription;
import ussr.samples.atron.ATRON;

public class ATRONDcdCyclicCarSimulation extends ATRONNativeCarSimulation {
    
    public static void main(String argv[]) {
        new ATRONDcdCyclicCarSimulation().main();
    }

    @Override
    protected Robot getRobot() {
        return new ATRON() {
            public Controller createController() {
                return new ATRONNativeController("dcdController");
            }
        };
    }
    
    static VectorDescription pos(float x, float y, float z) {
        final float Yoffset = 0.25f;
        return new VectorDescription(x*ATRON.UNIT, y*ATRON.UNIT-Yoffset, z*ATRON.UNIT);
    }
    
    protected ArrayList<ModulePosition> buildCar() {
        ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>();
        mPos.add(new ModulePosition("driver0", pos(0,0,0), ATRON.ROTATION_EW));
        mPos.add(new ModulePosition("axleOne", pos(1,-1,0), ATRON.ROTATION_UD));
        mPos.add(new ModulePosition("axleTwo", pos(-1,-1,0), ATRON.ROTATION_UD));
        mPos.add(new ModulePosition("wheel1", pos(-1,-2,1), ATRON.ROTATION_SN));
        mPos.add(new ModulePosition("wheel2", pos(-1,-2,-1), ATRON.ROTATION_NS));
        mPos.add(new ModulePosition("wheel3", pos(1,-2,1), ATRON.ROTATION_SN));
        mPos.add(new ModulePosition("wheel4", pos(1,-2,-1), ATRON.ROTATION_NS));
        mPos.add(new ModulePosition("spareTyre", pos(-1,0,1), ATRON.ROTATION_SN));
        return mPos;
    }

}
