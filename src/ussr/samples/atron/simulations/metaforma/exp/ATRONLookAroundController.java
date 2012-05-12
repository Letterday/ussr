package ussr.samples.atron.simulations.metaforma.exp;


import ussr.samples.atron.ATRONController;

public class ATRONLookAroundController extends ATRONController {

	public static void main( String[] args ) {
        new ATRONLookAroundSimulator().main();
    }
	
	@Override
	public void activate() {
		setup();
		int time = 0;
		 while(true) {
		
			 
			 if (time % 2000 < 1000 )
			 {
				 if (module.getProperty("name") == "wheel1left" || module.getProperty("name") == "wheel3left")
					 rotateContinuous( 1);
			 
				 if (module.getProperty("name") == "wheel2right" || module.getProperty("name") == "wheel4right")
					 rotateContinuous( -1);
				 
				
			 }
			 else
			 {
				 if (module.getProperty("name") == "wheel1left" || module.getProperty("name") == "wheel3left")
					 rotateContinuous( -1);
			 
				 if (module.getProperty("name") == "wheel2right" || module.getProperty("name") == "wheel4right")
					 rotateContinuous( 1);
			 
				 
			 }
			 
			 if (module.getProperty("name") == "axleOne5")
			 {
				 if (time % 4000 == 0)
					 rotateToDegree((float) -0.15);
				 
				 if (time % 4000 == 2000)
					 rotateToDegree((float) 0.15);
				 
			 }
			
			 if (module.getProperty("name") == "arm1" || module.getProperty("name") == "arm2")
			 {
				 rotateToDegree((float)Math.sin(time/1000));
				 //rotateContinuous( 1);
				 //if (speed == 2000)
					 //rotateToDegree((int) Math.round(Math.sin(speed / 2000) * 2));
				 //rotate
				 
			 }
			 
			/*	
			 module.getConnectors().get(0).disconnect();
				 module.getConnectors().get(1).disconnect();
				 module.getConnectors().get(2).disconnect();
				 module.getConnectors().get(3).disconnect();
				 module.getConnectors().get(4).disconnect();
				 module.getConnectors().get(5).disconnect();
				*/ 
			 //}
			
			 time = time + 1;
			 yield();
		 }
	}

}
