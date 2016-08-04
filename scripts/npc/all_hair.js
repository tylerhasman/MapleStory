var mhair = [30000, 30010, 30020, 30030, 30040, 30050, 30060, 30070, 30080, 30090, 30110, 30120, 30130, 30140, 30150, 30160, 30170, 30180, 30190, 30200, 30210, 30220, 30230, 30240, 30250, 30260, 30270, 30280, 30290, 30300, 30310, 30320, 30330, 30340, 30350, 30360, 30370, 30400, 30410, 30420, 30440, 30450, 30460, 30470, 30480, 30490, 30510, 30520, 30530, 30540, 30550, 30560, 30570, 30580, 30590, 30600, 30610, 30620, 30630, 30640, 30650, 30660, 30670, 30680, 30690, 30700, 30710, 30720, 30730, 30740, 30750, 30760, 30770, 30780, 30790, 30800, 30810, 30820, 30830, 30840, 30850, 30860, 30870, 30880, 30890, 30900, 30910, 30920];
var fhair = [31000, 31010, 31020, 31030, 31040, 31050, 31060, 31070, 31080, 31090, 31100, 31110, 31120, 31130, 31140, 31150, 31160, 31170, 31180, 31190, 31200, 31210, 31220, 31230, 31240, 31250, 31260, 31270, 31280, 31290, 31300, 31310, 31320, 31330, 31340, 31350, 31400, 31410, 31420, 31440, 31450, 31460, 31470, 31480, 31490, 31510, 31520, 31530, 31540, 31550, 31560, 31570, 31580, 31590, 31600, 31610, 31620, 31630, 31640, 31650, 31670, 31680, 31690, 31700, 31710, 31720, 31730, 31740, 31750, 31760, 31770, 31780, 31790, 31800, 31810, 31820, 31830, 31840, 31850, 31860, 31870, 31880, 31890];
var mface = [20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20009, 20010, 20011, 20012, 20013, 20014, 20016, 20017, 20018, 20019, 20020, 20021, 20022, 20023, 20024, 20026];
var fface = [21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21009, 21010, 21011, 21012, 21013, 21014, 21016, 21017, 21018, 21019, 21020, 21021, 21022, 21024, 21025];
var skin = [0, 1, 2, 3, 4, 5, 9, 10];

var choice = -1;
var status = -1;
var style = [];

var Random = Java.type("java.util.Random");

function start(){
	action(1, 0, 0);
}

function action(mode, type, selection){
	if(mode > 0){
		status++;
	}else{
		if(status == 2){
			status = 1;
		}
		status--;
	}
	
	if(status == -1){
		cm.dispose();
		return;
	}
	
	if(status == 0){
		cm.sendSimple("What would you like to change?\r\n#b#L0#Hair#l\r\n#L1#Face#l\r\n#L2#Hair Color#l\r\n#L3#Eye Color#l\r\n#L4#Skin Color#l\r\n#r#e#L5#Just fuck me up fam#l");
	}else if(status == 1){
		choice = selection;
		
		if(choice == 0){
			if(cm.getCharacter().getGender() == 0){
				style = mhair;
			}else{
				style = fhair;
			}
		}else if(choice == 1){
			if(cm.getCharacter().getGender() == 0){
				style = mface;
			}else{
				style = fface;
			}
		}else if(choice == 2){
			
			var current = parseInt((cm.getCharacter().getHair() / 10)) * 10;
			
			for(i = 0; i < 8;i++){
				style[i] = current + i;
			}
			
		}else if(choice == 3){
			var current = 0;
			
			if (cm.getCharacter().getGender() == 0) {
				current = cm.getCharacter().getFace() % 100 + 20000;
			} else {
				current = cm.getCharacter().getFace() % 100 + 21000;
			}
			
			for(i = 0; i < 8;i++){
				style[i] = current + i * 100;
			}
		}else if(choice == 4){
			style = skin;
		}else if(choice == 5){
			r = new Random();
			
			chr = cm.getCharacter();
			
			if(chr.getGender() == 0){
				chr.setHair(mhair[r.nextInt(mhair.length)] + r.nextInt(8));
				chr.setFace(mface[r.nextInt(mface.length)] + r.nextInt(8));
				chr.setSkinColor(skin[r.nextInt(skin.length)]);
			}else{
				chr.setHair(fhair[r.nextInt(fhair.length)] + r.nextInt(8));
				chr.setFace(fface[r.nextInt(fface.length)] + r.nextInt(8));
				chr.setSkinColor(skin[r.nextInt(skin.length)]);
			}
			
			cm.sendOk("I gotchu fam");
			cm.dispose();
			return;
		}
		
		cm.sendStyle("Make a choice", style);
		
	}else{
		if(selection == -1){
			status = -1;
			action(1, 0, 0);
			return;
		}
		
		if(choice == 0 || choice == 2){
			cm.getCharacter().setHair(style[selection]);
		}else if(choice == 1 || choice == 3){
			cm.getCharacter().setFace(style[selection]);
		}else{
			cm.getCharacter().setSkinColor(style[selection]);
		}
		cm.dispose();
	}
}