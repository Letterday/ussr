package ussr.samples.atron.simulations.metaforma.lib;


import ussr.samples.atron.simulations.metaforma.gen.Module;



public class MetaformaASEController extends MetaformaReflectionEventController {
	
	public void renameFromTo (int g1, int m1, int g2, int m2) {
		System.out.println(".renameFromTo " + g1 + "," + m1 + "," + g2 + "," + m2);
		//renameFromTo(Module.getOnNumber(g1, m1),Module.getOnNumber(g2, m2));
	}
	
	
	public void disconnect (int g1, int m1, int g2, int m2) {
		//System.out.println(".disconnect()");
//		disconnect(Module.getOnNumber(g1, m1), Module.getOnNumber(g2, m2));
	}
	
//	public void rotate (int g, int m, int degrees) {
//		rotate(Module.getOnNumber(g, m), degrees);
//		
//	}
//	
	public int getModuleId () {
		return getId().getNumber();
	}
	
	
	public void activate() {
		super.activate();	
	}
	
	public void colorize () {
		// just to override
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void handleStates() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getOpStateName() {
		// TODO Auto-generated method stub
		return null;
	}


}
