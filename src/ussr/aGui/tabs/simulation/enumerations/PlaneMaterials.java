package ussr.aGui.tabs.simulation.enumerations;

import java.util.Vector;

import ussr.physics.PhysicsParameters.Material;

public enum PlaneMaterials {

	
	DEFAULT(Material.DEFAULT),
	RUBBER (Material.RUBBER),
	WOOD(Material.WOOD),
	ICE(Material.ICE),
	CONCRETE(Material.CONCRETE),
	GLASS(Material.GLASS),
	IRON(Material.IRON),
	GRANITE(Material.GRANITE)
	;
	private Material planeMaterial;
	
	PlaneMaterials(Material planeMaterial){
		this.planeMaterial = planeMaterial;
	}
	
	
	/**
 	 * Returns the name of chosen enumeration with changes in it such that Java convention for constants(upper case) is replaced wit lower and
 	 * underscore is replaced with space and so on.
 	 * @return the name of chosen enumeration with changes in it such that Java convention for constants(upper case) is replaced wit lower and
 	 * underscore is replaced with space.
 	 */
 	public String getUserFriendlyName(){
 		char[] characters = this.toString().toLowerCase().toCharArray();
 		String name = (characters[0]+"").toUpperCase();
         for (int index =1;index<characters.length;index++){
         	name = name+characters[index];
         }		 
 		return name;
 	}
 	
 	
	/**
	 * Returns the array of objects representing the names of enumerations in users friendly format.
	 * @return the array of objects representing the names of enumerations in users friendly format.
	 */
	public static Object[] getAllInUserFriendlyFromat(){
 		Vector <String> namesTetxtures = new Vector<String>();
 		for (int textureNr=0;textureNr<values().length;textureNr++){
 			namesTetxtures.add(values()[textureNr].getUserFriendlyName()) ;
 		} 		
 		return namesTetxtures.toArray();
 	}
	
/*	public static String toJavaUSSRConvention(String nameMaterial){
 		return nameMaterial.toUpperCase(); 
 	}*/
}
