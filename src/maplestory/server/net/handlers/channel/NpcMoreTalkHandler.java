package maplestory.server.net.handlers.channel;

import javax.script.ScriptException;

import constants.MessageType;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.script.MapleScriptInstance;
import maplestory.script.NpcConversationManager;
import maplestory.script.QuestScriptManager;
import maplestory.server.net.MaplePacketHandler;

public class NpcMoreTalkHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		byte lastMsg = buf.readByte();
		byte action = buf.readByte();
		
		NpcConversationManager cm = client.getCharacter().getActiveNpcConversation();
		MapleScriptInstance activeNpc = client.getCharacter().getActiveNpc();
		
		if(cm == null)
			return;
		
		try {
			if(lastMsg == 2){
				if(action != 0){
					String text = readMapleAsciiString(buf);
					cm.setInputText(text);
					if(cm instanceof QuestScriptManager){
						activeNpc.questAction(action, lastMsg, -1);
					}else{
						activeNpc.action(action, lastMsg, -1);
					}
				}else{
					client.getCharacter().disposeOpenNpc();
				}
			}else{
				int selection = -1;
				if(buf.readableBytes() >= 4){
					selection = buf.readInt();
				}else if(buf.readableBytes() > 0){
					selection = buf.readByte();
				}
				if(activeNpc != null){
					if(cm instanceof QuestScriptManager){
						activeNpc.questAction(action, lastMsg, selection);
					}else{
						activeNpc.action(action, lastMsg, selection);
					}
				}
			}
		} catch (NoSuchMethodException | ScriptException e) {
			e.printStackTrace();
			client.getCharacter().disposeOpenNpc();
			client.getCharacter().sendMessage(MessageType.POPUP, "An error occured while talking to an npc. Please report this.");
		}
		
	}

}
