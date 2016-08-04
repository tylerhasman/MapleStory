package maplestory.guild;

public class GuildNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4027584652467130105L;

	public GuildNotFoundException(int guildId) {
		super("Guild with id "+guildId+" was not found");
	}
	
}
