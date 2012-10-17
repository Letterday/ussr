package ussr.samples.atron.simulations.metaforma.lib.Test;


import ussr.samples.atron.simulations.metaforma.gen.BrandtController;
import ussr.samples.atron.simulations.metaforma.lib.MfContext;
import ussr.samples.atron.simulations.metaforma.lib.SettingsBase;


public class TestCase {
	public MfContext context = new MfContext(new BrandtController(new SettingsBase()));
	
	public static void main(String[] args) {
		new TestCase();
	}
	
	public TestCase () {
		context.switchNorthSouth();
		run();
		context.switchEWN();
		run();
		context.switchEWS();
		run();
		context.switchNorthSouth();
		run();
		context.switchEWS();
		run();
		context.switchEastWestHemisphere(false, false);
		run();
		context.switchEastWestHemisphere(false, false);
		run();
		context.switchNorthSouth();
		run();
		context.switchEastWestHemisphere(true, false);
		run();
		context.switchEastWestHemisphere(false, true);
		run();
		context.switchEastWestHemisphere(true, true);
		run();
		
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
