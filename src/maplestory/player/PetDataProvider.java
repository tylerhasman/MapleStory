package maplestory.player;

import java.io.File;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;

public class PetDataProvider {

	private static MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
    
	private static MapleData petData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Pet.img");
	
}
