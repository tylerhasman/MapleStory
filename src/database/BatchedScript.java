package database;

import java.util.HashMap;
import java.util.Map;

public class BatchedScript {

	private final String script;
	
	private Map<Integer, Object[]> batches;
	private int index;
	
	public BatchedScript(String script) {
		this.script = script;
		batches = new HashMap<>();
		index = 0;
	}
	
	public String getScript() {
		return script;
	}
	
	public Map<Integer, Object[]> getBatches() {
		return batches;
	}
	
	public void addBatch(Object... values) {
		batches.put(index++, values);
	}
	
}
