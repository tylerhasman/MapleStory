var status;
var discount = false;
var cost = 3000;

var maps = [100000000, 101000000, 102000000, 103000000];
var chosen;
 
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
			
			if(cm.getClient().getCharacter().getJob().isBeginnerJob()){
				discount = true;
			}
			
			var text = "Hi #r#h ##k,\r\nwhere can I take you to?\r\n";
			
			for(var i = 0; i < maps.length;i++){
			
				mapid = maps[i];
				
				if(mapid == cm.getClient().getCharacter().getMapId()){
					continue;
				}
				
				name = get_map_name(mapid);
				
				text += "#b#L"+i+"#"+name+"#l\r\n";
			}
			
			if(discount){
				
			}else{
				
			}
			
			cm.sendSimple(text);
		
		}else if(status == 1 && mode == 1){
			chosen = maps[selection];
			
			cm.sendYesNo("Are you sure you want to go to #b"+get_map_name(chosen));
		}else if(status == 2 && mode == 1){
			cm.getClient().getCharacter().changeMap(get_map(chosen));
			cm.dispose();
		}else{
			cm.dispose();
		}
    }
}

function get_map(mapid){
	return cm.getClient().getChannel().getMapFactory().getMap(mapid);
}

function get_map_name(mapid){
	return cm.getClient().getChannel().getMapFactory().getMapName(mapid);
}