package maplestory.util;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Hex {

	private static Map<String, Object> cache = new HashMap<>();
	
	public static String toHex(byte[] bytes){
		StringBuilder sb = new StringBuilder();
	    for (byte b : bytes) {
	        sb.append(String.format("%02X ", b));
	    }
	    
	    return sb.toString();
	}

	public static byte[] toByteArray(String hex) {
		if(cache.containsKey(hex)){
			return (byte[]) cache.get(hex);
		}
		byte[] b = getByteArrayFromHexString(hex);
		cache.put(hex, b);
		return b;
	}
	
	private static byte[] getByteArrayFromHexString(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nexti = 0;
        int nextb = 0;
        boolean highoc = true;
        outer:
        for (;;) {
            int number = -1;
            while (number == -1) {
                if (nexti == hex.length()) {
                    break outer;
                }
                char chr = hex.charAt(nexti);
                if (chr >= '0' && chr <= '9') {
                    number = chr - '0';
                } else if (chr >= 'a' && chr <= 'f') {
                    number = chr - 'a' + 10;
                } else if (chr >= 'A' && chr <= 'F') {
                    number = chr - 'A' + 10;
                } else {
                    number = -1;
                }
                nexti++;
            }
            if (highoc) {
                nextb = number << 4;
                highoc = false;
            } else {
                nextb |= number;
                highoc = true;
                baos.write(nextb);
            }
        }
        return baos.toByteArray();
    }
	
}
