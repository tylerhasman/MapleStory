/* Pendant of Spirit code */

var RateType = Java.type("maplestory.world.RateManager.RateType");
var MessageType = Java.type("constants.MessageType");

var Timer = Java.type("tools.TimerManager");
var TimeUnit = Java.type("java.util.concurrent.TimeUnit");

var rate_id = "pendant_of_spirit";

bonus_1 = function(rate, val) { if(rate == RateType.EXP) { return val * 1.1; } }
bonus_2 = function(rate, val) { if(rate == RateType.EXP) { return val * 1.2; } }
bonus_3 = function(rate, val) { if(rate == RateType.EXP) { return val * 1.3; } }

function get_rate_manager(chr){
	return chr.getWorld().getRates();
}

function clear_pendant_buff(chr){
	rateManager = get_rate_manager(chr);
	
	rateManager.removeModifier(chr, rate_id);
	
	t = chr.getScriptVariable(rate_id+"_task");
	if(t != null){
		t.cancel(false);
		chr.setScriptVariable(rate_id+"_task", null);
	}
	chr.setScriptVariable(rate_id, null);
}

function get_bonus_function(level){
	if(level == 0){
		return bonus_1;
	}else if(level == 1){
		return bonus_2;
	}else{
		return bonus_3;
	}
}

function add_pendant_buff(chr, bonus_level){
	buff = get_bonus_function(bonus_level);
	
	rateManager = get_rate_manager(chr);
	
	rateManager.addModifier(chr, rate_id, buff);
	chr.setScriptVariable(rate_id, bonus_level);

	if(bonus_level == 0){
		chr.sendMessage(MessageType.PINK_TEXT, "Pendant of the Spirit has been equipped, you will now receive 10% bonus exp.");
	}else{
		chr.sendMessage(MessageType.PINK_TEXT, "Pendant of the Spirit has been equipped for " + (bonus_level + 1) + " hour(s), you will now receive " + (bonus_level + 1) + "0% bonus exp.");
	}
}

function get_current_level(chr){
	return chr.getScriptVariable(rate_id, -1);
}

function onEquip(chr, item){
	clear_pendant_buff(chr);
	task = Timer.scheduleRepeatingTask(
		function() {
			
			level = get_current_level(chr);
			level++;
			
			if(level < 3){
				add_pendant_buff(chr, level);
			}else{
				t = chr.getScriptVariable(rate_id+"_task");
				t.cancel(false);
				chr.setScriptVariable(rate_id+"_task", null);
			}
			
		}
	, 0, 1, TimeUnit.HOURS);
	
	chr.setScriptVariable(rate_id+"_task", task);
	
}

function onUnEquip(chr, item){
	clear_pendant_buff(chr);
}
