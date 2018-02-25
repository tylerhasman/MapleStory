//Dances with Balrog

var MapleJob = Java.type("maplestory.player.MapleJob");

var FIRST_JOB_ASK = "Do you want to become a #bWarrior#k? You need to meet some criteria in order to do so.#b You should be at least in level 10#k. Let's see...";
var FIRST_JOB_KEEP_TRAINING = "Train a bit more and I can show you the way of the #rWarrior#k.";
var FIRST_JOB_CONFIRM = "Oh, you look like you could be a #bWarrior#k. Would you like to become one? Once you do, you cannot undo it.";
var FIRST_JOB_ITEMS = [[1302077, 1]];//Weapon
var FIRST_JOB_BECOME_1 = "From here on out, you are going to the Warrior path. This is not an easy job, but if you have discipline and confidence in your own body and skills, you will overcome any difficulties in your path. Go, young Warrior!";
var FIRST_JOB_BECOME_2 = "You've gotten much stronger now. I just gave you a little bit of #bSP#k. When you open up the #bSkill#k menu on the lower left corner of the screen, there are skills you can learn by using SP's. One warning, though: You can't raise it all together all at once. There are also skills you can acquire only after having learned a couple of skills first.";
var FIRST_JOB_BECOME_3 = "Go now, and live as a strong #bWarrior#k.";
var FIRST_JOB_JOB = MapleJob.WARRIOR;

var SECOND_JOB_LETTER = 4031008;
var SECOND_JOB_REQUIRED = 4031012;
var SECOND_JOB_NPC = 1072000;
var SECOND_JOB_MAP = 102020300;
var SECOND_JOB_ASK = "Hmmm... you have grown a lot since I last saw you. Well, what do you think? Don't you want to get even more powerful than that? Pass a simple test and I'll do just that for you. Do you want to do it?";
var SECOND_JOB_COMPLETE_1 = "Haha...I knew you'd breeze through that test. I'll admit, you are a mighty warrior. I'll make you much stronger than you're right now. before that, however... you'll need to choose one of three paths given to you. It'll be a difficult decision for you to make, but... if there's any question to ask, please do so.";
var SECOND_JOB_EXPLAIN = "Alright, when you have made your decision, click on [I'll choose my occupation] at the bottom.#b\r\n#L0#Please explain to me what being the Fighter is all about.\r\n#L1#Please explain to me what being the Page is all about.\r\n#L2#Please explain to me what being the Spearman is all about.\r\n#L3#I'll choose my occupation!";
var MAIN_TOWN = "Perion";

var SECOND_JOB_FINAL_FLAVOUR =  "are a strong bunch. Keep working hard and you will be able to move mountains and slay dragons.";

var SECOND_JOB_DECIDE = "Now... have you made up your mind? Please choose the job you'd like to select for your 2nd job advancement. #b\r\n#L0#Fighter\r\n#L1#Page\r\n#L2#Spearman";

var SECOND_JOB_JOBS = [MapleJob.FIGHTER, MapleJob.PAGE, MapleJob.SPEARMAN];
var SECOND_JOB_EXPLANATIONS = [
	"Warriors that master #rSwords or Axes#k.\r\n\r\n#rFighters#k get #bRage#k, which boosts your party's weapon attack by 10. During 2nd job this is strongly appreciated, as it is free (except for -10 wep def, which is not going to impact the damage you take much at all), takes no Use slots and increases each party member's damage (except Mages) by several hundreds. The other classes can give themselves a weapon attack boost as well, but need items to do so. #rFighters#k also get #bPower Guard#k, reducing touch damage by 40% and deals it back to the monster. This is the main reason why #rFighters#k are considered soloers is because this reduces pot costs immensely.",
	"Warriors that master #rSwords or Maces/Blunt weapons#k.\r\n\r\n#rPages#k get #bThreaten#k, a skill that lowers the enemies' weapon defense and weapon attack by 20; this is mostly used to lower damage dealt to you. Pages also get #bPower Guard#k, reducing touch damage by 40% and deals it back to the monster. This is one of the main reason why #bPages/WKs#k are considered soloers, that's because this reduces pot costs immensely. Of course, constant KB and #bIce Charge#k helps also to the soloing factor.",
	"Warriors that master #rSpears or Polearms#k.\r\n\r\n#rSpearmen#k get #bHyper Body#k, which boosts your max HP/MP and that of your party by 60% when maxed. This skill is particularly useful for helping partied Thieves, Archers, and Mages to survive more hits from enemies and/or PQ bosses. They also get #bIron Will#k which gives +20 wep def and +20 mag def for 300 sec. It is basically a nerfed Bless with 100 seconds more duration but gives no accuracy or avoidability bonus. Even with this skill maxed, it isn't even close to being in the same league as Power Guard and is why Spearmen/Dark Knights are not considered a soloing class."
];


load("scripts/npc/abstract_job_advancer.js");