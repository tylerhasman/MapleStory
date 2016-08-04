
function destroy(){
	
	if(rm.isQuestStarted(20013)){
		
		items = [];
		if(rm.itemAmount(4032267) == 0){
			items[0] = [4032267, 1];
		}
		if(rm.itemAmount(4032268) == 0){
			items[1] = [4032268, 1];
		}
		
		rm.dropItems(rm.getReactor(), items);
		
	}
	
}
