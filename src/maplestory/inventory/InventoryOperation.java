package maplestory.inventory;

import lombok.Getter;
import maplestory.inventory.item.Item;

public class InventoryOperation {

	@Getter
	private int oldPosition;
	@Getter
	private int position;
	@Getter
	private Item item;
	private int mode;
	
	public InventoryOperation(OperationType type, Item item, int position) {
		this(type, item, 0, position);
	}
	
	public InventoryOperation(OperationType type, Item item, int oldPosition, int newPosition) {
		mode = type.getId();
		this.item = item;
		this.oldPosition = oldPosition;
		this.position = newPosition;
	}
	
	public OperationType getMode(){
		return OperationType.values()[mode];
	}
	
	public static InventoryOperation addItem(Item item, int position){
		return new InventoryOperation(OperationType.ADD_ITEM, item, position);
	}
	
	public static InventoryOperation updateAmount(Item item, int position){
		return new InventoryOperation(OperationType.UPDATE_QUANTITY, item, position);
	}
	
	public static InventoryOperation moveItem(Item item, int oldPosition, int newPosition){
		return new InventoryOperation(OperationType.MOVE_ITEM, item, oldPosition, newPosition);
	}
	
	public static InventoryOperation removeItem(Item item, int position){
		return new InventoryOperation(OperationType.REMOVE_ITEM, item, position);
	}
	
	@Override
	public String toString() {
		return getMode()+" "+item+" "+oldPosition+" "+position;
	}
	
	public static enum OperationType {
		
		ADD_ITEM(0),
		UPDATE_QUANTITY(1),
		MOVE_ITEM(2),
		REMOVE_ITEM(3)
		;
		
		private final int id;
		
		OperationType(int id){
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
	}
	
}
