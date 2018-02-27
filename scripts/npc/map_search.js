var status = -1;
var maps;

var MapFactory = Java.type("maplestory.map.MapleMapFactory");

function start(){
	action(1, 0, 0);
}

function action(mode, type, selection){
	
	if(mode == 1){
		status++;
	}else{
		cm.dispose();
		return;
	}
	
	if(status == 0){
		cm.sendGetText("Enter a map name: ");
	}else if(status == 1){
		maps = MapFactory.findMap(cm.getInputText());
		if(maps.size() == 0){
			cm.sendOk("I found zero maps with that search term!");
			status = -1;
			return;
		}
		str = "I found "+maps.size()+" maps for search term '"+cm.getInputText()+"'\r\n#b";
	
		for(i = 0; i < maps.size();i++){
			if(i > 50){
				break;
			}
			id = maps.get(i);
			
			str += "#L"+i+"#"+MapFactory.getMapName(id)+" ("+id+") #l\r\n";
		}
		
		cm.sendSimple(str);
	}else if(status == 2){
		cm.warp(maps.get(selection));
		cm.dispose();
	}
	
}