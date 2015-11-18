package nz.org.francis.scriptmanager;

import nz.org.francis.scriptmanager.compiler.ScriptOpcode;

/**
 *
 * @author Francis
 */
public interface Script {
    
    public ScriptOpcode getInstruction (int pos);
    
    public int getInstructionCount ();
    
    public Object getConstant (int pos);
    
    public int getIntLocalCount ();
    
    public int getObjLocalCount ();
}
