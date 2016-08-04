package maplestory.server.net.handlers.channel;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.server.net.MaplePacketHandler;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;

public class KeymapChangeHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		if(buf.readableBytes() != 8){
			
			buf.readInt();
			
			int numChanges = buf.readInt();
			
			for(int i = 0; i < numChanges;i++){
				
				int key = buf.readInt();
				int type = buf.readByte();
				int action = buf.readInt();
				
				Skill skill = SkillFactory.getSkill(action);
				
				if(skill != null && client.getCharacter().getSkillLevel(skill) <= 0){
					continue;
				}
				
				client.getCharacter().setKeybinding(key, type, action);
			}
			
		}
	}

}
