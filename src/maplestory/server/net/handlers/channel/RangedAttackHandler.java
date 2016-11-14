package maplestory.server.net.handlers.channel;

import constants.ItemConstants;
import constants.MapleBuffStat;
import constants.skills.DawnWarrior;
import constants.skills.NightWalker;
import constants.skills.Sniper;
import constants.skills.ThunderBreaker;
import constants.skills.WindArcher;
import io.netty.buffer.ByteBuf;
import maplestory.client.MapleClient;
import maplestory.inventory.CashInventory;
import maplestory.inventory.Inventory;
import maplestory.inventory.InventoryType;
import maplestory.inventory.MapleWeaponType;
import maplestory.inventory.item.Item;
import maplestory.inventory.item.ItemInfoProvider;
import maplestory.inventory.item.ItemType;
import maplestory.player.MapleCharacter;
import maplestory.server.net.PacketFactory;
import maplestory.skill.MapleStatEffect;
import maplestory.skill.Skill;
import maplestory.skill.SkillFactory;
import maplestory.util.Randomizer;

public class RangedAttackHandler extends AbstractDealDamageHandler {
	
	@Override
	public void handle(ByteBuf buf, MapleClient client) {

		MapleCharacter chr = client.getCharacter();
		
		AttackInfo attack = parseDamage(buf, chr, true);
		
		if(chr.getBuffEffect(MapleBuffStat.MORPH) != null){
			if(chr.getBuffEffect(MapleBuffStat.MORPH).isMorphWithoutAttack()){
				return;
			}
		}
		
		Item weapon = chr.getInventory(InventoryType.EQUIPPED).getItem(-11);
		
		MapleWeaponType weaponType = ItemInfoProvider.getWeaponType(weapon.getItemId());
		
		if(weaponType == MapleWeaponType.NOT_A_WEAPON){
			return;
		}
		
		int projectile = 0;
		int bulletCount = 1;
		MapleStatEffect effect = null;
		if(attack.skill != 0){
			effect = attack.getAttackEffect(chr, null);
			
			bulletCount = effect.getBulletCount();
			if(effect.getCooldown() > 0){
				chr.addCooldown(SkillFactory.getSkill(effect.getSourceId()), attack.skilllevel);
			}
		}
		boolean hasShadowPartner = chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != 0;
		
		if(hasShadowPartner){
			bulletCount *= 2;
		}
		
		Inventory inv = chr.getInventory(InventoryType.USE);
		
		projectile = inv.getProjectileId(bulletCount, weapon);
		
		boolean soulArrow = chr.getBuffedValue(MapleBuffStat.SOULARROW) != 0;
		boolean shadowClaw = chr.getBuffedValue(MapleBuffStat.SHADOW_CLAW) != 0;
		
		
		if(projectile != 0){
			
			if(!soulArrow && !shadowClaw && !isConsumeException(attack.skill)){
				
				int bulletConsume = bulletCount;
				
				if(effect != null && effect.getBulletConsume() != 0){
					bulletConsume = (effect.getBulletConsume() * (hasShadowPartner ? 2 : 1));
				}
				
				inv.removeItem(projectile, bulletConsume);
				
			}
			
		}
		
		if(projectile != 0 || soulArrow || isConsumeException(attack.skill)){
			int visProjectile = projectile;
			if(ItemType.THROWING_STAR.isThis(projectile)){
				CashInventory cashInventory = (CashInventory) chr.getInventory(InventoryType.CASH);
				
				int change = cashInventory.getVisibleProjectile();
				if(change >= 0){
					visProjectile = change;
				}
			}else if(soulArrow || attack.skill == WindArcher.PUPPET || attack.skill == Sniper.ARROW_ERUPTION || isConsumeException(attack.skill)){
				visProjectile = 0;
			}
			
			chr.getMap().broadcastPacket(PacketFactory.rangedAttack(chr, attack, visProjectile));
			
			if(effect != null){
				
				int money = effect.getMoneyCon();
				if(money != 0){
					int moneyMod = money / 2;
					money += Randomizer.nextInt(moneyMod);
					if(money > chr.getMeso()){
						money = chr.getMeso();
					}
					chr.giveMesos(-money);
				}
				
			}
			
			if(attack.skill != 0){
				Skill skill = SkillFactory.getSkill(attack.skill);
				MapleStatEffect effect2 = skill.getEffect(chr.getSkillLevel(skill));
				
				if(effect2.getCooldown() > 0){
					if(chr.isSkillCoolingDown(skill)){
						return;
					}else{
						chr.addCooldown(skill);
					}
				}
			}
			
			applyAttack(attack, chr, bulletCount);
			
			/*if(hasDifferentStanceRequirement(attack.skill)){
				chr.getMap().broadcastPacket(PacketFactory.rangedAttack(chr, attack.skill, attack.skilllevel, attack.rangedirection, attack.numAttackedAndDamage, visProjectile, attack.allDamage, attack.speed, attack.direction, attack.display));
			}else{
				chr.getMap().broadcastPacket(PacketFactory.rangedAttack(chr, attack.skill, attack.skilllevel, attack.rangedirection, attack.numAttackedAndDamage, visProjectile, attack.allDamage, attack.speed, attack.direction, attack.display));
			}*/
			
		}
		
	}
	

	
	private static boolean isConsumeException(int skillId){
		return skillId == DawnWarrior.SOUL_BLADE ||
				skillId == ThunderBreaker.SHARK_WAVE ||
				skillId == NightWalker.VAMPIRE;
	}

}
