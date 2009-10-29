package ussr.aGui.tabs.controllers;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;

import ussr.aGui.tabs.TabsInter;
import ussr.builder.SupportedModularRobots;
import ussr.builder.constructionTools.ATRONOperationsTemplate;
import ussr.builder.constructionTools.CKBotOperationsTemplate;
import ussr.builder.constructionTools.CommonOperationsTemplate;
import ussr.builder.constructionTools.ConstructionToolSpecification;
import ussr.builder.constructionTools.ConstructionTools;
import ussr.builder.constructionTools.MTRANOperationsTemplate;
import ussr.builder.constructionTools.OdinOperationsTemplate;
import ussr.builder.genericTools.ColorConnectors;
import ussr.builder.genericTools.RemoveModule;
import ussr.builder.helpers.BuilderHelper;
import ussr.description.geometry.VectorDescription;
import ussr.model.Module;
import ussr.physics.jme.JMESimulation;
import ussr.physics.jme.pickers.PhysicsPicker;
import ussr.samples.odin.modules.Odin;
import ussr.aGui.tabs.additionalResources.HintPanelInter;
import ussr.aGui.tabs.views.constructionTabs.ConstructRobotTab;


public class ConstructRobotTabController {


	private static String chosenItem ="Module" ;
	
	private static JMESimulation jmeSimulationLocal;

	/**
	 * Default positions of default construction modules for each modular robot
	 */
	private final static VectorDescription atronDefaultPosition = new VectorDescription(0,-0.441f,0.5f),
	mtranDefaultPosition = new VectorDescription(-1f,-0.4621f,0.5f),	
	odinDefaultPosition = new VectorDescription(1f,-0.4646f,0.5f),	
	ckbotDefaultPosition = new VectorDescription(2f,-0.4646f,0.5f); 

	/**
	 *  The name of the current modular robot
	 */
	private static SupportedModularRobots chosenMRname = SupportedModularRobots.ATRON; //MR- modular robot. Default is ATRON, just do not have the case when it is empty String


