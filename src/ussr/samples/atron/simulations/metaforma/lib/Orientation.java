package ussr.samples.atron.simulations.metaforma.lib;

public enum Orientation {
	BOTTOM_LEFT,BOTTOM_RIGHT,
	TOP_LEFT,TOP_RIGHT,
	LEFT_BOTTOM,
	LEFT_TOP,
	RIGHT_BOTTOM,
	RIGHT_TOP;

	public Direction getPrimaryBorder() {
		return Direction.valueOf(this.name().split("_")[1]);
	}
	
	public Direction getSecondaryBorder() {
		return Direction.valueOf(this.name().split("_")[0]);
	}

	public boolean is(Direction d) {
		return this.name().contains(d.name());
	}
	

	
}
