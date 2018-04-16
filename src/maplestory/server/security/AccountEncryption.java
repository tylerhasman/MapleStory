package maplestory.server.security;

public interface AccountEncryption {

	public String getRandomSalt();
	
	public String hash(String password, String salt);
	
}
