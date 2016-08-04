/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package tools.data.output;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import constants.ItemConstants;
import maplestory.guild.MapleGuildEmblem;
import maplestory.inventory.item.CashItem;
import maplestory.inventory.item.EquipItem;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.PetItem;
import maplestory.map.MapleMagicDoor;
import maplestory.party.MapleParty;
import maplestory.party.MapleParty.PartyEntry;
import maplestory.player.MapleCharacterSnapshot;
import maplestory.player.MaplePetInstance;
import maplestory.server.net.PacketFactory;
import maplestory.util.Hex;
import maplestory.util.StringUtil;

/**
 * Writes a maplestory-packet little-endian stream of bytes.
 * 
 * @author Frz
 * @version 1.0
 * @since Revision 352
 */
public class MaplePacketWriter extends GenericLittleEndianWriter {
    private ByteArrayOutputStream baos;

    /**
     * Constructor - initializes this stream with a default size.
     */
    public MaplePacketWriter() {
        this(32);
    }

    /**
     * Constructor - initializes this stream with size <code>size</code>.
     *
     * @param size The size of the underlying stream.
     */
    public MaplePacketWriter(int size) {
        this.baos = new ByteArrayOutputStream(size);
        setByteOutputStream(new BAOSByteOutputStream(baos));
    }

    /**
     * Gets a <code>MaplePacket</code> instance representing this
     * sequence of bytes.
     *
     * @return A <code>MaplePacket</code> with the bytes in this stream.
     */
    public byte[] getPacket() {
        return baos.toByteArray();
    }
    
    public void writePartyStatus(int channel, MapleParty party){
    	List<MapleCharacterSnapshot> members = new LinkedList<>();
    	List<PartyEntry> entries = party.getMembers();
    	
    	for(int i = 0; i < MapleParty.MAX_SIZE;i++){
    		if(entries.size() > i){
    			members.add(entries.get(i).getSnapshot());
    		}else{
    			members.add(new MapleCharacterSnapshot());
    		}
    	}
    	
    	for(MapleCharacterSnapshot snap : members){
    		writeInt(snap.getId());
    	}
    	
    	for(MapleCharacterSnapshot snap : members){
    		writeAsciiString(StringUtil.getRightPaddedStr(snap.getName(), '\0', 13));
    	}
    	
    	for(MapleCharacterSnapshot snap : members){
    		writeInt(snap.getJob());
    	}
    	
    	for(MapleCharacterSnapshot snap : members){
    		writeInt(snap.getLevel());
    	}
    	
    	for(MapleCharacterSnapshot snap : members){
    		if(snap.isOnline()){
    			if(snap.getChannel() == -1){
    				writeInt(Integer.MAX_VALUE);
    			}else{
    				writeInt(snap.getChannel() - 1);
    			}
    		}else{
    			writeInt(-2);
    		}
    	}
    	
    	writeInt(party.getLeader().getSnapshot().getId());
    	
    	for(MapleCharacterSnapshot snap : members){
    		if(!snap.isOnline()){
    			writeInt(0);
    		}else{
        		if(snap.getChannel() == channel){
        			writeInt(snap.getMapId());
        		}else{
        			writeInt(0);
        		}	
    		}
    	}
    	
    	for(MapleCharacterSnapshot snap : members){
    		if(snap.getChannel() == channel){
    			if(snap.isOnline() && snap.getLiveCharacter().getMagicDoors().size() > 0){
					for(MapleMagicDoor door : snap.getLiveCharacter().getMagicDoors()){
						writeInt(door.getTown().getMapId());
						writeInt(door.getTarget().getMapId());
						writeInt(door.getPosition().x);
						writeInt(door.getPosition().y);
					}
				}else{
					writeInt(999999999);
					writeInt(999999999);
					writeInt(0);
					writeInt(0);
				}
    		}else{
    			writeInt(999999999);
				writeInt(999999999);
				writeInt(0);
				writeInt(0);
    		}
    	}
    	
    }
    
    public void writeItemExpiration(long time){
    	writeLong(time);
    }
    
