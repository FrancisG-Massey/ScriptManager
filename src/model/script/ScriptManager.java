package model.script;

import model.script.executer.CompiledScript;
import model.script.executer.Context;

/**
 *
 * @author Francis
 */
public interface ScriptManager {	
	
	public CompiledScript getById (int id);
	
	public ApiFunction getApiFuncType (int id);
	
	public void invokeApiFunction (ApiFunction function, Context ctx);
}
