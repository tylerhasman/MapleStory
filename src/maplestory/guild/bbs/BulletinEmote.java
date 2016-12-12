package maplestory.guild.bbs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BulletinEmote {
	SMILE(0, false),
	SAD(1, false),
	HAPPY(2, false),
	ANGRY(100, true),
	SLY(101, true),
	DOCTOR(102, true),
	LOL(103, true),
	SURPRISE(104, true),
	CRY(105, true),
	FURIUOUS(106, true)
	;
	
	private final int id;

	private final boolean premium;
	
	public static BulletinEmote getById(int icon) {
		for(BulletinEmote emote : values()){
			if(emote.id == icon){
				return emote;
			}
		}
		return null;
	}
	
}
