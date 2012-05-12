package ussr.samples.atron.simulations.metaforma.exp;
//package ussr.samples.atron.simulations.metaforma.experimental;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class ATRONBusPrinter {
//	public static final int NONE = 0;
//	public static final int STATE_UPDATE = 1;
//	public static final int ACTION = 2;
//	public static final int MESSAGE = 4;
//	public static final int STATE_MESSAGE = 8;
//	public static final int WAITING_CONSTRAINT = 16;
//	public static final int STATE_INFO = 32;
//	public static final int WARNING = 64;
//	public static final int DEBUG = 128;
//	
//	public static final int ALL = 255;
//	
//	
//	private ATRONBus bus;
//	private int show;
//	private Map<Integer,Float> printLimiter = new HashMap<Integer,Float>();
//	
//	public ATRONBusPrinter (ATRONBus b, int visibleMessages) {
//		bus = b;
//		show = visibleMessages;
//	}
//	
//	public void print (int type, String s) {
//		if ((show & type) == type) {
//			if (type == WARNING) {
//				System.err.println(bus.getName() + ": " + s);
//			}
//			else {
//				System.out.println(bus.getName() + ": " + s);
//			}
//		}
//		else {
//		//	System.out.println("HIDING!");
//		}
//	}
//	
//	public void limited (int identifier, int type, String s){
//		
//		
//		if (!printLimiter.containsKey(identifier)) {
//			printLimiter.put(identifier,0f);
//		}
//	
//		if (bus.time() - printLimiter.get(identifier) > 5) {
//			print(type,s);
//			printLimiter.put(identifier,bus.getSimulation().getTime());
//		}
//	}
//	
//	public void state () {
//		
//	}
//	
//	public void stateSwitch () {
//		stateSwitch(-1);
//	}
//	
//	public void stateSwitch (int newstate) {
//		//print(STATE_UPDATE, ".state = " + bus.getState() + " -> " + newstate);
//		
//	}
//
//	public void stateBroadcast(int stateNew) {
//		print(STATE_MESSAGE, ".broadcast to " + stateNew + " (old = " + bus.getState() + ")");
//		
//	}
//
//	
//}
