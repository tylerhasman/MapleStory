
var GuildRankLevel = Java.type("maplestory.guild.MapleGuildRankLevel");

var status = -1;
var choice = -1;

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
		cm.sendSimple("Hello I am Heracle the Guild Master, what can I do for you?\r\n#b#L0#Create a guild#l\r\n#L1#Disband my guild#l\r\n#L2#Expand guild capacity#l");
	}else if(status == 1){
		choice = selection;
		if(selection == 0){
			guild = cm.getCharacter().getGuild();
			if(guild != null){
				cm.sendOk("I'm sorry but you can't be in two guilds at once");
			}else{
				cm.openGuildCreateMenu();
				cm.dispose();
			}
		}else if(selection == 1){
			guild = cm.getCharacter().getGuild();
			if(guild.getRankLevel(cm.getCharacter()) == GuildRankLevel.MASTER){
				if(guild != null){
					cm.sendYesNo("Are you sure you want to disband your guild?");
				}else{
					cm.sendOk("You are not in a guild!");
					cm.dispose();
				}
			}else{
				cm.sendOk("You must be the guilds owner to disband it.");
				cm.dispose();
			}
			
		}else if(selection == 2){
			cm.sendOk("Sorry we aren't taking guild expansion requests right now.");
			cm.dispose();
		}
	}else if(status == 2){
		if(choice == 1){
			guild = cm.getCharacter().getGuild();
			guild.disbandGuild();
			cm.sendOk("I have disbanded your guild");
			cm.dispose();
		}
	}else{
		cm.dispose();
	}
	
	
	
}