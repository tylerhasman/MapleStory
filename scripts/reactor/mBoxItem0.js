

function destroy() {
	
	point = null;
	
	if(rm.getReactor().getId() == 2001){
		if(rm.isQuestStarted(1008)){
			items = [];
			if(rm.itemAmount(4031161) == 0){
				items[0] = [4031161, 1];
			}
			if(rm.itemAmount(4031162) == 0){
				items[1] = [4031162, 1];
			}
			point = rm.dropItems(rm.getReactor(), items);
		}
	}
	
	if(point != null){
		rm.dropMesos(rm.getReactor(), point, 15);
	}else{
		rm.dropMesos(rm.getReactor(), 15);
	}
	
	

}