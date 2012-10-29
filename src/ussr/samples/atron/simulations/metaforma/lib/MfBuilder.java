package ussr.samples.atron.simulations.metaforma.lib;

import java.util.ArrayList;

import ussr.description.geometry.RotationDescription;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.samples.atron.ATRON;

public class MfBuilder {
	ConfigurationParams set = ConfigurationParams.getInst();
	
	
	public ArrayList<ModulePosition> buildRectangle (int width, int height, IModEnum mod) {
    	ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>(); 
    	
    	// Build floor
    	int i = 0;
    	for (int w = 0; w < width; w++) {
    		for (int h = 0; h < height; h++) {    			
    			RotationDescription rot = null; 
				if (w%2==0 && h%2==1) {
					rot = ATRON.ROTATION_NS;
				}
				else if (w%2==1 && h%2==0) {
					rot = ATRON.ROTATION_EW;
				}
				if (rot != null) {
					mPos.add(new ModulePosition(mod + "_" + i, aPos ((float)w,0,(float)h,aPos(0,-5,0,new VectorDescription())), rot));
					
					i++;
				}
    		}
    	}
    	return mPos;
    }
	
 	
    private void buildClover(IModEnum mod, ArrayList<ModulePosition> mPos, VectorDescription pos,int startId) {
    	
		mPos.add(new ModulePosition(mod + "_" + (startId + 0), aPos (0,0,0, pos), ATRON.ROTATION_NS));;
		mPos.add(new ModulePosition(mod + "_" + (startId + 1), aPos (2,0,0, pos), ATRON.ROTATION_NS));
		mPos.add(new ModulePosition(mod + "_" + (startId + 2), aPos (1,0,-1, pos), ATRON.ROTATION_EW));
		mPos.add(new ModulePosition(mod + "_" + (startId + 3), aPos (1,0,1, pos), ATRON.ROTATION_EW));
		
    }
    
    public ArrayList<ModulePosition> buildCar(int numberOfWheels, IModEnum mod) {
    	ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>();
    	VectorDescription offset = new VectorDescription(2,0,0);
        if(numberOfWheels==2) {
            mPos.add(new ModulePosition(mod + "_" + 0, aPos(-1,-1,0,offset), ATRON.ROTATION_UD));
            mPos.add(new ModulePosition(mod + "_" + 1, aPos(-1,-2,1,offset), ATRON.ROTATION_SN));
            mPos.add(new ModulePosition(mod + "_" + 2, aPos(-1,-2,-1,offset), ATRON.ROTATION_NS));
        } 
        else if(numberOfWheels==4) {
            mPos.add(new ModulePosition(mod + "_" + 0, aPos(0,0,0,offset), ATRON.ROTATION_EW));
            mPos.add(new ModulePosition(mod + "_" + 1, aPos(1,-1,0,offset), ATRON.ROTATION_UD));
            mPos.add(new ModulePosition(mod + "_" + 2, aPos(-1,-1,0,offset), ATRON.ROTATION_UD));
            mPos.add(new ModulePosition(mod + "_" + 3, aPos(-1,-2,1,offset), ATRON.ROTATION_SN));
            mPos.add(new ModulePosition(mod + "_" + 4, aPos(-1,-2,-1,offset), ATRON.ROTATION_NS));
            mPos.add(new ModulePosition(mod + "_" + 5, aPos(1,-2,1,offset), ATRON.ROTATION_SN));
            mPos.add(new ModulePosition(mod + "_" + 6, aPos(1,-2,-1,offset), ATRON.ROTATION_NS));
        } 
        else {
            throw new Error("Not implemented yet");
        }
        return mPos;
    }
    
	public ArrayList<ModulePosition> buildGrid(IModEnum mod) {
		ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>(); 
		int height = -5;
		
		if (set.getLadderWidth() == 2) {
			for (int i=1; i< set.getLadderLength()+1; i++) {
				buildClover(mod, mPos, aPos(i*2,height,4-i*2),i-1*4);
			}
		}
		
		for (int i=0; i< set.getLadderLength(); i++) {
			buildClover(mod, mPos, aPos(i*2,height,-i*2),set.getLadderLength()*4+i*4);
		}
		
		
		if (set.getLadderBegin()) {
			buildClover(mod, mPos, aPos(-2,height,-2),set.getLadderLength()*8+4);
		}
		
	
	
		
		
        return mPos;
	}
	
	
	public ArrayList<ModulePosition> buildGroupsOfThree(IModEnum mod) {
		ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>(); 
		int height = -5;
		
	
//		buildClover(mod, mPos, aPos(2,height,2),0);
//		buildClover(mod, mPos, aPos(0,height,0),4);
//		buildClover(mod, mPos, aPos(2,height,-2),8);
//		
//		buildClover(mod, mPos, aPos(12,height,12),12);
//		buildClover(mod, mPos, aPos(10,height,10),16);
//		buildClover(mod, mPos, aPos(8,height,12),20);
		

		
		buildClover(mod, mPos, aPos(-8,height,-8),24);
		buildClover(mod, mPos, aPos(-6,height,-6),28);
		buildClover(mod, mPos, aPos(-8,height,-4),32);
		
		
		buildClover(mod, mPos, aPos(-16,height,-16),36);
		buildClover(mod, mPos, aPos(-18,height,-18),40);
		buildClover(mod, mPos, aPos(-20,height,-16),44);

	
		
		
        return mPos;
	}
	
	 public ArrayList<ModulePosition> buildEight(IModEnum mod) {
		 ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>();
		VectorDescription position = new VectorDescription();
		mPos.add(new ModulePosition(mod + "_0", aPos(0,0,0,position), ATRON.ROTATION_NS_BROKEN));
		mPos.add(new ModulePosition(mod + "_1", aPos(1,0,-1,position), ATRON.ROTATION_EW));
		mPos.add(new ModulePosition(mod + "_2", aPos(1,0,1,position), ATRON.ROTATION_EW));
		mPos.add(new ModulePosition(mod + "_3", aPos(2,0,0,position), ATRON.ROTATION_NS_BROKEN));
		mPos.add(new ModulePosition(mod + "_4", aPos(3,0,-1,position), ATRON.ROTATION_EW));
		mPos.add(new ModulePosition(mod + "_5", aPos(3,0,1,position), ATRON.ROTATION_EW));
		mPos.add(new ModulePosition(mod + "_6", aPos(4,0,0,position), ATRON.ROTATION_NS_BROKEN));
		return mPos;
    }
	    
	
	
	private static VectorDescription aPos(float x, float y, float z) {
		return aPos(x,y,z,new VectorDescription());
	}
	 
	 private static VectorDescription aPos(float x, float y, float z, VectorDescription offset) {
        final float Xoffset = offset.getX();
        final float Yoffset = offset.getY();
        final float Zoffset = offset.getZ();
        return new VectorDescription(x*ATRON.UNIT+Xoffset, y*ATRON.UNIT+Yoffset, z*ATRON.UNIT+Zoffset);
    }


	
}
