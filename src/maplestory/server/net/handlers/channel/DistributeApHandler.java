package maplestory.server.net.handlers.channel;

import constants.MapleStat;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleJob;
import maplestory.server.net.MaplePacketHandler;

public class DistributeApHandler extends MaplePacketHandler {
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) {

		buf.readInt();
		int num = buf.readInt();
		
		MapleCharacter chr = client.getCharacter();
		
		if(chr.getRemainingAp() > 0){
			MapleStat stat = MapleStat.getByValue(num);
			
			if(addStat(client, stat)){
				chr.setRemainingAp(chr.getRemainingAp() - 1);
			}
			
		}
		
		client.sendReallowActions();
	}
	
    private static boolean addStat(MapleClient c, MapleStat stat) {

    	if(stat == MapleStat.MAXHP){
    		addHP(c.getCharacter(), addHP(c));
    	}else if(stat == MapleStat.MAXMP){
    		addMP(c.getCharacter(), addMP(c));
    	}else{
            return c.getCharacter().addStat(stat, 1) == 0;	
    	}

        return true;
    }
    
    static int addHP(MapleClient c) {
        MapleCharacter player = c.getCharacter();
        MapleJob job = player.getJob();
        int MaxHP = player.getMaxHp();
        if (/*player.getHpMpApUsed() > 9999 || */MaxHP >= 30000) {
            return MaxHP;
        }
        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            MaxHP += 20;
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            MaxHP += 6;
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            MaxHP += 16;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            MaxHP += 18;
        } else {
            MaxHP += 8;
        }
        return MaxHP;
    }

    static int addMP(MapleClient c) {
        MapleCharacter player = c.getCharacter();
        int MaxMP = player.getMaxMp();
        MapleJob job = player.getJob();
        if (/*player.getHpMpApUsed() > 9999 || */player.getMaxMp() >= 30000) {
            return MaxMP;
        }
        if (job.isA(MapleJob.WARRIOR) || job.isA(MapleJob.DAWNWARRIOR1) || job.isA(MapleJob.ARAN1)) {
            MaxMP += 2;
        } else if (job.isA(MapleJob.MAGICIAN) || job.isA(MapleJob.BLAZEWIZARD1)) {
            MaxMP += 18;
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.WINDARCHER1) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.NIGHTWALKER1)) {
            MaxMP += 10;
        } else if (job.isA(MapleJob.PIRATE) || job.isA(MapleJob.THUNDERBREAKER1)) {
            MaxMP += 14;
        } else {
            MaxMP += 6;
        }
        return MaxMP;
    }

    static void addHP(MapleCharacter player, int MaxHP) {
        MaxHP = Math.min(30000, MaxHP);
        //player.setHpMpApUsed(player.getHpMpApUsed() + 1);
        player.setMaxHp(MaxHP);
        //player.updateSingleStat(MapleStat.MAXHP, MaxHP);
    }

    static void addMP(MapleCharacter player, int MaxMP) {
        MaxMP = Math.min(30000, MaxMP);
        //player.setHpMpApUsed(player.getHpMpApUsed() + 1);
        player.setMaxMp(MaxMP);
       // player.updateSingleStat(MapleStat.MAXMP, MaxMP);
    }

}
