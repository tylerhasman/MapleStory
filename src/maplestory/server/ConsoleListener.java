package maplestory.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import constants.MessageType;

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
				}
				
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
