var InventoryType = Java.type("maplestory.inventory.InventoryType");
var InfoProvider = Java.type("maplestory.inventory.item.ItemInfoProvider");


function start(){
	chr = cm.getCharacter();
	
	str = "Here are your stats: \r\n\r\n";
	
	str += "#rStrength: "+chr.getBaseStr() +" => "+chr.getStr()+"\r\n";
	str += "#dDexterity: "+chr.getBaseDex() +" => "+chr.getDex()+"\r\n";
	str += "#kLuck: "+chr.getBaseLuk() +" => "+chr.getLuk()+"\r\n";
	str += "#bIntelligence: "+chr.getBaseInt() +" => "+chr.getInt()+"\r\n\r\n";
	
	str += "#rMax HP: "+chr.getBaseMaxHp() +" => "+chr.getMaxHp()+"\r\n";
	str += "#bMax MP: "+chr.getBaseMaxMp() +" => "+chr.getMaxMp()+"\r\n";
	
	str += "#rWeapon ATK: "+chr.getWeaponAttack()+"\r\n";
	str += "#bMagic ATK: "+chr.getMagicAttack()+"\r\n\r\n";
	
	str += "#b Displayed Damage Range: "+chr.getMinDamage()+" - "+chr.getMaxDamage()+"\r\n\r\n";
	
	
	cm.sendOk(str);
	
	cm.dispose();
}