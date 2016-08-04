package maplestory.party;

import lombok.Getter;

public enum PartyOperationType {

	ERROR(1, true),
	SILENT_UPDATE(7),
	CREATE(8),
	INVITE(4),
	CANT_CREATE_BEGINNER(10, true),
	ERROR_2(11, true),
	LEAVE(12, false, true),
	DISBAND(12, false, true),
	EXPEL(12, false, true),
	CHECK_THE_SOURCE(13, true),
	ERROR_3(14, true),
	JOIN(15),
	ALREADY_IN_PARTY(16, true),
	FULL(17, true),
	NOT_FOUND_IN_CHANNEL(19, true),
	DENY_INVITE(23, true),
	CHANGE_LEADER(27)
	;
	
	@Getter
	private final byte code;
	
	@Getter
	private final boolean message;
	
	@Getter
	private final boolean isDestroyOperation;
	
	private PartyOperationType(int code) {
		this(code, false);
	}
	
	private PartyOperationType(int code, boolean msg) {
		this(code, msg, false);
	}
	
	private PartyOperationType(int code, boolean msg, boolean isDestroy) {
		this.code = (byte) code;
		message = msg;
		isDestroyOperation = isDestroy;
	}
	
}
