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
		bus = new ATRONBus(this,ATRONBusPrinter.ALL - ATRONBusPrinter.STATE_MESSAGE);// Printer.ACTION + Printer.STATE_UPDATE);

		bus.state = 0;

		bus.initBaseTime();

		assignRoles(true);
		
		while (true) {

			bus.maintainConnection();
			bus.getPrint().state();
			
			
			bus.execAt("m3",1).con.disconnect("f0").next();
			bus.execAt("f2",2).con.disconnect("f0").next();
			
			bus.execAt("f0",3).rotateDegrees(90).next();
			
			bus.execAt("m2",4).rotateDegrees(90).next();
			bus.execAt("m3",5).con.connect("f2").next();
			
			
			//bus.execAt("f2",1).con.disconnect("f0").next();
			//bus.execAt("f3",2).connect("f1").connect("f2").connect("f4").connect("f5");
			//bus.execAt("f6",3).connect("f4").connect("f5");
			
			//bus.execAt("f0",1).disconnect(0).disconnect(6).next();
	
			//bus.execAt("f0",2).rotateDegrees(90).next();
			
			//bus.execAt("m2",3).rotateDegrees(90).next();
			
			//bus.execAt("m3",4).connect(6,true).next();
			
//			bus.execAt("f0",5).con.disconnect(4).next();
//			
//			bus.execAt("m2",6).con.disconnect(6).next();
//		
//			bus.execAt("m3",7).rotateDegrees(180).next();
//		
//
//			bus.execAt("m2",8).con.connect(6,true).next();
//
//			bus.execAt("m3",9).con.disconnect(6).next();
//			
//			bus.execAt("m2",10).rotateDegrees(90).next();
//			
//			bus.execAt("f6",11).con.disconnect(4).next(); // 6 for second round
//			bus.execAt("f6",12).con.disconnect(6).next();
//			bus.execAt("f6",13).rotateDegrees(90).next();
//			
//			
//			bus.execAt("f0",14).rotateDegrees(90).next();
//			bus.execAt("f0",15).con.connect(4,true).next();
//			
//			bus.execAt("f5",16).rotateDegrees(90).next();
//			bus.execAt("f5",17).con.connect(4,true).next();
//			
//			bus.execAt("f6",18).con.connect(2,true).next();
//			
//			if(bus.state==19) {
//				assignRoles(false);
//			}
//			
//			if(bus.state==20) {
//				bus.state = 1;
//				if (bus.moduleMatcher("f0")) {
//					System.out.println("===================");
//					System.out.println("===================");
//					System.out.println("===================");
//				}
//			}
			colorizeConnectors();
			yield();
		}
	}

	public void colorize() {
		
		if (bus.moduleMatcher("m1")) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#00FFFF"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#FFFF00"));
		}
		else if (bus.moduleMatcher("m2")) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#008888"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#888800"));
		}
		else if (bus.moduleMatcher("m3")) {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#003333"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#333300"));
		}
		else {
			getModule().getComponent(0).setModuleComponentColor(Color.decode("#0000FF"));
			getModule().getComponent(1).setModuleComponentColor(Color.decode("#FF0000"));
		}
		colorizeConnectors();
	}

	private void colorizeConnectors() {
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
			trans.put("m1", "f6");
			trans.put("m2", "f5");
			trans.put("m3", "f4");
			
		}
		
		for (Map.Entry<String, String> entry : trans.entrySet()) {
		   if (bus.moduleMatcher(entry.getKey())) {
			   System.out.println(entry.getKey() + " renamed to " + entry.getValue());
			   bus.setName(entry.getValue());
			   
				 
			   bus.state++;
				  
			   
			   return;
		   }
		   
		}

		colorize();
		
		
		
		
	} 

	public void handleMessage(byte[] message, int messageLength, int connector) {
		bus.handleMessage(message, (byte)messageLength, (byte)connector);
		colorize ();
	}

}
