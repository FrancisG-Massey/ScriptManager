package nz.org.francis.scriptmanager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Francis
 */
public enum ApiFunctionType implements ApiFunction {
	PRINT(1, new ScriptDataType[] { ScriptDataType.STRING }, new ScriptDataType[] {});
	
	private final int code;
	
	private final List<ScriptDataType> params;
	private final List<ScriptDataType> returns;
	
	ApiFunctionType (int code, ScriptDataType[] params, ScriptDataType[] returns) {
		this.code = code;
		this.params = Collections.unmodifiableList(Arrays.asList(params));
		this.returns = Collections.unmodifiableList(Arrays.asList(returns));
	}
	
	@Override
	public int getOpcode () {
		return code;
	}

	@Override
	public String getName() {
		return name().toLowerCase();
	}
	
	@Override
	public List<ScriptDataType> getParamSignature () {
		return params;
	}
	
	@Override
	public List<ScriptDataType> getReturnSignature () {
		return returns;
	}
	
	private static Map<String, ApiFunctionType> lookup;
	
	public static ApiFunctionType forName (String name) {
		if (lookup == null) {
			lookup = new HashMap<>();
			for (ApiFunctionType type : values()) {
				lookup.put(type.getName(), type);
			}
		}
		return lookup.get(name);
	}
}
