package ussr.samples.atron.simulations.metaforma.exp;
//package ussr.samples.atron.simulations.metaforma.experimental;
//
//import java.awt.Color;
//import java.util.HashMap;
//import java.util.Map;
//
//import ussr.model.debugging.DebugInformationProvider;
//import ussr.samples.atron.ATRONController;
//
//public class ATRONMetaModuleController extends ATRONController {
//
//	public ATRONBus bus;
//	DebugInformationProvider info;
//	
//	
//	
//	public static void main(String[] args) {
//		ATRONMetaModuleSimulation.main(args);
//	}
//
//	
//	public void activate() {
//		
//		setup();
//		bus = new ATRONBus(this);// Printer.ACTION + Printer.STATE_UPDATE);
//		info = this.getModule().getDebugInformationProvider();
//		
//
//		bus.state = 0;
//
//		bus.initBaseTime();
//
//		//assignRoles(true);
//		
//
//		while (true) {
//
////			bus.maintainPosition();
////
////			bus.execAt(Module.F0,1).con.disconnect(Module.MetaR).next();
////			bus.execAt(Module.F0,2).con.disconnect(Module.F2).next();
////			bus.execAt(Module.F0,3).rotateDegrees(90).next();
////			bus.execAt(Module.MetaL,4).rotateDegrees(90).next();
////			bus.execAt(Module.MetaR,5).con.connect(Module.F3).next();
////			bus.execAt(Module.MetaL,6).con.disconnect(Module.F0).next();
//			
////			bus.execAt("m3",5).con.connect("f2").next();
//			
//			
//			//bus.execAt("f2",1).con.disconnect("f0").next();
//			//bus.execAt("f3",2).connect("f1").connect("f2").connect("f4").connect("f5");
//			//bus.execAt("f6",3).connect("f4").connect("f5");
//			
//			//bus.execAt("f0",1).disconnect(0).disconnect(6).next();
//	
//			//bus.execAt("f0",2).rotateDegrees(90).next();
//			
//			//bus.execAt("m2",3).rotateDegrees(90).next();
//			
//			//bus.execAt("m3",4).connect(6,true).next();
//			
////			bus.execAt("f0",5).con.disconnect(4).next();
////			
////			bus.execAt("m2",6).con.disconnect(6).next();
////		
////			bus.execAt("m3",7).rotateDegrees(180).next();
////		
////
////			bus.execAt("m2",8).con.connect(6,true).next();
////
////			bus.execAt("m3",9).con.disconnect(6).next();
////			
////			bus.execAt("m2",10).rotateDegrees(90).next();
////			
////			bus.execAt("f6",11).con.disconnect(4).next(); // 6 for second round
////			bus.execAt("f6",12).con.disconnect(6).next();
////			bus.execAt("f6",13).rotateDegrees(90).next();
////			
////			
////			bus.execAt("f0",14).rotateDegrees(90).next();
////			bus.execAt("f0",15).con.connect(4,true).next();
////			
////			bus.execAt("f5",16).rotateDegrees(90).next();
////			bus.execAt("f5",17).con.connect(4,true).next();
////			
////			bus.execAt("f6",18).con.connect(2,true).next();
////			
////			if(bus.state==19) {
////				assignRoles(false);
////			}
////			
////			if(bus.state==20) {
////				bus.state = 1;
////				if (bus.moduleMatcher("f0")) {
////					System.out.println("===================");
////					System.out.println("===================");
////					System.out.println("===================");
////				}
////			}
//			colorizeConnectors();
//			yield();
//		}
//	}
//
////	public void colorize() {
////		
////		if (bus.getId() == Module.MetaC) {
////			getModule().getComponent(0).setModuleComponentColor(Color.decode("#00FFFF"));
////			getModule().getComponent(1).setModuleComponentColor(Color.decode("#FFFF00"));
////		}
////		else if (bus.getId() == Module.MetaL) {
////			getModule().getComponent(0).setModuleComponentColor(Color.decode("#008888"));
////			getModule().getComponent(1).setModuleComponentColor(Color.decode("#888800"));
////		}
////		else if (bus.getId() == Module.MetaR) {
////			getModule().getComponent(0).setModuleComponentColor(Color.decode("#003333"));
////			getModule().getComponent(1).setModuleComponentColor(Color.decode("#333300"));
////		}
////		else {
////			getModule().getComponent(0).setModuleComponentColor(Color.decode("#0000FF"));
////			getModule().getComponent(1).setModuleComponentColor(Color.decode("#FF0000"));
////		}
////		colorizeConnectors();
////	}
//
//	private void colorizeConnectors() {
//		module.getConnectors().get(0).setColor(Color.RED);
//		module.getConnectors().get(1).setColor(Color.BLACK);
//		module.getConnectors().get(2).setColor(Color.BLUE);
//		module.getConnectors().get(3).setColor(Color.WHITE);
//		module.getConnectors().get(4).setColor(Color.RED);
//		module.getConnectors().get(5).setColor(Color.BLACK);
//		module.getConnectors().get(6).setColor(Color.BLUE);
//		module.getConnectors().get(7).setColor(Color.WHITE);
//		
//	}
//
////	private void assignRoles(boolean firstTime) {
////		
////		Map <Module,Module> trans = new HashMap<Module,Module>();
////		trans.put(Module.F0, Module.MetaC);
////		trans.put(Module.F1, Module.MetaR);
////		trans.put(Module.F2, Module.MetaL);
////		trans.put(Module.F3, Module.F0);
////		trans.put(Module.F4, Module.F1);
////		trans.put(Module.F5, Module.F2);
////		trans.put(Module.F6, Module.F3);
////	
////		
////		if (firstTime) {
////			trans.put(Module.F7, Module.F4);
////			trans.put(Module.F8, Module.F5);
////			trans.put(Module.F9, Module.F6);
////		}
////		else {
////			trans.put(Module.F1, Module.F6);
////			trans.put(Module.F2, Module.F5);
////			trans.put(Module.F3, Module.F4);
////			
////		}
////		
////		for (Map.Entry<Module, Module> entry : trans.entrySet()) {
////		   if (bus.getId() == entry.getKey()) {
////			   bus.setId(entry.getValue());
////	 
////			   bus.state++;
////			
////			   return;
////		   }
////		   
////		}
////
////		colorize();
////		
////		
////		
////		
////	} 
//
//	
//
//	public void handleMessage(byte[] message, int messageLength, int connector) {
//		
//		bus().receive(new Msg(message),(byte)connector);
//	//	colorize ();
//	}
//
//
//	public ATRONBus bus() {
//		if (bus == null) {
//			bus = new ATRONBus(this);
//		}
//		return bus;
//	}
//	
//	public DebugInformationProvider info() {
//		if (info == null) {
//			info = this.getModule().getDebugInformationProvider();
//		}
//		return info;
//	}
//
//}
