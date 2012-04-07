package ussr.model.debugging;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ussr.model.DebugInformationProvider;
import ussr.model.Module;
import ussr.physics.PhysicsFactory.DebugProviderFactory;

public class SimpleWindowedInformationProvider extends ConsoleInformationProvider {

    private JTextArea text;
    private JFrame frame;
    
    public SimpleWindowedInformationProvider(Module module, boolean verbose) {
        super(module,verbose);
    }

    @Override public synchronized void displayInformation() {
        if(frame==null) createWindow();
        if(!frame.isVisible()) frame.setVisible(true);
        text.setText(super.getInformation());
    }
    
    @Override protected void notificationHook() {
        if(frame!=null && frame.isVisible()) displayInformation();
    }
    
    private void createWindow() {
        frame = new JFrame("USSR debug");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        text = new JTextArea("(no information)");
        text.setColumns(80);
        // Content must be a panel
        JPanel gui = new JPanel();
        gui.add(text);
        //Create and set up the content pane.
        frame.setContentPane(gui);
        gui.setOpaque(true);
         //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static DebugProviderFactory getFactory(final boolean verbose) {
        return new DebugProviderFactory() {
            @Override
            public DebugInformationProvider getDebugProvider(Module module) {
                return new SimpleWindowedInformationProvider(module,verbose);
            }
        };
    }

    
}