	public static void jButtonGroupActionPerformed(AbstractButton button,JMESimulation jmeSimulation ) {
		jmeSimulationLocal = jmeSimulation;
		
		
		
		

		String chosenModularRobot = button.getText();			
		chosenMRname = SupportedModularRobots.valueOf(chosenModularRobot.toUpperCase());
		adaptTabToChosenMR(chosenMRname);
		
		addNewDefaultConstructionModule(jmeSimulation); //Add default construction module
		// Set default construction tool to be "On selected  connector"
		jmeSimulation.setPicker(new ConstructionToolSpecification(jmeSimulation, chosenMRname,ConstructionTools.ON_SELECTED_CONNECTOR));
		
		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[1]);//Informing user
 
	}
	
	
	private static void adaptTabToChosenMR(SupportedModularRobots chosenMRname){
		
		/*Disable and enable available components*/
		ConstructRobotTab.setRadioButtonsEnabled(false);
		ConstructRobotTab.getJComboBoxEntity().setEnabled(true);
		ConstructRobotTab.setEnabledRotationToolBar(true);
		ConstructRobotTab.setEnabledGenericToolBar(true);
		
		ConstructRobotTab.setEnabledConstructionToolsToolBar(true);
		ConstructRobotTab.setEnabledButtonsArrows(false);
		
		/*Adapt tab to chosen modular robot*/
		if (chosenMRname.equals(SupportedModularRobots.ATRON)){
			ConstructRobotTab.getRadionButtonATRON().setSelected(true);
			ConstructRobotTab.getjComboBoxStandardRotations().setModel(new javax.swing.DefaultComboBoxModel(TabsInter.ATRONStandardRotations.values()));
			ConstructRobotTab.getJComboBoxNrConnectorsConstructionTool().setModel(new javax.swing.DefaultComboBoxModel(TabsInter.ATRON_CONNECTORS));

		}else if (chosenMRname.equals(SupportedModularRobots.MTRAN)){
			ConstructRobotTab.getRadioButtonMTRAN().setSelected(true);
			ConstructRobotTab.getJButtonMove().setEnabled(false);
			ConstructRobotTab.getjComboBoxStandardRotations().setModel(new javax.swing.DefaultComboBoxModel(TabsInter.MTRANStandardRotations.values()));
			ConstructRobotTab.getJComboBoxNrConnectorsConstructionTool().setModel(new javax.swing.DefaultComboBoxModel(TabsInter.MTRAN_CONNECTORS));

		}else if (chosenMRname.equals(SupportedModularRobots.ODIN)){
			ConstructRobotTab.getRadionButtonODIN().setSelected(true);
			ConstructRobotTab.setEnabledRotationToolBar(false);// for Odin not yet relevant
			ConstructRobotTab.getJComboBoxNrConnectorsConstructionTool().setModel(new javax.swing.DefaultComboBoxModel(TabsInter.ODIN_CONNECTORS));

			
		}else if (chosenMRname.equals(SupportedModularRobots.CKBOTSTANDARD)){
			ConstructRobotTab.getRadionButtonCKBOTSTANDARD().setSelected(true);
			ConstructRobotTab.getjComboBoxStandardRotations().setModel(new javax.swing.DefaultComboBoxModel( TabsInter.CKBotStandardRotations.values() ));
			ConstructRobotTab.getJComboBoxNrConnectorsConstructionTool().setModel(new javax.swing.DefaultComboBoxModel(TabsInter.CKBOT_CONNECTORS));
		}
	}





	public static void addNewDefaultConstructionModule(JMESimulation jmeSimulation){
		//System.out.println("Modular robot specific--> New Module");//for debugging     	
		CommonOperationsTemplate comATRON = new ATRONOperationsTemplate(jmeSimulation);
		CommonOperationsTemplate comMTRAN = new MTRANOperationsTemplate(jmeSimulation);
		CommonOperationsTemplate comOdin = new OdinOperationsTemplate(jmeSimulation);
		CommonOperationsTemplate comCKBot = new CKBotOperationsTemplate(jmeSimulation);

		VectorDescription zeroPosition = new VectorDescription(0,0,0);		

		if (chosenMRname.equals(SupportedModularRobots.ATRON)&& moduleExists(atronDefaultPosition,jmeSimulation)==false ){
			comATRON.addDefaultConstructionModule(/*this.chosenMRname.toString()*/"default", atronDefaultPosition);
		}else if (chosenMRname.equals(SupportedModularRobots.MTRAN)&& moduleExists(mtranDefaultPosition,jmeSimulation)==false){
			comMTRAN.addDefaultConstructionModule(chosenMRname.toString(),mtranDefaultPosition );
		}else if (chosenMRname.equals(SupportedModularRobots.ODIN)&& moduleExists(odinDefaultPosition,jmeSimulation)==false){
			Odin.setDefaultConnectorSize(0.006f);// make connector bigger in order to select them sucessfuly with "on Connector tool"
			comOdin.addDefaultConstructionModule(chosenMRname.toString(), odinDefaultPosition);
		}else if (chosenMRname.equals(SupportedModularRobots.CKBOTSTANDARD)&& moduleExists(ckbotDefaultPosition,jmeSimulation)==false){			
			comCKBot.addDefaultConstructionModule(chosenMRname.toString(), ckbotDefaultPosition);
		}else {
			//com.addDefaultConstructionModule(this.chosenMRname, zeroPosition);
			//com1.addDefaultConstructionModule(this.chosenMRname,mtranPosition );
			//com2.addDefaultConstructionModule(this.chosenMRname,mtranPosition );
		}

	}


	/**
	 * Checks if module already exists at current position.
	 * @param currentPosition, the position of the module to check.     
	 *@return true, if module exists at current position.
	 */	
	public static boolean moduleExists(VectorDescription currentPosition,JMESimulation jmeSimulation){
		int amountModules = jmeSimulation.getModules().size();
		for (int module =0;module<amountModules;module++){
			Module currentModule =jmeSimulation.getModules().get(module); 
			String moduleType = currentModule.getProperty(BuilderHelper.getModuleTypeKey());
			VectorDescription modulePosition;
			if (moduleType.equalsIgnoreCase("MTRAN")){
				modulePosition = currentModule.getPhysics().get(1).getPosition(); 
			}else{
				modulePosition = currentModule.getPhysics().get(0).getPosition();
			}
			if (modulePosition.equals(currentPosition)){
				return true;
			}
		}
		return false;    	
	}





	/**
	 *  Initializes the tool for variating the properties of modules selected in simulation environment. 
	 * @param jmeSimulation
	 */
	public static void jButton6ActionPerformed(JMESimulation jmeSimulation) {		
		jmeSimulation.setPicker(new ConstructionToolSpecification(jmeSimulation,chosenMRname,ConstructionTools.VARIATION));
	}


	/**
	 * Initializes the tool for placing the modules on connectors selected with left side of the mouse in simulation environment. 
	 * @param evt, selection with left side of the mouse event (jButton selection).     
	 *//*	
	public static void jButton7ActionPerformed(JMESimulation jmeSimulation) {		
		jmeSimulation.setPicker(new ConstructionToolSpecification(jmeSimulation, chosenMRname,ConstructionTools.ON_SELECTED_CONNECTOR));
	}*/

	public static void jButtonDeleteActionPerformed(JMESimulation jmeSimulation) {
		if (chosenItem.equalsIgnoreCase("Module")){
			jmeSimulation.setPicker(new RemoveModule());
		}else if (chosenItem.equalsIgnoreCase("Robot")){
			//TODO
		}
		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[5]); //Informing user
	}

	public static void jButtonMoveActionPerformed(JMESimulation jmeSimulation) {	
		if (chosenItem.equalsIgnoreCase("Module")){
			jmeSimulation.setPicker(new PhysicsPicker(true, true));
		}else if (chosenItem.equalsIgnoreCase("Robot")){
			//TODO
		}
		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[3]);//Informing user
	}

	public static void jButtonColorConnectorsActionPerformed(JMESimulation jmeSimulation) {	
		if (chosenItem.equalsIgnoreCase("Module")){
			jmeSimulation.setPicker(new ColorConnectors());	
		}else if (chosenItem.equalsIgnoreCase("Robot")){
			//TODO
		}
		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[6]);//Informing user

	}





	public static void jComboBoxEntityActionPerformed(JComboBox jComboBox1,JMESimulation jmeSimulation) {
		chosenItem = jComboBox1.getSelectedItem().toString();
		if (chosenItem.equalsIgnoreCase("Module") ){
			ConstructRobotTab.setEnabledGenericToolBar(true);
		}else if(chosenItem.equalsIgnoreCase("Robot")){
			//TODO Support it
			ConstructRobotTab.setEnabledGenericToolBar(false);
		}		
	}




	/**
	 * Initializes the tool for rotating modules selected in simulation environment with opposite rotation. 
	 * @param evt, selection with left side of the mouse event (jButton selection).     
	 */	
	public static void jButtonOppositeRotationActionPerformed(JMESimulation jmeSimulation) {
		jmeSimulation.setPicker(new ConstructionToolSpecification(jmeSimulation, chosenMRname,ConstructionTools.OPPOSITE_ROTATION));
		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[2]);//Informing user
	}

	/**
	 * Initializes the tool for rotating default modules selected in simulation environment with standard rotations. 
	 *  
	 */	
	public static void jComboBox2ActionPerformed(JMESimulation jmeSimulation) {
		jmeSimulation.setPicker(new ConstructionToolSpecification(jmeSimulation, chosenMRname,ConstructionTools.STANDARD_ROTATIONS,ConstructRobotTab.getjComboBoxStandardRotations().getSelectedItem().toString()));		
	}

	/**
	 * Initializes the tool for placing new modules on selected connectors of the module in interest in simulation environment. 
	 * @param jmeSimulation, the physical simulation.
	 */
	public static void jButtonOnSelectedConnectorActionPerformed(JMESimulation jmeSimulation) {
		ConstructRobotTab.setEnabledRotationToolBar(false);
		ConstructRobotTab.getJButtonMove().setEnabled(false);
		jmeSimulation.setPicker(new ConstructionToolSpecification(jmeSimulation, chosenMRname,ConstructionTools.ON_SELECTED_CONNECTOR));
		
		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[7]); //Informing user
	}

	/**
	 * Initializes the tool for adding new modules on connector chosen in combo box(GUI) by user. Later user selects the module to apply it to.
	 * @param comboBoxNrConnectorsConstructionTool, JComboBox containing the number of connectors.
	 * @param jmeSimulation, the physical simulation.
	 */
	public static void jComboBoxNrConnectorsConstructionToolActionPerformed(JComboBox comboBoxNrConnectorsConstructionTool,JMESimulation jmeSimulation) {
		ConstructRobotTab.setEnabledRotationToolBar(false);
		ConstructRobotTab.getJButtonMove().setEnabled(false);
		
		int chosenConnectorNr = Integer.parseInt(comboBoxNrConnectorsConstructionTool.getSelectedItem().toString());
	    jmeSimulation.setPicker(new ConstructionToolSpecification(jmeSimulation, chosenMRname,ConstructionTools.ON_CHOSEN_CONNECTOR,chosenConnectorNr));
	    
	    ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[8]); //Informing user
	}

	/**
	 * Initializes the tool for adding new modules to all connectors of selected module.
	 * @param jmeSimulation, the physical simulation.
	 */
	public static void jButtonConnectAllModulesActionPerformed(JMESimulation jmeSimulation) {
		ConstructRobotTab.setEnabledRotationToolBar(false);
		ConstructRobotTab.getJButtonMove().setEnabled(false);
		
		jmeSimulation.setPicker(new ConstructionToolSpecification(jmeSimulation, chosenMRname,ConstructionTools.ON_ALL_CONNECTORS));
		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[9]); //Informing user
		
	}



	static ConstructionToolSpecification constructionTools = new ConstructionToolSpecification(jmeSimulationLocal, chosenMRname,ConstructionTools.LOOP,0);
	static int connectorNr;

	/**
	 * Initializes the tool for moving new module from connector to another connector.
	 * @param jmeSimulation, the physical simulation.
	 */
	public static void jButtonJumpFromConnToConnectorActionPerformed(JMESimulation jmeSimulation) {
		
		ConstructRobotTab.setEnabledRotationToolBar(false);
		ConstructRobotTab.getJButtonMove().setEnabled(false);
		
		
		
		connectorNr =0;
		ConstructionToolSpecification constructionToolsnew = new ConstructionToolSpecification(jmeSimulation, chosenMRname,ConstructionTools.LOOP,connectorNr);
		constructionTools = constructionToolsnew;
		jmeSimulation.setPicker(constructionToolsnew); 
		
		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[10]); //Informing user
	}





	public static void jButtonOnNextConnectorActionPerformed(
			JMESimulation jmeSimulation) {
		
	  connectorNr++;
		if (chosenMRname.equals(SupportedModularRobots.ATRON)){
			if (connectorNr>TabsInter.ATRON_CONNECTORS.length){ connectorNr=0;}       
		}else if (chosenMRname.equals(SupportedModularRobots.MTRAN)){
			if (connectorNr>TabsInter.MTRAN_CONNECTORS.length){ connectorNr=0;}
		}else if (chosenMRname.equals(SupportedModularRobots.ODIN)){
			if (connectorNr>12){ connectorNr=0;}// OdinBall
		}else if(chosenMRname.equals(SupportedModularRobots.CKBOTSTANDARD)){
			if (connectorNr>TabsInter.CKBOT_CONNECTORS.length){ connectorNr=0;}
		}
		//TODO NEED TO GET BACK FOR ODIN WHICH TYPE OF MODULE IS SELECTED. 
		int amountModules = jmeSimulation.getModules().size();
		String lastModuleType = jmeSimulation.getModules().get(amountModules-1).getProperty(BuilderHelper.getModuleTypeKey());
		if (lastModuleType.equalsIgnoreCase("OdinBall")){
			//do nothing
		}else{
			constructionTools.moveToNextConnector(connectorNr);
		}
		
		//TODO CHECK IF THE MODULE IS ALREADY ON CONNECTOR AND THEN DO NOT PLACE NEW ONE THERE
	}





	public static void jButtonOnPreviousConnectorActionPerformed(
			JMESimulation jmeSimulation) {
		connectorNr--;
		if (chosenMRname.equals(SupportedModularRobots.ATRON) && connectorNr<0){
			connectorNr =TabsInter.ATRON_CONNECTORS.length-1;//reset
		}else if (chosenMRname.equals(SupportedModularRobots.MTRAN) && connectorNr<0){connectorNr=TabsInter.MTRAN_CONNECTORS.length-1;}
		else if (chosenMRname.equals(SupportedModularRobots.ODIN) && connectorNr<0){connectorNr=11;
		}else if (chosenMRname.equals(SupportedModularRobots.CKBOTSTANDARD) && connectorNr<0){connectorNr= TabsInter.CKBOT_CONNECTORS.length-1;}

		int amountModules = jmeSimulation.getModules().size();
		String lastModuleType = jmeSimulation.getModules().get(amountModules-1).getProperty(BuilderHelper.getModuleTypeKey());
		if (lastModuleType.equalsIgnoreCase("OdinBall")){
			//do nothing
		}else{
			constructionTools.moveToNextConnector(connectorNr);
		}
		//TODO CHECK IF THE MODULE IS ALREADY ON CONNECTOR AND THEN DO NOT PLACE NEW ONE THERE
	}
	
	public static void adjustTabToSelectedModule(SupportedModularRobots supportedModularRobot){
		ConstructRobotTab.setRadioButtonsEnabled(true);
		if (supportedModularRobot.equals(SupportedModularRobots.ATRON)){
			ConstructRobotTab.getRadionButtonATRON().setSelected(true);			
		}else if (supportedModularRobot.equals(SupportedModularRobots.ODIN)){
			ConstructRobotTab.getRadionButtonODIN().setSelected(true);
		}else if (supportedModularRobot.equals(SupportedModularRobots.MTRAN)){			
			ConstructRobotTab.getRadioButtonMTRAN().setSelected(true);
		}else if (supportedModularRobot.equals(SupportedModularRobots.CKBOTSTANDARD)){
			ConstructRobotTab.getRadionButtonCKBOTSTANDARD().setSelected(true);
		}
		adaptTabToChosenMR(supportedModularRobot);
		ConstructRobotTab.setRadioButtonsEnabled(false);
	}


	public static void jButtonStartNewRobotActionPerformed(JMESimulation jmeSimulation) {
		ConstructRobotTab.setRadioButtonsEnabled(true);
		ConstructRobotTab.getHintPanel().setText(HintPanelInter.builInHintsConstrucRobotTab[11]); //Informing user
		BuilderHelper.deleteAllModules(jmeSimulation);
	}
		
	//FIXME MAKE IT MORE GENERIC BY MEANS OF IDENTIFYING THE LAST TYPE OF MODULE IN XML FILE
	public static void adaptTabToModuleInSimulation(JMESimulation jmeSimulation){
		if (jmeSimulation.getModules().size()>0){
			/*Adapt first module*/
			String modularRobotName = jmeSimulation.getModules().get(0).getProperty(BuilderHelper.getModuleTypeKey());
			if (modularRobotName.toUpperCase().contains(SupportedModularRobots.ATRON.toString())){
				
				adaptTabToChosenMR(SupportedModularRobots.ATRON);
			} else if (modularRobotName.toUpperCase().contains(SupportedModularRobots.ODIN.toString())){
				adaptTabToChosenMR(SupportedModularRobots.ODIN);
			} else if (modularRobotName.toUpperCase().contains(SupportedModularRobots.MTRAN.toString())){
				adaptTabToChosenMR(SupportedModularRobots.MTRAN);
			}else if(modularRobotName.toUpperCase().contains(SupportedModularRobots.CKBOTSTANDARD.toString())){
				adaptTabToChosenMR(SupportedModularRobots.CKBOTSTANDARD);
			}
			
			jmeSimulation.setPicker(new ConstructionToolSpecification(jmeSimulation, chosenMRname,ConstructionTools.ON_SELECTED_CONNECTOR));
		}
		
	}
	

}
