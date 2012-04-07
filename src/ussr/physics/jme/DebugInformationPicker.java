package ussr.physics.jme;

import ussr.physics.PhysicsSimulation;
import ussr.physics.jme.pickers.CustomizedPicker;

import com.jme.scene.Geometry;

public class DebugInformationPicker extends CustomizedPicker {
    public static void install(JMESimulation simulation) {
        simulation.setPicker(new DebugInformationPicker());
    }

    @Override
    protected void pickModuleComponent(JMEModuleComponent component) {
        component.getModel().getDebugInformationProvider().displayInformation();
    }

    @Override
    protected void pickTarget(Geometry target, JMESimulation jmeSimulation) {
        ;
    }

    public static void install(PhysicsSimulation simulation) {
        if(!(simulation instanceof JMESimulation)) {
            throw new Error("Unexpected implementation layer");
        }
        install((JMESimulation)simulation);
    }

}
