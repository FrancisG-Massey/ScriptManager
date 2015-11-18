package nz.org.francis.scriptmanager;

import java.util.List;

/**
 *
 * @author Francis
 */
public interface ApiFunction {
	
	public int getOpcode();
	
	public String getName();
	
	public List<ScriptDataType> getParamSignature ();
	
	public List<ScriptDataType> getReturnSignature ();
}
