package ussr.samples.atron.simulations.metaforma.lib;

import java.util.ArrayList;
import java.util.BitSet;

import ussr.description.geometry.RotationDescription;
import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.samples.atron.ATRON;

public class MfBuilder {
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
    
	public ArrayList<ModulePosition> buildGrid(BitSet build, IModEnum mod) {
		ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>(); 
		int height = -5;
		buildClover(mod, mPos, aPos(-2,height,2),0);
		buildClover(mod, mPos, aPos(0,height,4),4);
		buildClover(mod, mPos, aPos(2,height,2),8);
		
		buildClover(mod, mPos, aPos(4,height,0),12);
		buildClover(mod, mPos, aPos(2,height,-2),16);

		buildClover(mod, mPos, aPos(6,height,-2),20);
		buildClover(mod, mPos, aPos(-2,height,6),24);
		buildClover(mod, mPos, aPos(-4,height,8),28);
		
		
		//if (build.get(3)) buildClover(prefix, useASE, mPos, aPos(4,-5,0),12);
		
		
        return mPos;
	}
	
	 public ArrayList<ModulePosition> buildEight2(VectorDescription position) {
		 ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>(); 
        mPos.add(new ModulePosition("Struct_0", aPos(0,0,0,position), ATRON.ROTATION_NS_BROKEN));
        mPos.add(new ModulePosition("Struct_1", aPos(1,0,1,position), ATRON.ROTATION_EW));
        mPos.add(new ModulePosition("Struct_2", aPos(1,0,-1,position), ATRON.ROTATION_EW));
        mPos.add(new ModulePosition("Struct_3", aPos(2,0,0,position), ATRON.ROTATION_NS_BROKEN));
        mPos.add(new ModulePosition("Struct_4", aPos(3,0,1,position), ATRON.ROTATION_EW));
        mPos.add(new ModulePosition("Struct_5", aPos(3,0,-1,position), ATRON.ROTATION_EW));
        mPos.add(new ModulePosition("Struct_6", aPos(4,0,0,position), ATRON.ROTATION_NS_BROKEN));
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
