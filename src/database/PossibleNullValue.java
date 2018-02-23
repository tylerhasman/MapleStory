package database;

public class PossibleNullValue {

	private final Object value;
	
	private final int sqlType;
	
	public PossibleNullValue(Object val, int sqlType) {
		value = val;
		this.sqlType = sqlType;
	}
	
	public Object getValue() {
		return value;
	}
	
	public int getSqlType() {
		return sqlType;
	}
	
}
