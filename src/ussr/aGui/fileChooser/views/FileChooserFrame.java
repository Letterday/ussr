package ussr.aGui.fileChooser.views;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import ussr.aGui.GuiFrames;
import ussr.aGui.fileChooser.FileFilter;
import ussr.aGui.fileChooser.controllers.FileChooserControllerInter;


/**
 * Defines visual appearance of the file chooser in several dialog forms: Open, Save and Save As.
 * Limits the file choosing to extensions specified in fileExtensions ArrayList.   
 * @author Konstantinas
 */
@SuppressWarnings("serial")
public abstract class FileChooserFrame extends GuiFrames implements FileChooserFrameInter {

	
	/**
	 * Used as flag to indicate that file chooser should open build in default directory.
	 */
	private String defaultDirectory = "";
	
	/**
	 * The file chooser appearance, which is integrated into the frame.
	 */
	protected javax.swing.JFileChooser jFileChooser;

	/**
	 * Extensions of the files, which will be available to filter out by the file chooser
	 */
	protected ArrayList<String> fileExtensions;

	/**
	 * Controller for file extensions.
	 */
	protected FileChooserControllerInter fileChooserController;
	
	
	protected Map<String, String> fileDescriptionsAndExtensions;
	
	/**
	 * @param fileExtensions
	 * @param fileChooserController
	 * @param defaultDirectory, default directory for file chooser to open.
	 */
	public FileChooserFrame(ArrayList<String> fileExtensions, FileChooserControllerInter fileChooserController){
		this.fileExtensions= fileExtensions;
		this.fileChooserController= fileChooserController;
		initComponents();
	}
	
	
	public FileChooserFrame(Map<String, String> fileDescriptionsAndExtensions, FileChooserControllerInter fileChooserController){
		this.fileDescriptionsAndExtensions= fileDescriptionsAndExtensions;
		this.fileChooserController= fileChooserController;
		initComponents();
	}
	
	/** 
	 * This method is called from within the constructor to initialize the form(frame) of the file chooser.
	 */	
	public void initComponents() {
       if (defaultDirectory.isEmpty()){
		this.jFileChooser = new javax.swing.JFileChooser();
       }else{
    	   this.jFileChooser = new javax.swing.JFileChooser(defaultDirectory);    	   
       }
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);		
		getContentPane().setLayout(new java.awt.FlowLayout());	
		this.jFileChooser.setAcceptAllFileFilterUsed(false);	
		getContentPane().add(this.jFileChooser);// MAC HAS PROBLEMS WITH THAT		
		pack();
		changeToLookAndFeel(this);// for all platforms
		setSize(580,450);//THINK MORE HERE
	}	

	/**
	 * Sets the file extensions for file chooser to filter out (file filters).
	 * @param fileExtensions, the ArrayList containing the file extensions in the format (for instance: ".xml").
	 */
	public void setFilesToFilter(ArrayList<String> fileExtensions){		
		for (int index=0; index<fileExtensions.size(); index++){
			if (fileExtensions.get(index).indexOf(".")==0){// if format has "." in the beginning. For example ".xml"
				jFileChooser.setFileFilter(new FileFilter (fileExtensions.get(index)));			
			}else{
				throw new Error("Wrong format of file extension. Point is missing in the beginning. It should be for instance: .xml");
			}
		}
	}
	
	public void setFilesToFilterOutWithDescription(){
		Iterator<String> keyIterator = fileDescriptionsAndExtensions.keySet().iterator();
		Iterator<String> valueIterator = fileDescriptionsAndExtensions.values().iterator();
		while(keyIterator.hasNext()){
			jFileChooser.setFileFilter(new FileFilter (keyIterator.next(),valueIterator.next()));			
		}
	}
	
	/**
	 * Sets specific default directory to open.
	 * @param defaultDirectory, the directory for file chooser to open as default.
	 */
	public void setDefaultDirectory(String defaultDirectory) {
		this.defaultDirectory = defaultDirectory;
	}
	
}
