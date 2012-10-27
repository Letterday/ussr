package ussr.samples.atron.simulations.metaforma.lib.Test;


import ussr.samples.atron.simulations.metaforma.gen.BrandtController;
import ussr.samples.atron.simulations.metaforma.gen.BrandtController.*;
import ussr.samples.atron.simulations.metaforma.lib.MfContext;
import ussr.samples.atron.simulations.metaforma.lib.Module;


public class TestCase {
	public MfContext context = new MfContext(new BrandtController());
	
	public static void main(String[] args) {
		new TestCase();
	}
	
	public TestCase () {
		Mod Clover_East = Mod.Clover_East;
		Mod Clover_South = Mod.Clover_South;
		BrandtController.Coll Clover = BrandtController.Coll.Clover;
		BrandtController.Coll F = BrandtController.Coll.F;
		
		if (!Clover.contains(Clover_East) || F.contains(Clover_East)) {
			throw new Error(Clover_East + " should be in " + Clover);
		}
		
		Module.Mod = Mod.NONE;
		
		Module F_10a = new Module(Mod.F,10);
		Module F_10b = Module.value(F_10a.ord());
		
		if (!F_10a.equals(F_10b)) {
			throw  new Error(F_10a + " != " + F_10b);
		}
		
		for (int i=0; i<255;i++) {
			System.out.println(Module.value(i));
		}
		
//		for(Mod m:Mod.values()) {
//			System.out.println(m + ": " + m.ord());
//		}
		
//		context.switchNorthSouth();
//		run();
//		context.switchEWN();
//		run();
//		context.switchEWS();
//		run();
//		context.switchNorthSouth();
//		run();
//		context.switchEWS();
//		run();
//		context.switchEastWestHemisphere(false, false);
//		run();
//		context.switchEastWestHemisphere(false, false);
//		run();
//		context.switchNorthSouth();
//		run();
//		context.switchEastWestHemisphere(true, false);
//		run();
//		context.switchEastWestHemisphere(false, true);
//		run();
//		context.switchEastWestHemisphere(true, true);
//		run();
		
	}
	
	public void run () {
		for (byte b=0; b<8; b++) {
			byte rel = context.abs2rel(b);
			byte abs = context.rel2abs(rel);
			System.out.println(String.format("%d	%d	%b", b,rel,b==abs));
			if (b!=abs) {
				throw new Error("abs(rel(abs)) != abs");
			}
		}
		
	}
	
	
}
