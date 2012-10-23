package ussr.samples.atron.simulations.metaforma.lib;

public enum Orientation {
	BOTTOM_LEFT,BOTTOM_RIGHT,
	TOP_LEFT,TOP_RIGHT,
	LEFT_BOTTOM,
	LEFT_TOP,
	RIGHT_BOTTOM,
	RIGHT_TOP;

	public BorderLine getPrimaryBorder() {
		return BorderLine.valueOf(this.name().split("_")[1]);
	}
	
	public BorderLine getSecondaryBorder() {
		return BorderLine.valueOf(this.name().split("_")[0]);
	}

	public boolean is(BorderLine d) {
		return this.name().contains(d.name());
	}
	

	
}
