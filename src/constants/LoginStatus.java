package constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum LoginStatus {

	OFFLINE(0),
	LOGGED_IN(1),
	IN_GAME(2)
	;
	
	@Getter
	private final int id;
	
	public static LoginStatus byId(int id){
		for(LoginStatus status : values()){
			if(status.id == id){
				return status;
			}
		}
		return null;
	}
	
}
