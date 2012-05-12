package ussr.samples.atron.simulations.metaforma.lib;

import java.util.ArrayList;

import ussr.description.geometry.VectorDescription;
import ussr.description.setup.ModulePosition;
import ussr.samples.atron.ATRON;

public class MetaformaBuilder {
	 public ArrayList<ModulePosition> rectangle(int width, int height, VectorDescription position, String prefix) {
		 ArrayList<ModulePosition> mPos = new ArrayList<ModulePosition>();
	    	int nr = 0;
	    	for (int i=0; i<width; i++) {
	    		for (int j=0; j<height; j++) {
	    			if ((i+j)%2==1) {
	    				mPos.add(new ModulePosition(prefix + nr, aPos(i,0,j,position), (i%2==0) ? ATRON.ROTATION_NS : ATRON.ROTATION_EW));
	    				nr++;
	    			}
	        	}
	    	}
	    	
	    	return mPos;
	    }
	 
	 private static VectorDescription aPos(float x, float y, float z, VectorDescription offset) {
        final float Xoffset = offset.getX();
        final float Yoffset = offset.getY();
        final float Zoffset = offset.getZ();
        return new VectorDescription(x*ATRON.UNIT+Xoffset, y*ATRON.UNIT+Yoffset, z*ATRON.UNIT+Zoffset);
    }
}
