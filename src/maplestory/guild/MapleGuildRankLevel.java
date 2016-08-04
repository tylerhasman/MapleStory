package maplestory.guild;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MapleGuildRankLevel {

	/**
	 * Full control over the guild
	 */
	MASTER(4, "rank_master"),
	/**
	 * Has elevated powers compared to members, for example, inviting players
	 */
	JR_MASTER(3, "rank_jr_master"),
	/**
	 * The final rank before jr master
	 */
	MEMBER_3(2, "rank_member_3"),
	/**
	 * The middle member rank
	 */
	MEMBER_2(1, "rank_member_2"),
	/**
	 * The lowest rank
	 */
	MEMBER_1(0, "rank_member_1")
	;

	/**
	 * The order in which maplestory clients read them from a packet
	 */
	@Getter
	private static final MapleGuildRankLevel[] packetOrder = new MapleGuildRankLevel[] {
		
		MASTER,
		JR_MASTER,
		MEMBER_3,
		MEMBER_2,
		MEMBER_1
		
	};
	
	@Getter
	private final int id;
	
	@Getter
	private final String databaseName;
	
	public int getPacketId(){
		return 5 - id;
	}

	public static MapleGuildRankLevel getById(int id) {
		for(MapleGuildRankLevel level : values()){
			if(level.id == id){
				return level;
			}
		}
		return null;
	}
	
}
