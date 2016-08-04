package maplestory.guild;

import database.QueryResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MapleGuildEmblem {

	private int background = 0;
	private int backgroundColor = 0;
	private int logo = 0;
	private int logoColor = 0;
	
	protected MapleGuildEmblem(QueryResult query) {
		this.background = query.get("emblem_background");
		this.backgroundColor = query.get("emblem_background_color");
		this.logo = query.get("emblem_logo");
		this.logoColor = query.get("emblem_logo_color");
	}
	
}
