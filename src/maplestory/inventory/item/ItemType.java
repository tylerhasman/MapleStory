package maplestory.inventory.item;

import maplestory.inventory.MapleWeaponType;

public enum ItemType {

	RECHARGABLE{
		@Override
		public boolean isThis(int itemId) {
			return itemId / 10000 == 233 || itemId / 10000 == 207;
		}
	}, 
	
	ARROW{
		@Override
		public boolean isThis(int itemId) {
			return itemId / 1000 == 2060;
		}
	}, 
	
	BOLT{
		@Override
		public boolean isThis(int itemId) {
			return itemId / 1000 == 2061;
		}
	}, 
	
	THROWING_STAR{
		@Override
		public boolean isThis(int itemId) {
			return itemId / 10000 == 207;
		}
	}, 
	
	BULLET{
		@Override
		public boolean isThis(int itemId) {
			return itemId / 10000 == 233;
		}
	}, 
	
	SUMMONING_BAG {
		@Override
		public boolean isThis(int itemId) {
			/*return itemId >= 2100000 && itemId <= 2101056;*/
			return ItemInfoProvider.getSummoningBagEntries(itemId).size() > 0;
		}
	}, 
	
	TOWN_SCROLL{
		@Override
		public boolean isThis(int itemId) {
			return itemId >= 2030000 && itemId < 2030021;
		}
	}, 
	
	MASTERY_BOOK{
		@Override
		public boolean isThis(int itemId) {
			return itemId >= 2290000 &&  itemId <= 2290139;
		}
	}, 
	
	CASH{
		@Override
		public boolean isThis(int itemId) {
			return itemId / 1000000 == 5;
		}
	}, 
	
	USE{
		@Override
		public boolean isThis(int itemId) {
			return itemId >= 2000000 && itemId < 3000000;
		}
	}, 
	
	ETC{
		@Override
		public boolean isThis(int itemId) {
			return itemId >= 4000000 && itemId < 5000000;
		}
	},
	
	SETUP{
		@Override
		public boolean isThis(int itemId) {
			return itemId >= 3000000 && itemId < 4000000;
		}
	},
	
	PET{
		@Override
		public boolean isThis(int itemId) {
			return itemId >= 5000000 && itemId < 5001000;
		}
	}, 
	
	ADVENTURER_MOUNT{
		@Override
		public boolean isThis(int itemId) {
			return (itemId >= 1902000 && itemId <= 1902002) || itemId == 1912000;
		}
	},
	
	CYGNUS_MOUNT {
		@Override
		public boolean isThis(int itemId) {
			return (itemId >= 1902005 && itemId <= 1902007) || itemId == 1912005;
		}
	}, 
	
	OVERALL{
		@Override
		public boolean isThis(int itemId) {
			return itemId / 10000 == 105;
		}
	},
	
	TWO_HANDED_WEAPON {
		@Override
		public boolean isThis(int itemId) {
			
			MapleWeaponType type = ItemInfoProvider.getWeaponType(itemId);
			
			switch (type) {
            case AXE2H:
            case BOW:
            case CLAW:
            case CROSSBOW:
            case POLE_ARM:
            case SPEAR:
            case SWORD2H:
            case GUN:
            case KNUCKLE:
                return true;
            default:
                return false;
			}
			
		}
	},
	
	WEAPON {
		@Override
		public boolean isThis(int itemId) {
			return itemId >= 1302000 && itemId < 1492024;
		}
	}, 
	
	SCROLL {
		@Override
		public boolean isThis(int itemId) {
			return itemId >= 2040000 && itemId <= 2049100;
		}
	},
	
	CLEAN_SLATE_SCROLL {

		@Override
		public boolean isThis(int itemId) {
			return itemId > 2048999 && itemId < 2049004;
		}
		
	},
	
	CHAOS_SCROLL {

		@Override
		public boolean isThis(int itemId) {
			return itemId >= 2049100 && itemId <= 2049103;
		}
		
	}, 
	
	MESSENGER {
		@Override
		public boolean isThis(int itemId) {
			return itemId / 10000 == 539;
		}
	}, 
	
	CHAIR {
		@Override
		public boolean isThis(int itemId) {
			return itemId / 10000 == 301;
		}
	}
	
	
	;
	
	public abstract boolean isThis(int itemId);
	
}
