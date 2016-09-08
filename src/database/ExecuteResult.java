package database;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExecuteResult {

	private final int numRowsChanged;
	private final List<Integer> generatedKeys;

	public int getNumRowsChanged() {
		return numRowsChanged;
	}
	
	public List<Integer> getGeneratedKeys() {
		if(generatedKeys == null){
			throw new IllegalStateException("No generated keys available");
		}
		return generatedKeys;
	}
	
}
