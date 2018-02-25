//Athena Pierce

var MapleJob = Java.type("maplestory.player.MapleJob");

var FIRST_JOB_ASK = "So you decided to become a #rBowman#k?";
var FIRST_JOB_KEEP_TRAINING = "Train a bit more and I can show you the way of the #rBowman#k.";
var FIRST_JOB_CONFIRM = "It is an important and final choice. You will not be able to turn back.";
var FIRST_JOB_ITEMS = [[1452051, 1], [2060000, 1000]];//Bow + Arrow
var FIRST_JOB_BECOME_1 = "Alright, from here out, you are a part of us! Be faithful to the archer way and you are sure to succeed.";
var FIRST_JOB_BECOME_2 = "You've gotten much stronger now. I just gave you a little bit of #bSP#k. When you open up the #bSkill#k menu on the lower left corner of the screen, there are skills you can learn by using SP's. One warning, though: You can't raise it all together all at once. There are also skills you can acquire only after having learned a couple of skills first.";
var FIRST_JOB_BECOME_3 = "Go now, and live as a proud Bowman.";
var FIRST_JOB_JOB = MapleJob.BOWMAN;

var SECOND_JOB_LETTER = 4031010;
var SECOND_JOB_REQUIRED = 4031012;
var SECOND_JOB_NPC = 1072002;
var SECOND_JOB_MAP = 106010000;
var SECOND_JOB_ASK = "Hmmm... you have grown a lot since I last saw you. I don't see the weakling I saw before, and instead, look much more like a bowman now. Well, what do you think? Don't you want to get even more powerful than that? Pass a simple test and I'll do just that for you. Do you want to do it?";
var SECOND_JOB_COMPLETE_1 = "Haha...I knew you'd breeze through that test. I'll admit, you are a great bowman. I'll make you much stronger than you're right now. before that, however... you'll need to choose one of two paths given to you. It'll be a difficult decision for you to make, but... if there's any question to ask, please do so.";
var SECOND_JOB_EXPLAIN = "Alright, when you have made your decision, click on [I'll choose my occupation] at the bottom.#b\r\n#L0#Please explain to me what being the Hunter is all about.\r\n#L1#Please explain to me what being the Crossbowman is all about.\r\n#L3#I'll choose my occupation!";
var MAIN_TOWN = "Henesys";

var SECOND_JOB_FINAL_FLAVOUR =  "are an intelligent bunch with incredible vision, able to pierce the arrow through the heart of the monsters with ease... please train yourself each and everyday. I'll help you become even stronger than you already are.";

var SECOND_JOB_DECIDE = "Now... have you made up your mind? Please choose the job you'd like to select for your 2nd job advancement. #b\r\n#L0#Hunter\r\n#L1#Crossbowman";

var SECOND_JOB_JOBS = [MapleJob.HUNTER, MapleJob.CROSSBOWMAN];
var SECOND_JOB_EXPLANATIONS = [
	"Archers that master #rBows#k.\r\n\r\n#bHunters#k have a higher damage/minute output in early levels, with attacks having a faster pace but slightly weaker than Crossbowmans. #bHunters#k get #rArrow Bomb#k, a slightly weaker attack that can cause up to 6 enemies to get stunned.",
	"Archers that master #rCrossbows#k.\r\n\r\n#bCrossbowmans'#k attack power grows higher the higher level you are, when compared to Hunters. #bCrossbowmans#k get #rIron Arrow#k, a stronger attack that does not home on enemies but can go through walls."
];


load("scripts/npc/abstract_job_advancer.js");