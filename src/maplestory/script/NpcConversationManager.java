package maplestory.script;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import maplestory.life.MapleNPC;
import maplestory.player.MapleCharacter;
import maplestory.player.MapleJob;
import maplestory.server.net.PacketFactory;

public class NpcConversationManager extends AbstractScriptManager {
	
	@Getter
	private final MapleNPC npc;
	@Getter @Setter
	private String inputText;
	
	public NpcConversationManager(MapleCharacter chr, MapleNPC npc) {
	    super(chr);
	    this.npc = npc;
	}
	
	/*
	 * 0 = ariant colliseum
	 * 1 = Dojo
	 * 2 = Carnival 1
	 * 3 = Carnival 2
	 * 4 = Ghost Ship PQ?
	 * 5 = Pyramid PQ
	 * 6 = Kerning Subway
	 */
	public void sendDimensionalMirror(String text) {
		getClient().sendPacket(PacketFactory.dimensionalMirror(text));
	}
	
	public void dispose() {
		getCharacter().disposeOpenNpc();
	}
	
	public void openStorage(){
		getCharacter().openStorage(npc.getId());
	}
	
	public void sendNext(String text) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 0, text, new byte[] {0x00, 0x01}, (byte) 0));
	}
	
	public void sendPrev(String text) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 0, text, new byte[] {0x01, 0x00}, (byte) 0));
	}
	
	public void sendNextPrev(String text) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 0, text, new byte[] {0x01, 0x01}, (byte) 0));
	}
	
	public void sendOk(String text) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 0, text, new byte[] {0x00, 0x00}, (byte) 0));
	}
	
	public void sendYesNo(String text) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 1, text, new byte[0], (byte) 0));
	}
	
	public void sendAcceptDecline(String text) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 0x0C, text, new byte[0], (byte) 0));
	}
	
	public void sendSimple(String text) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 4, text, new byte[0], (byte) 0));
	}
	
	public void sendNext(String text, byte speaker) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 0, text, new byte[] {0x00, 0x01}, speaker));
	}
	
	public void sendPrev(String text, byte speaker) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 0, text, new byte[] {0x01, 0x00}, speaker));
	}
	
	public void sendNextPrev(String text, byte speaker) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 0, text, new byte[] {0x01, 0x01}, speaker));
	}
	
	public void sendOk(String text, byte speaker) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 0, text, new byte[] {0x00, 0x00}, speaker));
	}
	
	public void sendYesNo(String text, byte speaker) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 1, text, new byte[0], speaker));
	}
	
	public void sendAcceptDecline(String text, byte speaker) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 0x0C, text, new byte[0], speaker));
	}
	
	public void sendSimple(String text, byte speaker) {
	    getClient().sendPacket(PacketFactory.getNPCTalk(npc, (byte) 4, text, new byte[0], speaker));
	}
	
	public void sendStyle(String text, int[] styles){
		getClient().sendPacket(PacketFactory.getNPCTalkStyle(npc, text, styles));
	}

	public void sendGetNumber(String text, int def, int min, int max) {
		getClient().sendPacket(PacketFactory.getNPCTalkNum(npc, text, def, min, max));
	}

	public void sendGetText(String text) {
		getClient().sendPacket(PacketFactory.getNPCTalkText(npc, text, ""));
	}
	
	public void sendJobAdvance(){
		MapleCharacter chr = getClient().getCharacter();
		MapleJob[] p = chr.getJob().getNext();
		
		if(p == null){
			sendPrev("There is no next job advancement for you!");
			return;
		}
		
		List<MapleJob> possible;
		
		possible = Arrays.stream(p).filter(job -> job.getLevelRequirement() <= chr.getLevel()).collect(Collectors.toList());
		
		List<MapleJob> notPossible = Arrays.stream(p).filter(job -> job.getLevelRequirement() > chr.getLevel()).collect(Collectors.toList());
		
		String message = "#bSelect your next job#k\r\n\r\n";
		
		for(int i = 0; i < possible.size();i++){
			MapleJob job = possible.get(i);
			
			message += "#L"+job.getId()+"##b"+job.getName()+"#l\r\n";
			
		}
		
		message += "\r\n\r\n";
		
		for(int i = 0; i < notPossible.size();i++){
			MapleJob job = notPossible.get(i);
			
			message += "#r"+job.getName()+" #e(Requires Level "+job.getLevelRequirement()+")#n\r\n";
			
		}
		
		message += "\r\n\r\n#L100##kGo back#l";
		
		sendSimple(message);
		
	}
	
	public MapleJob handleJobAdvanceSelect(int selection){
		if(selection == 100){
			return null;
		}
		MapleCharacter chr = getClient().getCharacter();
		
		MapleJob selected = MapleJob.getById(selection);
		
		if(chr.getLevel() >= selected.getLevelRequirement()){
			return selected;
		}else{
			return null;
		}
	}
	
	public String getNpcName(){
		return npc.getName();
	}
	
}

