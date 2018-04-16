package maplestory.server.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;

import lombok.SneakyThrows;

public class Sha256AccountEncryption implements AccountEncryption {

	public String getRandomSalt() {
		SecureRandom sr = new SecureRandom();
		
		byte[] bytes = new byte[64];
		
		sr.nextBytes(bytes);
		
		return toBase64(bytes);
	}
	
	@SneakyThrows
	public String hash(String password, String salt) {
		String combined = salt + password;
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		
		md.update(combined.getBytes());
		
		byte[] digest = md.digest();
		
		return toBase64(digest);
	}
	
	public String toBase64(byte[] bytes){
		Encoder b64 = Base64.getEncoder();
		
		return b64.encodeToString(bytes);
	}
	
}
