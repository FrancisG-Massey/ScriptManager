package nz.org.francis.scriptmanager;

/**
 *
 * @author Francis
 */
public enum ScriptDataType {
	INT(1, true),
	STRING(2, false),
	LONG(3, false),
	BOOLEAN(4, true);
	
	private final int id;
	
	private final boolean intBase;
	
	ScriptDataType (int id, boolean intBase) {
		this.id = id;
		this.intBase = intBase;
	}
	
	public int getID () {
		return id;
	}
	
	public boolean intBase () {
		return intBase;
	}
	
	public static ScriptDataType forID (int id) {
		for (ScriptDataType type : values()) {
			if (type.id == id) {
				return type;
			}
		}
		return null;
	}
	
	public static ScriptDataType forName (String name) {
		for (ScriptDataType type : values()) {
			if (type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}
}
