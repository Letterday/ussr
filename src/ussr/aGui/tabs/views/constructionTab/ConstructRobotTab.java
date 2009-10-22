package ussr.aGui.tabs.views.constructionTab;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JRadioButton;

import ussr.aGui.FramesInter;
import ussr.aGui.GuiFrames;
import ussr.aGui.MainFrameInter;

import ussr.aGui.tabs.Tabs;
import ussr.aGui.tabs.controllers.ConstructionTabController;
import ussr.physics.jme.JMESimulation;

/**
 * Defines visual appearance of the tab called "1 Step: Construct Robot".  
 * @author Konstantinas
 */
public class ConstructRobotTab extends Tabs {

	/**
	 * The container for radio buttons of supported modular robots (ATRON,Odin and so on).
	 */
	private static ArrayList<AbstractButton> jRadioButtons =  new ArrayList<AbstractButton>() ;

	/**
	 * The constants of grid bag layout used during design of the tab.
	 */
	private GridBagConstraints gridBagConstraints = new GridBagConstraints();	


	/**
	 * Defines visual appearance of the tab called "1 Step: Construct Robot".
	 * @param firstTabbedPane, location of the tab in the main GUI frame. True if it is the first tabbed pane.
	 * @param tabTitle, the title of the tab
	 * @param jmeSimulation, the physical simulation.
	 * @param imageIconDirectory,the directory for icon displayed in the top-left corner of the tab.
	 */
	public ConstructRobotTab(boolean firstTabbedPane,String tabTitle,JMESimulation jmeSimulation,String imageIconDirectory){
		super(firstTabbedPane,tabTitle,jmeSimulation,imageIconDirectory);

		/*instantiate new panel, which will be the container for all components situated in the tab*/		
		super.jComponent = new javax.swing.JPanel(new GridBagLayout());
		//super.fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		initComponents();
	}



