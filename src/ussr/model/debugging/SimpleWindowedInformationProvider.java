package ussr.model.debugging;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ussr.model.Module;
import ussr.physics.PhysicsFactory.DebugProviderFactory;
import ussr.samples.atron.simulations.metaforma.lib.MfController;

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
//    	if (module.getController() instanceof MetaformaController) {
//    		frame = new JFrame(((MetaformaController)module.getController()).getTitle());
//    	}
//    	else {
    		frame = new JFrame(module.getProperty("name"));
//    	}
//        The title is not updated dynamically :(
    		
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        text = new JTextArea("(no information)");
        JScrollPane spane = new JScrollPane(text);
        text.setColumns(65);
        text.setRows(40);

 
        // Content must be a panel
        JPanel gui = new JPanel();
        gui.add(spane);
        
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