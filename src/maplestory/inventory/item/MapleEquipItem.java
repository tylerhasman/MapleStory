package maplestory.inventory.item;

public class MapleEquipItem extends MapleItem implements EquipItem {

	protected EquipItemInfo itemInfo;
	
	public MapleEquipItem(int itemId, int amount, EquipItemInfo itemInfo) {
		super(itemId, amount);
		if(amount > 1){
			throw new IllegalArgumentException("amount cannot be greater than 1 ("+amount+")");
		}
		this.itemInfo = itemInfo;
	}
	
	public MapleEquipItem(int itemId, int amount, String owner, EquipItemInfo itemInfo) {
		super(itemId, amount, owner);
		if(amount > 1){
			throw new IllegalArgumentException("amount cannot be greater than 1 ("+amount+")");
		}
		this.itemInfo = itemInfo;
	}
	
	@Override
	public String toString() {
		return super.toString()+" "+itemInfo.serialize();
	}
	
	@Override
	public boolean canMerge(Item other) {
		return false;
	}
	
	@Override
	public EquipItemInfo getStatInfo() {
		return itemInfo;
	}

	@Override
	public Item copyOf(int amount) {
		MapleEquipItem copy = new MapleEquipItem(getItemId(), amount, getOwner(), new EquipItemInfo(itemInfo));
		
		return copy;
	}

	@Override
	public int getUpgradeSlotsAvailble() {
		return itemInfo.getUpgradeSlots();
	}

	@Override
	public int getLevel() {
		return itemInfo.getLevel();
	}

	@Override
	public int getItemLevel() {
		return itemInfo.getItemLevel();
	}

	@Override
	public int getStr() {
		return itemInfo.getStr();
	}

	@Override
	public int getDex() {
		return itemInfo.getDex();
	}

	@Override
	public int getInt() {
		return itemInfo.getInt_();
	}

	@Override
	public int getLuk() {
		return itemInfo.getLuk();
	}

	@Override
	public int getHp() {
		return itemInfo.getHp();
	}

	@Override
	public int getMp() {
		return itemInfo.getMp();
	}

	@Override
	public int getWeaponAttack() {
		return itemInfo.getWeaponAttack();
	}

	@Override
	public int getMagicAttack() {
		return itemInfo.getMagicAttack();
	}

	@Override
	public int getWeaponDefense() {
		return itemInfo.getWeaponDefense();
	}

	@Override
	public int getMagicDefense() {
		return itemInfo.getMagicDefense();
	}

	@Override
	public int getAccuracy() {
		return itemInfo.getAccuracy();
	}

	@Override
	public int getAvoid() {
		return itemInfo.getAvoid();
	}

	@Override
	public int getSpeed() {
		return itemInfo.getSpeed();
	}

	@Override
	public int getHands() {
		return itemInfo.getHands();
	}

	@Override
	public int getJump() {
		return itemInfo.getJump();
	}

	@Override
	public int getHammerUpgrades() {
		return itemInfo.getHammerUpgrades();
	}

	@Override
	public int getRequiredJob() {
		return itemInfo.getRequiredJob();
	}

	@Override
	public int getRequiredLevel() {
		return itemInfo.getRequiredLevel();
	}

	@Override
	public int getRequiredStr() {
		return itemInfo.getRequiredStr();
	}

	@Override
	public int getRequiredDex() {
		return itemInfo.getRequiredDex();
	}

	@Override
	public int getRequiredLuk() {
		return itemInfo.getRequiredLuk();
	}

	@Override
	public int getRequiredInt() {
		return itemInfo.getRequiredInt();
	}

	@Override
	public boolean isCash() {
		return itemInfo.isCash();
	}

	@Override
	public boolean isUntradeableOnEquip() {
		return itemInfo.isUntradeableOnEquip();
	}

}
