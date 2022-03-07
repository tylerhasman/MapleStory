package database;

import java.io.File;
import java.io.IOException;

import database.MonsterDropManager.MonsterDrop;
import me.tyler.mdf.MapleFile;
import me.tyler.mdf.MapleFileFactory;
import me.tyler.mdf.Node;
import me.tyler.mdf.ReadWriteMapleFile;

public class ConvertToMapleData {

	public static void main(String[] args) throws IOException {
		MonsterDropManager manager = new MonsterDropManager();
		
		File file = new File("MonsterDrops.mdf");
		
		ReadWriteMapleFile mf = MapleFileFactory.getReadWriteMapleFile(file, false);
		
		Node global = mf.getRootNode().writeNullNode("global");
	
		for(int i = 0; i < manager.getGlobalDrops().size();i++){
			MonsterDrop drop = manager.getGlobalDrops().get(i);
			Node node = global.writeNullNode(String.valueOf(i));
			node.writeInt("item", drop.getItemId());
			node.writeInt("min", drop.min);
			node.writeInt("max", drop.max);
			node.writeInt("chance", drop.getChance());
			
		}
		
		mf.write(file);
	}
	
}
