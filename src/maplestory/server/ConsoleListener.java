package maplestory.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.List;

import constants.MessageType;
import maplestory.channel.MapleChannel;
import maplestory.player.MapleCharacter;
import maplestory.world.World;

public class ConsoleListener implements Runnable {

	@Override
	public void run() {
		
		MapleServer server = MapleServer.getInstance();
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))){
			while(true){
				
				String cmd = reader.readLine();
				
				String[] args = cmd.split(" ");
				
				if(args[0].equalsIgnoreCase("shutdown")){
					server.shutdown();
				}else if(args[0].equalsIgnoreCase("broadcast")){
					
					String msg = "";
					
					for(int i = 1; i < args.length;i++){
						msg += args[i] + " ";
					}
					
					final String finalMsg = msg;
					
					MapleServer.getWorlds().forEach(world -> world.broadcastMessage(MessageType.NOTICE, finalMsg));
				}else if(args[0].equalsIgnoreCase("refreshranks")) {
					
					for(World world : MapleServer.getWorlds()) {
						world.getRankManager().updateRankings();
					}
				}else if(args[0].equalsIgnoreCase("players")) {
					
					for(World world : MapleServer.getWorlds()) {
						for(MapleCharacter chr : world.getPlayerStorage().getAllPlayers()) {
							System.out.println(chr.getName()+" "+chr.getWorldId()+" "+chr.getMapId());
						}
					}
					
				}else if(args[0].equalsIgnoreCase("dump")) {
					List<ThreadInfo> threads = Arrays.asList(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true));
					
					File file = new File("dump_"+System.currentTimeMillis()+".log");
					
					try {
						file.createNewFile();
						
						try(FileOutputStream fos = new FileOutputStream(file)){
							try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos))){
								
								for(ThreadInfo info : threads){
									out.write(info.toString());
									out.newLine();
								}
								
								out.flush();
							}
						}
						
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else {
					System.out.println("Unknown cmd");
				}
				
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
