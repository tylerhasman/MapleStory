var Item = Java.type("maplestory.inventory.item.Item");
var ItemInfo = Java.type("maplestory.inventory.item.ItemInfoProvider");
var HashMap = Java.type("java.util.HashMap");
var choices = [];
var status = -1;

function start(){
	action(1, 0, -1);
}

function action(mode, type, selection){
	if(mode == 1){
		status++;
	}else if(mode == 0){
	
		status = 0;
		choices = [];
	
	}else{
		cm.dispose();
		return;
	}
	if(status == 0){
		cm.sendGetText("Which item are you looking for?");
	}else if(status == 1){
		results = query_item();
		
		if(!is_valid_query()){
			cm.sendPrev("Please enter a more specific search term");
			return;
		}
		
		/*if(results.size() > 75){
			cm.sendOk("I found #r"+results.size()+"#k results, please try a more precise search term.");
			cm.dispose();
			return;
		}*/
		
		if(results.size() == 0){
			cm.sendPrev("I found no results for the search term "+cm.getInputText());
			return;
		}
		
		message = "I found #b"+results.size()+"#k results for the search term #b'"+cm.getInputText()+"'#k";
	
		if(results.size() > 0){
			message += "\r\nHere they are:\r\n";
		}
	
		iter = results.keySet().iterator();
	
		i = 0;
		
		max = 75;
		
		while(iter.hasNext()){
			if(max <= 0){
				break;
			}
			id = iter.next();
		
			//message += "#L"+i+"##i"+id+"# "+id+" #l";
			message += "#L"+i+"##i"+id+"# #l";
			choices[i] = id;
			i++;
			max--;
		}

		cm.sendSimple(message);
		
	}else if(status == 2){
		choice = choices[selection];
		
		cm.giveItem(choice, ItemInfo.getSlotMax(choice));
		
		cm.sendOk("I have added "+ItemInfo.getSlotMax(choice)+" #b"+results.get(choice)+"#k to your inventory.");
		
		cm.dispose();
	}
}

function is_valid_query(){
	query = cm.getInputText();
	
	return query.length() > 3;
}

function query_item(){
	query = cm.getInputText();

	itemIds = ItemInfo.getAllItemIds();
		
	names = new HashMap();
	
	for each(id in itemIds){
		names.put(id, ItemInfo.getItemName(id));
	}
		
	matches = new HashMap();
		
	for each(item in names.entrySet()){
		if(item.getValue() == null){
			continue;
		}
		
		if(item.getValue().toLowerCase().contains(query.toLowerCase())){
			if(ItemInfo.getSlotMax(item.getKey()) > 0){
				matches.put(item.getKey(), item.getValue());
			}
		}
	}
	
	return matches;
}