package maplestory.life;

import java.util.concurrent.TimeUnit;

import constants.MessageType;
import tools.TimerManager;
import tools.TimerManager.MapleTask;
import lombok.Getter;
import lombok.Setter;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;

public class MapleMount {

	@Getter
	private int itemId, skillId;
	
	@Getter @Setter
	private int tiredness, exp, level;
	
	private MapleTask tiredenessSchedule;
	private MapleCharacter owner;
	
	@Getter @Setter
	private boolean active;
	
	public MapleMount(MapleCharacter owner, int id, int skillid) {
		itemId = id;
		this.skillId = skillid;
		tiredness = 0;
		level = 1;
		exp = 0;
		this.owner = owner;
		active = true;
	}
	
	public MountType getMountType() {
		for(MountType mt : MountType.values()){
			if(mt.itemid == itemId){
				return mt;
			}
		}
		
		return MountType.NONE;
	}
	
	private void increaseTiredness(){
		if(owner != null){
			tiredness++;
			owner.getMap().broadcastPacket(PacketFactory.getUpdateMount(owner.getId(), this, false));
			if(tiredness > 99){
				tiredness = 95;
				owner.dispelSkill(owner.getJob().getId() * 10000000 + 1004);
				owner.sendMessage(MessageType.PINK_TEXT, "Your mount is tired and requires food.");
			}
		}else{
			cancelSchedule();
		}
	}
	
	public void cancelSchedule() {
		if(tiredenessSchedule != null){
			tiredenessSchedule.cancel(false);
			tiredenessSchedule = null;
		}
	}

	public void startSchedule(){
		cancelSchedule();
		tiredenessSchedule = TimerManager.scheduleRepeatingTask(new Runnable() {
			
			@Override
			public void run() {
				increaseTiredness();
			}
		}, 60, 60, TimeUnit.SECONDS);
	}
	
	public static enum MountType{
		
		HOG(1902000),
		SILVER_MANE(1902001),
		RED_DRACO(1902002),
		MIMIANA(1902005),
		MIMIO(1902006),
		SHINJOU(1902007),
		FROG(1902008),
		OSTRICH(1902009),
		FROG_2(1902010),
		TURTLE(1902011),
		YETI(1902012),
		NONE(0)
		;
		
		@Getter
		private final int itemid;
		
		private MountType(int itemid) {
			this.itemid = itemid;
		}
		
	}
	
}
