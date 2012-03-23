package ussr.samples.atron.simulations.wouter;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import ussr.samples.atron.ATRONController;

public class ATRONMetaModuleController extends ATRONController {

	ATRONBus bus;

	public static void main(String[] args) {
		new ATRONMetaModuleSimulation().main();
	}

	public void activate() {
		setup();
		bus = new ATRONBus(this);

		bus.state = 0;

		

		//bus.disconnect("!m", 0);
		bus.initBaseTime();

		assignRoles(true);
		
		while (true) {
			colorize ();
			
			
			bus.maintainConnection();
			bus.printState();

			
			bus.execAt("f0",1).disconnect(0).disconnect(6).bcNextState();
	
			bus.execAt("f0",2).rotateDegrees(90);
			
			bus.execAt("m2",3).rotateDegrees(90);
			
			bus.execAt("m3",4).connect(6,false).bcNextState();
			
			bus.execAt("f0",5).disconnect(4).bcNextState();
			
			bus.execAt("m2",6).disconnect(6).bcNextState();
		
			bus.execAt("m3",7).rotateDegrees(180);
		
			bus.execAt("m2",8).rotateDegrees(90);

			bus.execAt("m2",9).connect(6,false).bcNextState();

			
			bus.execAt("m3",10).disconnect(6).bcNextState();
			
			bus.execAt("m2",11).rotateDegrees(90);

			
		
			bus.execAt("f6",12).disconnect(4).bcNextState();
			bus.execAt("f6",13).rotateDegrees(90);
			
			
			
			bus.execAt("f0",14).rotateDegrees(90);
			bus.execAt("f0",15).connect(4,false).bcNextState();
			
			bus.execAt("f5",16).rotateDegrees(90);
			bus.execAt("f5",17).connect(4,false).bcNextState();
			
			bus.execAt("f6",18).connect(2,false).bcNextState();
			
			if(bus.state==19) {
				assignRoles(false);
			}
			
			if(bus.state==20) {
				bus.state = 1;
			}
			
			yield();
		}
	}

	private void colorize() {
		
		if (bus.containsModule("m1")) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#00FFFF"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#FFFF00"));
		}
		else if (bus.containsModule("m2")) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#008888"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#888800"));
		}
		else if (bus.containsModule("m3")) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#003333"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#333300"));
		}
		else {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#0000FF"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#FF0000"));
		}
			
		
		module.getConnectors().get(0).setColor(Color.RED);
		module.getConnectors().get(1).setColor(Color.BLACK);
		module.getConnectors().get(2).setColor(Color.BLUE);
		module.getConnectors().get(3).setColor(Color.WHITE);
		module.getConnectors().get(4).setColor(Color.RED);
		module.getConnectors().get(5).setColor(Color.BLACK);
		module.getConnectors().get(6).setColor(Color.BLUE);
		module.getConnectors().get(7).setColor(Color.WHITE);
	}

	private void assignRoles(boolean firstTime) {
		
		
		Map <String,String> trans = new HashMap<String,String>();
		trans.put("f0", "m1");
		trans.put("f1", "m3");
		trans.put("f2", "m2");
		trans.put("f3", "f0");
		trans.put("f4", "f1");
		trans.put("f5", "f2");
		trans.put("f6", "f3");
	
		
		if (firstTime) {
			trans.put("f7", "f4");
			trans.put("f8", "f5");
			trans.put("f9", "f6");
		}
		else {
			trans.put("m1", "f4");
			trans.put("m2", "f5");
			trans.put("m3", "f6");
			
		}
		
		for (Map.Entry<String, String> entry : trans.entrySet()) {
		   if (bus.containsModule(entry.getKey())) {
			   System.out.println(entry.getKey() + "->" + entry.getValue());
			   bus.setName(entry.getValue());
			   
				 
			   bus.state++;
				  
			   
			   return;
		   }
		   
		}

		
		
		
		
		
	}

	public void handleMessage(byte[] message, int messageLength, int connector) {
		bus.handleMessage(message, messageLength, connector);
	}

}
