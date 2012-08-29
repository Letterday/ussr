package ussr.samples.atron.simulations.metaforma.lib;

import ussr.samples.atron.ATRONController;

public abstract class MetaformaApi  extends ATRONController {
	protected static final int MAX_BYTE = Byte.MAX_VALUE;
	
	protected static byte min(int i, int j) {
		return (byte) Math.min(i, j);
	}
	
	protected static byte max(int i, int j) {
		return (byte) Math.max(i, j);
	}

	public static boolean isNORTH(int c) {
		return c >= 0 && c <= 3;
	}
	
	public static boolean isSOUTH(int c) {
		return c >= 4 && c <= 7;
	}
	
	public static boolean isWEST(int c) {
		return c == 0 || c == 1 || c == 4 || c == 5;
	}
	
	public static boolean isEAST(int c) {
		return c == 2 || c == 3 || c == 6 || c == 7;
	}
	
	public static boolean isMALE(int c) {
		return c%2 == 0;
	}
	
	public static boolean isFEMALE(int c) {
		return c%2 == 1;
	}
	
	
}
