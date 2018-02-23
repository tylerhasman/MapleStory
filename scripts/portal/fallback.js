

function enter(){
	pm.sendHint("You entered portal #b"+pm.getPortal().getScriptName()+"#k\r\nIt has no script!", 200, 15);
	print("Missing portal script "+pm.getPortal().getScriptName());
}