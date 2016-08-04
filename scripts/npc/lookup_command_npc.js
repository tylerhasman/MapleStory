var Item = Java.type("maplestory.inventory.item.Item");
var choices = [];
var ItemInfo = Java.type("maplestory.inventory.item.ItemInfoProvider");

function start(){

	if(results.size() > 75){
		cm.sendOk("I found #r"+results.size()+"#k results, thats a lot... try a more precise search term.");
		cm.dispose();
		return;
	}
	
	if(results.size() == 0){
		cm.sendOk("I found no results for the search term "+search_term);
		cm.dispose();
		return;
	}

	message = "I found #b"+results.size()+"#k results for the search term #b'"+search_term+"'#k";
	
	if(results.size() > 0){
		message += "\r\nHere they are:\r\n";
	}
	
	iter = results.keySet().iterator();
	
	i = 0;
	
	while(iter.hasNext()){
		id = iter.next();
		
		message += "#L"+i+"##i"+id+"# "+id+" #l";
		choices[i] = id;
		i++;
	}

	cm.sendSimple(message);

}

function action(mode, type, selection){
	if(selection == -1){
		cm.dispose();
		return;
	}
	if(mode == 1){
		choice = choices[selection];
		
		cm.giveItem(choice, ItemInfo.getSlotMax(choice));
		
		cm.sendOk("I have added "+ItemInfo.getSlotMax(choice)+" #b"+results.get(choice)+"#k to your inventory.");
		
		cm.dispose();
	}
}