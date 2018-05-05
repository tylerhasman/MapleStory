package maplestory.server.security;

import org.mindrot.BCrypt;

public class BCryptAccountEncryption implements AccountEncryption {

	@Override
	public String getRandomSalt() {
		return BCrypt.gensalt();
	}

	@Override
	public String hash(String password, String salt) {
		return BCrypt.hashpw(password, salt);
	}

}
