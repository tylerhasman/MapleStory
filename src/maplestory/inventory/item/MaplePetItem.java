package maplestory.inventory.item;

import lombok.Setter;
import lombok.Getter;


public class MaplePetItem extends MapleCashItem implements PetItem {
	
	@Getter @Setter
	private String petName;
	
	@Getter @Setter
	private int closeness, petLevel, fullness;
	
	@Getter @Setter
	private boolean summoned;
	
	public MaplePetItem(int itemId, int amount, long expirationDate, long uniqueId, PetDataSnapshot petData) {
		super(itemId, amount, expirationDate, uniqueId);
		petName = petData.getPetName();
		closeness = petData.getCloseness();
		petLevel = petData.getPetLevel();
		fullness = petData.getFullness();
	}
	
	public MaplePetItem(int itemId, int amount, String owner, long expirationDate, long uniqueId, PetDataSnapshot petData) {
		super(itemId, amount, owner, expirationDate, uniqueId);
		if(petData != null){
			petName = petData.getPetName();
			closeness = petData.getCloseness();
			petLevel = petData.getPetLevel();
			fullness = petData.getFullness();
		}else{
			petName = ItemInfoProvider.getItemName(itemId);
			closeness = 0;
			petLevel = 0;
			fullness = 20;
		}
	}
	
	@Override
	public Item copyOf(int amount) {
		return new MaplePetItem(getItemId(), amount, getExpirationDate(), getUniqueId(), createPetSnapshot());
	}

	@Override
	public PetDataSnapshot createPetSnapshot() {
		return new PetDataSnapshot(petName, closeness, petLevel, fullness, summoned);
	}

}
