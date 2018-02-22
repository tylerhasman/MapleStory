package maplestory.script.legacy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdinScriptPatches {

	private Map<String, String> patches;
	
	public OdinScriptPatches(String patchFilePaths) {
		patches = new HashMap<>();
		try {
			List<String> lines = Files.readAllLines(new File(patchFilePaths).toPath());
		
			for(String line : lines) {
				if(line.isEmpty()) {
					continue;
				}
				if(line.startsWith("@@")) {
					continue;
				}
				String[] parts = line.split(" -> ");
				
				String original = parts[0];
				String patch = parts[1];
				
				patches.put(original, patch);
				
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public String patchData(String input) {
		
		for(String key : patches.keySet()) {
			input = input.replace(key, patches.get(key));
		}
		
		
		return input;
	}
	
}
