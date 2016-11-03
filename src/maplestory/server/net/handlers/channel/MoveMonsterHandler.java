package maplestory.server.net.handlers.channel;

import java.awt.Point;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.life.MapleMonster;
import maplestory.life.MobSkill;
import maplestory.life.MobSkillFactory;
import maplestory.life.movement.MovementPath;
import maplestory.map.MapleMapObject;
import maplestory.map.MapleMapObjectType;
import maplestory.server.net.PacketFactory;
import maplestory.util.Pair;
import maplestory.util.Randomizer;

public class MoveMonsterHandler extends MovementPacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		int objectid = buf.readInt();
        short moveid = buf.readShort();
        MapleMapObject mmo = client.getCharacter().getMap().getObject(objectid);
        if (mmo == null || mmo.getType() != MapleMapObjectType.MONSTER) {
            return;
        }
        MapleMonster monster = (MapleMonster) mmo;
        if(!monster.isAlive()){
        	return;
        }
        MovementPath res;
        byte skillByte = buf.readByte();
        byte skill = buf.readByte();
        int skill_1 = buf.readByte() & 0xFF;
        byte skill_2 = buf.readByte();
        byte skill_3 = buf.readByte();
        byte skill_4 = buf.readByte();
        buf.skipBytes(8);
        MobSkill toUse = null;
        if (skillByte == 1 && monster.getStats().getSkills().size() > 0) {
            int random = Randomizer.nextInt(monster.getStats().getSkills().size());
            Pair<Integer, Integer> skillToUse = monster.getStats().getSkills().get(random);
            toUse = MobSkillFactory.getMobSkill(skillToUse.getLeft(), skillToUse.getRight());
            int percHpLeft = (monster.getHp() / monster.getMaxHp()) * 100;
            if (toUse.getHp() < percHpLeft || !monster.canUseSkill(toUse)) {
                toUse = null;
            }
        }
        if ((skill_1 >= 100 && skill_1 <= 200) && monster.hasSkill(skill_1, skill_2)) {
            MobSkill skillData = MobSkillFactory.getMobSkill(skill_1, skill_2);
            if (skillData != null && monster.canUseSkill(skillData)) {
                monster.useSkill(skillData, client.getCharacter());
            }
        }
        buf.readByte();
        buf.readInt(); // whatever
        short start_x = buf.readShort(); // hmm.. startpos?
        short start_y = buf.readShort(); // hmm...
        Point startPos = new Point(start_x, start_y);
        res = parseMovement(buf);
        /*if (monster.getController() != client.getCharacter()) {
            if (monster.isAttackedBy(client.getCharacter())) {// aggro and controller change
                monster.switchController(client.getCharacter(), true);
            } else {
                return;
            }
        } else if (skill == -1 && monster.isControllerKnowsAboutAggro() && !monster.isMobile() && !monster.isFirstAttack()) {
            monster.setControllerHasAggro(false);
            monster.setControllerKnowsAboutAggro(false);
        }*/
        //boolean aggro = monster.isControllerHasAggro();
        if (toUse != null) {
            client.sendPacket(PacketFactory.getMonsterMoveResponse(objectid, moveid, monster.getMp(), true, toUse.getSkillId(), toUse.getLevel()));
        } else {
            client.sendPacket(PacketFactory.getMonsterMoveResponse(objectid, moveid, monster.getMp(), true));
        }
        /*if (aggro) {
            monster.setControllerKnowsAboutAggro(true);
        }*/
        if (res != null) {
            client.getCharacter().getMap().broadcastPacket(client.getCharacter(), PacketFactory.getMoveMonsterPacket(skillByte, skill, skill_1, skill_2, skill_3, skill_4, objectid, startPos, res), monster.getPosition());
            res.translateLife(monster);
            mmo.setPosition(monster.getPosition());
        }
	}

}
