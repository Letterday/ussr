package ussr.aGui.tabs.views;


/**
 * Supports definition of visual appearance(view in MVC pattern) of tabs, which are pluged-in main GUI window. 
 * @author Konstantinas
 */
public interface TabsInter {
	
	
	/**
	 * The directory for keeping jpg icons used in the tabs.
	 */
	public final String DIRECTORY_ICONS = "resources/mainFrame/icons/tabs/jpg/";
	
	/**
	 * The names of the icons used in tabs
	 */
	public final String DELETE = "delete.jpg", MOVE = "move.jpg", COLOUR_CONNECTORS = "colourConnectors.jpg",
	                    ATTENTION = "attention.jpg", ERROR = "error.jpg", INFORMATION ="information.jpg", 
	                    VISUALIZER = "visualizer.jpg", CONSOLE = "console.jpg", OPPOSITE = "opposite.jpg";
	
	/**
     * Getter method common for all tabs and is used by GUI during addition of new tab.
     */
	public javax.swing.JComponent getJComponent();
	
	/**
	 * Getter method common for all tabs and is used by GUI during addition of new tab.
	 * @return tabTitle, the title of the tab.
	 */
	public String getTabTitle();
	
	
	public boolean isFirstTabbedPane();
	
	public String getImageIconDirectory();


}
