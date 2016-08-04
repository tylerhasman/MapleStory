
var blessing = 2022458

function start() {
	
	if(cm.hasItemBuff(blessing)){
		cm.sendOk("Don't stop training. Every ounce of your energy is required to protect the world of Maple....");
	}else{
	    cm.useItem(blessing);
		cm.playSkillEffect(1010);
	}

	cm.dispose();
}