	/**
	 * Initializes the visual appearance of all components in the tab.
	 * Follows Strategy  pattern.
	 */
	/* (non-Javadoc)
	 * @see ussr.aGui.tabs.views.Tabs#initComponents()
	 */
	public void initComponents(){

		/*Instantiation of components*/
		jToolBarGenericTools = new javax.swing.JToolBar();
		jToolBarRotationTools = new javax.swing.JToolBar();
		jToolBarConstructionTools = new javax.swing.JToolBar();

		jButtonDelete =   new javax.swing.JButton();
		jButtonMove =   new javax.swing.JButton();	
		jButtonColorConnetors =   new javax.swing.JButton();
		jButtonOppositeRotation =   new javax.swing.JButton();
		jButtonOnSelectedConnector =   new javax.swing.JButton();
		jButtonConnectAllModules = new javax.swing.JButton();
		jButtonJumpFromConnToConnector = new javax.swing.JButton();
		jButtonOnNextConnector = new javax.swing.JButton();
		jButtonOnPreviousConnector = new javax.swing.JButton();

		jLabel1000 = new javax.swing.JLabel();
		jLabel10002 = new javax.swing.JLabel();	
		jLabel10003 = new javax.swing.JLabel();	

		jComboBoxEntity = new javax.swing.JComboBox();
		jComboBoxStandardRotations = new javax.swing.JComboBox();
		jComboBoxNrConnectorsConstructionTool = new javax.swing.JComboBox();

		jSeparator1 = new javax.swing.JToolBar.Separator();
		jSeparator2 = new javax.swing.JToolBar.Separator();


		/*Description of components */
		jLabel10002.setText("Add initial module:");
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.insets = new Insets(0,0,10,0);
		super.jComponent.add(jLabel10002,gridBagConstraints);

		final ButtonGroup buttonGroup = new ButtonGroup() ;

		radionButtonATRON =  new JRadioButton();
		radionButtonATRON.setFocusable(true);// direct the user to what should be done first
		radionButtonATRON.setText("ATRON");		
		radionButtonATRON.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButtonGroupActionPerformed(radionButtonATRON,jmeSimulation);
			}
		});
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		//gridBagConstraints.ipady = 10; 
		super.jComponent.add(radionButtonATRON,gridBagConstraints);
		buttonGroup.add(radionButtonATRON);
		jRadioButtons.add(radionButtonATRON);

		radionButtonODIN =  new JRadioButton();
		radionButtonODIN.setText("Odin");
		radionButtonODIN.setFocusable(false);
		radionButtonODIN.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButtonGroupActionPerformed(radionButtonODIN,jmeSimulation);
			}
		});
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		//gridBagConstraints.ipady = 10; 
		super.jComponent.add(radionButtonODIN,gridBagConstraints);
		buttonGroup.add(radionButtonODIN);
		jRadioButtons.add(radionButtonODIN);

		radioButtonMTRAN =  new JRadioButton();
		radioButtonMTRAN.setText("MTran");
		radioButtonMTRAN.setFocusable(false);
		radioButtonMTRAN.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButtonGroupActionPerformed(radioButtonMTRAN,jmeSimulation);
			}
		});
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		//gridBagConstraints.ipady = 10; 
		super.jComponent.add(radioButtonMTRAN,gridBagConstraints);
		buttonGroup.add(radioButtonMTRAN);
		jRadioButtons.add(radioButtonMTRAN);

		radionButtonCKBOTSTANDARD =  new JRadioButton();
		radionButtonCKBOTSTANDARD.setText("CKBotStandard");
		radionButtonCKBOTSTANDARD.setFocusable(false);
		radionButtonCKBOTSTANDARD.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButtonGroupActionPerformed(radionButtonCKBOTSTANDARD,jmeSimulation);
			}
		});
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		//gridBagConstraints.ipady = 40; 
		super.jComponent.add(radionButtonCKBOTSTANDARD,gridBagConstraints);
		buttonGroup.add(radionButtonCKBOTSTANDARD);	
		jRadioButtons.add(radionButtonCKBOTSTANDARD);		

		/*		button14.setText("Robot Ready");		
		button14.setToolTipText("Robot is ready");		
		button14.setFocusable(false); 
		button14.setPreferredSize(new java.awt.Dimension(30, 30));	
		button14.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButton14ActionPerformed(jmeSimulation, button14);
			}
		});
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 3;
		super.jComponent.add(button14,gridBagConstraints);	*/	

		jLabel1000.setText("Apply rotation:");		
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.insets = new Insets(0,0,10,10);  //make some space at the bootm
		super.jComponent.add(jLabel1000,gridBagConstraints);

		jToolBarRotationTools.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		jToolBarRotationTools.setFloatable(false);//user can not make the tool bar to float
		jToolBarRotationTools.setRollover(true);// the components inside are roll over
		jToolBarRotationTools.setToolTipText("Rotation tools");
		jToolBarRotationTools.setPreferredSize(new Dimension(185,GuiFrames.COMMON_HEIGHT+2));
		/*External layout of the toolbar in the panel*/
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 1;		
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 4;	


		jButtonOppositeRotation.setToolTipText("Opposite");		
		jButtonOppositeRotation.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + OPPOSITE));
		jButtonOppositeRotation.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));
		jButtonOppositeRotation.setFocusable(false); 
		jButtonOppositeRotation.setEnabled(false);
		jButtonOppositeRotation.setPreferredSize(new java.awt.Dimension(FramesInter.BUTTONS_WIDTH, FramesInter.COMMON_HEIGHT-3));	
		jButtonOppositeRotation.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButton15ActionPerformed(jmeSimulation);
			}
		});

		jComboBoxStandardRotations.setToolTipText("Standard");
		jComboBoxStandardRotations.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " " }));
		jComboBoxStandardRotations.setPreferredSize(new java.awt.Dimension(140, GuiFrames.COMMON_HEIGHT-3));
		jComboBoxStandardRotations.setEnabled(false);
		jComboBoxStandardRotations.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jComboBox2ActionPerformed(jmeSimulation);
			}
		});

		/*Internal layout of the toolbar*/
		GroupLayout jToolBarRotationToolsLayout = new GroupLayout(jToolBarRotationTools);
		jToolBarRotationTools.setLayout(jToolBarRotationToolsLayout);


		jToolBarRotationToolsLayout.setHorizontalGroup(
				jToolBarRotationToolsLayout.createSequentialGroup()
				//Forces preferred side of component
				.addComponent(jButtonOppositeRotation,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxStandardRotations,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)				
		);

		jToolBarRotationToolsLayout.setVerticalGroup(
				jToolBarRotationToolsLayout.createSequentialGroup()
				.addGroup(jToolBarRotationToolsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jButtonOppositeRotation,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
								.addComponent(jComboBoxStandardRotations,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))						
		);

		super.jComponent.add(jToolBarRotationTools,gridBagConstraints);

		jLabel10003.setText("Generic funtionality for:");
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 1;// reset grid width
		gridBagConstraints.insets = new Insets(0,0,10,10);  //bring padding back and make some space at boom
		super.jComponent.add(jLabel10003,gridBagConstraints);		

		jComboBoxEntity.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Module", "Robot" }));
		jComboBoxEntity.setPreferredSize(new java.awt.Dimension(59, GuiFrames.COMMON_HEIGHT));
		jComboBoxEntity.setEnabled(false);
		jComboBoxEntity.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jComboBox1ActionPerformed(jComboBoxEntity,jmeSimulation);
			}
		});
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		super.jComponent.add(jComboBoxEntity,gridBagConstraints);

		/*	jLabel10004 = new javax.swing.JLabel();		
		jLabel10004.setText("Generic functionality:");
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		super.jComponent.add(jLabel10004,gridBagConstraints);	*/	

		jToolBarGenericTools.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		jToolBarGenericTools.setFloatable(false);//user can not make the tool bar to float
		jToolBarGenericTools.setRollover(true);// the buttons inside are roll over
		jToolBarGenericTools.setToolTipText("Generic tools");
		jToolBarGenericTools.setPreferredSize(new Dimension(195,GuiFrames.COMMON_HEIGHT));		
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 2;		
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 3;		

		jButtonMove.setToolTipText("Move");		
		jButtonMove.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + MOVE));
		jButtonMove.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));
		jButtonMove.setFocusable(false); 
		jButtonMove.setEnabled(false);
		jButtonMove.setPreferredSize(new java.awt.Dimension(FramesInter.BUTTONS_WIDTH, FramesInter.COMMON_HEIGHT));	
		jButtonMove.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButton11ActionPerformed(jmeSimulation);
			}
		});
		jToolBarGenericTools.add(jButtonMove);

		jButtonDelete.setToolTipText("Delete");
		jButtonDelete.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + DELETE));
		jButtonDelete.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));		
		jButtonDelete.setFocusable(false);
		jButtonDelete.setEnabled(false);
		jButtonDelete.setPreferredSize(new java.awt.Dimension(FramesInter.BUTTONS_WIDTH, FramesInter.COMMON_HEIGHT));
		jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButton10ActionPerformed(jmeSimulation);
			}
		});

		jToolBarGenericTools.add(jButtonDelete);		

		jButtonColorConnetors.setToolTipText("Color Connectors");
		jButtonColorConnetors.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + COLOUR_CONNECTORS));
		jButtonColorConnetors.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));		
		jButtonColorConnetors.setFocusable(false);
		jButtonColorConnetors.setEnabled(false);
		jButtonColorConnetors.setPreferredSize(new java.awt.Dimension(FramesInter.BUTTONS_WIDTH, FramesInter.BUTTONS_WIDTH));
		jButtonColorConnetors.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButtonColorConnectorsActionPerformed(jmeSimulation);
			}
		});

		jToolBarGenericTools.add(jButtonColorConnetors);	

		super.jComponent.add(jToolBarGenericTools,gridBagConstraints);


		jLabel10001 = new javax.swing.JLabel();		
		jLabel10001.setText("Choose construction tool:");
		//jLabel10001.setFont( new Font("Times New Roman", Font.PLAIN, 12).deriveFont(fontAttributes));		
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.insets = new Insets(0,0,10,10);  //make some space at the bottom and right
		super.jComponent.add(jLabel10001,gridBagConstraints);

		jToolBarConstructionTools.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		jToolBarConstructionTools.setFloatable(false);//user can not make the tool bar to float
		jToolBarConstructionTools.setRollover(true);// the buttons inside are roll over
		jToolBarConstructionTools.setToolTipText("Construction tools");
		jToolBarConstructionTools.setPreferredSize(new Dimension(195,GuiFrames.COMMON_HEIGHT+2));		
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 1;		
		gridBagConstraints.gridy = 7;
		gridBagConstraints.gridwidth = 4;		

		jButtonOnSelectedConnector.setToolTipText("Select connector");
		jButtonOnSelectedConnector.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + ON_SELECTED_CONNECTOR));
		jButtonOnSelectedConnector.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));		
		jButtonOnSelectedConnector.setFocusable(false);
		jButtonOnSelectedConnector.setEnabled(false);
		jButtonOnSelectedConnector.setPreferredSize(new java.awt.Dimension(FramesInter.BUTTONS_WIDTH, FramesInter.BUTTONS_WIDTH-3));
		jButtonOnSelectedConnector.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButtonOnSelectedConnectorActionPerformed(jmeSimulation);
			}
		});				

		jComboBoxNrConnectorsConstructionTool.setToolTipText("Select connector Nr to connect new module to and then select module");
		jComboBoxNrConnectorsConstructionTool.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " " }));
		jComboBoxNrConnectorsConstructionTool.setPreferredSize(new java.awt.Dimension(40, GuiFrames.COMMON_HEIGHT-4));
		jComboBoxNrConnectorsConstructionTool.setEnabled(false);
		jComboBoxNrConnectorsConstructionTool.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jComboBoxNrConnectorsConstructionToolActionPerformed(jComboBoxNrConnectorsConstructionTool,jmeSimulation);
			}
		});

		jButtonConnectAllModules.setToolTipText("Connect all modules");
		jButtonConnectAllModules.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + CONNECT_ALL_MODULES));
		jButtonConnectAllModules.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));		
		jButtonConnectAllModules.setFocusable(false);
		jButtonConnectAllModules.setEnabled(false);
		jButtonConnectAllModules.setPreferredSize(new java.awt.Dimension(FramesInter.BUTTONS_WIDTH, FramesInter.BUTTONS_WIDTH-3));
		jButtonConnectAllModules.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButtonConnectAllModulesActionPerformed(jmeSimulation);
			}
		});	

		jSeparator1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		jSeparator1.setPreferredSize(new Dimension(6,30));


		jButtonJumpFromConnToConnector.setToolTipText("Jump from one connector to the next connector(loop)");
		jButtonJumpFromConnToConnector.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + JUMP_FROM_CONN_TO_CONNECTOR));
		jButtonJumpFromConnToConnector.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));		
		jButtonJumpFromConnToConnector.setFocusable(false);
		jButtonJumpFromConnToConnector.setEnabled(false);
		jButtonJumpFromConnToConnector.setPreferredSize(new java.awt.Dimension(FramesInter.BUTTONS_WIDTH, FramesInter.BUTTONS_WIDTH-3));
		jButtonJumpFromConnToConnector.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButtonJumpFromConnToConnectorActionPerformed(jmeSimulation);
			}
		});	

		jButtonOnNextConnector.setToolTipText("On next connector");
		jButtonOnNextConnector.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + NEXT));
		jButtonOnNextConnector.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));		
		jButtonOnNextConnector.setFocusable(false);
		jButtonOnNextConnector.setEnabled(false);
		jButtonOnNextConnector.setPreferredSize(new java.awt.Dimension(FramesInter.BUTTONS_WIDTH, FramesInter.BUTTONS_WIDTH-3));
		jButtonOnNextConnector.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButtonOnNextConnectorActionPerformed(jmeSimulation);
			}
		});	
		
		jButtonOnPreviousConnector.setToolTipText("On previous connector");
		jButtonOnPreviousConnector.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + PREVIOUS));
		jButtonOnPreviousConnector.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));		
		jButtonOnPreviousConnector.setFocusable(false);
		jButtonOnPreviousConnector.setEnabled(false);
		jButtonOnPreviousConnector.setPreferredSize(new java.awt.Dimension(FramesInter.BUTTONS_WIDTH, FramesInter.BUTTONS_WIDTH-3));
		jButtonOnPreviousConnector.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ConstructionTabController.jButtonOnPreviousConnectorActionPerformed(jmeSimulation);
			}
		});

		jSeparator2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
		jSeparator2.setPreferredSize(new Dimension(6,30));


		/*Internal layout of the toolbar*/
		GroupLayout jToolBarConstructionToolsLayout = new GroupLayout(jToolBarConstructionTools);
		jToolBarConstructionTools.setLayout(jToolBarConstructionToolsLayout);

		jToolBarConstructionToolsLayout.setHorizontalGroup(
				jToolBarConstructionToolsLayout.createSequentialGroup()
				//Forces preferred side of component
				.addComponent(jButtonOnSelectedConnector,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxNrConnectorsConstructionTool,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)				
								.addComponent(jButtonConnectAllModules,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
										//Forces preferred side of component and also specifies it explicitly. For instance:6. 
										.addComponent(jSeparator1,GroupLayout.PREFERRED_SIZE, 6,
												GroupLayout.PREFERRED_SIZE)
												.addComponent(jButtonJumpFromConnToConnector,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE)
														.addComponent(jButtonOnPreviousConnector,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																		GroupLayout.PREFERRED_SIZE)
														.addComponent(jButtonOnNextConnector,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																		GroupLayout.PREFERRED_SIZE)
														.addComponent(jSeparator2,GroupLayout.PREFERRED_SIZE, 6,
																GroupLayout.PREFERRED_SIZE)
																


		);

		jToolBarConstructionToolsLayout.setVerticalGroup(
				jToolBarConstructionToolsLayout.createSequentialGroup()
				.addGroup(jToolBarConstructionToolsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jButtonOnSelectedConnector,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
								.addComponent(jComboBoxNrConnectorsConstructionTool,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
										.addComponent(jButtonConnectAllModules,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
												//Forces preferred side of component and also specifies it explicitly. For instance:28. 
												.addComponent(jSeparator1,GroupLayout.PREFERRED_SIZE, 28,
														GroupLayout.PREFERRED_SIZE)
														.addComponent(jButtonJumpFromConnToConnector,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
																.addComponent(jButtonOnPreviousConnector,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																		GroupLayout.PREFERRED_SIZE)
																.addComponent(jButtonOnNextConnector,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																.addComponent(jSeparator2,GroupLayout.PREFERRED_SIZE, 28,
																		GroupLayout.PREFERRED_SIZE))
																		
		);

		super.jComponent.add(jToolBarConstructionTools,gridBagConstraints);
	}



	public  void setEnabled(){
		jComponent.setEnabled(false);
	}

	public static void setRadioButtonsEnabled(boolean enabled){
		for (AbstractButton radioButton: jRadioButtons ){
			radioButton.setEnabled(enabled);
		}
	}

	/*Getters and setters*/

	/**
	 * Enables and disables the tool bar containing generic tools for manipulating modules.
	 * @param enable,true for tool bar to be disabled. 
	 */
	public static void setEnabledGenericToolBar(boolean enable){
		jButtonDelete.setEnabled(enable);
		jButtonMove.setEnabled(enable);
		jButtonColorConnetors.setEnabled(enable);
	}

	public static javax.swing.JButton getJButtonMove() {
		return jButtonMove;
	}


	/**
	 * Enables and disables the tool bar containing the tools for constructing the shape(morphology) of modular robot.
	 * @param enable,true for tool bar to be disabled. 
	 */
	public static void setEnabledConstructionToolsToolBar(boolean enable){
		jButtonOnSelectedConnector.setEnabled(enable);
		jComboBoxNrConnectorsConstructionTool.setEnabled(enable);
		jButtonConnectAllModules.setEnabled(enable);
		jButtonJumpFromConnToConnector.setEnabled(enable);
		jButtonOnNextConnector.setEnabled(enable);
		jButtonOnPreviousConnector.setEnabled(enable);
	}

	/**
	 * Enables and disables tool bar containing tools for applying rotation to module(s).
	 * @param enable, true for tool bar to be disabled.
	 */
	public static void setEnabledRotationToolBar(boolean enable){
		jButtonOppositeRotation.setEnabled(enable);
		jComboBoxStandardRotations.setEnabled(enable);	
	}


	public static javax.swing.JComboBox getJComboBox1() {
		return jComboBoxEntity;
	}

	public static javax.swing.AbstractButton getButton1() {
		return radionButtonATRON;
	}
	
	public static javax.swing.JLabel getJLabel10001() {
		return jLabel10001;
	}

	public static javax.swing.JComboBox getjComboBoxStandardRotations() {
		return jComboBoxStandardRotations;
	}

	public static javax.swing.JComboBox getJComboBoxNrConnectorsConstructionTool() {
		return jComboBoxNrConnectorsConstructionTool;
	}
	
	public static javax.swing.JButton getJButtonOnNextConnector() {
		return jButtonOnNextConnector;
	}
	
	public static javax.swing.JButton getJButtonOnPreviousConnector() {
		return jButtonOnPreviousConnector;
	}

	/*Declaration of tab components*/
	private static javax.swing.JComboBox jComboBoxEntity;
	private static javax.swing.JComboBox jComboBoxStandardRotations;
	private static javax.swing.JComboBox jComboBoxNrConnectorsConstructionTool;
	
	private static javax.swing.JLabel jLabel1000;
	private static javax.swing.JLabel jLabel10001;
	private static javax.swing.JLabel jLabel10002;
	private static javax.swing.JLabel jLabel10003;

	private static  javax.swing.AbstractButton radionButtonATRON;
	private static javax.swing.AbstractButton  radioButtonMTRAN;
	private static javax.swing.AbstractButton radionButtonODIN;
	private static javax.swing.AbstractButton radionButtonCKBOTSTANDARD;

	private  static javax.swing.JButton jButtonDelete;
	private  static javax.swing.JButton jButtonMove;
	private  static javax.swing.JButton jButtonColorConnetors;	
	private  static javax.swing.JButton jButtonOppositeRotation;
	private  static javax.swing.JButton jButtonOnSelectedConnector;
	private  static javax.swing.JButton jButtonConnectAllModules;
	private  static javax.swing.JButton jButtonJumpFromConnToConnector;
	private  static javax.swing.JButton jButtonOnNextConnector;
	private  static javax.swing.JButton jButtonOnPreviousConnector;

	private static javax.swing.JToolBar jToolBarGenericTools;
	private static javax.swing.JToolBar jToolBarRotationTools;
	private static javax.swing.JToolBar jToolBarConstructionTools;

	private javax.swing.JToolBar.Separator  jSeparator1;
	private javax.swing.JToolBar.Separator  jSeparator2;

}