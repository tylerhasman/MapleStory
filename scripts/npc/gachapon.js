
var ItemInfoProvider = Java.type("maplestory.inventory.item.ItemInfoProvider")

var ticket = 5220000;

var status;

function start(){
	status = -1;
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
		if(cm.hasItem(ticket, 1)){
			cm.sendYesNo("Would you like to use a #i"+ticket+" # Gachapon Ticket ?");
		}else{
			cm.sendOk("You need a #i"+ticket+" # to use #rGachapon");
			cm.dispose();
		}
	}else if(status == 1){
		cm.giveItem(ticket, -1);
		
		reward = ItemInfoProvider.getRandomGachaponItem(area);
		
		cm.giveItem(reward.getItemId(), reward.getAmount());
		
		if(cm.hasItem(ticket, 1)){
			if(reward.getAmount() == 1){
				cm.sendYesNo("You have won a #i "+reward.getItemId()+" #\r\n\r\nWould you like to play again?");	
			}else{
				cm.sendYesNo("You have won "+reward.getAmount()+" #i "+reward.getItemId()+" #\r\n\r\nWould you like to play again?");
			}
		}else{
			if(reward.getAmount() == 1){
				cm.sendOk("You have won a #i "+reward.getItemId()+" #");	
			}else{
				cm.sendOk("You have won "+reward.getAmount()+" #i "+reward.getItemId()+" #");
			}
			cm.dispose();
		}
		
	}else if(status == 2){
		status = 0;
		action(1, 0, -1);
	}
	
}