var items = [1302024, 1002516];

function enter(){
	pm.dropItemPresent(pm.getClient().getCharacter().getPosition(), items);
	pm.showInfo("ITEM_PICKUP_2");
	pm.block();
}