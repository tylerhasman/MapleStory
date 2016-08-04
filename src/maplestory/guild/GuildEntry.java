package maplestory.guild;

import java.lang.ref.WeakReference;

import lombok.Getter;
import lombok.Setter;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleCharacterSnapshot;

public class GuildEntry {
	
	private WeakReference<MapleCharacter> player;
	@Getter
	private MapleCharacterSnapshot snapshot;
	@Getter
	private final int guildId;
	
	public GuildEntry(int guildId, MapleCharacter chr) {
		this(guildId, chr.createSnapshot());
		player = new WeakReference<>(chr);
	}
	
	public GuildEntry(int guildId, MapleCharacterSnapshot snapshot){
		this.guildId = guildId;
		this.snapshot = snapshot;
		player = new WeakReference<MapleCharacter>(null);
	}
	
	public void updateSnapshot(){
		MapleCharacter chr = player.get();
		if(chr != null){
			snapshot = chr.createSnapshot();
		}
	}
	
	public void updatePlayerReference(MapleCharacter chr){
		if(chr.getId() != snapshot.getId()){
			throw new IllegalArgumentException("Character is not this character! Expected "+snapshot.getId()+" got "+chr.getId());
		}
		
		player = new WeakReference<>(chr);
	}

	public boolean isOnline() {
		return snapshot.isOnline();
	}
	
}