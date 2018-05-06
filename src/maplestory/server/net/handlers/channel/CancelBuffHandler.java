package maplestory.server.net.handlers.channel;

import constants.skills.Bishop;
import constants.skills.Bowmaster;
import constants.skills.Corsair;
import constants.skills.DarkKnight;
import constants.skills.FPArchMage;
import constants.skills.Hermit;
import constants.skills.ILArchMage;
import constants.skills.Marksman;
import constants.skills.Priest;
import constants.skills.Ranger;
import constants.skills.Sniper;
import constants.skills.WindArcher;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;
import maplestory.skill.SkillFactory;

public class CancelBuffHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {

		int sourceid = buf.readInt();

		MapleCharacter chr = client.getCharacter();

		switch (sourceid) {
		case FPArchMage.BIG_BANG:
		case ILArchMage.BIG_BANG:
		case Bishop.BIG_BANG:
		case Bowmaster.HURRICANE:
		case Marksman.PIERCING_ARROW:
		case Corsair.RAPID_FIRE:
		case WindArcher.HURRICANE:
			chr.getMap().broadcastPacket(PacketFactory.skillCancel(client.getCharacter(), sourceid), chr.getId());
			break;
		default:
			client.getCharacter().cancelEffect(SkillFactory.getSkill(sourceid).getEffect(1));
			break;
		}

	}


}
