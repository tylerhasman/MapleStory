load("scripts/constants/HenesysPQ.js");

function destroy(){
	rm.dropItem(rm.getReactor(), seedGreen, 1);
	print(rm.getReactor().getId());
}