package maplestory.util;

public class Hex {

	public static String toHex(byte[] bytes){
		StringBuilder sb = new StringBuilder();
	    for (byte b : bytes) {
	        sb.append(String.format("%02X ", b));
	    }
	    
	    return sb.toString();
	}

	public static byte[] toByteArray(String hex) {
	
		byte[] b = getByteArrayFromHexString(hex);
		
		return b;
	}
	
	/**
	 * Credit to http://stackoverflow.com/a/140861/2284885
	 */
	private static byte[] getByteArrayFromHexString(String hex) {
	    int len = hex.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
	                             + Character.digit(hex.charAt(i+1), 16));
	    }
	    return data;
    }
	
}
