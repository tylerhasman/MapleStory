
var Song = Java.type("constants.Song");

var status = -1;

function start(){
	action(1, 0, -1);
}

function action(mode, type, selection){
	if(mode == 1){
		status++;
	}else{
		cm.dispose();
		return;
	}
	if(status == 0){
		s = "Please select a song: \r\n#b";
		i = 0;
		for each(song in Song.values()){
			s += "#L"+(i++)+"#"+song.getId()+"#l\r\n";
		}
		
		cm.sendSimple(s);
	}else if(status == 1){
		song = Song.values()[selection];
		
		if(song == null){
			cm.sendOk("Something has gone wrong!");
			cm.dispose();
			return;
		}
		
		cm.changeMusic(song.getId());
		cm.dispose();
	}
}