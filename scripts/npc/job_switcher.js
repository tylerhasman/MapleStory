
var MapleJob = Java.type("maplestory.player.MapleJob");

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
		s = "Please select a job: \r\n#b";
		
		for each(job in MapleJob.values()){
			s += "#L"+job.getId()+"#"+job.getName()+"#l\r\n";
		}
		
		cm.sendSimple(s);
	}else if(status == 1){
		job = MapleJob.getById(selection);
		
		if(job == null){
			cm.sendOk("Something has gone wrong!");
			cm.dispose();
		}
		
		cm.changeJob(job);
		cm.dispose();
	}
}