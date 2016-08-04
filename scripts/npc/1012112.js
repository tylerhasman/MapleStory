
load("scripts/constants/HenesysPQ.js");//Several constants loaded from here

var status = -1;
var minLevel = 10;
var requiredPlayers = 1;

function start(){

	status = -1;
	action(1, 0, 0);

}

function meetsRequirements(members){
	for(var i = 0; i < members.size();i++){
		member = members.get(i).getSnapshot();
		if(!member.isOnline()){
			continue;
		}
		if(member.getMapId() == henesysParkId){
			if(member.getLevel() < minLevel){
				return false;
			}
		}
	}
	return true;
}

function getOnMap(members, map){
	onmap = 0;
	for(var i = 0; i < members.size();i++){
		member = members.get(i).getSnapshot();
		if(member.getMapId() == map){
			onmap++;
		}
	}
	return onmap;
}

function action(mode, type, selection){
	
	if(mode == -1){
		cm.dispose();
		return;
	}
	
	
	if(mode == 1){
		status++;
	}else{
		status--;
	}
	
	if(cm.getCharacter().getMapId() == henesysParkId){
		party = cm.getCharacter().getParty();
	
		if(party == null || !party.isLeader(cm.getCharacter())){
			if(status == 0){
				cm.sendNext("Hi there! I'm Tory. This place is covered with mysterious aura of the full moon, and no one person can enter here by him/herself.");
			}else if(status == 1){
				cm.sendOk("If you'd like to enter here, the leader of your party will have to talk to me. Talk to your party leader about this.");
                cm.dispose();
			}
		}else{
			if(status == 0){
				cm.sendNext("I'm Tory. Inside here is a beautiful hill where the primrose blooms. There's a tiger that lives in the hill, Growlie, and he seems to be looking for something to eat");
			}else if(status == 1){
				cm.sendSimple("Would you like to head over to the hill of primrose and join forces with your party members to help Growlie out?\r\n#b#L0# Yes, I will go.#l");
			}else if(status == 2){
				var members = party.getMembers();
				
				if(members.size() < requiredPlayers){
					cm.sendOk("You will need at least "+requiredPlayers+" players to enter.");
					cm.dispose();
					return;
				}
				
				if(!meetsRequirements(members)){
					cm.sendOk("A member of your party does not meet the level requirement.");
                    cm.dispose();
					return;
				}
				
				if(getOnMap(members, henesysParkId) < requiredPlayers){
					cm.sendOk("A member of your party is not presently in the map.");
					cm.dispose();
				}
				
				partyMap = cm.getClient().getChannel().getMapFactory().getMap(PQMapId);
				
				if(partyMap.getPlayers().size() > 0){
                    cm.sendOk("Someone is already attempting the PQ. Please wait for them to finish, or find another channel.");
                    cm.dispose();
                    return;
				}
				
				partyMap.executeMapScript(cm.getCharacter(), "initialize");
				
				for each(member in party.getMembers()){
					if(member.getSnapshot().isOnline() && member.getSnapshot().getMapId() == henesysParkId){
						member.getSnapshot().getLiveCharacter().changeMap(PQMapId);
					}
				}
				
				cm.dispose();
				
			}
		}
	}else if(cm.getCharacter().getMapId() == exitMapSuccess || cm.getCharacter().getMapId() == exitMapFail){
		pq_progress = cm.getCharacter().getPartyQuestProgress();
		if(status == 0){
			cm.sendSimple("I appreciate you giving some rice cakes to Growlie. It looks like you have nothing else to do now. Would you like to leave this place?\r\n#L0#I want to give you the rest of my rice cakes.#l\r\n#L1#Yes, please get me out of here.#l");
		}else if(status == 1){
			if(selection == 0){
				if(pq_progress.isRiceHatGiven()){
					cm.sendNext("Do you like the hat I gave you? I ate so much of your rice cake that I will have to say no to your offer of rice cake for a little while.");
                    cm.dispose();
				}else if(pq_progress.getRiceCakesGiven() >= 20){
					cm.sendYesNo("I appreciate the thought, but I am okay now. I still have some of the rice cakes you gave me stored at home. To show you my appreciation, I prepared a small gift for you. Would you like to accept it?");
				}else{
					var amount = cm.itemAmount(riceCake);
					
					if(amount > 0){
						cm.giveItem(riceCake, -amount);
					
						pq_progress.setRiceCakesGiven(pq_progress.getRiceCakesGiven() + amount);
					
						cm.sendOk("Thank you for giving me #b"+amount+"#k rice cakes, I really appreciate it! The total amount you have given me is "+pq_progress.getRiceCakesGiven());
					}else{
						if(pq_progress.getRiceCakesGiven() > 0){
							cm.sendOk("You don't have enough extra rice cakes but that is ok... At least you have given me a total of "+pq_progress.getRiceCakesGiven()+" rice cakes!");
						}else{
							cm.sendOk("You don't have enough extra rice cakes but that is ok...");
						}
						
					}
					
					cm.dispose();
				}
			}else{
				cm.giveItem(riceCake, -cm.itemAmount(riceCake));
				cm.warp(100000200);
				cm.dispose();
			}
		}else if(status == 2){
			if(cm.canHold(1002798)){
				pq_progress.setRiceHatGiven(true);
				cm.giveItem(1002798, 1);
				cm.sendOk("It will go really well with you. I promise.");
				cm.dispose();
			}else{
				cm.sendOk("Make sure you have at least one free slot in your equip inventory.");
			}

		}
	}

}