package nz.org.francis.scriptmanager;

import nz.org.francis.scriptmanager.compiler.CompiledScript;
import nz.org.francis.scriptmanager.executer.Context;

/**
 *
 * @author Francis
 */
public interface ScriptManager {	
	
	public CompiledScript getById (int id);
	
	public ApiFunction getApiFuncType (int id);
	
	public void invokeApiFunction (ApiFunction function, Context ctx);
}
