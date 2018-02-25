//Thief Lord

var MapleJob = Java.type("maplestory.player.MapleJob");

var FIRST_JOB_ASK = "Oh...! You look like someone that can definitely be a part of us... all you need is a little sinister mind, and... yeah... so, what do you think? Wanna be the Thief?";
var FIRST_JOB_KEEP_TRAINING = "Train a bit more and I can show you the way of the #rThief#k.";
var FIRST_JOB_CONFIRM = "Oh, you look like you could be a #bThief#k. Would you like to become one? Once you do, you cannot undo it.";
var FIRST_JOB_ITEMS = [[1472061, 1], [2070000, 500]];//Glove + Throwing Stars
var FIRST_JOB_BECOME_1 = "From here on out, you are going the Thief path. This is not an easy job, but if you have discipline and confidence in your own wit, you will overcome any difficulties in your path. Go, young Thief!";
var FIRST_JOB_BECOME_2 = "You've gotten much stronger now. I just gave you a little bit of #bSP#k. When you open up the #bSkill#k menu on the lower left corner of the screen, there are skills you can learn by using SP's. One warning, though: You can't raise it all together all at once. There are also skills you can acquire only after having learned a couple of skills first.";
var FIRST_JOB_BECOME_3 = "Go now, and live as a cunning #bThief#k.";
var FIRST_JOB_JOB = MapleJob.THIEF;

var SECOND_JOB_LETTER = 4031011;
var SECOND_JOB_REQUIRED = 4031012;
var SECOND_JOB_NPC = 1072003;
var SECOND_JOB_MAP = 102040000;
var SECOND_JOB_ASK = "Hmmm... you have grown a lot since I last saw you. Well, what do you think? Don't you want to get even more powerful than that? Pass a simple test and I'll do just that for you. Do you want to do it?";
var SECOND_JOB_COMPLETE_1 = "Haha...I knew you'd breeze through that test. I'll admit, you are a cunning thief. I'll make you much stronger than you're right now. before that, however... you'll need to choose one of three paths given to you. It'll be a difficult decision for you to make, but... if there's any question to ask, please do so.";
var SECOND_JOB_EXPLAIN = "Alright, when you have made your decision, click on [I'll choose my occupation] at the bottom.#b\r\n#L0#Please explain to me what being the Assassin is all about.\r\n#L1#Please explain to me what being the Bandit is all about.\r\n#L3#I'll choose my occupation!";
var MAIN_TOWN = "Kerning City";

var SECOND_JOB_FINAL_FLAVOUR =  "are a cunning bunch. Keep working hard and you will be able to outwit any foe that comes your way.";

var SECOND_JOB_DECIDE = "Now... have you made up your mind? Please choose the job you'd like to select for your 2nd job advancement. #b\r\n#L0#Assassin\r\n#L1#Bandit";

var SECOND_JOB_JOBS = [MapleJob.ASSASIN, MapleJob.BANDIT];
var SECOND_JOB_EXPLANATIONS = [
	"Thieves that master #rClaws#k.\r\n\r\n#bAssassins#k are far ranged attackers. They are quite Meso efficient and have good damage potential, but cost more than Bandits.",
	"Thieves that master #rDaggers#k.\r\n\r\n#bBandits#k are quick melee attackers and are quite powerful among the 2nd jobs. They aren't as Meso efficient as Assassins and do not have the benefit of ranged attack but make up for it in much greater raw power."
];


load("scripts/npc/abstract_job_advancer.js");