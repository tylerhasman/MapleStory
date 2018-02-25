
/*

	Abstract Job Advancer,
	Used for Grendel, Athena, etc.
	
	Required variables from the script loading this:
	
	FIRST_JOB_ASK             ex."So you decided to become a #rBowman#k?";
	FIRST_JOB_KEEP_TRAINING   ex."Train a bit more and I can show you the way of the #rBowman#k.";
	FIRST_JOB_CONFIRM         ex."It is an important and final choice. You will not be able to turn back.";
	FIRST_JOB_ITEMS           ex.[[1452051, 1], [2060000, 1000]];//Bow + Arrow
	FIRST_JOB_BECOME_1        ex."Alright, from here out, you are a part of us! Be faithful to the archer way and you are sure to succeed.";
	FIRST_JOB_BECOME_2        ex."You've gotten much stronger now. I just gave you a little bit of #bSP#k. When you open up the #bSkill#k menu on the lower left corner of the screen, there are skills you can learn by using SP's. One warning, though: You can't raise it all together all at once. There are also skills you can acquire only after having learned a couple of skills first.";
	FIRST_JOB_BECOME_3        ex."Now a reminder. Once you have chosen, you cannot change up your mind and try to pick another path. Go now, and live as a proud Bowman.";
	

*/

var MapleJob = Java.type("maplestory.player.MapleJob");

var ACTION_FIRST_JOB = 0;
var ACTION_SECOND_JOB = 1;

var status = -1;
var playerAction = -1;
var desiredJob;

function start(){
	action(1, 0, 0);
}

function action(mode, type, selection){

	if(mode == 1){
		status++;
	}else if(mode == 0){
		status--;
	}else{
		cm.dispose();
		return;
	}
	
	if(status == -1){
		cm.dispose();
		return;
	}
	
	if(status == 0){
		job = cm.getCharacter().getJob();
		if(job == MapleJob.BEGINNER){
			playerAction = ACTION_FIRST_JOB;
			if(cm.getCharacter().getLevel() >= 10){
				cm.sendYesNo(FIRST_JOB_ASK);
			}else{
				cm.sendOk(FIRST_JOB_KEEP_TRAINING);
				cm.dispose();
			}
		}else if(job == FIRST_JOB_JOB){
			playerAction = ACTION_SECOND_JOB;
			if(cm.getCharacter().getLevel() >= 30){
				
				if(cm.hasItem(SECOND_JOB_LETTER, 1)){
					cm.sendOk("Please get that letter to #b#p"+SECOND_JOB_NPC+"##k who's around #b#m"+SECOND_JOB_MAP+"##k near "+MAIN_TOWN+". They are taking care of the job of an instructor in place of me. Give them the letter and they'll test you in place of me. Best of luck to you.");
					cm.dispose();
				}else if(cm.hasItem(SECOND_JOB_REQUIRED, 1)){
					cm.sendNext(SECOND_JOB_COMPLETE_1);
				}else{
					cm.sendYesNo(SECOND_JOB_ASK);
				}
				
			}else{
				cm.sendNext("Please come see me once you are level 30");
			}
		
		}else{
			cm.sendOk("It seems I cannot help you");
		}
	}else{
		if(playerAction == ACTION_FIRST_JOB){
			
			if(status == 1){
				cm.sendNextPrev(FIRST_JOB_CONFIRM);
			}else if(status == 2){
				
				if(canHold(FIRST_JOB_ITEMS)){
					if(cm.getCharacter().getJob() == MapleJob.BEGINNER){
						cm.changeJob(FIRST_JOB_JOB);
						for each(i in FIRST_JOB_ITEMS){
							cm.giveItem(i[0], i[1]);
						}
						cm.getCharacter().resetStats();
					}
					
					cm.sendNext(FIRST_JOB_BECOME_1);
				}else{
					cm.sendNext("Make some room in your inventory and talk back to me.");
					cm.dispose();
				}

			}else if(status == 3){
				cm.sendNextPrev(FIRST_JOB_BECOME_2);
			}else if(status == 4){
				cm.sendNextPrev(FIRST_JOB_BECOME_3);
			}else{
				cm.dispose();
			}
		}else if(playerAction == ACTION_SECOND_JOB){
			if(status == 1){
				if(cm.hasItem(SECOND_JOB_REQUIRED, 1)){
					cm.sendSimple(SECOND_JOB_EXPLAIN);
				}else{
					cm.giveItem(SECOND_JOB_LETTER);
					cm.sendOk("Please get that letter to #b#p"+SECOND_JOB_NPC+"##k who's around #b#m"+SECOND_JOB_MAP+"##k near "+MAIN_TOWN+". They are taking care of the job of an instructor in place of me. Give them the letter and they'll test you in place of me. Best of luck to you.");
					cm.dispose();
				}
			}else if(status == 2){
				if(selection < SECOND_JOB_JOBS.length){
					print(selection);
					if(selection == -1){
						//These means we are coming back from a 'no' response in status 3
						status -= 2;
						action(1, 0, 0);
					}else{
						cm.sendNext(SECOND_JOB_EXPLANATIONS[selection]);
						status -= 2;//We go back instead of forward, holy moly
					}
				}else{
					cm.sendSimple(SECOND_JOB_DECIDE);
				}
			}else if(status == 3){
				desiredJob = SECOND_JOB_JOBS[selection];
				if(desiredJob == null){
					cm.sendOk("Invalid selection "+selection);//Player must be hacking
					return;
				}
				cm.sendYesNo("So you want to make the second job advancement as the #b" + SECOND_JOB_JOBS[selection].getName() + "#k? You know you won't be able to choose a different job for the 2nd job advancement once you make your desicion here, right?");
			}else if(status == 4){
				
				if (cm.hasItem(SECOND_JOB_REQUIRED))
					cm.giveItem(SECOND_JOB_REQUIRED, -1);
				
				cm.sendNext("Alright, you're a #b" + desiredJob.getName() + "#k from here on out. #b" + desiredJob.getName() + "s#k "+SECOND_JOB_FINAL_FLAVOUR);
				if(cm.getCharacter().getJob() != desiredJob){
					cm.changeJob(desiredJob);
				}
			
			}else{
				cm.dispose();
			}
			
		}else{
			cm.dispose();
		}
	}
}

function canHold(items){
	for each(i in items){
		if(!cm.canHold(i[0])){
			return false;
		}
	}
	return true;
}