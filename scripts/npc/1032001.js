//Grendel

var MapleJob = Java.type("maplestory.player.MapleJob");

var FIRST_JOB_ASK = "Want to be a #bMagician#k? There are some standards to meet. because we can't just accept EVERYONE in... #bYour level should be at least 8#k, let's see.";
var FIRST_JOB_KEEP_TRAINING = "Train a bit more and I can show you the way of the #rMagician#k.";
var FIRST_JOB_CONFIRM = "Oh...! You look like someone that can definitely be a part of us... all you need is a little sinister mind, and... yeah... so, what do you think? Wanna be the Magician?";
var FIRST_JOB_ITEMS = [[1372043, 1]];//Wand
var FIRST_JOB_BECOME_1 = "Alright, from here out, you are a part of us! Be honest with your studies and you can't go wrong as a magician.";
var FIRST_JOB_BECOME_2 = "You've gotten much stronger now. I just gave you a little bit of #bSP#k. When you open up the #bSkill#k menu on the lower left corner of the screen, there are skills you can learn by using SP's. One warning, though: You can't raise it all together all at once. There are also skills you can acquire only after having learned a couple of skills first.";
var FIRST_JOB_BECOME_3 = "Go now, and live as a proud #bMagician#k.";
var FIRST_JOB_JOB = MapleJob.MAGICIAN;

var SECOND_JOB_LETTER = 4031009;
var SECOND_JOB_REQUIRED = 4031012;
var SECOND_JOB_NPC = 1072001;
var SECOND_JOB_MAP = 101020000;
var SECOND_JOB_ASK = "Hmmm... you have grown a lot since I last saw you. The progress you have made is astonishing. Well, what do you think? Don't you want to get even more powerful than that? Pass a simple test and I'll do just that for you. Do you want to do it?";
var SECOND_JOB_COMPLETE_1 = "Haha...I knew you'd breeze through that test. I'll admit, you are a great magician. I'll make you much stronger than you're right now. before that, however... you'll need to choose one of three paths given to you. It'll be a difficult decision for you to make, but... if there's any question to ask, please do so.";
var SECOND_JOB_EXPLAIN = "Alright, when you have made your decision, click on [I'll choose my occupation] at the bottom.#b\r\n#L0#Please explain to me what being the Wizard (Fire / Poison) is all about.\r\n#L1#Please explain to me what being the Wizard (Ice / Lighting) is all about.\r\n#L2#Please explain to me what being the Cleric is all about.\r\n#L3#I'll choose my occupation!";
var MAIN_TOWN = "Ellinia";

var SECOND_JOB_FINAL_FLAVOUR =  "are the intelligent bunch with incredible magical prowess, able to pierce the mind and the psychological structure of the monsters with ease... please train yourself each and everyday. I'll help you become even stronger than you already are.";

var SECOND_JOB_DECIDE = "Now... have you made up your mind? Please choose the job you'd like to select for your 2nd job advancement. #b\r\n#L0#Wizard (Fire / Poison)\r\n#L1#Wizard (Ice / Lighting)\r\n#L2#Cleric";

var SECOND_JOB_JOBS = [MapleJob.FP_WIZARD, MapleJob.IL_WIZARD, MapleJob.CLERIC];
var SECOND_JOB_EXPLANATIONS = [
	"Magicians that master #rFire/Poison-based magic#k.\r\n\r\n#bWizards#k are a active class that deal magical, elemental damage. These abilities grants them a significant advantage against enemies weak to their element. With their skills #rMeditation#k and #rSlow#k, #bWizards#k can increase their magic attack and reduce the opponent's mobility. #bFire/Poison Wizards#k contains a powerful flame arrow attack and poison attack.",
	"Magicians that master #rIce/Lightning-based magic#k.\r\n\r\n#bWizards#k are a active class that deal magical, elemental damage. These abilities grants them a significant advantage against enemies weak to their element. With their skills #rMeditation#k and #rSlow#k, #bWizards#k can increase their magic attack and reduce the opponent's mobility. #bIce/Lightning Wizards#k have a freezing ice attack and a striking lightning attack.",
	"Magicians that master #rHoly magic#k.\r\n\r\n#bClerics#k are a powerful supportive class, bound to be accepted into any Party. That's because the have the power to #rHeal#k themselves and others in their party. Using #rBless#k, #bClerics#k can buff the attributes and reduce the amount of damage taken. This class is on worth going for if you find it hard to survive. #bClerics#k are especially effective against undead monsters."
];


load("scripts/npc/abstract_job_advancer.js");