package ussr.samples.atron.simulations.metaforma.gen;


import ussr.samples.atron.network.ATRONReflectionEventController;



public class ASEController extends ATRONReflectionEventController {

	public int test() {
		return 4;
	}
	
	
	
	public void activate() {
		super.activate();
		System.out.println("activate!!");
		//eventConnection.sendEvent("test", new Object[]{"hoi"});
//		while (true) {
//			yield();
//		}
	}


}
