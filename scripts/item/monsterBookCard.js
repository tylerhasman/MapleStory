
function onPickup(chr, item){
	chr.getMonsterBook().increaseLevel(item.getItemId() % 10000);
}