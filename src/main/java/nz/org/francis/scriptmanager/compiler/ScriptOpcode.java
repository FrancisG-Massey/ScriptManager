package nz.org.francis.scriptmanager.compiler;

/**
 *
 * @author Francis
 */
public enum ScriptOpcode {
	LOAD_INT_CONST(0, true),
	LOAD_STR_CONST(1),
	LOAD_LOCAL(2, true),
	LOAD_INT_LOCAL(3, true),
	ASSIGN_LOCAL(4, true),
	ASSIGN_LOCAL_INT(5, true),
	LOAD_GLOBAL(6, true),
	ASSIGN_GLOBAL(7, true),
	DROP_OBJ(8),
	DROP_INT(9),
	INVOKE_API(10, true),
	INVOKE_SCRIPT(11, true),
	RETURN(12),
	JUMP(19, true),
	JUMP_TRUE(20, true),
	JUMP_FALSE(21, true),
	JUMP_EQ(22, true),
	JUMP_NT_EQ(23, true),
	JUMP_GT(24, true),
	JUMP_LT(25, true),
	JUMP_GT_EQ(26, true),
	JUMP_LT_EQ(27, true),
	ADD(100),
	SUBTRACT(101),
	MULTIPLY(102),
	DIVIDE(103),
	POWER(104),
	MODULO(105);
	
	private final int opcode;
	private final boolean hasConst;
	
	ScriptOpcode (int code) {
		this(code, false);
	}
	
	ScriptOpcode (int code, boolean hasConst) {
		this.opcode = code;
		this.hasConst = hasConst;
	}
	
	public int getOpcode () {
		return opcode;
	}
	
	public boolean hasIntConst () {
		return hasConst;
	}
	
	public static ScriptOpcode forOpcode (int opcode) {
		for (ScriptOpcode type : values()) {
			if (type.opcode == opcode) {
				return type;
			}
		}
		return null;
	}
	
}
