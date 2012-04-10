package ussr.model.debugging;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ussr.model.Module;
import ussr.physics.PhysicsFactory.DebugProviderFactory;

/**
 * A debug information provider that provides a continuously updating per-module information window
 * @author ups
 */
public class SimpleWindowedInformationProvider extends ConsoleInformationProvider {

    private JTextArea text;
    private JFrame frame;
    
    /**
     * @see ConsoleInformationProvider
     */
    public SimpleWindowedInformationProvider(Module module, boolean verbose) {
        super(module,verbose);
    }

    /**
     * Show/create window if not visible, then display textual information
     * @see DebugInformationProvider#displayInformation()
     */
    @Override public synchronized void displayInformation() {
        if(frame==null) createWindow();
        if(!frame.isVisible()) frame.setVisible(true);
        text.setText(super.getInformation());
    }
    
    /**
     * Override hook to display information in window if already visible
     */
    @Override protected void notificationHook() {
        if(frame!=null && frame.isVisible()) displayInformation();
    }
    
    /**
     * Create window
     */
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

    /**
     * @see ConsoleInformationProvider#getFactory(boolean)
     */
    public static DebugProviderFactory getFactory(final boolean verbose) {
        return new DebugProviderFactory() {
            @Override
            public DebugInformationProvider getDebugProvider(Module module) {
                return new SimpleWindowedInformationProvider(module,verbose);
            }
        };
    }

    
}
