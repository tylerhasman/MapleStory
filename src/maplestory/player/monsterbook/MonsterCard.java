package maplestory.player.monsterbook;

import lombok.Getter;

public class MonsterCard {

	@Getter
	private final int monsterId, level;
	
	protected MonsterCard(int monsterId, int level){
		this.monsterId = monsterId;
		this.level = level;
	}
	
	
}
