package nz.org.francis.scriptmanager;

import nz.org.francis.scriptmanager.compiler.CompiledScript;
import nz.org.francis.scriptmanager.compiler.Linker;
import java.io.File;
import nz.org.francis.scriptmanager.executer.Context;

/**
 *
 * @author Francis
 */
public class ModelScriptManager implements ScriptManager {
	
	private File sourceFolder;
	
	private Linker linker;

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		ModelScriptManager instance = new ModelScriptManager();
		instance.sourceFolder = new File("");
		instance.linker = new Linker();
	}

	@Override
	public CompiledScript getById(int id) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ApiFunction getApiFuncType(int id) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void invokeApiFunction(ApiFunction function, Context ctx) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
