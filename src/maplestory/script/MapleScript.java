package maplestory.script;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import constants.ServerConstants;
import lombok.Getter;

public class MapleScript {

	private static Map<String, MapleScript> cache = new HashMap<>();
	
	@Getter
	private File file;
	
	private String fileContents;
	
	@Getter
	private boolean usingFallback;
	
	@Getter
	private boolean disabled;
	
	public MapleScript(String filePath){
		this(filePath, null);
	}
	
	public MapleScript(String filePath, String fallback){
		this(new File(filePath), fallback == null ? null : new File(fallback));
	}
	
	public MapleScript(File file, File fallback){
		if(!file.exists()){
			file = fallback;
			usingFallback = true;
			disabled = false;
		}
		
		if(file == null || !file.exists()){
			usingFallback = false;
			disabled = true;
		}
		
		this.file = file;
	}
	
	private static MapleScript getCachedCopy(File file){
		return cache.get(file.getPath().toLowerCase());
	}
	
	public MapleScriptInstance execute(Bindings engineBindings) throws ScriptException, IOException {
		
		if(ServerConstants.CACHE_SCRIPTS){
			MapleScript cachedCopy = getCachedCopy(file);
			
			if(cachedCopy != null){
				return cachedCopy.execute(engineBindings);
			}
		}
		
		if(disabled){
			return null;
		}
		
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		engine.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);
		
		if(fileContents == null){
			fileContents = parseFile();
		}
		
		engine.eval(fileContents);
		
		Invocable inv = (Invocable) engine;
		
		if(ServerConstants.CACHE_SCRIPTS){
			cache.put(file.getPath().toLowerCase(), this);
		}
		
		return new MapleScriptInstance(inv);
	}
	
	private String parseFile() throws IOException{
		String script = "";
		

		List<String> lines = Files.readAllLines(file.toPath());
		
		for(String line : lines){
			script += line + "\r\n";
		}
		
		
		return script;
		
	}
	
}
