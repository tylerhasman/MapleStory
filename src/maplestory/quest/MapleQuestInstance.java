package maplestory.quest;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import maplestory.util.StringUtil;

public class MapleQuestInstance {

	@Setter
	private int status;
	
	private int questId;
	
	@Getter
	private int forfeits;
	
	@Getter @Setter
	private int npc;
	
	@Getter @Setter
	private long completionTime;
	
	@Getter
	private Map<Integer, Integer> progress;
	
	public MapleQuestInstance(MapleQuest quest, int forfeits, int npc, MapleQuestStatus status) {
		questId = quest.getId();
		this.forfeits = forfeits;
		this.npc = npc;
		this.status = status.getId();
		progress = new HashMap<>();
		if(status == MapleQuestStatus.STARTED){
			loadMobs(quest);
		}
	}
	
	public void setProgress(int id, int value){
		progress.put(id, value);
	}
	
	public void setAnyProgress(int value) {
		if(progress.size() == 0) {
			setProgress(0, value);
		}else {
			progress.put(progress.keySet().iterator().next(), value);
		}
	}
	
	private void loadMobs(MapleQuest quest){
		for(int mob : quest.getQuestInfo().getRelevantMobs().keySet()){
			setProgress(mob, 0);
		}
	}
	
	public int getProgress(int id){
		if(!progress.containsKey(id)){
			return 0;
		}
		return progress.get(id);
	}
	
	public int getAnyProgress() {
		if(progress.size() == 0) {
			return getProgress(0);
		}
		return getProgress(progress.keySet().iterator().next());
	}
	
	
	public MapleQuestStatus getStatus(){
		return MapleQuestStatus.getById(status);
	}
	
	public MapleQuest getQuest(){
		return MapleQuest.getQuest(questId);
	}
	
	public void complete(){
		completionTime = System.currentTimeMillis();
		status = MapleQuestStatus.COMPLETED.getId();
		progress.clear();
	}
	
	public void forfeit(){
		forfeits++;
		status = MapleQuestStatus.NOT_STARTED.getId();
	}
	
	@AllArgsConstructor
	public static enum MapleQuestStatus{
		NOT_STARTED(0),
		STARTED(1),
		COMPLETED(2);
		
		@Getter
		private final int id;
		
		public static MapleQuestStatus getById(int id){
			for(MapleQuestStatus qs : values()){
				if(qs.id == id){
					return qs;
				}
			}
			return null;
		}
	}

	public boolean progressMob(int id) {
		if(progress.containsKey(id)){
			int amount = getProgress(id);
			if(getQuest().getQuestInfo().getRelevantMobs().get(id) < amount + 1){
				return false;
			}
			amount++;
			progress.put(id, amount);
			return true;
		}
		return false;
	}
	
	public String getInfo(){
		return String.valueOf(getProgress(0));
	}

	public String getQuestData() {
        StringBuilder str = new StringBuilder();
        for (int ps : progress.values()) {
            str.append(StringUtil.getLeftPaddedStr(String.valueOf(ps), '0', 3));
        }
        return str.toString();
    }

}
