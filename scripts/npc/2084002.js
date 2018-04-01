
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
	
	if(cm.getCharacter().getMapId() == 390009999){
		
		if(status == 0){
			
			if(cm.getCharacter().getJob() == MapleJob.BEGINNER){
				
				if(cm.getCharacter().getLevel() < 8 || cm.getCharacter().getMeso() < 500){
					cm.sendNext("Hello young adventurer, I'm #bGold Richie#k and will help you since your starting out here.");
				}else if(cm.getCharacter().getLevel() < 10){
					cm.sendNext("It looks like your at least #blevel 8#k and have #b500 mesos#k so I can make you a #bBlaze Wizard#k if you would like.");
				}else if(cm.getCharacter().getLevel() >= 10){
					cm.sendNext("Wow, good job! You made it to level 10 so I can make you into any of 5 different jobs.");
				}
			}else if(cm.getCharacter().getJob() == MapleJob.NOBLESSE){
				if(cm.getCharacter().getLevel() < 8 || cm.getCharacter().getMeso() < 500){
					cm.sendNext("Hello young noblesse, I'm #bGold Richie#k and will help you since your starting out here.");
				}else if(cm.getCharacter().getLevel() < 10){
					cm.sendNext("It looks like your at least #blevel 8#k and have #b500 mesos#k so I can make you a #bmagician#k if you would like.");
				}else if(cm.getCharacter().getLevel() >= 10){
					cm.sendNext("Wow, good job! You made it to level 10 so I can make you into any of 5 different jobs.");
				}
			}else if(cm.getCharacter().getJob() == MapleJob.LEGEND){
				if(cm.getCharacter().getLevel() < 10 || cm.getCharacter().getMeso() < 500){
					cm.sendNext("Hello young noblesse, I'm #bGold Richie#k and will help you since your starting out here.");
				}else if(cm.getCharacter().getLevel() >= 10){
					cm.sendNext("Wow, good job! You made it to level 10 so I can make you into a true #bAran#k.");
				}
			}else{
				cm.sendOk("Congratulations, when your ready to leave go to the portal up above me");
				cm.dispose();
			}
			
		}else if(status == 1){
			if(cm.getCharacter().getJob() == MapleJob.BEGINNER){
				
				if(cm.getCharacter().getLevel() < 8 || cm.getCharacter().getMeso() < 500){
					cm.sendYesNo("It seems your too low level to job advance and get out of here but I can help you out with that. If you can #rcollect#k hmm... lets say #b500 mesos#k and reach at least #blevel 8#k, then I'll be able to get you out of here. How does that sound?");
				}else if(cm.getCharacter().getLevel() < 10){
					cm.sendYesNo("It will cost #b500 mesos#k and you will become a #bMagician#k. How does that sound?");
				}else if(cm.getCharacter().getLevel() >= 10){
					cm.sendSimple("Which would you like to become?\r\n #b#L0#Warrior#l \r\n#L1#Thief#l \r\n#L2#Magician#l \r\n#L3#Bowman#l \r\n#L4#Pirate#l");
				}
				
			}else if(cm.getCharacter().getJob() == MapleJob.NOBLESSE){
				if(cm.getCharacter().getLevel() < 8 || cm.getCharacter().getMeso() < 500){
					cm.sendYesNo("It seems your too low level to job advance and get out of here but I can help you out with that. If you can #rcollect#k hmm... lets say #b500 mesos#k and reach at least #blevel 8#k, then I'll be able to get you out of here. How does that sound?");
				}else if(cm.getCharacter().getLevel() < 10){
					cm.sendYesNo("It will cost #b500 mesos#k and you will become a #bBlaze Wizard#k. How does that sound?");
				}else if(cm.getCharacter().getLevel() >= 10){
					cm.sendSimple("Which would you like to become?\r\n #b#L0#Dawn Warrior#l \r\n#L1#Night Walker#l \r\n#L2#Blaze Wizard#l \r\n#L3#Wind Archer#l \r\n#L4#Thunder Breaker#l");
				}
			}else if(cm.getCharacter().getJob() == MapleJob.ARAN){
				if(cm.getCharacter().getLevel() < 10 || cm.getCharacter().getMeso() < 500){
					cm.sendYesNo("It seems your too low level to job advance and get out of here but I can help you out with that. If you can #rcollect#k hmm... lets say #b500 mesos#k and reach at least #blevel 10#k, then I'll be able to get you out of here. How does that sound?");
				}else if(cm.getCharacter().getLevel() >= 10){
					cm.sendYesNo("Are you ready to become an #bAran#k?");
				}
			}
		}else if(status == 2){
			if(cm.getCharacter().getJob() == MapleJob.BEGINNER){
				
				if(cm.getCharacter().getLevel() < 8 || cm.getCharacter().getMeso() < 500){
					cm.warp(390000000);
					cm.dispose();
				}else if(cm.getCharacter().getLevel() < 10){
					if(cm.getCharacter().getMeso() < 500){
						cm.sendOk("You need at least #r500 mesos#k.");
					}else{
						cm.giveMesos(-500);
						cm.getCharacter().changeJob(MapleJob.MAGICIAN);
						
						cm.sendOk("Congratulations, when your ready to leave go to the portal up above me");
						cm.dispose();
					}
				}else if(cm.getCharacter().getLevel() >= 10){
					if(cm.getCharacter().getMeso() < 500){
						cm.sendOk("You need at least #r500 mesos#k.");
					}else{
						cm.giveMesos(-500);
						if(selection == 0){
							cm.getCharacter().changeJob(MapleJob.WARRIOR);
						}else if(selection == 1){
							cm.getCharacter().changeJob(MapleJob.THIEF);
						}else if(selection == 2){
							cm.getCharacter().changeJob(MapleJob.MAGICIAN);
						}else if(selection == 3){
							cm.getCharacter().changeJob(MapleJob.BOWMAN);
						}else if(selection == 4){
							cm.getCharacter().changeJob(MapleJob.PIRATE);
						}
						
						cm.sendOk("Congratulations, when your ready to leave go to the portal up above me");
						cm.dispose();
					}
				}
				
			}else if(cm.getCharacter().getJob() == MapleJob.NOBLESSE){
				if(cm.getCharacter().getLevel() < 8 || cm.getCharacter().getMeso() < 500){
					cm.warp(390000000);
					cm.dispose();
				}else if(cm.getCharacter().getLevel() < 10){
					if(cm.getCharacter().getMeso() < 500){
						cm.sendOk("You need at least #r500 mesos#k.");
					}else{
						cm.giveMesos(-500);
						cm.getCharacter().changeJob(MapleJob.BLAZEWIZARD1);
						
						cm.sendOk("Congratulations, when your ready to leave go to the portal up above me");
						cm.dispose();
					}
				}else if(cm.getCharacter().getLevel() >= 10){
					if(cm.getCharacter().getMeso() < 500){
						cm.sendOk("You need at least #r500 mesos#k.");
					}else{
						cm.giveMesos(-500);
						if(selection == 0){
							cm.getCharacter().changeJob(MapleJob.DAWNWARRIOR1);
						}else if(selection == 1){
							cm.getCharacter().changeJob(MapleJob.NIGHTWALKER1);
						}else if(selection == 2){
							cm.getCharacter().changeJob(MapleJob.BLAZEWIZARD1);
						}else if(selection == 3){
							cm.getCharacter().changeJob(MapleJob.WINDARCHER1);
						}else if(selection == 4){
							cm.getCharacter().changeJob(MapleJob.THUNDERBREAKER1);
						}
						
						cm.sendOk("Congratulations, when your ready to leave go to the portal up above me");
						cm.dispose();
					}
				}
			}else if(cm.getCharacter().getJob() == MapleJob.LEGEND){
				if(cm.getCharacter().getLevel() < 10 || cm.getCharacter().getMeso() < 500){
					cm.warp(390000000);
					cm.dispose();
				}else if(cm.getCharacter().getLevel() >= 10){
					if(cm.getCharacter().getMeso() < 500){
						cm.sendOk("You need at least #r500 mesos#k.");
					}else{
						cm.giveMesos(-500);
						cm.getCharacter().changeJob(MapleJob.ARAN1);
						
						cm.sendOk("Congratulations, when your ready to leave go to the portal up above me");
						cm.dispose();
					}
				}
			}
		}
			
	}else{
		cm.dispose();
	}
}