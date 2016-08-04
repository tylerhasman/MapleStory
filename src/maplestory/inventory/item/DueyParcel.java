package maplestory.inventory.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class DueyParcel {

	@Getter
	private int parcelId;
	
	@Getter
	private String sender;
	
	@Getter
	private int mesos;

	@Getter
	private Item gift;
	
	@Getter
	private long expirationTime;
	
	@Getter
	private String message;
	
	public boolean hasMessage(){
		return message != null;
	}
	
}
