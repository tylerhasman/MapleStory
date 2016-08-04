package maplestory.guild;

import lombok.Getter;

public enum GuildOperationType {

	CHANGE_RANK(0x40),
	DENY_INVITATION(0x37),
	CAPACITY_CHANGE(0x3A),
	DISBAND(0x32),
	EMBLEM_CHANGE(0x42),
	INVITE(0x05),
	UPDATE_MEMBER(0x3C),
	UPDATE_ONLINE(0x3D),
	UPDATE_NOTICE(0x44),
	QUEST_WAITING_NOTICE(0x4C),
	MEMBER_LEAVE(0x2C),
	MEMBER_EXPEL(0x2F),
	ADD_MEMBER(0x27),
	UPDATE_RANK_NAMES(0x3E),
	SHOW_GUILD_INFO(0x1A),
	SHOW_GUILD_RANKS(0x49),
	SHOW_PLAYER_RANKS(0x49),
	UPDATE_GUILD_POINTS(0x48)
	;
	
	@Getter
	private final byte code;
	
	private GuildOperationType(int code) {
		this.code = (byte) code;
	}

	public static GuildOperationType getLeaveCode(boolean expelled){
		return expelled ? MEMBER_EXPEL : MEMBER_LEAVE;
	}
	
}