    private void writeItemInfo(Item item, int pos, boolean noPosition){
    	boolean isCash = item instanceof CashItem;
        boolean isPet = item instanceof PetItem;
        boolean isRing = false;
        EquipItem equip = null;
        if (item instanceof EquipItem) {
            equip = (EquipItem) item;
            //isRing = equip.getRingId() > -1;
        }
        if (!noPosition) {
            if (equip != null) {
                if (pos < 0) {
                    pos *= -1;
                }
                writeShort(pos > 100 ? pos - 100 : pos);
            } else {
                write(pos);
            }
        }
        write(isPet ? 3 : (equip == null ? 2 : 1));
        writeInt(item.getItemId());
        writeBool(isCash);
        if (isCash) {
            writeLong(isPet ? ((PetItem)item).getUniqueId() : isRing ? /*equip.getRingId()*/ 0 : ((CashItem) item).getUniqueId());
        }
        if(item instanceof CashItem){
        	writeItemExpiration(((CashItem)item).getExpirationDate());	
        }else{
        	writeItemExpiration(-1);
        }
        
        if (isPet) {
        	PetItem petItem = (PetItem) item;
        	
        	writeAsciiString(StringUtil.getRightPaddedStr(petItem.getPetName(), '\0', 13));
        	write(petItem.getPetLevel());
        	writeShort(petItem.getCloseness());
        	write(petItem.getFullness());
        	writeItemExpiration(petItem.getExpirationDate());
        	writeInt(0);
        	write(new byte[] {(byte) 0x50, (byte) 0x46});
        	writeInt(0);
        	return;
        }
        if (equip == null) {
            writeShort(item.getAmount());
            writeMapleAsciiString(" "+item.getOwner());
            writeShort(item.getFlag()); // flag

            if (ItemConstants.isRechargable(item.getItemId())) {
                writeInt(2);
                write(new byte[]{(byte) 0x54, 0, 0, (byte) 0x34});
            }
            return;
        }
        write(equip.getUpgradeSlotsAvailble()); // upgrade slots
        write(equip.getLevel()); // level
        writeShort(equip.getStr()); // str
        writeShort(equip.getDex()); // dex
        writeShort(equip.getInt()); // int
        writeShort(equip.getLuk()); // luk
        writeShort(equip.getHp()); // hp
        writeShort(equip.getMp()); // mp
        writeShort(equip.getWeaponAttack()); // watk
        writeShort(equip.getMagicAttack()); // matk
        writeShort(equip.getWeaponDefense()); // wdef
        writeShort(equip.getMagicDefense()); // mdef
        writeShort(equip.getAccuracy()); // accuracy
        writeShort(equip.getAvoid()); // avoid
        writeShort(equip.getHands()); // hands
        writeShort(equip.getSpeed()); // speed
        writeShort(equip.getJump()); // jump
        writeMapleAsciiString(" "+equip.getOwner()); // owner name
        writeShort(equip.getFlag()); //Item Flags

        if (isCash) {
            for (int i = 0; i < 10; i++) {
                write(0x40);
            }
        } else {
            write(0);
            write(equip.getItemLevel()); //Item Level
            writeShort(0);
            writeShort(0);//Item EXP //Works pretty weird :s
            writeInt(equip.getHammerUpgrades()); //WTF NEXON ARE YOU SERIOUS?
            writeLong(0);
        }
        writeLong(PacketFactory.getTime(-2));
        writeInt(-1);

    }
    
    
    public void writeItemInfo(Item item){
    	writeItemInfo(item, -1, true);
    }
    
    public void writeItemInfo(Item item, int pos){
    	writeItemInfo(item, pos, false);
    }
    


    
	public void writeGuildEmblem(MapleGuildEmblem emblem) {
		writeShort(emblem.getBackground());
		write(emblem.getBackgroundColor());
		writeShort(emblem.getLogo());
		write(emblem.getLogoColor());
	}

    /**
     * Changes this packet into a human-readable hexadecimal stream of bytes.
     *
     * @return This packet as hex digits.
     */
    @Override
    public String toString() {
        return Hex.toHex(baos.toByteArray());
    }

    public void writeCashItemInformation(CashItem item, int accountId) {
		writeCashItemInformation(item, accountId, null);
	}
	
	public void writeCashItemInformation(CashItem item, int accountId, String giftMessage) {
		writeLong(item.getUniqueId());
		if(giftMessage == null){
			writeInt(accountId);
			writeInt(0);
		}
		writeInt(item.getItemId());
		if(giftMessage == null){
			writeInt(item.getCashShopEntryId());
			writeShort(item.getAmount());
		}
		writeAsciiString(StringUtil.getRightPaddedStr(item.getItemMeta().getGiftFrom(), '\0', 13));
		if(giftMessage != null){
			writeAsciiString(StringUtil.getRightPaddedStr(giftMessage, '\0', 73));
			return;
		}
		writeItemExpiration(item.getExpirationDate());
		writeLong(0);
	}

	public void writePetInfo(MaplePetInstance instance, boolean showpet) {
		PetItem source = instance.getSource();
		write(1);
		if(showpet){
			write(0);
		}
		writeInt(source.getItemId());
		writeMapleAsciiString(source.getPetName());
		writeInt((int) source.getUniqueId());
		writeInt(0);
		writePos(instance.getPosition());
		write(instance.getStance());
		writeInt(instance.getFoothold());
	}

}
