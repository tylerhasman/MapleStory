package maplestory.server.net.handlers.login;

import java.sql.SQLException;

import constants.EquipSlot;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemFactory;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleJob;
import maplestory.server.net.MaplePacketHandler;
import maplestory.server.net.PacketFactory;

public class CreateCharHandler extends MaplePacketHandler {

	@Override
	public void handle(ByteBuf buf, MapleClient client) {
		
		String name = readMapleAsciiString(buf);
		
		int job = buf.readInt();
		int face = buf.readInt();
		int hair = buf.readInt() + buf.readInt();
		int skincolor = buf.readInt();
		int top = buf.readInt();
		int bottom = buf.readInt();
		int shoes = buf.readInt();
		int weapon = buf.readInt();
		byte gender = buf.readByte();
		int cap = -1;
		int guideBook = 0;
		
		try {
			if(MapleCharacter.checkNameTaken(name, client.getWorld())){
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		MapleCharacter character = MapleCharacter.getDefault(client);
		
		character.setWorld(client.getWorld().getId());

		character.setName(name);
		
		if(job == 0){
			character.setMapId(130030000);
			character.setJob(MapleJob.NOBLESSE);
			guideBook = 4161047;
		}else if(job == 1){
			character.setMapId(0);
			character.setJob(MapleJob.BEGINNER);
			guideBook = 4161001;
		}else if(job == 2){
			character.setMapId(914000000);
			character.setJob(MapleJob.LEGEND);
			guideBook = 4161048;
		}else{
			return;
		}
		
		character.getInventory(guideBook).addItem(ItemFactory.getItem(guideBook, 1));
		
		character.setFace(face);
		character.setSkinColor(skincolor);
		character.setGender(gender);
		character.setHair(hair);
		
		Inventory inventory = character.getInventory(InventoryType.EQUIPPED);
		
		if(cap >= 0){
			Item cap_eq = ItemFactory.getItem(cap, 1);
			inventory.setItem(EquipSlot.HAT.getSlot(), cap_eq);
		}
		
		Item top_eq = ItemFactory.getItem(top, 1);
		Item bottom_eq = ItemFactory.getItem(bottom, 1);
		Item shoes_eq = ItemFactory.getItem(shoes, 1);
		Item weapon_eq = ItemFactory.getItem(weapon, 1);
		inventory.setItem(EquipSlot.TOP.getSlot(), top_eq);
		inventory.setItem(EquipSlot.PANTS.getSlot(), bottom_eq);
		inventory.setItem(EquipSlot.SHOES.getSlot(), shoes_eq);
		inventory.setItem(EquipSlot.WEAPON.getSlot(), weapon_eq);
		
		try {
			character.saveToDatabase(true);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		
		client.sendPacket(PacketFactory.addNewCharEntry(character));
		
	}

}
