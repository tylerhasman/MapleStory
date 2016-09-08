package constants;

import lombok.AllArgsConstructor;
import maplestory.inventory.item.ItemInfoProvider;

@AllArgsConstructor
public enum ItemLetterFont {

	RED(3991000, 3990000, 3990020),
	GREEN(3991026, 3990010, 3990022),
	COMPASS(3994102, -1, -1) {
		@Override
		public int getCharacter(char c) {
			if(c == 'n' || c == 'N'){
				return 3994102;
			}else if(c == 'e' || c == 'E'){
				return 3994103;
			}else if(c == 'w' || c == 'W'){
				return 3994104;
			}else if(c == 's' || c == 'S'){
				return 3994105;
			}
			return RED.getCharacter(c);
		}
	};
	
	private final int lettersId;
	
	private final int numbersId;
	
	private final int specialCharactersId;
	
	public int getWidth(char c){
		if(!isCharacterSupported(c)){
			return -1;
		}	
		
		int id = getCharacter(c);
		
		return ItemInfoProvider.getItemWidth(id);
	}
	
	public int getCharacter(char c){
		
		if(Character.isLetter(c)){
			c = Character.toUpperCase(c);
			
			int code = c - 65;
			
			return lettersId + code;
		}else if(Character.isDigit(c)){
			int code = c - 48;
			
			return numbersId + code;
		}else if(c == '+' || c == '-'){
			if(c == '+'){
				return specialCharactersId;
			}else{
				return specialCharactersId+1;
			}
		}
		
		return -1;
	}
	
	public boolean isCharacterSupported(char c){
		return getCharacter(c) >= 0;
	}

	public int calculateWidth(String text) {
		int width = 0;
		for(char c : text.toCharArray()){
			if(c == ' '){
				width += 30;
			}
			if(!isCharacterSupported(c)){
				continue;
			}
			width += getWidth(c);
		}
		return width;
	}
	
}
