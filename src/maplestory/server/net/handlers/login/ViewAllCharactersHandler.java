package maplestory.server.net.handlers.login;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.player.MapleCharacter;
import maplestory.server.MapleServer;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class ViewAllCharactersHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		List<MapleCharacter> characters = new ArrayList<>();
		
		for(int i = 0; i < MapleServer.getWorlds().size();i++){
			
			List<MapleCharacter> chrs = client.loadCharacters(i);
			
			characters.addAll(chrs);
			
		}
		
		int unk = characters.size() + 3 - characters.size() % 3;
		
		client.sendPacket(PacketFactory.showAllCharacters(characters.size(), unk));
		
		for(int i = 0; i < MapleServer.getWorlds().size();i++){
			
			int worldId = i;//Lambda requires 'i' to be final
			
			List<MapleCharacter> chrs = characters.stream().filter(chr -> chr.getWorldId() == worldId).collect(Collectors.toList());
			
			client.sendPacket(PacketFactory.showAllCharacterInfo(i, chrs));
		}
		
	}

}
